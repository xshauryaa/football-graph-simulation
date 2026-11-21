package main.model;

/**
 * A nominal place in a formation: a Position plus a reference location on the pitch.
 *
 * A Slot is a template element. It does not know which Player fills it (see Lineup)
 * and it is not a player's live location (see Player.getCoordinates()). Slots are
 * immutable, and two slots are equal only if they are the same object, so two slots
 * with identical contents (e.g. the two CBs) remain distinct.
 */
public final class Slot {

    private static final double MIN_FRACTION = 0.0;
    private static final double MAX_FRACTION = 1.0;

    private final Position position; // eg. RW, ST, CAM, CF
    private final Coordinates coordinates; // nominal location as a fraction of the pitch (0.0 to 1.0 on both axes)

    // REQUIRES: position is not null; coordinates is not null and both x and y are between 0.0 and 1.0.
    //           Coordinates follow the pitch convention: origin top-left, x along the length, y along the width,
    //           with the team attacking toward x = 1.0 (see Coordinates.rotated180 to flip for the other side).
    // EFFECTS: Constructs a Slot; throws IllegalArgumentException if any requirement is violated
    public Slot(Position position, Coordinates coordinates) {
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates must not be null");
        }
        if (!isFraction(coordinates.getX()) || !isFraction(coordinates.getY())) {
            throw new IllegalArgumentException(
                    "Slot coordinates must be fractions between " + MIN_FRACTION + " and " + MAX_FRACTION
                            + ", got " + coordinates);
        }
        this.position = position;
        this.coordinates = coordinates;
    }

    private static boolean isFraction(double value) {
        return value >= MIN_FRACTION && value <= MAX_FRACTION;
    }

    public Position getPosition() {
        return position;
    }

    // EFFECTS: returns the nominal location as fractions of the pitch (not metres)
    public Coordinates getCoordinates() {
        return coordinates;
    }
}
