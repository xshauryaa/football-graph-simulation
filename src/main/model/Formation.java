package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A reusable reference arrangement of slots, e.g. "4-3-3". It describes a starting
 * structure only: it holds no players (see Lineup) and does not constrain movement during play.
 *
 * Each slot may have a list of slots it can pass to. These are static reference options,
 * not live feasibility; whether a pass is actually possible is decided during the match.
 */
public class Formation {

    public static final int SLOTS_PER_FORMATION = 11;

    private String name; // eg. "4-3-3"
    private final List<Slot> slots; // slots in the formation, at most SLOTS_PER_FORMATION
    private final Map<Slot, List<Slot>> passingOptions; // slot -> slots it can pass to; keys and values are in slots

    // REQUIRES: name is not null or blank
    // EFFECTS: Constructs an empty Formation; throws IllegalArgumentException otherwise
    public Formation(String name) {
        setName(name);
        this.slots = new ArrayList<>();
        this.passingOptions = new LinkedHashMap<>();
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

    // EFFECTS: removes the slot and every passing option that mentions it, so no stale references remain.
    //          Returns true if the slot was in the formation.
    public boolean removeSlot(Slot slot) {
        if (!slots.remove(slot)) {
            return false;
        }
        passingOptions.remove(slot);
        for (List<Slot> options : passingOptions.values()) {
            options.remove(slot);
        }
        return true;
    }

    // REQUIRES: slot is in this formation
    // EFFECTS: returns an unmodifiable copy of the slots this slot can pass to (empty if none set);
    //          throws IllegalArgumentException if the slot is not in this formation
    public List<Slot> getPassingOptions(Slot slot) {
        requireMember(slot);
        List<Slot> options = passingOptions.get(slot);
        if (options == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(options));
    }

    // REQUIRES: slot and every option are in this formation, no option is null, repeated, or the slot itself
    // EFFECTS: replaces the passing options for the slot; throws IllegalArgumentException otherwise
    public void setPassingOptions(Slot slot, List<Slot> options) {
        requireMember(slot);
        if (options == null || options.contains(null)) {
            throw new IllegalArgumentException("Options must not be null and must contain no null slots");
        }
        for (Slot option : options) {
            requireMember(option);
            if (option == slot) {
                throw new IllegalArgumentException("A slot cannot pass to itself");
            }
            if (options.indexOf(option) != options.lastIndexOf(option)) {
                throw new IllegalArgumentException("Options must not contain the same slot twice");
            }
        }
        passingOptions.put(slot, new ArrayList<>(options));
    }

    // EFFECTS: returns an unmodifiable snapshot of all passing options
    public Map<Slot, List<Slot>> getPassingOptions() {
        Map<Slot, List<Slot>> copy = new LinkedHashMap<>();
        for (Map.Entry<Slot, List<Slot>> entry : passingOptions.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
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

    private void requireMember(Slot slot) {
        if (slot == null || !slots.contains(slot)) {
            throw new IllegalArgumentException("Slot must be non-null and belong to this formation");
        }
    }
}
