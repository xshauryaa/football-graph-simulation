package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A read-only, live view of a Match for decision-makers and rules.
 *
 * It stores nothing of its own: every query reads the current state from its owners (players hold their
 * coordinates, the ball its holder, the match its score and clock). Teams are identified by Side and players
 * are handed out as PlayerView, so nothing reached through a MatchState can change the match.
 *
 * Because the view is live, the match loop must not change state while players are deciding: in each tick,
 * every player decides from the same state first, and only then are the chosen actions applied.
 *
 * "On the pitch" means assigned in the team's lineup; bench players are not part of the state.
 */
public final class MatchState {

    private final Match match;

    // REQUIRES: match is not null
    // EFFECTS: Constructs a view over the match; throws IllegalArgumentException otherwise
    public MatchState(Match match) {
        if (match == null) {
            throw new IllegalArgumentException("Match must not be null");
        }
        this.match = match;
    }

    // ---------------------------------------------------------------- players

    // EFFECTS: returns every on-pitch player's current coordinates, home players first.
    //          The map is a fresh, unmodifiable snapshot of this moment.
    public Map<PlayerView, Coordinates> getPlayerCoordinates() {
        Map<PlayerView, Coordinates> coordinates = new LinkedHashMap<>();
        for (Side side : Side.values()) {
            for (Player player : lineupPlayers(side)) {
                coordinates.put(player, player.getCoordinates());
            }
        }
        return Collections.unmodifiableMap(coordinates);
    }

    // EFFECTS: returns the on-pitch players of the side, in formation slot order
    public List<PlayerView> getPlayers(Side side) {
        requireSide(side);
        return Collections.unmodifiableList(new ArrayList<PlayerView>(lineupPlayers(side)));
    }

