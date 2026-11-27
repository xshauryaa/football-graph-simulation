package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A reusable reference arrangement of slots, e.g. "4-3-3". It describes a starting
 * structure only: it holds no players (see Lineup) and does not constrain movement during play.
 *
 * A formation does not restrict who can pass to whom: every slot can in principle pass to every
 * other slot. Which passes are actually good is decided by live feasibility and scoring during
 * the match, not by the formation.
 */
public class Formation {

    public static final int SLOTS_PER_FORMATION = 11;

    private String name; // eg. "4-3-3"
    private final List<Slot> slots; // slots in the formation, at most SLOTS_PER_FORMATION

    // REQUIRES: name is not null or blank
    // EFFECTS: Constructs an empty Formation; throws IllegalArgumentException otherwise
    public Formation(String name) {
        setName(name);
        this.slots = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Formation name must not be null or blank");
        }
        this.name = name;
    }

    // EFFECTS: returns an unmodifiable view of the slots
    public List<Slot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    // REQUIRES: slot is not null, is not already in this formation, and the formation is not full
    // EFFECTS: adds the slot; throws IllegalArgumentException otherwise
    public void addSlot(Slot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("Slot must not be null");
        }
        if (slots.contains(slot)) {
            throw new IllegalArgumentException("Slot is already in this formation");
        }
        if (slots.size() >= SLOTS_PER_FORMATION) {
            throw new IllegalArgumentException("A formation has at most " + SLOTS_PER_FORMATION + " slots");
        }
        slots.add(slot);
    }

    // EFFECTS: removes the slot; returns true if the slot was in the formation
    public boolean removeSlot(Slot slot) {
        return slots.remove(slot);
    }

    // EFFECTS: returns true if the formation has exactly SLOTS_PER_FORMATION slots and exactly one GK.
    //          A formation may be built up incrementally, so this is checked on demand, not on construction.
    public boolean isValid() {
        if (slots.size() != SLOTS_PER_FORMATION) {
            return false;
        }
        int goalkeepers = 0;
        for (Slot slot : slots) {
            if (slot.getPosition() == Position.GK) {
                goalkeepers++;
            }
        }
        return goalkeepers == 1;
    }

    // EFFECTS: returns a new formation for a team attacking the opposite way: every slot is rotated 180 degrees
    //          about the pitch centre (so a left-back stays on the team's own left), keeping its position.
    //          This formation is left unchanged, and the result shares no Slot objects with it, so mutating
    //          one never affects the other.
    public Formation flipped() {
        Formation flipped = new Formation(name);
        for (Slot slot : slots) {
            flipped.addSlot(new Slot(slot.getPosition(), slot.getCoordinates().rotated180(1, 1)));
        }
        return flipped;
    }

}
