package test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.model.AttackDirection;
import main.model.Coordinates;
import main.model.Formation;
import main.model.Formations;
import main.model.Match;
import main.model.MatchPeriod;
import main.model.MatchState;
import main.model.Pitch;
import main.model.Player;
import main.model.PlayerView;
import main.model.Position;
import main.model.Side;
import main.model.Slot;
import main.model.Team;

class MatchStateTest {

    private static final double HALF_LENGTH = 100;

    private Pitch pitch;
    private Team home;
    private Team away;
    private Player homeBench;
    private Match match;
    private MatchState state;

    // EFFECTS: returns a team whose lineup fills every slot of the formation, plus the given extra squad players
    private static Team teamWithLineup(String name, Formation formation, Player... bench) {
        List<Player> starters = new ArrayList<>();
        for (int i = 0; i < formation.getSlots().size(); i++) {
            starters.add(player(name + " " + (i + 1), i + 1, formation.getSlots().get(i).getPosition()));
        }
        List<Player> squad = new ArrayList<>(starters);
        Collections.addAll(squad, bench);
        Team team = new Team(name, squad);
        team.setFormation(formation);
        for (int i = 0; i < starters.size(); i++) {
            team.assign(formation.getSlots().get(i), starters.get(i));
        }
        return team;
    }

    private static Player player(String name, int number, Position position) {
        return new Player(name, number, new Coordinates(10, 10), Collections.singletonList(position));
    }

    private static Player inLineup(Team team, Position position) {
        for (Map.Entry<Slot, Player> e : team.getLineup().get().getAssignments().entrySet()) {
            if (e.getKey().getPosition() == position) {
                return e.getValue();
            }
        }
        throw new AssertionError("No " + position);
    }

    private static void placeAll(Team team, double x, double y) {
        for (Player p : team.getLineup().get().getAssignments().values()) {
            p.setCoordinates(new Coordinates(x, y));
        }
    }

    @BeforeEach
    void setUp() {
        pitch = new Pitch(105, 68);
        homeBench = player("Bench", 12, Position.CM);
        home = teamWithLineup("Home", Formations.fourThreeThree(), homeBench);
        away = teamWithLineup("Away", Formations.fourThreeThree());
        match = new Match(pitch, home, away, HALF_LENGTH);
        state = new MatchState(match);
    }

    @Test
    void rejectsNullMatch() {
        assertThrows(IllegalArgumentException.class, () -> new MatchState(null));
    }

    // ---------------------------------------------------------------- players

    @Test
    void playerCoordinatesCoverOnlyLineupPlayers() {
        Map<PlayerView, Coordinates> coords = state.getPlayerCoordinates();
        assertEquals(22, coords.size());
        assertFalse(coords.containsKey(homeBench));
        Player keeper = inLineup(home, Position.GK);
        assertEquals(keeper.getCoordinates(), coords.get(keeper));
    }

    @Test
    void playerCoordinatesMapIsAnUnmodifiableSnapshot() {
        Player keeper = inLineup(home, Position.GK);
        Map<PlayerView, Coordinates> before = state.getPlayerCoordinates();
        assertThrows(UnsupportedOperationException.class, () -> before.put(keeper, new Coordinates(1, 1)));

        keeper.setCoordinates(new Coordinates(3, 34));
        assertEquals(new Coordinates(10, 10), before.get(keeper));
        assertEquals(new Coordinates(3, 34), state.getPlayerCoordinates().get(keeper)); // the view itself is live
    }

    @Test
    void playersBySide() {
        assertEquals(11, state.getPlayers(Side.HOME).size());
        assertEquals(11, state.getPlayers(Side.AWAY).size());
        assertFalse(state.getPlayers(Side.HOME).contains(homeBench));
        assertThrows(UnsupportedOperationException.class,
                () -> state.getPlayers(Side.HOME).add(homeBench));
        assertThrows(IllegalArgumentException.class, () -> state.getPlayers(null));
    }

    @Test
    void sideOfOnPitchPlayersAndRejectsBench() {
        assertEquals(Side.HOME, state.sideOf(inLineup(home, Position.ST)));
        assertEquals(Side.AWAY, state.sideOf(inLineup(away, Position.ST)));
        assertThrows(IllegalArgumentException.class, () -> state.sideOf(homeBench));
    }

