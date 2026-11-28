package test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.model.Ball;
import main.model.Coordinates;
import main.model.Player;
import main.model.Position;

class BallTest {

    private static final Coordinates CENTRE_SPOT = new Coordinates(52.5, 34);

    private Ball ball;
    private Player messi;
    private Player xavi;

    private static Player player(String name, int number, double x, double y) {
        return new Player(name, number, new Coordinates(x, y), Collections.singletonList(Position.CM));
    }

    @BeforeEach
    void setUp() {
        ball = new Ball(CENTRE_SPOT);
        messi = player("Messi", 10, 60, 30);
        xavi = player("Xavi", 6, 40, 34);
    }

    @Test
    void newBallIsLooseUntouchedAndAtItsStart() {
        assertFalse(ball.isHeld());
        assertFalse(ball.getHolder().isPresent());
        assertFalse(ball.getLastTouchedBy().isPresent());
        assertEquals(CENTRE_SPOT, ball.getCoordinates());
    }

    @Test
    void rejectsNullStart() {
        assertThrows(IllegalArgumentException.class, () -> new Ball(null));
    }

    @Test
    void giveToMakesPlayerHolderAndLastTouch() {
        ball.giveTo(messi);
        assertTrue(ball.isHeld());
        assertSame(messi, ball.getHolder().get());
        assertSame(messi, ball.getLastTouchedBy().get());
    }

    @Test
    void giveToRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> ball.giveTo(null));
        assertFalse(ball.isHeld());
    }

    @Test
    void heldBallIsAtHolderAndFollowsThem() {
        ball.giveTo(messi);
        assertEquals(messi.getCoordinates(), ball.getCoordinates());
        Coordinates moved = new Coordinates(80, 20);
        messi.setCoordinates(moved);
        assertEquals(moved, ball.getCoordinates());
    }

    @Test
    void giveToAnotherPlayerTransfersPossession() {
        ball.giveTo(xavi);
        ball.giveTo(messi);
        assertSame(messi, ball.getHolder().get());
        assertSame(messi, ball.getLastTouchedBy().get());
        assertEquals(messi.getCoordinates(), ball.getCoordinates());
    }

    @Test
    void releaseLeavesBallWhereHolderWasAndKeepsLastTouch() {
        ball.giveTo(messi);
        Coordinates whereReleased = messi.getCoordinates();
        ball.release();

        assertFalse(ball.isHeld());
        assertEquals(whereReleased, ball.getCoordinates());
        assertSame(messi, ball.getLastTouchedBy().get());

        messi.setCoordinates(new Coordinates(90, 10));
        assertEquals(whereReleased, ball.getCoordinates()); // a loose ball no longer follows the player
    }

    @Test
    void releaseRequiresHeldBall() {
        assertThrows(IllegalStateException.class, () -> ball.release());
    }

    @Test
    void moveToMovesLooseBallWithoutCountingAsTouch() {
        ball.giveTo(xavi);
        ball.release();
        Coordinates target = new Coordinates(70, 40);
        ball.moveTo(target);
        assertEquals(target, ball.getCoordinates());
        assertSame(xavi, ball.getLastTouchedBy().get());
    }

    @Test
    void moveToRejectsHeldBallAndNull() {
        assertThrows(IllegalArgumentException.class, () -> ball.moveTo(null));
        ball.giveTo(messi);
        assertThrows(IllegalStateException.class, () -> ball.moveTo(new Coordinates(1, 1)));
        assertSame(messi, ball.getHolder().get());
    }

    @Test
    void placeAtMakesDeadBallLooseAndClearsLastTouch() {
        ball.giveTo(messi);
        ball.placeAt(CENTRE_SPOT);
        assertFalse(ball.isHeld());
        assertFalse(ball.getLastTouchedBy().isPresent());
        assertEquals(CENTRE_SPOT, ball.getCoordinates());
    }

    @Test
    void placeAtRejectsNullAndLeavesBallUnchanged() {
        ball.giveTo(messi);
        assertThrows(IllegalArgumentException.class, () -> ball.placeAt(null));
        assertSame(messi, ball.getHolder().get());
    }
}
