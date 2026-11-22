package test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import main.model.Coordinates;
import main.model.Formation;
import main.model.Player;
import main.model.Position;
import main.model.Slot;
import main.model.Team;

class TeamTest {

    private static Player player(String name, int number) {
        return new Player(name, number, new Coordinates(10, 10), Collections.singletonList(Position.CM));
    }

    @Test
    void constructsWithValidInput() {
        Player a = player("A", 1);
        Team team = new Team("FC Barcelona", Arrays.asList(a));
        assertEquals("FC Barcelona", team.getName());
        assertEquals(Collections.singletonList(a), team.getSquad());
    }

    @Test
    void allowsEmptySquad() {
        assertTrue(new Team("Empty FC", Collections.<Player>emptyList()).getSquad().isEmpty());
    }

    @Test
    void rejectsBadName() {
        assertThrows(IllegalArgumentException.class, () -> new Team(null, new ArrayList<Player>()));
        assertThrows(IllegalArgumentException.class, () -> new Team("", new ArrayList<Player>()));
    }

    @Test
    void rejectsNullSquadOrNullPlayer() {
        assertThrows(IllegalArgumentException.class, () -> new Team("T", null));
        assertThrows(IllegalArgumentException.class, () -> new Team("T", Arrays.asList(player("A", 1), null)));
    }

    @Test
    void squadIsDefensivelyCopied() {
        List<Player> source = new ArrayList<>(Arrays.asList(player("A", 1)));
        Team team = new Team("T", source);
        source.add(player("B", 2));
        assertEquals(1, team.getSquad().size());
    }

    @Test
    void squadGetterIsUnmodifiable() {
        Team team = new Team("T", Arrays.asList(player("A", 1)));
        assertThrows(UnsupportedOperationException.class, () -> team.getSquad().add(player("B", 2)));
    }

    // --- lineup, bench and substitutions ---

    private Formation twoSlotFormation(Slot gk, Slot st) {
        Formation f = new Formation("two");
        f.addSlot(gk);
        f.addSlot(st);
        return f;
    }

    @Test
    void hasNoLineupUntilFormationIsSet() {
        Player a = player("A", 1);
        Team team = new Team("T", Arrays.asList(a));
        assertFalse(team.getLineup().isPresent());
        assertEquals(Collections.singletonList(a), team.getBench());
        assertThrows(IllegalArgumentException.class,
                () -> team.assign(new Slot(Position.GK, new Coordinates(0.1, 0.5)), a));
        assertThrows(IllegalArgumentException.class, () -> team.substitute(a, a));
    }

    @Test
    void setFormationStartsEmptyLineup() {
        Team team = new Team("T", Arrays.asList(player("A", 1)));
        Formation f = twoSlotFormation(new Slot(Position.GK, new Coordinates(0.1, 0.5)),
                new Slot(Position.ST, new Coordinates(0.8, 0.5)));
        team.setFormation(f);
        assertSame(f, team.getLineup().get().getFormation());
        assertTrue(team.getLineup().get().getAssignments().isEmpty());
    }

    @Test
    void setFormationReplacesPreviousLineup() {
        Player a = player("A", 1);
        Slot gk = new Slot(Position.GK, new Coordinates(0.1, 0.5));
        Team team = new Team("T", Arrays.asList(a));
        team.setFormation(twoSlotFormation(gk, new Slot(Position.ST, new Coordinates(0.8, 0.5))));
        team.assign(gk, a);
        team.setFormation(new Formation("other"));
        assertTrue(team.getLineup().get().getAssignments().isEmpty());
        assertEquals(Collections.singletonList(a), team.getBench());
    }

    @Test
    void onlySquadPlayersCanBeAssigned() {
        Slot gk = new Slot(Position.GK, new Coordinates(0.1, 0.5));
        Team team = new Team("T", Arrays.asList(player("A", 1)));
        team.setFormation(twoSlotFormation(gk, new Slot(Position.ST, new Coordinates(0.8, 0.5))));
        assertThrows(IllegalArgumentException.class, () -> team.assign(gk, player("Outsider", 2)));
        assertThrows(IllegalArgumentException.class, () -> team.assign(gk, null));
        assertTrue(team.getLineup().get().getAssignments().isEmpty());
    }

    @Test
    void substituteSwapsPlayerOnAndSendsPlayerOffToBench() {
        Player starter = player("Starter", 1);
        Player sub = player("Sub", 2);
        Slot gk = new Slot(Position.GK, new Coordinates(0.1, 0.5));
        Team team = new Team("T", Arrays.asList(starter, sub));
        team.setFormation(twoSlotFormation(gk, new Slot(Position.ST, new Coordinates(0.8, 0.5))));
        team.assign(gk, starter);
        assertEquals(Collections.singletonList(sub), team.getBench());

        team.substitute(starter, sub);

        assertSame(sub, team.getLineup().get().getPlayer(gk).get());
        assertEquals(Collections.singletonList(starter), team.getBench());
    }

