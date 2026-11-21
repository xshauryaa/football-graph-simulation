package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A team: a name, a squad of eligible players, and (once a formation is chosen) a lineup
 * saying which squad players fill which formation slots.
 *
 * Only squad players can be placed in the lineup or substituted on. Squad players not in
 * the lineup are the bench.
 */
public class Team {

    private String name; // eg. FC Barcelona
    private List<Player> squad; // every player available to this team
    private Lineup lineup; // null until a formation is set; exposed as Optional

    // REQUIRES: name is not null or blank, squad is not null and has no null elements
    // EFFECTS: Constructs a Team with the given name and a copy of the squad, and no formation or lineup yet;
    //          throws IllegalArgumentException if any requirement is violated
    public Team(String name, List<Player> squad) {
        setName(name);
        setSquad(squad);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name must not be null or blank");
        }
        this.name = name;
    }

    // EFFECTS: returns an unmodifiable view of the squad
    public List<Player> getSquad() {
        return Collections.unmodifiableList(squad);
    }

    // REQUIRES: squad is not null, has no null elements, and still contains every player in the current lineup
    // EFFECTS: stores a copy of the given squad, so later changes to the argument do not affect this team;
    //          throws IllegalArgumentException otherwise, leaving the team unchanged
    public void setSquad(List<Player> squad) {
        if (squad == null || squad.contains(null)) {
            throw new IllegalArgumentException("Squad must not be null and must contain no null players");
        }
        if (lineup != null) {
            for (Player starter : lineup.getAssignments().values()) {
                if (!squad.contains(starter)) {
                    throw new IllegalArgumentException("New squad must contain every player in the lineup");
                }
            }
        }
        this.squad = new ArrayList<>(squad);
    }

    // EFFECTS: returns the current lineup, or empty if no formation has been set
    public Optional<Lineup> getLineup() {
        return Optional.ofNullable(lineup);
    }

    // REQUIRES: formation is not null
    // EFFECTS: starts a new, empty lineup for the formation, replacing any previous lineup.
    //          Players are not carried over; assign them again.
    public void setFormation(Formation formation) {
        this.lineup = new Lineup(formation);
    }

    // REQUIRES: a formation has been set, slot belongs to it, and player is in the squad and not already in the lineup
    // EFFECTS: places the player in the slot, replacing whoever was there; throws IllegalArgumentException otherwise.
    //          For a swap during play, use substitute.
    public void assign(Slot slot, Player player) {
        Lineup current = requireLineup();
        requireInSquad(player);
        current.assign(slot, player);
    }

    // REQUIRES: a formation has been set, playerOff is in the lineup, and playerOn is in the squad but not in the lineup
    // EFFECTS: playerOn takes over playerOff's slot and playerOff goes to the bench;
    //          throws IllegalArgumentException otherwise, leaving the lineup unchanged.
    //          This only changes the assignment; it does not move playerOn on the pitch.
    public void substitute(Player playerOff, Player playerOn) {
        Lineup current = requireLineup();
        requireInSquad(playerOn);
        Slot slot = current.getSlot(playerOff)
                .orElseThrow(() -> new IllegalArgumentException("Player to substitute off is not in the lineup"));
        if (current.contains(playerOn)) {
            throw new IllegalArgumentException("Player to substitute on is already in the lineup");
        }
        current.assign(slot, playerOn);
    }

    // EFFECTS: returns the squad players who are not in the lineup (all of the squad if there is no lineup)
    public List<Player> getBench() {
        List<Player> bench = new ArrayList<>();
        for (Player player : squad) {
            if (lineup == null || !lineup.contains(player)) {
                bench.add(player);
            }
        }
        return Collections.unmodifiableList(bench);
    }

    private Lineup requireLineup() {
        if (lineup == null) {
            throw new IllegalArgumentException("No formation has been set for this team");
        }
        return lineup;
    }

    private void requireInSquad(Player player) {
        if (player == null || !squad.contains(player)) {
            throw new IllegalArgumentException("Player must be non-null and in the squad");
        }
    }
}
