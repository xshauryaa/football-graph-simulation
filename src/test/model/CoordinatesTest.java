package test.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import main.model.Coordinates;

class CoordinatesTest {

    @Test
    void storesValues() {
        Coordinates c = new Coordinates(10.5, 20);
        assertEquals(10.5, c.getX());
        assertEquals(20, c.getY());
    }

    @Test
    void allowsOrigin() {
        assertDoesNotThrow(() -> new Coordinates(0, 0));
    }

    @Test
    void rejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(-0.1, 5));
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(5, -0.1));
    }

    @Test
    void rejectsNonFiniteValues() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(Double.NaN, 5));
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(5, Double.POSITIVE_INFINITY));
    }

    @Test
    void distanceToUsesStraightLine() {
        Coordinates a = new Coordinates(0, 0);
        Coordinates b = new Coordinates(3, 4);
        assertEquals(5.0, a.distanceTo(b), 1e-9);
        assertEquals(5.0, b.distanceTo(a), 1e-9);
        assertEquals(0.0, a.distanceTo(a), 1e-9);
    }

    @Test
    void translateReturnsNewInstanceAndLeavesOriginalUnchanged() {
        Coordinates a = new Coordinates(10, 10);
        Coordinates b = a.translate(5, -3);
        assertEquals(new Coordinates(15, 7), b);
        assertEquals(new Coordinates(10, 10), a);
    }

    @Test
    void translateRejectsResultBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(1, 1).translate(-2, 0));
    }

    @Test
    void rotated180FlipsBothAxesAboutPitchCentre() {
        assertEquals(new Coordinates(95, 58), new Coordinates(10, 10).rotated180(105, 68));
        assertEquals(new Coordinates(52.5, 34), new Coordinates(52.5, 34).rotated180(105, 68));
    }

    @Test
    void rotated180MapsCornersToOppositeCorners() {
        assertEquals(new Coordinates(105, 68), new Coordinates(0, 0).rotated180(105, 68));
        assertEquals(new Coordinates(0, 0), new Coordinates(105, 68).rotated180(105, 68));
    }

    @Test
    void rotated180TwiceReturnsOriginal() {
        Coordinates c = new Coordinates(23.5, 41);
        assertEquals(c, c.rotated180(105, 68).rotated180(105, 68));
    }

    @Test
    void rotated180WorksOnFractionalCoordinates() {
        assertEquals(new Coordinates(0.9, 0.25), new Coordinates(0.1, 0.75).rotated180(1, 1));
    }

    @Test
    void rotated180RejectsPointOutsideRectangle() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(106, 10).rotated180(105, 68));
    }

    @Test
    void equalsAndHashCodeAreValueBased() {
        Coordinates a = new Coordinates(1, 2);
        Coordinates b = new Coordinates(1, 2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Coordinates(2, 1));
        assertNotEquals(a, null);
    }
}