    @Test
    void teammatesExcludeThePlayerAndOpponentsAreTheOtherSide() {
        Player striker = inLineup(home, Position.ST);
        List<PlayerView> teammates = state.teammatesOf(striker);
        assertEquals(10, teammates.size());
        assertFalse(teammates.contains(striker));
        List<PlayerView> opponents = state.opponentsOf(striker);
        assertEquals(11, opponents.size());
        for (PlayerView o : opponents) {
            assertEquals(Side.AWAY, state.sideOf(o));
        }
    }

    @Test
    void sideWithoutLineupHasNoPlayers() {
        Team noLineup = new Team("Empty", Collections.singletonList(player("Solo", 1, Position.GK)));
        MatchState s = new MatchState(new Match(pitch, home, noLineup));
        assertTrue(s.getPlayers(Side.AWAY).isEmpty());
        assertEquals(11, s.getPlayerCoordinates().size());
        assertEquals(Double.POSITIVE_INFINITY, s.distanceToNearestOpponent(inLineup(home, Position.ST)));
    }

    // ---------------------------------------------------------------- ball

    @Test
    void looseBallHasNoHolderOrPossession() {
        assertFalse(state.isBallHeld());
        assertFalse(state.getBallHolder().isPresent());
        assertFalse(state.getSideInPossession().isPresent());
        assertFalse(state.getLastTouchedBy().isPresent());
        assertEquals(new Coordinates(52.5, 34), state.getBallCoordinates());
    }

    @Test
    void heldBallReportsHolderSideAndFollowsThePlayer() {
        Player striker = inLineup(away, Position.ST);
        match.getBall().giveTo(striker);
        assertTrue(state.isBallHeld());
        assertSame(striker, state.getBallHolder().get());
        assertEquals(Side.AWAY, state.getSideInPossession().get());
        assertSame(striker, state.getLastTouchedBy().get());

        striker.setCoordinates(new Coordinates(70, 20));
        assertEquals(new Coordinates(70, 20), state.getBallCoordinates());
    }

    // ---------------------------------------------------------------- context

    @Test
    void scoreAndGoalDifferenceFromEachSide() {
        match.startFirstHalf();
        match.recordGoal(home);
        match.recordGoal(home);
        match.recordGoal(away);
        assertEquals(2, state.getGoals(Side.HOME));
        assertEquals(1, state.getGoals(Side.AWAY));
        assertEquals(1, state.goalDifferenceFor(Side.HOME));
        assertEquals(-1, state.goalDifferenceFor(Side.AWAY));
        assertEquals("Home", state.getTeamName(Side.HOME));
        assertEquals("Away", state.getTeamName(Side.AWAY));
    }

    @Test
    void periodAndRemainingTime() {
        assertEquals(MatchPeriod.NOT_STARTED, state.getPeriod());
        match.startFirstHalf();
        match.advanceClock(30);
        assertEquals(30, state.getElapsedInPeriodSeconds(), 1e-9);
        assertEquals(70, state.getRemainingInPeriodSeconds(), 1e-9);
        match.advanceClock(80);
        assertEquals(0, state.getRemainingInPeriodSeconds(), 1e-9);
    }

    @Test
    void attackDirectionsAndGoalsSwapAtHalfTime() {
        assertEquals(AttackDirection.TOWARD_MAX_X, state.getAttackDirection(Side.HOME));
        assertEquals(new Coordinates(105, 34), state.goalAttackedBy(Side.HOME));
        assertEquals(new Coordinates(0, 34), state.goalDefendedBy(Side.HOME));
        assertEquals(new Coordinates(0, 34), state.goalAttackedBy(Side.AWAY));

        match.startFirstHalf();
        match.endHalf();
        match.startSecondHalf();
        assertEquals(AttackDirection.TOWARD_MIN_X, state.getAttackDirection(Side.HOME));
        assertEquals(new Coordinates(0, 34), state.goalAttackedBy(Side.HOME));
        assertEquals(new Coordinates(105, 34), state.goalAttackedBy(Side.AWAY));
    }

