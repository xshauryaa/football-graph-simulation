package main.model;

import java.util.List;

/**
 * A read-only view of a player: what decision-makers and rules may look at, with no way to change it.
 * MatchState hands out PlayerView rather than Player, so code that only reads the match cannot move players.
 */
public interface PlayerView {

    String getName();

    int getNumber();

    // EFFECTS: returns the player's live location, in metres
    Coordinates getCoordinates();

    // EFFECTS: returns an unmodifiable list of the positions the player can play
    List<Position> getPlayablePositions();
}
