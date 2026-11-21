package main.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Which Player fills which Slot of a Formation, for one team.
 *
 * Keeping this separate from Formation lets one formation be reused by any team, and keeps
 * the nominal slot, the assigned player, and the player's live coordinates as distinct concepts.
 * Assigning a player here does not move them; their live location stays on Player.
 */
public class Lineup {

    private final Formation formation;
    private final Map<Slot, Player> assignments;

    // REQUIRES: formation is not null
    // EFFECTS: Constructs a Lineup with no players assigned; throws IllegalArgumentException otherwise
    public Lineup(Formation formation) {
        if (formation == null) {
            throw new IllegalArgumentException("Formation must not be null");
        }
        this.formation = formation;
        this.assignments = new LinkedHashMap<>();
    }

    public Formation getFormation() {
        return formation;
    }

    // REQUIRES: slot belongs to the formation, player is not null and is not already assigned to another slot
    // EFFECTS: assigns the player to the slot, replacing whoever was there; throws IllegalArgumentException otherwise
    public void assign(Slot slot, Player player) {
        if (slot == null || !formation.getSlots().contains(slot)) {
            throw new IllegalArgumentException("Slot must be non-null and belong to the formation");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        for (Map.Entry<Slot, Player> entry : assignments.entrySet()) {
            if (entry.getValue() == player && entry.getKey() != slot) {
                throw new IllegalArgumentException("Player is already assigned to another slot");
            }
        }
        assignments.put(slot, player);
    }

    // EFFECTS: removes any assignment for the slot; returns true if there was one
    public boolean unassign(Slot slot) {
        return assignments.remove(slot) != null;
    }

    // EFFECTS: returns the player in the slot, or empty if the slot is unfilled
    public Optional<Player> getPlayer(Slot slot) {
        return Optional.ofNullable(assignments.get(slot));
    }

    // EFFECTS: returns the slot the player currently fills, or empty if the player is not in the lineup
    public Optional<Slot> getSlot(Player player) {
        for (Map.Entry<Slot, Player> entry : assignments.entrySet()) {
            if (entry.getValue() == player) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    // EFFECTS: returns true if the player fills any slot
    public boolean contains(Player player) {
        return getSlot(player).isPresent();
    }

    // EFFECTS: returns an unmodifiable snapshot of the current assignments
    public Map<Slot, Player> getAssignments() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(assignments));
    }

    // EFFECTS: returns true if the formation is valid and every one of its slots has a player
    public boolean isComplete() {
        return formation.isValid() && assignments.size() == formation.getSlots().size();
    }
}
