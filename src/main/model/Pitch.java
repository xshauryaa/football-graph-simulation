package main.model;

public class Pitch {

    // Width of a goal mouth between the posts, in metres (Laws of the Game: 7.32 m / 8 yards).
    public static final double GOAL_WIDTH = 7.32;

    // forms a 2d plane with length and width, where the top left corner is (0,0) and the bottom right corner is (length, width)
    private int length; // in meters
    private int width;  // in meters

    // REQUIRES: length and width are positive
    // EFFECTS: Constructs a Pitch with the given dimensions; throws IllegalArgumentException otherwise
    public Pitch(int length, int width) {
        validateDimension(length, "length");
        validateDimension(width, "width");
        this.length = length;
        this.width = width;
    }

    // EFFECTS: returns true if the coordinates lie on the pitch (edges included): 0 <= x <= length and 0 <= y <= width
    public boolean contains(Coordinates coordinates) {
        double x = coordinates.getX();
        double y = coordinates.getY();
        return x >= 0 && x <= length && y >= 0 && y <= width;
    }

    // EFFECTS: returns the centre of the goal at the end the direction points to:
    //          (length, width / 2) for TOWARD_MAX_X, (0, width / 2) for TOWARD_MIN_X.
    //          Throws IllegalArgumentException if end is null.
    public Coordinates getGoalCentre(AttackDirection end) {
        if (end == null) {
            throw new IllegalArgumentException("End must not be null");
        }
        double x = end == AttackDirection.TOWARD_MAX_X ? length : 0;
        return new Coordinates(x, width / 2.0);
    }

    private static void validateDimension(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException("Pitch " + name + " must be positive, got " + value);
        }
    }

    // Getters and Setters
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        validateDimension(length, "length");
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        validateDimension(width, "width");
        this.width = width;
    }
}
