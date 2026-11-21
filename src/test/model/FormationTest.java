package test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    void passingOptionsAreEmptyUntilSet() {
        assertTrue(formation.getPassingOptions(gk).isEmpty());
    }

    @Test
    void setAndGetPassingOptions() {
        formation.setPassingOptions(gk, Arrays.asList(cb, st));
        assertEquals(Arrays.asList(cb, st), formation.getPassingOptions(gk));
    }

    @Test
    void setPassingOptionsRejectsInvalidInput() {
        Slot outsider = slot(Position.CM);
        assertThrows(IllegalArgumentException.class, () -> formation.setPassingOptions(null, Arrays.asList(cb)));
        assertThrows(IllegalArgumentException.class, () -> formation.setPassingOptions(outsider, Arrays.asList(cb)));
        assertThrows(IllegalArgumentException.class, () -> formation.setPassingOptions(gk, null));
        assertThrows(IllegalArgumentException.class, () -> formation.setPassingOptions(gk, Arrays.asList(cb, null)));
        assertThrows(IllegalArgumentException.class, () -> formation.setPassingOptions(gk, Arrays.asList(outsider)));
        assertThrows(IllegalArgumentException.class, () -> formation.setPassingOptions(gk, Arrays.asList(gk)));
        assertThrows(IllegalArgumentException.class, () -> formation.setPassingOptions(gk, Arrays.asList(cb, cb)));
        assertTrue(formation.getPassingOptions(gk).isEmpty());
    }

    @Test
    void getPassingOptionsRejectsUnknownSlot() {
        assertThrows(IllegalArgumentException.class, () -> formation.getPassingOptions(slot(Position.CM)));
    }

    @Test
    void setPassingOptionsCopiesInput() {
        List<Slot> source = new java.util.ArrayList<>(Arrays.asList(cb));
        formation.setPassingOptions(gk, source);
        source.add(st);
        assertEquals(Collections.singletonList(cb), formation.getPassingOptions(gk));
    }

    @Test
    void passingOptionViewsAreUnmodifiable() {
        formation.setPassingOptions(gk, Arrays.asList(cb));
        assertThrows(UnsupportedOperationException.class, () -> formation.getPassingOptions(gk).add(st));
        Map<Slot, List<Slot>> all = formation.getPassingOptions();
        assertThrows(UnsupportedOperationException.class, () -> all.put(st, Arrays.asList(gk)));
        assertThrows(UnsupportedOperationException.class, () -> all.get(gk).add(st));
        assertEquals(Collections.singletonList(cb), formation.getPassingOptions(gk));
    }

    @Test
    void removeSlotCleansUpPassingOptions() {
        formation.setPassingOptions(gk, Arrays.asList(cb, st));
        formation.setPassingOptions(cb, Arrays.asList(st));
        assertTrue(formation.removeSlot(cb));
        assertFalse(formation.getSlots().contains(cb));
        assertEquals(Collections.singletonList(st), formation.getPassingOptions(gk));
        assertFalse(formation.getPassingOptions().containsKey(cb));
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
}
