package test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.model.Coordinates;
import main.model.Formation;
import main.model.Lineup;
import main.model.Player;
import main.model.Position;
import main.model.Slot;

class LineupTest {

    private Formation formation;
    private Slot gk;
    private Slot st;
    private Lineup lineup;

    private static Player player(String name, int number) {
        return new Player(name, number, new Coordinates(10, 10), Collections.singletonList(Position.CM));
    }

    @BeforeEach
    void setUp() {
        formation = new Formation("test");
        gk = new Slot(Position.GK, new Coordinates(0.05, 0.5));
        st = new Slot(Position.ST, new Coordinates(0.8, 0.5));
        formation.addSlot(gk);
        formation.addSlot(st);
        lineup = new Lineup(formation);
    }

    @Test
    void rejectsNullFormation() {
        assertThrows(IllegalArgumentException.class, () -> new Lineup(null));
    }

    @Test
    void slotsStartUnfilled() {
        assertFalse(lineup.getPlayer(gk).isPresent());
        assertTrue(lineup.getAssignments().isEmpty());
    }

    @Test
    void assignAndRetrieve() {
        Player keeper = player("Keeper", 1);
        lineup.assign(gk, keeper);
        assertSame(keeper, lineup.getPlayer(gk).get());
    }

    @Test
    void assignReplacesExistingPlayerInSlot() {
        lineup.assign(gk, player("A", 1));
        Player b = player("B", 2);
        lineup.assign(gk, b);
        assertSame(b, lineup.getPlayer(gk).get());
    }

    @Test
    void reassigningSamePlayerToSameSlotIsAllowed() {
        Player a = player("A", 1);
        lineup.assign(gk, a);
        assertDoesNotThrow(() -> lineup.assign(gk, a));
    }

    @Test
    void rejectsSlotOutsideFormationNullsAndDuplicatePlayer() {
        Player a = player("A", 1);
        Slot outsider = new Slot(Position.CM, new Coordinates(0.5, 0.5));
        assertThrows(IllegalArgumentException.class, () -> lineup.assign(outsider, a));
        assertThrows(IllegalArgumentException.class, () -> lineup.assign(null, a));
        assertThrows(IllegalArgumentException.class, () -> lineup.assign(gk, null));
        lineup.assign(gk, a);
        assertThrows(IllegalArgumentException.class, () -> lineup.assign(st, a));
        assertFalse(lineup.getPlayer(st).isPresent());
    }

    @Test
    void unassignRemovesPlayer() {
        lineup.assign(gk, player("A", 1));
        assertTrue(lineup.unassign(gk));
        assertFalse(lineup.getPlayer(gk).isPresent());
        assertFalse(lineup.unassign(gk));
    }

    @Test
    void assignmentsSnapshotIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> lineup.getAssignments().put(gk, player("A", 1)));
    }

    @Test
    void assigningDoesNotMoveThePlayer() {
        Player a = player("A", 1);
        Coordinates before = a.getCoordinates();
        lineup.assign(gk, a);
        assertEquals(before, a.getCoordinates());
    }

    @Test
    void getSlotAndContainsFindAssignedPlayers() {
        Player a = player("A", 1);
        lineup.assign(st, a);
        assertSame(st, lineup.getSlot(a).get());
        assertTrue(lineup.contains(a));
        Player b = player("B", 2);
        assertFalse(lineup.getSlot(b).isPresent());
        assertFalse(lineup.contains(b));
    }

    @Test
    void sameFormationCanBackTwoLineups() {
        Lineup other = new Lineup(formation);
        lineup.assign(gk, player("A", 1));
        assertFalse(other.getPlayer(gk).isPresent());
    }

    @Test
    void incompleteWhenFormationInvalidOrSlotsUnfilled() {
        lineup.assign(gk, player("A", 1));
        lineup.assign(st, player("B", 2));
        assertFalse(lineup.isComplete()); // formation has only 2 slots, so it is not valid

        Formation full = new Formation("full");
        Slot[] slots = new Slot[Formation.SLOTS_PER_FORMATION];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Slot(i == 0 ? Position.GK : Position.CM, new Coordinates(0.5, 0.5));
            full.addSlot(slots[i]);
        }
        Lineup fullLineup = new Lineup(full);
        for (int i = 0; i < slots.length - 1; i++) {
            fullLineup.assign(slots[i], player("P" + i, i + 1));
        }
        assertFalse(fullLineup.isComplete());
        fullLineup.assign(slots[slots.length - 1], player("Last", 99));
        assertTrue(fullLineup.isComplete());
    }

    // --- placeOnPitch ---

    @Test
    void placeOnPitchConvertsSlotFractionsToMetres() {
        Player keeper = player("Keeper", 1);
        Player striker = player("Striker", 9);
        lineup.assign(gk, keeper);
        lineup.assign(st, striker);

        lineup.placeOnPitch(new main.model.Pitch(100, 60));

        assertEquals(new Coordinates(5, 30), keeper.getCoordinates());
        assertEquals(new Coordinates(80, 30), striker.getCoordinates());
    }

    @Test
    void placeOnPitchSkipsUnfilledSlots() {
        Player keeper = player("Keeper", 1);
        lineup.assign(gk, keeper);
        lineup.placeOnPitch(new main.model.Pitch(100, 60));
        assertEquals(new Coordinates(5, 30), keeper.getCoordinates());
        assertEquals(1, lineup.getAssignments().size());
    }

    @Test
    void placeOnPitchRejectsNullPitch() {
        assertThrows(IllegalArgumentException.class, () -> lineup.placeOnPitch(null));
    }

    @Test
    void flippedFormationPlacesTeamOnTheOppositeSide() {
        Formation flipped = formation.flipped();
        Lineup awayLineup = new Lineup(flipped);
        Player keeper = player("AwayKeeper", 1);
        awayLineup.assign(flipped.getSlots().get(0), keeper);

        awayLineup.placeOnPitch(new main.model.Pitch(100, 60));

        assertEquals(95, keeper.getCoordinates().getX(), 1e-9);
        assertEquals(30, keeper.getCoordinates().getY(), 1e-9);
    }

    @Test
    void playersCanMoveAwayAfterBeingPlaced() {
        Player keeper = player("Keeper", 1);
        lineup.assign(gk, keeper);
        lineup.placeOnPitch(new main.model.Pitch(100, 60));
        keeper.setCoordinates(new Coordinates(40, 10));
        assertEquals(new Coordinates(40, 10), keeper.getCoordinates());
        assertEquals(new Coordinates(0.05, 0.5), gk.getCoordinates());
    }
}
