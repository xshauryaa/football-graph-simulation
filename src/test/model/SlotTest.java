package test.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import main.model.Coordinates;
import main.model.Position;
import main.model.Slot;

class SlotTest {

    @Test
    void storesValues() {
        Coordinates c = new Coordinates(0.8, 0.5);
        Slot slot = new Slot(Position.ST, c);
        assertEquals(Position.ST, slot.getPosition());
        assertEquals(c, slot.getCoordinates());
    }

    @Test
    void allowsFractionBoundsInclusive() {
        assertDoesNotThrow(() -> new Slot(Position.GK, new Coordinates(0, 0)));
        assertDoesNotThrow(() -> new Slot(Position.ST, new Coordinates(1, 1)));
    }

    @Test
    void rejectsCoordinatesAboveOne() {
        assertThrows(IllegalArgumentException.class, () -> new Slot(Position.ST, new Coordinates(1.01, 0.5)));
        assertThrows(IllegalArgumentException.class, () -> new Slot(Position.ST, new Coordinates(0.5, 1.01)));
    }

    @Test
    void rejectsNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Slot(Position.ST, new Coordinates(-0.01, 0.5)));
        assertThrows(IllegalArgumentException.class, () -> new Slot(Position.ST, new Coordinates(0.5, -0.01)));
    }

    @Test
    void rejectsNulls() {
        assertThrows(IllegalArgumentException.class, () -> new Slot(null, new Coordinates(0.5, 0.5)));
        assertThrows(IllegalArgumentException.class, () -> new Slot(Position.CM, null));
    }

    @Test
    void slotsWithIdenticalContentsAreDistinct() {
        Slot a = new Slot(Position.CB, new Coordinates(0.2, 0.4));
        Slot b = new Slot(Position.CB, new Coordinates(0.2, 0.4));
        assertNotEquals(a, b);
        assertEquals(a, a);
    }

    @Test
    void fractionalCoordinatesCanBeFlippedForTheOtherSide() {
        Slot slot = new Slot(Position.LB, new Coordinates(0.25, 0.1));
        assertEquals(new Coordinates(0.75, 0.9), slot.getCoordinates().rotated180(1, 1));
    }
}
