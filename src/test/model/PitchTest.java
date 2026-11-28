package test.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import main.model.Coordinates;
import main.model.Pitch;

class PitchTest {

    @Test
    void storesDimensions() {
        Pitch pitch = new Pitch(105, 68);
        assertEquals(105, pitch.getLength());
        assertEquals(68, pitch.getWidth());
    }

    @Test
    void rejectsNonPositiveDimensionsInConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new Pitch(0, 68));
        assertThrows(IllegalArgumentException.class, () -> new Pitch(105, 0));
        assertThrows(IllegalArgumentException.class, () -> new Pitch(-105, 68));
        assertThrows(IllegalArgumentException.class, () -> new Pitch(105, -68));
    }

    @Test
    void rejectsNonPositiveDimensionsInSetters() {
        Pitch pitch = new Pitch(105, 68);
        assertThrows(IllegalArgumentException.class, () -> pitch.setLength(0));
        assertThrows(IllegalArgumentException.class, () -> pitch.setWidth(-1));
        assertEquals(105, pitch.getLength());
        assertEquals(68, pitch.getWidth());
    }

    @Test
    void containsPointsInsideAndOnEdges() {
        Pitch pitch = new Pitch(105, 68);
        assertTrue(pitch.contains(new Coordinates(50, 30)));
        assertTrue(pitch.contains(new Coordinates(0, 0)));
        assertTrue(pitch.contains(new Coordinates(105, 68)));
    }

    @Test
    void doesNotContainPointsBeyondEdges() {
        Pitch pitch = new Pitch(105, 68);
        assertFalse(pitch.contains(new Coordinates(105.1, 10)));
        assertFalse(pitch.contains(new Coordinates(10, 68.1)));
    }

    @Test
    void doesNotContainPointsBeforeTheOrigin() {
        Pitch pitch = new Pitch(105, 68);
        assertFalse(pitch.contains(new Coordinates(-0.1, 10)));
        assertFalse(pitch.contains(new Coordinates(10, -0.1)));
    }

    @Test
    void lengthAndWidthAreNotInterchangeable() {
        Pitch pitch = new Pitch(105, 68);
        assertTrue(pitch.contains(new Coordinates(100, 10)));
        assertFalse(pitch.contains(new Coordinates(10, 100)));
    }

    @Test
    void goalCentresAreOnTheGoalLines() {
        Pitch pitch = new Pitch(105, 68);
        assertEquals(new Coordinates(105, 34), pitch.getGoalCentre(main.model.AttackDirection.TOWARD_MAX_X));
        assertEquals(new Coordinates(0, 34), pitch.getGoalCentre(main.model.AttackDirection.TOWARD_MIN_X));
        assertThrows(IllegalArgumentException.class, () -> pitch.getGoalCentre(null));
        assertEquals(7.32, Pitch.GOAL_WIDTH, 1e-9);
    }
}
