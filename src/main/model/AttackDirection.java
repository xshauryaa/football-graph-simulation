package main.model;

/**
 * Which end of the pitch a team is attacking. With the pitch's origin at the top-left and x along the length,
 * a team either attacks toward x = length or toward x = 0. The sign is +1 or -1 respectively, so
 * "forward" for a team is sign * dx.
 */
public enum AttackDirection {
    TOWARD_MAX_X(1),
    TOWARD_MIN_X(-1);

    private final int sign;

    AttackDirection(int sign) {
        this.sign = sign;
    }

    public int getSign() {
        return sign;
    }

    // EFFECTS: returns the opposite direction
    public AttackDirection opposite() {
        return this == TOWARD_MAX_X ? TOWARD_MIN_X : TOWARD_MAX_X;
    }
}
