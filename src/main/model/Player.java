package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player implements PlayerView {

    public static final int MIN_NUMBER = 1;
    public static final int MAX_NUMBER = 99;

    private String name; // eg. Lionel Messi
    private int number; // eg. 10
    private Coordinates coordinates; // current location on the pitch, in metres
    private List<Position> playablePositions; // eg. [RW, ST, CAM, CF]

    // REQUIRES: name is not null or blank, number is between MIN_NUMBER and MAX_NUMBER,
    //           coordinates is not null, playablePositions is not null, non-empty and has no null elements
    // EFFECTS: Constructs a Player with the given name, number, coordinates, and playable positions;
    //          throws IllegalArgumentException if any requirement is violated
    public Player(String name, int number, Coordinates coordinates, List<Position> playablePositions) {
        setName(name);
        setNumber(number);
        setCoordinates(coordinates);
        setPlayablePositions(playablePositions);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name must not be null or blank");
        }
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        if (number < MIN_NUMBER || number > MAX_NUMBER) {
            throw new IllegalArgumentException(
                    "Player number must be between " + MIN_NUMBER + " and " + MAX_NUMBER + ", got " + number);
        }
        this.number = number;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Player coordinates must not be null");
        }
        this.coordinates = coordinates;
    }

    // EFFECTS: returns an unmodifiable view of the player's playable positions
    public List<Position> getPlayablePositions() {
        return Collections.unmodifiableList(playablePositions);
    }

    // EFFECTS: stores a copy of the given positions, so later changes to the argument do not affect this player
    public void setPlayablePositions(List<Position> playablePositions) {
        if (playablePositions == null || playablePositions.isEmpty() || playablePositions.contains(null)) {
            throw new IllegalArgumentException("Playable positions must be non-empty and contain no nulls");
        }
        this.playablePositions = new ArrayList<>(playablePositions);
    }
}