    @Test
    void pitchDimensions() {
        assertEquals(105, state.getPitchLength(), 1e-9);
        assertEquals(68, state.getPitchWidth(), 1e-9);
    }

    // ---------------------------------------------------------------- offside

    @Test
    void offsideLineIsTheSecondLastDefender() {
        placeAll(away, 60, 34);
        inLineup(away, Position.GK).setCoordinates(new Coordinates(100, 34));
        inLineup(away, Position.RB).setCoordinates(new Coordinates(80, 60));
        assertEquals(80, state.offsideLineFor(Side.HOME), 1e-9);

        Player striker = inLineup(home, Position.ST);
        striker.setCoordinates(new Coordinates(85, 34));
        assertTrue(state.isInOffsidePosition(striker));
        striker.setCoordinates(new Coordinates(80, 34));
        assertFalse(state.isInOffsidePosition(striker)); // level is onside
        striker.setCoordinates(new Coordinates(79, 34));
        assertFalse(state.isInOffsidePosition(striker));
    }

    @Test
    void offsideLineMovesUpToTheBallWhenItIsFurtherForward() {
        placeAll(away, 60, 34);
        inLineup(away, Position.GK).setCoordinates(new Coordinates(100, 34));
        inLineup(away, Position.RB).setCoordinates(new Coordinates(80, 60));
        match.getBall().moveTo(new Coordinates(90, 34));
        assertEquals(90, state.offsideLineFor(Side.HOME), 1e-9);
        Player striker = inLineup(home, Position.ST);
        striker.setCoordinates(new Coordinates(85, 34));
        assertFalse(state.isInOffsidePosition(striker));
    }

    @Test
    void nobodyIsOffsideInTheirOwnHalf() {
        placeAll(away, 30, 34);
        match.getBall().moveTo(new Coordinates(20, 34));
        assertEquals(52.5, state.offsideLineFor(Side.HOME), 1e-9);
        Player striker = inLineup(home, Position.ST);
        striker.setCoordinates(new Coordinates(50, 34));
        assertFalse(state.isInOffsidePosition(striker));
        striker.setCoordinates(new Coordinates(53, 34));
        assertTrue(state.isInOffsidePosition(striker));
    }

    @Test
    void offsideWorksForTheSideAttackingTowardMinX() {
        placeAll(home, 40, 34);
        inLineup(home, Position.GK).setCoordinates(new Coordinates(2, 34));
        inLineup(home, Position.LB).setCoordinates(new Coordinates(20, 8));
        assertEquals(20, state.offsideLineFor(Side.AWAY), 1e-9);

        Player striker = inLineup(away, Position.ST);
        striker.setCoordinates(new Coordinates(15, 34));
        assertTrue(state.isInOffsidePosition(striker));
        striker.setCoordinates(new Coordinates(25, 34));
        assertFalse(state.isInOffsidePosition(striker));
    }

    @Test
    void withFewerThanTwoOpponentsNobodyIsOffside() {
        Formation keeperOnly = new Formation("Keeper only");
        Slot gk = new Slot(Position.GK, new Coordinates(0.05, 0.5));
        keeperOnly.addSlot(gk);
        Player keeper = player("Lone Keeper", 1, Position.GK);
        Team thin = new Team("Thin", Collections.singletonList(keeper));
        thin.setFormation(keeperOnly);
        thin.assign(gk, keeper);
        MatchState s = new MatchState(new Match(pitch, home, thin));

        assertEquals(105, s.offsideLineFor(Side.HOME), 1e-9);
        Player striker = inLineup(home, Position.ST);
        striker.setCoordinates(new Coordinates(104, 34));
        assertFalse(s.isInOffsidePosition(striker));
    }

    // ---------------------------------------------------------------- pressure

    @Test
    void distanceToNearestOpponent() {
        placeAll(away, 100, 60);
        Player striker = inLineup(home, Position.ST);
        striker.setCoordinates(new Coordinates(50, 30));
        inLineup(away, Position.CB).setCoordinates(new Coordinates(53, 34)); // 3-4-5 triangle
        assertEquals(5, state.distanceToNearestOpponent(striker), 1e-9);
    }
}