    @Test
    void substituteRejectsInvalidSwapsAndLeavesLineupUnchanged() {
        Player starter = player("Starter", 1);
        Player other = player("Other", 2);
        Player benched = player("Benched", 3);
        Player outsider = player("Outsider", 4);
        Slot gk = new Slot(Position.GK, new Coordinates(0.1, 0.5));
        Slot st = new Slot(Position.ST, new Coordinates(0.8, 0.5));
        Team team = new Team("T", Arrays.asList(starter, other, benched));
        team.setFormation(twoSlotFormation(gk, st));
        team.assign(gk, starter);
        team.assign(st, other);

        assertThrows(IllegalArgumentException.class, () -> team.substitute(starter, outsider)); // not in squad
        assertThrows(IllegalArgumentException.class, () -> team.substitute(benched, starter)); // off player not in lineup
        assertThrows(IllegalArgumentException.class, () -> team.substitute(starter, other)); // on player already playing
        assertThrows(IllegalArgumentException.class, () -> team.substitute(starter, null));

        assertSame(starter, team.getLineup().get().getPlayer(gk).get());
        assertSame(other, team.getLineup().get().getPlayer(st).get());
    }

    @Test
    void benchViewIsUnmodifiable() {
        Team team = new Team("T", Arrays.asList(player("A", 1)));
        assertThrows(UnsupportedOperationException.class, () -> team.getBench().add(player("B", 2)));
    }

    @Test
    void setSquadCannotDropPlayersInTheLineup() {
        Player a = player("A", 1);
        Player b = player("B", 2);
        Slot gk = new Slot(Position.GK, new Coordinates(0.1, 0.5));
        Team team = new Team("T", Arrays.asList(a, b));
        team.setFormation(twoSlotFormation(gk, new Slot(Position.ST, new Coordinates(0.8, 0.5))));
        team.assign(gk, a);

        assertThrows(IllegalArgumentException.class, () -> team.setSquad(Arrays.asList(b)));
        assertEquals(2, team.getSquad().size());
        assertDoesNotThrow(() -> team.setSquad(Arrays.asList(a)));
    }

    @Test
    void placeOnPitchMovesLineupPlayersButNotBench() {
        Player starter = player("Starter", 1);
        Player bench = player("Bench", 2);
        Coordinates benchBefore = bench.getCoordinates();
        Slot gk = new Slot(Position.GK, new Coordinates(0.1, 0.5));
        Team team = new Team("T", Arrays.asList(starter, bench));
        team.setFormation(twoSlotFormation(gk, new Slot(Position.ST, new Coordinates(0.8, 0.5))));
        team.assign(gk, starter);

        team.placeOnPitch(new main.model.Pitch(100, 60));

        assertEquals(new Coordinates(10, 30), starter.getCoordinates());
        assertEquals(benchBefore, bench.getCoordinates());
    }

    @Test
    void placeOnPitchRequiresAFormation() {
        Team team = new Team("T", Arrays.asList(player("A", 1)));
        assertThrows(IllegalArgumentException.class, () -> team.placeOnPitch(new main.model.Pitch(100, 60)));
    }

    @Test
    void twoTeamsCanShareAFormationAtOppositeEnds() {
        Slot gk = new Slot(Position.GK, new Coordinates(0.1, 0.5));
        Formation shared = twoSlotFormation(gk, new Slot(Position.ST, new Coordinates(0.8, 0.5)));
        Player homeKeeper = player("Home", 1);
        Player awayKeeper = player("Away", 1);
        Team home = new Team("Home", Arrays.asList(homeKeeper));
        Team away = new Team("Away", Arrays.asList(awayKeeper));
        home.setFormation(shared);
        away.setFormation(shared.flipped());
        home.assign(shared.getSlots().get(0), homeKeeper);
        away.assign(away.getLineup().get().getFormation().getSlots().get(0), awayKeeper);

        main.model.Pitch pitch = new main.model.Pitch(100, 60);
        home.placeOnPitch(pitch);
        away.placeOnPitch(pitch);

        assertEquals(10, homeKeeper.getCoordinates().getX(), 1e-9);
        assertEquals(90, awayKeeper.getCoordinates().getX(), 1e-9);
        assertEquals(30, awayKeeper.getCoordinates().getY(), 1e-9);
    }
}
