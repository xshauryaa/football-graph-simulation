package main.model;

import java.util.List;

public class Player {
    
    private String name; // eg. Lionel Messi
    private int number; // eg. 10
    private Coordinates coordinates; // current location on the pitch, in metres
    private List<Position> playablePositions; // eg. [RW, ST, CAM, CF]

    // REQUIRES: name is not null, number is between 1 and 99, coordinates is not null, playablePositions is not null
    // EFFECTS: Constructs a Player object with the given name, number, coordinates, and playable positions
    public Player(String name, int number, Coordinates coordinates, List<Position> playablePositions) {
        this.name = name;
        this.number = number;
        this.coordinates = coordinates;
        this.playablePositions = playablePositions;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public List<Position> getPlayablePositions() {
        return playablePositions;
    }

    public void setPlayablePositions(List<Position> playablePositions) {
        this.playablePositions = playablePositions;
    }
}
