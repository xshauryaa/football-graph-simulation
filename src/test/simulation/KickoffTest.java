package test.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.model.Coordinates;
import main.model.Formation;
import main.model.Formations;
import main.model.Match;
import main.model.Pitch;
import main.model.Player;
import main.model.Position;
import main.model.Slot;
import main.model.Team;
import main.simulation.Kickoff;

class KickoffTest {

    private static final double CENTRE_CIRCLE_RADIUS = 9.15;

    private Pitch pitch;
    private Team home;
    private Team away;
    private Match match;

    // EFFECTS: returns a team whose lineup fills every slot of the formation, one player per slot
    private static Team teamWithLineup(String name, Formation formation) {
        List<Player> squad = new ArrayList<>();
        for (int i = 0; i < formation.getSlots().size(); i++) {
            squad.add(new Player(name + " " + (i + 1), i + 1, new Coordinates(0, 0),
                    Collections.singletonList(formation.getSlots().get(i).getPosition())));
        }
        Team team = new Team(name, squad);
        team.setFormation(formation);
        for (int i = 0; i < squad.size(); i++) {
            team.assign(formation.getSlots().get(i), squad.get(i));
        }
        return team;
    }

    private static Player playerIn(Team team, Position position) {
        for (Map.Entry<Slot, Player> e : team.getLineup().get().getAssignments().entrySet()) {
            if (e.getKey().getPosition() == position) {
                return e.getValue();
            }
        }
        throw new AssertionError("No " + position + " in lineup");
    }

    @BeforeEach
    void setUp() {
        pitch = new Pitch(105, 68);
        home = teamWithLineup("Home", Formations.fourThreeThree());
        away = teamWithLineup("Away", Formations.fourThreeThree());
        match = new Match(pitch, home, away);
        match.startFirstHalf();
    }

    @Test
    void everyPlayerStartsInTheirOwnHalf() {
        Kickoff.take(match, home);
        double halfway = pitch.getLength() / 2.0;
        for (Player p : home.getLineup().get().getAssignments().values()) {
            assertTrue(p.getCoordinates().getX() <= halfway, p.getName() + " should be in the home half");
        }
        for (Player p : away.getLineup().get().getAssignments().values()) {
            assertTrue(p.getCoordinates().getX() >= halfway, p.getName() + " should be in the away half");
        }
    }

    @Test
    void mostAdvancedKickingPlayerTakesTheBallOnTheCentreSpot() {
        Kickoff.take(match, home);
        Player striker = playerIn(home, Position.ST);
        assertEquals(match.getCentreSpot(), striker.getCoordinates());
        assertSame(striker, match.getBall().getHolder().get());
        assertEquals(match.getCentreSpot(), match.getBall().getCoordinates());
    }

    @Test
    void awayTeamCanKickOff() {
        Kickoff.take(match, away);
        Player striker = playerIn(away, Position.ST);
        assertSame(striker, match.getBall().getHolder().get());
        assertNotEquals(match.getCentreSpot(), playerIn(home, Position.ST).getCoordinates());
    }

    @Test
    void defendingTeamIsOutsideTheCentreCircle() {
        Kickoff.take(match, home);
        for (Player p : away.getLineup().get().getAssignments().values()) {
            assertTrue(p.getCoordinates().distanceTo(match.getCentreSpot()) >= CENTRE_CIRCLE_RADIUS,
                    p.getName() + " is inside the centre circle");
        }
    }

    @Test
    void eachTeamKeepsItsOwnLeftAndRight() {
        Kickoff.take(match, home);
        // Home attacks toward max x, so its left is small y; away attacks toward min x, so its left is large y.
        assertTrue(playerIn(home, Position.LB).getCoordinates().getY() < pitch.getWidth() / 2.0);
        assertTrue(playerIn(away, Position.LB).getCoordinates().getY() > pitch.getWidth() / 2.0);
    }

    @Test
    void goalkeepersStandNearTheirOwnGoals() {
        Kickoff.take(match, home);
        assertEquals(0.05 * Kickoff.KICKOFF_DEPTH_SCALE * pitch.getLength(),
                playerIn(home, Position.GK).getCoordinates().getX(), 1e-9);
        assertEquals(pitch.getLength() * (1 - 0.05 * Kickoff.KICKOFF_DEPTH_SCALE),
                playerIn(away, Position.GK).getCoordinates().getX(), 1e-9);
    }

    @Test
    void teamsSwapHalvesForTheSecondHalfKickoff() {
        match.endHalf();
        match.startSecondHalf();
        Kickoff.take(match, away);
        double halfway = pitch.getLength() / 2.0;
        for (Player p : home.getLineup().get().getAssignments().values()) {
            assertTrue(p.getCoordinates().getX() >= halfway, p.getName() + " should now defend the max-x end");
        }
        assertTrue(playerIn(away, Position.GK).getCoordinates().getX() < halfway);
    }

    @Test
    void kickoffAfterAGoalResetsPlayersAndBall() {
        Kickoff.take(match, home);
        Player striker = playerIn(home, Position.ST);
        striker.setCoordinates(new Coordinates(100, 30));
        match.getBall().release();
        match.recordGoal(home);

        Kickoff.take(match, away);

        assertTrue(striker.getCoordinates().getX() < pitch.getLength() / 2.0);
        assertSame(playerIn(away, Position.ST), match.getBall().getHolder().get());
    }

    @Test
    void arrangeComputesTargetsWithoutMovingAnyone() {
        Player keeper = playerIn(home, Position.GK);
        Coordinates before = keeper.getCoordinates();
        Map<Player, Coordinates> targets = Kickoff.arrange(match, home);
        assertEquals(22, targets.size());
        assertEquals(before, keeper.getCoordinates());
        assertNotEquals(before, targets.get(keeper));
        assertFalse(match.getBall().isHeld());
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Kickoff.take(null, home));
        Team stranger = teamWithLineup("Stranger", Formations.fourThreeThree());
        assertThrows(IllegalArgumentException.class, () -> Kickoff.take(match, stranger));
    }

    @Test
    void requiresCompleteLineupsForBothTeams() {
        Team noLineup = new Team("No Lineup", Collections.singletonList(
                new Player("Solo", 1, new Coordinates(0, 0), Collections.singletonList(Position.GK))));
        Match m = new Match(pitch, home, noLineup);
        assertThrows(IllegalArgumentException.class, () -> Kickoff.take(m, home));

        Team partial = teamWithLineup("Partial", Formations.fourThreeThree());
        partial.getLineup().get().unassign(partial.getLineup().get().getFormation().getSlots().get(0));
        Match m2 = new Match(pitch, home, partial);
        assertThrows(IllegalArgumentException.class, () -> Kickoff.take(m2, home));
    }
}