    // REQUIRES: player is on the pitch
    // EFFECTS: returns the side the player plays for; throws IllegalArgumentException otherwise
    public Side sideOf(PlayerView player) {
        for (Side side : Side.values()) {
            if (lineupPlayers(side).contains(player)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Player is not on the pitch");
    }

    // REQUIRES: player is on the pitch
    // EFFECTS: returns the player's on-pitch teammates, excluding the player
    public List<PlayerView> teammatesOf(PlayerView player) {
        List<PlayerView> teammates = new ArrayList<>(getPlayers(sideOf(player)));
        teammates.remove(player);
        return Collections.unmodifiableList(teammates);
    }

    // REQUIRES: player is on the pitch
    // EFFECTS: returns the on-pitch players of the other side
    public List<PlayerView> opponentsOf(PlayerView player) {
        return getPlayers(sideOf(player).opposite());
    }

    // ---------------------------------------------------------------- ball

    public Coordinates getBallCoordinates() {
        return match.getBall().getCoordinates();
    }

    public boolean isBallHeld() {
        return match.getBall().isHeld();
    }

    // EFFECTS: returns the player holding the ball, or empty if it is loose
    public Optional<PlayerView> getBallHolder() {
        return match.getBall().getHolder().map(p -> (PlayerView) p);
    }

    // EFFECTS: returns the side holding the ball, or empty if it is loose
    public Optional<Side> getSideInPossession() {
        return getBallHolder().map(this::sideOf);
    }

    // EFFECTS: returns the last player to touch the ball, or empty if nobody has since it was placed
    public Optional<PlayerView> getLastTouchedBy() {
        return match.getBall().getLastTouchedBy().map(p -> (PlayerView) p);
    }

    // ---------------------------------------------------------------- match context

    public String getTeamName(Side side) {
        return team(side).getName();
    }

    public int getGoals(Side side) {
        return match.getGoals(team(side));
    }

    // EFFECTS: returns the side's goals minus the opponent's (positive when winning)
    public int goalDifferenceFor(Side side) {
        return getGoals(side) - getGoals(side.opposite());
    }

    public MatchPeriod getPeriod() {
        return match.getPeriod();
    }

    public double getElapsedInPeriodSeconds() {
        return match.getElapsedInPeriodSeconds();
    }

    // EFFECTS: returns the seconds left in the current half, never below 0
    public double getRemainingInPeriodSeconds() {
        return Math.max(0, match.getHalfLengthSeconds() - match.getElapsedInPeriodSeconds());
    }

    public AttackDirection getAttackDirection(Side side) {
        return match.getAttackDirection(team(side));
    }

    // ---------------------------------------------------------------- pitch geometry

    public double getPitchLength() {
        return match.getPitch().getLength();
    }

    public double getPitchWidth() {
        return match.getPitch().getWidth();
    }

    // EFFECTS: returns the centre of the goal the side is attacking
    public Coordinates goalAttackedBy(Side side) {
        return match.getPitch().getGoalCentre(getAttackDirection(side));
    }

    // EFFECTS: returns the centre of the goal the side is defending
    public Coordinates goalDefendedBy(Side side) {
        return match.getPitch().getGoalCentre(getAttackDirection(side).opposite());
    }

    // ---------------------------------------------------------------- derived queries

    // EFFECTS: returns the x coordinate an attacker of the side must be beyond to be in an offside position.
    //          It is the furthest forward (in the side's attack direction) of: the second-last opponent,
    //          the ball, and the halfway line (nobody is offside in their own half). If the opponents have
    //          fewer than two players on the pitch, the line is the goal line, so nobody is offside.
    public double offsideLineFor(Side attackingSide) {
        AttackDirection direction = getAttackDirection(attackingSide);
        int sign = direction.getSign();
        List<PlayerView> defenders = getPlayers(attackingSide.opposite());

        double secondLastDefender;
        if (defenders.size() < 2) {
            secondLastDefender = match.getPitch().getGoalCentre(direction).getX();
        } else {
            // Sort defenders by how deep they are toward their own goal; take the second deepest.
            List<Double> depths = new ArrayList<>();
            for (PlayerView defender : defenders) {
                depths.add(sign * defender.getCoordinates().getX());
            }
            Collections.sort(depths, Collections.reverseOrder());
            secondLastDefender = sign * depths.get(1);
        }

        double halfway = getPitchLength() / 2.0;
        double line = furthest(sign, secondLastDefender, getBallCoordinates().getX());
        return furthest(sign, line, halfway);
    }

    // REQUIRES: player is on the pitch
    // EFFECTS: returns true if the player is strictly beyond their side's offside line
    public boolean isInOffsidePosition(PlayerView player) {
        Side side = sideOf(player);
        int sign = getAttackDirection(side).getSign();
        return sign * player.getCoordinates().getX() > sign * offsideLineFor(side);
    }

    // REQUIRES: player is on the pitch
    // EFFECTS: returns the distance in metres to the nearest opponent (a basic pressure signal),
    //          or positive infinity if the opponents have nobody on the pitch
    public double distanceToNearestOpponent(PlayerView player) {
        double nearest = Double.POSITIVE_INFINITY;
        for (PlayerView opponent : opponentsOf(player)) {
            nearest = Math.min(nearest, player.getCoordinates().distanceTo(opponent.getCoordinates()));
        }
        return nearest;
    }

    // ---------------------------------------------------------------- helpers

    // EFFECTS: returns whichever x is further forward for a team attacking with the given sign
    private static double furthest(int sign, double a, double b) {
        return sign * a >= sign * b ? a : b;
    }

    private Team team(Side side) {
        requireSide(side);
        return side == Side.HOME ? match.getHome() : match.getAway();
    }

    private List<Player> lineupPlayers(Side side) {
        Optional<Lineup> lineup = team(side).getLineup();
        if (!lineup.isPresent()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(lineup.get().getAssignments().values());
    }

    private static void requireSide(Side side) {
        if (side == null) {
            throw new IllegalArgumentException("Side must not be null");
        }
    }
}
