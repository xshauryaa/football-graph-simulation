package main.model;

import java.util.List;

public class Team {

    private String name; // eg. FC Barcelona
    private List<Player> squad; // list of players in the team

    public Team(String name, List<Player> squad) {
        this.name = name;
        this.squad = squad;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getSquad() {
        return squad;
    }

    public void setSquad(List<Player> squad) {
        this.squad = squad;
    }
}
