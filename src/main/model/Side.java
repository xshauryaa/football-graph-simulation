package main.model;

/**
 * Which of the two teams in a match. MatchState identifies teams by Side rather than by Team,
 * so read-only code never gets a mutable Team.
 */
public enum Side {
    HOME,
    AWAY;

    // EFFECTS: returns the other side
    public Side opposite() {
        return this == HOME ? AWAY : HOME;
    }
}
