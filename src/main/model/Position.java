package main.model;

/**
 * A traditional football playing position (the role a player fills), not a
 * location on the pitch. For locations see Coordinates.
 */
public enum Position {
    GK("Goalkeeper"),

    RB("Right Back"),
    CB("Centre Back"),
    LB("Left Back"),
    RWB("Right Wing Back"),
    LWB("Left Wing Back"),

    CDM("Defensive Midfielder"),
    CM("Central Midfielder"),
    CAM("Attacking Midfielder"),
    RM("Right Midfielder"),
    LM("Left Midfielder"),

    RW("Right Winger"),
    LW("Left Winger"),
    CF("Centre Forward"),
    ST("Striker");

    private final String displayName;

    Position(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
