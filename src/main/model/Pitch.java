package main.model;

public class Pitch {

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

    // EFFECTS: returns true if the coordinates lie on the pitch (edges included)
    public boolean contains(Coordinates coordinates) {
        return coordinates.getX() <= length && coordinates.getY() <= width;
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
