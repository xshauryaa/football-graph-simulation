package test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import main.model.Coordinates;
import main.model.Formation;
import main.model.Formations;
import main.model.Position;
import main.model.Slot;

/**
 * Checks every factory method in Formations. To cover a new formation, add one entry to cases();
 * allFactoryMethodsAreCovered fails if a public factory method is missing from that list.
 */
class FormationsTest {

    private static final double EPSILON = 1e-9;

    private static final Set<Position> DEFENDERS =
            EnumSet.of(Position.LB, Position.CB, Position.RB, Position.LWB, Position.RWB);
    private static final Set<Position> MIDFIELDERS =
            EnumSet.of(Position.CDM, Position.CM, Position.CAM, Position.LM, Position.RM);
    private static final Set<Position> FORWARDS =
            EnumSet.of(Position.LW, Position.RW, Position.ST, Position.CF);

    // One formation under test: its expected name, how to build it, and how many of each position it has
    static final class Case {
        final String name;
        final Supplier<Formation> factory;
        final Map<Position, Integer> counts;

        Case(String name, Supplier<Formation> factory, Object... positionCountPairs) {
            this.name = name;
            this.factory = factory;
            this.counts = new EnumMap<>(Position.class);
            for (int i = 0; i < positionCountPairs.length; i += 2) {
                counts.put((Position) positionCountPairs[i], (Integer) positionCountPairs[i + 1]);
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static List<Case> cases() {
        return Arrays.asList(
                new Case("4-3-3", Formations::fourThreeThree,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.CM, 3, Position.LW, 1, Position.ST, 1, Position.RW, 1),
                new Case("4-3-3 Attack", Formations::fourThreeThreeAttack,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.CM, 2, Position.CAM, 1, Position.LW, 1, Position.ST, 1, Position.RW, 1),
                new Case("4-3-3 Holding", Formations::fourThreeThreeHolding,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.CM, 2, Position.CDM, 1, Position.LW, 1, Position.ST, 1, Position.RW, 1),
                new Case("4-3-3 False 9", Formations::fourThreeThreeFalseNine,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.CM, 3, Position.LW, 1, Position.CF, 1, Position.RW, 1),
                new Case("4-4-2", Formations::fourFourTwo,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.LM, 1, Position.CM, 2, Position.RM, 1, Position.ST, 2),
                new Case("4-4-2 Attack", Formations::fourFourTwoAttack,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.LM, 1, Position.CM, 2, Position.RM, 1, Position.ST, 2),
                new Case("4-4-2 Holding", Formations::fourFourTwoHolding,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.LM, 1, Position.CDM, 2, Position.RM, 1, Position.ST, 2),
                new Case("4-2-4", Formations::fourTwoFour,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.CM, 2, Position.LW, 1, Position.ST, 2, Position.RW, 1),
                new Case("4-2-3-1", Formations::fourTwoThreeOne,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.CDM, 2, Position.CAM, 3, Position.ST, 1),
                new Case("4-1-2-1-2 Narrow", Formations::fourOneTwoOneTwoNarrow,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.CDM, 1, Position.CM, 2, Position.CAM, 1, Position.ST, 2),
                new Case("4-1-2-1-2 Wide", Formations::fourOneTwoOneTwoWide,
                        Position.GK, 1, Position.LB, 1, Position.CB, 2, Position.RB, 1,
                        Position.CDM, 1, Position.LM, 1, Position.RM, 1, Position.CAM, 1, Position.ST, 2),
                new Case("3-4-3", Formations::threeFourThree,
                        Position.GK, 1, Position.CB, 3,
                        Position.LM, 1, Position.CM, 2, Position.RM, 1, Position.LW, 1, Position.ST, 1, Position.RW, 1),
                new Case("3-4-3 Diamond", Formations::threeFourThreeDiamond,
                        Position.GK, 1, Position.CB, 3,
                        Position.CDM, 1, Position.LM, 1, Position.RM, 1, Position.CAM, 1,
                        Position.LW, 1, Position.ST, 1, Position.RW, 1),
                new Case("5-3-2", Formations::fiveThreeTwo,
                        Position.GK, 1, Position.LWB, 1, Position.CB, 3, Position.RWB, 1,
                        Position.CM, 3, Position.ST, 2),
                new Case("5-4-1", Formations::fiveFourOne,
                        Position.GK, 1, Position.LWB, 1, Position.CB, 3, Position.RWB, 1,
                        Position.LM, 1, Position.CM, 2, Position.RM, 1, Position.ST, 1),
                new Case("5-2-1-2", Formations::fiveTwoOneTwo,
                        Position.GK, 1, Position.LWB, 1, Position.CB, 3, Position.RWB, 1,
                        Position.CM, 2, Position.CAM, 1, Position.ST, 2));
    }

    // ---------------------------------------------------------------- coverage and uniqueness

    @Test
    void allFactoryMethodsAreCovered() {
        int factoryMethods = 0;
        for (Method m : Formations.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())
                    && m.getReturnType() == Formation.class) {
                factoryMethods++;
            }
        }
        assertEquals(factoryMethods, cases().size(),
                "Every public factory method in Formations should have an entry in cases()");
    }

    @Test
    void formationNamesAreUnique() {
        Set<String> names = new HashSet<>();
        for (Case c : cases()) {
            assertTrue(names.add(c.factory.get().getName()), "Duplicate formation name: " + c.name);
        }
    }

    // ---------------------------------------------------------------- per formation

