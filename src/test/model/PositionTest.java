package test.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import main.model.Position;

class PositionTest {

    @Test
    void everyPositionHasADisplayName() {
        for (Position p : Position.values()) {
            assertNotNull(p.getDisplayName());
            assertFalse(p.getDisplayName().trim().isEmpty());
        }
    }
}
