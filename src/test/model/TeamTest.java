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
}