    @ParameterizedTest
    @MethodSource("cases")
    void isNamedAndValid(Case c) {
        Formation f = c.factory.get();
        assertEquals(c.name, f.getName());
        assertEquals(Formation.SLOTS_PER_FORMATION, f.getSlots().size());
        assertTrue(f.isValid());
    }

    @ParameterizedTest
    @MethodSource("cases")
    void hasExactlyTheExpectedPositions(Case c) {
        Map<Position, Integer> actual = new EnumMap<>(Position.class);
        for (Slot s : c.factory.get().getSlots()) {
            actual.merge(s.getPosition(), 1, Integer::sum);
        }
        assertEquals(c.counts, actual);
    }

    @ParameterizedTest
    @MethodSource("cases")
    void goalkeeperIsDeepestAndLinesAreOrderedTowardAttack(Case c) {
        Formation f = c.factory.get();
        double keeperX = Double.NaN;
        double maxDefenderX = Double.NEGATIVE_INFINITY;
        double minMidfielderX = Double.POSITIVE_INFINITY;
        double maxMidfielderX = Double.NEGATIVE_INFINITY;
        double minForwardX = Double.POSITIVE_INFINITY;
        double minOutfieldX = Double.POSITIVE_INFINITY;

        for (Slot s : f.getSlots()) {
            double x = s.getCoordinates().getX();
            Position p = s.getPosition();
            if (p == Position.GK) {
                keeperX = x;
                continue;
            }
            minOutfieldX = Math.min(minOutfieldX, x);
            if (DEFENDERS.contains(p)) {
                maxDefenderX = Math.max(maxDefenderX, x);
            } else if (MIDFIELDERS.contains(p)) {
                minMidfielderX = Math.min(minMidfielderX, x);
                maxMidfielderX = Math.max(maxMidfielderX, x);
            } else if (FORWARDS.contains(p)) {
                minForwardX = Math.min(minForwardX, x);
            } else {
                fail("Position not classified as defender, midfielder or forward: " + p);
            }
        }

        assertTrue(keeperX < minOutfieldX, "GK should be the deepest player");
        assertTrue(maxDefenderX < minMidfielderX, "Defence should sit behind midfield");
        assertTrue(maxMidfielderX < minForwardX, "Midfield should sit behind attack");
    }

    @ParameterizedTest
    @MethodSource("cases")
    void leftSidedPositionsAreOnTheLeftAndRightSidedOnTheRight(Case c) {
        for (Slot s : c.factory.get().getSlots()) {
            double y = s.getCoordinates().getY();
            switch (s.getPosition()) {
                case LB: case LWB: case LM: case LW:
                    assertTrue(y < 0.5, s.getPosition() + " should be on the small-y (left) side");
                    break;
                case RB: case RWB: case RM: case RW:
                    assertTrue(y > 0.5, s.getPosition() + " should be on the large-y (right) side");
                    break;
                default:
                    break;
            }
        }
    }

    @ParameterizedTest
    @MethodSource("cases")
    void isSymmetricLeftToRight(Case c) {
        List<Slot> slots = c.factory.get().getSlots();
        for (Slot s : slots) {
            Position mirroredPosition = mirror(s.getPosition());
            double mirroredY = 1.0 - s.getCoordinates().getY();
            boolean found = false;
            for (Slot other : slots) {
                if (other.getPosition() == mirroredPosition
                        && Math.abs(other.getCoordinates().getX() - s.getCoordinates().getX()) < EPSILON
                        && Math.abs(other.getCoordinates().getY() - mirroredY) < EPSILON) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "No mirror image for " + s.getPosition() + " at " + s.getCoordinates());
        }
    }

    @ParameterizedTest
    @MethodSource("cases")
    void noTwoSlotsShareALocation(Case c) {
        List<Coordinates> seen = new ArrayList<>();
        for (Slot s : c.factory.get().getSlots()) {
            assertFalse(seen.contains(s.getCoordinates()), "Two slots at " + s.getCoordinates());
            seen.add(s.getCoordinates());
        }
    }

    @ParameterizedTest
    @MethodSource("cases")
    void eachCallReturnsAnIndependentFormation(Case c) {
        Formation a = c.factory.get();
        Formation b = c.factory.get();
        assertNotSame(a, b);
        a.removeSlot(a.getSlots().get(0));
        assertTrue(b.isValid());
    }

    @ParameterizedTest
    @MethodSource("cases")
    void flippedVersionIsValidWithKeeperAtTheOtherEnd(Case c) {
        Formation flipped = c.factory.get().flipped();
        assertTrue(flipped.isValid());
        Map<Position, Integer> actual = new EnumMap<>(Position.class);
        for (Slot s : flipped.getSlots()) {
            actual.merge(s.getPosition(), 1, Integer::sum);
            if (s.getPosition() == Position.GK) {
                assertEquals(0.95, s.getCoordinates().getX(), EPSILON);
            }
        }
        assertEquals(c.counts, actual);
    }

    // EFFECTS: returns the position on the opposite flank (LB <-> RB etc.); central positions map to themselves
    private static Position mirror(Position p) {
        switch (p) {
            case LB: return Position.RB;
            case RB: return Position.LB;
            case LWB: return Position.RWB;
            case RWB: return Position.LWB;
            case LM: return Position.RM;
            case RM: return Position.LM;
            case LW: return Position.RW;
            case RW: return Position.LW;
            default: return p;
        }
    }
}
