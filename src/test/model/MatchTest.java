package test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.model.AttackDirection;
import main.model.Coordinates;
import main.model.Match;
import main.model.MatchPeriod;
import main.model.Pitch;
import main.model.Player;
import main.model.Position;
import main.model.Team;

class MatchTest {

    private Pitch pitch;
    private Team home;
    private Team away;
    private Match match;

    private static Team team(String name) {
        List<Player> squad = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            squad.add(new Player(name + " " + i, i, new Coordinates(0, 0), Collections.singletonList(Position.CM)));
        }
        return new Team(name, squad);
    }

    @BeforeEach
    void setUp() {
        pitch = new Pitch(105, 68);
        home = team("Home");
        away = team("Away");
        match = new Match(pitch, home, away);
    }

    // ---------------------------------------------------------------- construction

    @Test
    void startsNotStartedNilNilWithLooseBallOnCentreSpot() {
        assertEquals(MatchPeriod.NOT_STARTED, match.getPeriod());
        assertEquals(0, match.getGoals(home));
        assertEquals(0, match.getGoals(away));
        assertEquals(0, match.getElapsedInPeriodSeconds());
        assertEquals(Match.DEFAULT_HALF_LENGTH_SECONDS, match.getHalfLengthSeconds());
        assertFalse(match.getBall().isHeld());
        assertEquals(new Coordinates(52.5, 34), match.getBall().getCoordinates());
        assertEquals(new Coordinates(52.5, 34), match.getCentreSpot());
    }

    @Test
    void rejectsNullsAndATeamPlayingItself() {
        assertThrows(IllegalArgumentException.class, () -> new Match(null, home, away));
        assertThrows(IllegalArgumentException.class, () -> new Match(pitch, null, away));
        assertThrows(IllegalArgumentException.class, () -> new Match(pitch, home, null));
        assertThrows(IllegalArgumentException.class, () -> new Match(pitch, home, home));
    }

    @Test
    void rejectsAPlayerInBothSquads() {
        Player shared = home.getSquad().get(0);
        Team other = new Team("Other", Collections.singletonList(shared));
        assertThrows(IllegalArgumentException.class, () -> new Match(pitch, home, other));
    }

    @Test
    void rejectsInvalidHalfLength() {
        assertThrows(IllegalArgumentException.class, () -> new Match(pitch, home, away, 0));
        assertThrows(IllegalArgumentException.class, () -> new Match(pitch, home, away, -1));
        assertThrows(IllegalArgumentException.class, () -> new Match(pitch, home, away, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Match(pitch, home, away, Double.POSITIVE_INFINITY));
    }

    @Test
    void opponentOfReturnsTheOtherTeam() {
        assertSame(away, match.opponentOf(home));
        assertSame(home, match.opponentOf(away));
        assertThrows(IllegalArgumentException.class, () -> match.opponentOf(team("Stranger")));
    }

    // ---------------------------------------------------------------- direction

    @Test
    void homeAttacksTowardMaxXUntilSecondHalf() {
        assertEquals(AttackDirection.TOWARD_MAX_X, match.getAttackDirection(home));
        assertEquals(AttackDirection.TOWARD_MIN_X, match.getAttackDirection(away));
        match.startFirstHalf();
        assertEquals(AttackDirection.TOWARD_MAX_X, match.getAttackDirection(home));
        match.endHalf();
        assertEquals(AttackDirection.TOWARD_MAX_X, match.getAttackDirection(home)); // half-time: not swapped yet
    }

    @Test
    void teamsSwapEndsForTheSecondHalf() {
        match.startFirstHalf();
        match.endHalf();
        match.startSecondHalf();
        assertEquals(AttackDirection.TOWARD_MIN_X, match.getAttackDirection(home));
        assertEquals(AttackDirection.TOWARD_MAX_X, match.getAttackDirection(away));
        match.endHalf();
        assertEquals(AttackDirection.TOWARD_MIN_X, match.getAttackDirection(home));
    }

    @Test
    void attackDirectionRejectsUnknownTeam() {
        assertThrows(IllegalArgumentException.class, () -> match.getAttackDirection(team("Stranger")));
    }

    @Test
    void attackDirectionSignsAndOpposites() {
        assertEquals(1, AttackDirection.TOWARD_MAX_X.getSign());
        assertEquals(-1, AttackDirection.TOWARD_MIN_X.getSign());
        assertEquals(AttackDirection.TOWARD_MIN_X, AttackDirection.TOWARD_MAX_X.opposite());
        assertEquals(AttackDirection.TOWARD_MAX_X, AttackDirection.TOWARD_MIN_X.opposite());
    }

    // ---------------------------------------------------------------- periods

    @Test
    void periodsFollowTheLegalOrder() {
        match.startFirstHalf();
        assertEquals(MatchPeriod.FIRST_HALF, match.getPeriod());
        match.endHalf();
        assertEquals(MatchPeriod.HALF_TIME, match.getPeriod());
        match.startSecondHalf();
        assertEquals(MatchPeriod.SECOND_HALF, match.getPeriod());
        match.endHalf();
        assertEquals(MatchPeriod.FULL_TIME, match.getPeriod());
    }

    @Test
    void illegalTransitionsThrowAndLeavePeriodUnchanged() {
        assertThrows(IllegalStateException.class, () -> match.startSecondHalf());
        assertThrows(IllegalStateException.class, () -> match.endHalf());
        match.startFirstHalf();
        assertThrows(IllegalStateException.class, () -> match.startFirstHalf());
        assertThrows(IllegalStateException.class, () -> match.startSecondHalf());
        assertEquals(MatchPeriod.FIRST_HALF, match.getPeriod());
        match.endHalf();
        match.startSecondHalf();
        match.endHalf();
        assertThrows(IllegalStateException.class, () -> match.endHalf());
        assertEquals(MatchPeriod.FULL_TIME, match.getPeriod());
    }

    // ---------------------------------------------------------------- clock

    @Test
    void clockAccumulatesAndReportsTimeUp() {
        Match short_ = new Match(pitch, home, away, 10);
        short_.startFirstHalf();
        short_.advanceClock(4);
        short_.advanceClock(5.5);
        assertEquals(9.5, short_.getElapsedInPeriodSeconds(), 1e-9);
        assertFalse(short_.isPeriodTimeUp());
        short_.advanceClock(0.5);
        assertTrue(short_.isPeriodTimeUp());
        assertEquals(MatchPeriod.FIRST_HALF, short_.getPeriod()); // the clock does not end the half by itself
    }

    @Test
    void clockResetsBetweenPeriods() {
        match.startFirstHalf();
        match.advanceClock(100);
        match.endHalf();
        assertEquals(0, match.getElapsedInPeriodSeconds());
        assertFalse(match.isPeriodTimeUp());
        match.startSecondHalf();
        assertEquals(0, match.getElapsedInPeriodSeconds());
    }

    @Test
    void clockOnlyRunsDuringAHalf() {
        assertThrows(IllegalStateException.class, () -> match.advanceClock(1));
        match.startFirstHalf();
        match.endHalf();
        assertThrows(IllegalStateException.class, () -> match.advanceClock(1));
    }

    @Test
    void clockRejectsInvalidDurations() {
        match.startFirstHalf();
        assertThrows(IllegalArgumentException.class, () -> match.advanceClock(0));
        assertThrows(IllegalArgumentException.class, () -> match.advanceClock(-1));
        assertThrows(IllegalArgumentException.class, () -> match.advanceClock(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> match.advanceClock(Double.POSITIVE_INFINITY));
        assertEquals(0, match.getElapsedInPeriodSeconds());
    }

    // ---------------------------------------------------------------- score

    @Test
    void recordGoalCountsForTheRightTeam() {
        match.startFirstHalf();
        match.recordGoal(home);
        match.recordGoal(home);
        match.recordGoal(away);
        assertEquals(2, match.getGoals(home));
        assertEquals(1, match.getGoals(away));
    }

    @Test
    void goalsOnlyCountWhileInPlay() {
        assertThrows(IllegalStateException.class, () -> match.recordGoal(home));
        match.startFirstHalf();
        match.endHalf();
        assertThrows(IllegalStateException.class, () -> match.recordGoal(home));
        assertEquals(0, match.getGoals(home));
    }

    @Test
    void scoreRejectsUnknownTeam() {
        match.startFirstHalf();
        Team stranger = team("Stranger");
        assertThrows(IllegalArgumentException.class, () -> match.recordGoal(stranger));
        assertThrows(IllegalArgumentException.class, () -> match.getGoals(stranger));
    }

    @Test
    void inPlayPeriods() {
        assertFalse(MatchPeriod.NOT_STARTED.isInPlay());
        assertTrue(MatchPeriod.FIRST_HALF.isInPlay());
        assertFalse(MatchPeriod.HALF_TIME.isInPlay());
        assertTrue(MatchPeriod.SECOND_HALF.isInPlay());
        assertFalse(MatchPeriod.FULL_TIME.isInPlay());
    }
}
