package main.simulation;

import java.util.LinkedHashMap;
import java.util.Map;

import main.model.AttackDirection;
import main.model.Coordinates;
import main.model.Lineup;
import main.model.Match;
import main.model.Player;
import main.model.Slot;
import main.model.Team;

/**
 * The kickoff restart: used at the start of each half and after every goal.
 *
 * Every player starts in their own half. Each team's formation (always written as attacking toward x = 1)
 * is compressed toward its own goal by KICKOFF_DEPTH_SCALE, turned to face the team's current attack
 * direction, and converted to metres. The kicking team's most advanced player stands on the centre spot
 * with the ball.
 *
 * arrange() only computes target positions; take() applies them. Players are placed directly for now
 * (see CLAUDE.md: teleporting is fine for kickoffs).
 */
public final class Kickoff {

    // Formation depth is multiplied by this at kickoff. Slots reach x = 0.85 at most, so 0.48 keeps every player
    // inside their own half (max 0.408 of the length), and a central player outside the centre circle
    // (9.15 m radius) on a standard 105 m pitch.
    public static final double KICKOFF_DEPTH_SCALE = 0.48;

    private Kickoff() {
        // utility class: not meant to be instantiated
    }

    // REQUIRES: match is not null, kickingTeam plays in the match, and both teams have complete lineups
    // EFFECTS: returns where every lineup player of both teams should stand for the kickoff, in metres;
    //          throws IllegalArgumentException otherwise
    public static Map<Player, Coordinates> arrange(Match match, Team kickingTeam) {
        requireValid(match, kickingTeam);
        Map<Player, Coordinates> targets = new LinkedHashMap<>();
        addTeam(match, match.getHome(), targets);
        addTeam(match, match.getAway(), targets);
        targets.put(taker(kickingTeam), match.getCentreSpot());
        return targets;
    }

    // REQUIRES: as for arrange
    // MODIFIES: match's players and ball
    // EFFECTS: moves every lineup player to their kickoff position and gives the ball to the kicking team's
    //          taker on the centre spot; throws IllegalArgumentException otherwise
    public static void take(Match match, Team kickingTeam) {
        Map<Player, Coordinates> targets = arrange(match, kickingTeam);
        for (Map.Entry<Player, Coordinates> entry : targets.entrySet()) {
            entry.getKey().setCoordinates(entry.getValue());
        }
        match.getBall().placeAt(match.getCentreSpot());
        match.getBall().giveTo(taker(kickingTeam));
    }

    // EFFECTS: returns the kicking team's most advanced player by formation slot (first one on ties)
    static Player taker(Team kickingTeam) {
        Lineup lineup = kickingTeam.getLineup().get();
        Slot furthest = null;
        for (Slot slot : lineup.getFormation().getSlots()) {
            if (furthest == null || slot.getCoordinates().getX() > furthest.getCoordinates().getX()) {
                furthest = slot;
            }
        }
        return lineup.getPlayer(furthest).get();
    }

    private static void addTeam(Match match, Team team, Map<Player, Coordinates> targets) {
        AttackDirection direction = match.getAttackDirection(team);
        double length = match.getPitch().getLength();
        double width = match.getPitch().getWidth();
        for (Map.Entry<Slot, Player> entry : team.getLineup().get().getAssignments().entrySet()) {
            Coordinates slot = entry.getKey().getCoordinates();
            Coordinates compressed = new Coordinates(slot.getX() * KICKOFF_DEPTH_SCALE, slot.getY());
            Coordinates facing = direction == AttackDirection.TOWARD_MAX_X ? compressed : compressed.rotated180(1, 1);
            targets.put(entry.getValue(), facing.scaled(length, width));
        }
    }

    private static void requireValid(Match match, Team kickingTeam) {
        if (match == null) {
            throw new IllegalArgumentException("Match must not be null");
        }
        if (kickingTeam != match.getHome() && kickingTeam != match.getAway()) {
            throw new IllegalArgumentException("Kicking team is not playing in this match");
        }
        for (Team team : new Team[] {match.getHome(), match.getAway()}) {
            if (!team.getLineup().isPresent() || !team.getLineup().get().isComplete()) {
                throw new IllegalArgumentException(team.getName() + " needs a complete lineup to kick off");
            }
        }
    }
}
