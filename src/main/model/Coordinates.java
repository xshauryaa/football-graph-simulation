package main.model;

import java.util.Objects;

/**
 * An immutable point on the pitch plane, in metres.
 *
 * Coordinate system (matches Pitch): origin (0,0) is the top-left corner,
 * x increases along the pitch length, y increases along the pitch width.
 *
 * This is a purely geometric location. It does not know whether it lies on any
 * particular pitch; use Pitch.contains to check bounds. Values may be negative or exceed
 * the pitch size, so a point off the pitch (e.g. a ball over a touchline) can be represented.
 */
public final class Coordinates {

    private final double x; // metres along the length
    private final double y; // metres along the width

    // REQUIRES: x and y are finite (not NaN or infinite)
    // EFFECTS: Constructs a Coordinates at (x, y); throws IllegalArgumentException otherwise
    public Coordinates(double x, double y) {
        if (!isValidAxis(x) || !isValidAxis(y)) {
            throw new IllegalArgumentException(
                    "Coordinates must be finite, got (" + x + ", " + y + ")");
        }
        this.x = x;
        this.y = y;
    }

    private static boolean isValidAxis(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // EFFECTS: returns the straight-line distance to other, in metres
    public double distanceTo(Coordinates other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    // EFFECTS: returns a new Coordinates offset by (dx, dy)
    public Coordinates translate(double dx, double dy) {
        return new Coordinates(x + dx, y + dy);
    }

    // Rotates this point 180 degrees about the centre of a length x width rectangle:
    // (x, y) becomes (length - x, width - y). Both axes flip so a team's left stays its left
    // when it attacks the other way. Takes plain dimensions rather than a Pitch to avoid a
    // circular dependency (Pitch already uses Coordinates). Pass (1, 1) to flip fractional
    // coordinates, or the pitch's length and width to flip metres.
    // REQUIRES: length and width are finite
    // EFFECTS: returns the rotated point. A point outside the rectangle rotates to a point outside it on the other side.
    public Coordinates rotated180(double length, double width) {
        return new Coordinates(length - x, width - y);
    }

    // Scales each axis: (x, y) becomes (x * length, y * width). Used to turn a fractional slot location
    // (0.0 to 1.0 on both axes) into metres on a pitch of the given size.
    // REQUIRES: length and width are finite
    // EFFECTS: returns the scaled point
    public Coordinates scaled(double length, double width) {
        return new Coordinates(x * length, y * width);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coordinates)) {
            return false;
        }
        Coordinates other = (Coordinates) o;
        return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
