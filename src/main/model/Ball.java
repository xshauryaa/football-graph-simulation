package main.model;

import java.util.Optional;

/**
 * The match ball. At any moment it is either held by a player or loose.
 *
 * - Held: the ball has no position of its own; its position is the holder's live coordinates.
 *   This keeps Player as the single owner of player locations, so the two can never drift apart.
 * - Loose: the ball has its own coordinates (in metres, same system as Pitch).
 *
 * The ball also remembers the last player to touch it, which the rules need to decide
 * restarts (e.g. corner vs goal kick). Height and velocity are not modelled yet; velocity
 * arrives with the time-stepped match loop.
 */
public class Ball {

    private Player holder; // null when the ball is loose; exposed as Optional
    private Coordinates looseCoordinates; // only meaningful when the ball is loose; null while held
    private Player lastTouchedBy; // null until someone touches it; exposed as Optional

    // REQUIRES: coordinates is not null
    // EFFECTS: Constructs a loose ball at the given coordinates that nobody has touched yet;
    //          throws IllegalArgumentException otherwise
    public Ball(Coordinates coordinates) {
        placeAt(coordinates);
    }

    // EFFECTS: returns where the ball is: the holder's coordinates if held, otherwise its own
    public Coordinates getCoordinates() {
        return holder != null ? holder.getCoordinates() : looseCoordinates;
    }

    // EFFECTS: returns true if a player has the ball
    public boolean isHeld() {
        return holder != null;
    }

    // EFFECTS: returns the player holding the ball, or empty if it is loose
    public Optional<Player> getHolder() {
        return Optional.ofNullable(holder);
    }

    // EFFECTS: returns the last player to touch the ball, or empty if nobody has touched it since it was placed
    public Optional<Player> getLastTouchedBy() {
        return Optional.ofNullable(lastTouchedBy);
    }

    // REQUIRES: player is not null
    // MODIFIES: this
    // EFFECTS: the player takes the ball (a touch); throws IllegalArgumentException if player is null
    public void giveTo(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        holder = player;
        lastTouchedBy = player;
        looseCoordinates = null;
    }

    // REQUIRES: the ball is held
    // MODIFIES: this
    // EFFECTS: the holder lets go of the ball, which becomes loose at the holder's current coordinates.
    //          The holder stays recorded as the last touch. Throws IllegalStateException if the ball is loose.
    public void release() {
        if (holder == null) {
            throw new IllegalStateException("Cannot release a ball that nobody holds");
        }
        looseCoordinates = holder.getCoordinates();
        holder = null;
    }

    // REQUIRES: the ball is loose and coordinates is not null
    // MODIFIES: this
    // EFFECTS: moves the loose ball (e.g. while rolling or in flight); does not count as a touch.
    //          Throws IllegalStateException if the ball is held, IllegalArgumentException if coordinates is null.
    public void moveTo(Coordinates coordinates) {
        if (holder != null) {
            throw new IllegalStateException("A held ball moves with its holder; release it first");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates must not be null");
        }
        looseCoordinates = coordinates;
    }

    // REQUIRES: coordinates is not null
    // MODIFIES: this
    // EFFECTS: places a dead ball for a restart (e.g. the centre spot for a kickoff): it becomes loose at the
    //          given coordinates and the last touch is cleared, since nobody has touched the placed ball yet.
    //          Throws IllegalArgumentException if coordinates is null.
    public void placeAt(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates must not be null");
        }
        holder = null;
        looseCoordinates = coordinates;
        lastTouchedBy = null;
    }
}
