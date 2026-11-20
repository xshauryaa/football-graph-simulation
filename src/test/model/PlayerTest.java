package test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import main.model.Coordinates;
import main.model.Player;
import main.model.Position;

class PlayerTest {

    private static final Coordinates START = new Coordinates(50, 30);

    private static Player validPlayer() {
        return new Player("Lionel Messi", 10, START, Arrays.asList(Position.RW, Position.CAM));
    }

    @Test
    void constructsWithValidInput() {
        Player p = validPlayer();
        assertEquals("Lionel Messi", p.getName());
        assertEquals(10, p.getNumber());
        assertEquals(START, p.getCoordinates());
        assertEquals(Arrays.asList(Position.RW, Position.CAM), p.getPlayablePositions());
    }

    @Test
    void rejectsBadName() {
        List<Position> pos = Collections.singletonList(Position.ST);
        assertThrows(IllegalArgumentException.class, () -> new Player(null, 9, START, pos));
        assertThrows(IllegalArgumentException.class, () -> new Player("  ", 9, START, pos));
    }

    @Test
    void numberBoundsAreInclusive() {
        List<Position> pos = Collections.singletonList(Position.ST);
        assertDoesNotThrow(() -> new Player("A", 1, START, pos));
        assertDoesNotThrow(() -> new Player("A", 99, START, pos));
        assertThrows(IllegalArgumentException.class, () -> new Player("A", 0, START, pos));
        assertThrows(IllegalArgumentException.class, () -> new Player("A", 100, START, pos));
    }

    @Test
    void rejectsNullCoordinates() {
        assertThrows(IllegalArgumentException.class,
                () -> new Player("A", 9, null, Collections.singletonList(Position.ST)));
    }

    @Test
    void rejectsNullEmptyOrNullContainingPositions() {
        assertThrows(IllegalArgumentException.class, () -> new Player("A", 9, START, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Player("A", 9, START, Collections.<Position>emptyList()));
        assertThrows(IllegalArgumentException.class,
                () -> new Player("A", 9, START, Arrays.asList(Position.ST, null)));
    }

    @Test
    void failedSetterLeavesStateUnchanged() {
        Player p = validPlayer();
        assertThrows(IllegalArgumentException.class, () -> p.setNumber(100));
        assertThrows(IllegalArgumentException.class, () -> p.setCoordinates(null));
        assertEquals(10, p.getNumber());
        assertEquals(START, p.getCoordinates());
    }

    @Test
    void playerCanMoveAwayFromStartingCoordinates() {
        Player p = validPlayer();
        Coordinates moved = new Coordinates(70, 12);
        p.setCoordinates(moved);
        assertEquals(moved, p.getCoordinates());
    }

    @Test
    void positionsAreDefensivelyCopied() {
        List<Position> source = new ArrayList<>(Arrays.asList(Position.RW));
        Player p = new Player("A", 9, START, source);
        source.add(Position.GK);
        assertEquals(Collections.singletonList(Position.RW), p.getPlayablePositions());
    }

    @Test
    void positionsGetterIsUnmodifiable() {
        Player p = validPlayer();
        assertThrows(UnsupportedOperationException.class, () -> p.getPlayablePositions().add(Position.GK));
    }
}
