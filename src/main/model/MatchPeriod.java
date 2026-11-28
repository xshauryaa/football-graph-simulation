package main.model;

/**
 * The phase a match is in. Legal order: NOT_STARTED -> FIRST_HALF -> HALF_TIME -> SECOND_HALF -> FULL_TIME.
 */
public enum MatchPeriod {
    NOT_STARTED,
    FIRST_HALF,
    HALF_TIME,
    SECOND_HALF,
    FULL_TIME;

    // EFFECTS: returns true if the ball can be in play during this period
    public boolean isInPlay() {
        return this == FIRST_HALF || this == SECOND_HALF;
    }
}
