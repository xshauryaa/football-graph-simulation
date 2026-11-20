package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Team {

    private String name; // eg. FC Barcelona
    private List<Player> squad; // list of players in the team

    // REQUIRES: name is not null or blank, squad is not null and has no null elements
    // EFFECTS: Constructs a Team with the given name and a copy of the squad;
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

    // EFFECTS: stores a copy of the given squad, so later changes to the argument do not affect this team
    public void setSquad(List<Player> squad) {
        if (squad == null || squad.contains(null)) {
            throw new IllegalArgumentException("Squad must not be null and must contain no null players");
        }
        this.squad = new ArrayList<>(squad);
    }
}
