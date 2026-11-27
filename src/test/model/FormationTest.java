package test.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.model.Coordinates;
import main.model.Formation;
import main.model.Position;
import main.model.Slot;

class FormationTest {

    private Formation formation;
    private Slot gk;
    private Slot cb;
    private Slot st;

    private static Slot slot(Position position) {
        return new Slot(position, new Coordinates(0.5, 0.5));
    }

    @BeforeEach
    void setUp() {
        formation = new Formation("test");
        gk = slot(Position.GK);
        cb = slot(Position.CB);
        st = slot(Position.ST);
        formation.addSlot(gk);
        formation.addSlot(cb);
        formation.addSlot(st);
    }

    @Test
    void rejectsBadName() {
        assertThrows(IllegalArgumentException.class, () -> new Formation(null));
        assertThrows(IllegalArgumentException.class, () -> new Formation("  "));
        assertThrows(IllegalArgumentException.class, () -> formation.setName(""));
        assertEquals("test", formation.getName());
    }

    @Test
    void addSlotRejectsNullDuplicateAndOverflow() {
        assertThrows(IllegalArgumentException.class, () -> formation.addSlot(null));
        assertThrows(IllegalArgumentException.class, () -> formation.addSlot(gk));
        for (int i = formation.getSlots().size(); i < Formation.SLOTS_PER_FORMATION; i++) {
            formation.addSlot(slot(Position.CM));
        }
        assertThrows(IllegalArgumentException.class, () -> formation.addSlot(slot(Position.CM)));
    }

    @Test
    void slotsViewIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> formation.getSlots().add(slot(Position.CM)));
    }

    @Test
    void removeSlotReturnsFalseForUnknownSlot() {
        assertFalse(formation.removeSlot(slot(Position.CM)));
    }

    @Test
    void isValidRequiresElevenSlotsAndOneGoalkeeper() {
        assertFalse(formation.isValid());
        for (int i = formation.getSlots().size(); i < Formation.SLOTS_PER_FORMATION; i++) {
            formation.addSlot(slot(Position.CM));
        }
        assertTrue(formation.isValid());
    }

    @Test
    void isValidFailsWithNoOrTwoGoalkeepers() {
        Formation noKeeper = new Formation("none");
        for (int i = 0; i < Formation.SLOTS_PER_FORMATION; i++) {
            noKeeper.addSlot(slot(Position.CM));
        }
        assertFalse(noKeeper.isValid());

        Formation twoKeepers = new Formation("two");
        twoKeepers.addSlot(slot(Position.GK));
        twoKeepers.addSlot(slot(Position.GK));
        for (int i = 2; i < Formation.SLOTS_PER_FORMATION; i++) {
            twoKeepers.addSlot(slot(Position.CM));
        }
        assertFalse(twoKeepers.isValid());
    }

    @Test
    void formationsAreIndependentOfEachOther() {
        Formation other = new Formation("other");
        assertTrue(other.getSlots().isEmpty());
        assertEquals(3, formation.getSlots().size());
    }

    // --- flipped ---

    @Test
    void flippedRotatesSlotsKeepingPositions() {
        Formation f = new Formation("f");
        f.addSlot(new Slot(Position.LB, new Coordinates(0.25, 0.1)));
        f.addSlot(new Slot(Position.ST, new Coordinates(0.8, 0.5)));

        Formation flipped = f.flipped();

        assertEquals("f", flipped.getName());
        assertEquals(2, flipped.getSlots().size());
        assertEquals(Position.LB, flipped.getSlots().get(0).getPosition());
        assertEquals(new Coordinates(0.75, 0.9), flipped.getSlots().get(0).getCoordinates());
        assertEquals(Position.ST, flipped.getSlots().get(1).getPosition());
        assertEquals(0.2, flipped.getSlots().get(1).getCoordinates().getX(), 1e-9);
        assertEquals(0.5, flipped.getSlots().get(1).getCoordinates().getY(), 1e-9);
    }

    @Test
    void flippedLeavesOriginalUnchangedAndSharesNoSlots() {
        Coordinates before = gk.getCoordinates();
        Formation flipped = formation.flipped();
        assertEquals(before, gk.getCoordinates());
        assertEquals(3, formation.getSlots().size());
        for (Slot s : flipped.getSlots()) {
            assertFalse(formation.getSlots().contains(s));
        }
        flipped.addSlot(slot(Position.CM));
        assertEquals(3, formation.getSlots().size());
    }

    @Test
    void flippedEmptyFormationIsEmpty() {
        assertTrue(new Formation("empty").flipped().getSlots().isEmpty());
    }

    @Test
    void flippedFormationKeepsValidity() {
        for (int i = formation.getSlots().size(); i < Formation.SLOTS_PER_FORMATION; i++) {
            formation.addSlot(slot(Position.CM));
        }
        assertTrue(formation.flipped().isValid());
    }

    @Test
    void flippedTwiceRestoresCoordinatesAndPositions() {
        Formation twice = formation.flipped().flipped();
        for (int i = 0; i < formation.getSlots().size(); i++) {
            assertEquals(formation.getSlots().get(i).getCoordinates(), twice.getSlots().get(i).getCoordinates());
            assertEquals(formation.getSlots().get(i).getPosition(), twice.getSlots().get(i).getPosition());
        }
    }

    @Test
    void removeSlotRemovesIt() {
        assertTrue(formation.removeSlot(cb));
        assertFalse(formation.getSlots().contains(cb));
        assertEquals(2, formation.getSlots().size());
    }
}
