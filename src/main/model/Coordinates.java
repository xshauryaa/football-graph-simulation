package main.model;

import java.util.Objects;

/**
 * An immutable point on the pitch plane, in metres.
 *
 * Coordinate system (matches Pitch): origin (0,0) is the top-left corner,
 * x increases along the pitch length, y increases along the pitch width.
 *
 * This is a purely geometric location. It does not know whether it lies on any
 * particular pitch; use Pitch to check bounds.
 */
public final class Coordinates {

    private final double x; // metres along the length
    private final double y; // metres along the width

    // REQUIRES: x and y are finite and non-negative
    // EFFECTS: Constructs a Coordinates at (x, y); throws IllegalArgumentException otherwise
    public Coordinates(double x, double y) {
        if (!isValidAxis(x) || !isValidAxis(y)) {
            throw new IllegalArgumentException(
                    "Coordinates must be finite and non-negative, got (" + x + ", " + y + ")");
        }
        this.x = x;
        this.y = y;
    }

    private static boolean isValidAxis(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value >= 0;
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

    // EFFECTS: returns a new Coordinates offset by (dx, dy); throws if the result is negative
    public Coordinates translate(double dx, double dy) {
        return new Coordinates(x + dx, y + dy);
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
