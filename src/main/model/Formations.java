package main.model;

/**
 * Factory methods for concrete formations.
 *
 * Every formation here is written for a team attacking toward x = 1.0 on a pitch where the origin is
 * the top-left corner, x runs along the length, and y runs along the width (all as fractions 0.0 to 1.0).
 * Facing +x with y increasing downward, a team's left side is the small-y side and its right side is the
 * large-y side. For a team attacking the other way, use Formation.flipped().
 *
 * Each formation is assembled line by line (goalkeeper, defence, midfield, attack) from the private
 * helpers below. Every helper adds exactly one line, left to right, so lines can be mixed freely.
 */
public final class Formations {

    private Formations() {
        // utility class: not meant to be instantiated
    }

    // Who sits in the middle of a midfield three. The two side players are always CMs.
    private enum MidfieldThreeRole {
        DEFAULT,   // central CM, slightly deeper than the other two
        ATTACKING, // CAM pushed ahead of the two CMs
        HOLDING    // CDM sitting in front of the back line
    }

    // The shape of a midfield four.
    private enum MidfieldFourRole {
        DEFAULT,        // flat: LM, CM, CM, RM
        ATTACK,         // LM, CM, CM, RM with the whole line, especially the wide players, pushed higher
        HOLDING,        // LM, CDM, CDM, RM: a double pivot sitting deeper
        DIAMOND,        // wide diamond: CDM at the base, LM and RM on the flanks, CAM at the tip
        NARROW_DIAMOND  // narrow diamond: CDM at the base, two CMs inside, CAM at the tip
    }

    // ---------------------------------------------------------------- 4-3-3 family

    // EFFECTS: returns a new 4-3-3: back four, three CMs, LW-ST-RW
    public static Formation fourThreeThree() {
        Formation f = new Formation("4-3-3");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldThree(f, MidfieldThreeRole.DEFAULT);
        addForwardThree(f, false);
        return f;
    }

    // EFFECTS: returns a new 4-3-3 Attack: back four, two CMs behind a CAM, LW-ST-RW
    public static Formation fourThreeThreeAttack() {
        Formation f = new Formation("4-3-3 Attack");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldThree(f, MidfieldThreeRole.ATTACKING);
        addForwardThree(f, false);
        return f;
    }

    // EFFECTS: returns a new 4-3-3 Holding: back four, a CDM behind two CMs, LW-ST-RW
    public static Formation fourThreeThreeHolding() {
        Formation f = new Formation("4-3-3 Holding");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldThree(f, MidfieldThreeRole.HOLDING);
        addForwardThree(f, false);
        return f;
    }

    // EFFECTS: returns a new 4-3-3 False 9: back four, three CMs, LW-CF-RW with the CF dropping deep
    public static Formation fourThreeThreeFalseNine() {
        Formation f = new Formation("4-3-3 False 9");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldThree(f, MidfieldThreeRole.DEFAULT);
        addForwardThree(f, true);
        return f;
    }

    // ---------------------------------------------------------------- 4-4-2 family

    // EFFECTS: returns a new 4-4-2: back four, flat midfield four, two strikers
    public static Formation fourFourTwo() {
        Formation f = new Formation("4-4-2");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldFour(f, MidfieldFourRole.DEFAULT);
        addForwardTwo(f);
        return f;
    }

    // EFFECTS: returns a new 4-4-2 Attack: back four, a high midfield four, two strikers
    public static Formation fourFourTwoAttack() {
        Formation f = new Formation("4-4-2 Attack");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldFour(f, MidfieldFourRole.ATTACK);
        addForwardTwo(f);
        return f;
    }

    // EFFECTS: returns a new 4-4-2 Holding: back four, LM-CDM-CDM-RM, two strikers
    public static Formation fourFourTwoHolding() {
        Formation f = new Formation("4-4-2 Holding");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldFour(f, MidfieldFourRole.HOLDING);
        addForwardTwo(f);
        return f;
    }

    // ---------------------------------------------------------------- 4-2-4

    // EFFECTS: returns a new 4-2-4: back four, two CMs, LW-ST-ST-RW
    public static Formation fourTwoFour() {
        Formation f = new Formation("4-2-4");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldTwo(f);
        addForwardFour(f);
        return f;
    }

    // ---------------------------------------------------------------- 4-2-3-1

    // EFFECTS: returns a new 4-2-3-1: back four, two CDMs, three CAMs, a lone striker
    public static Formation fourTwoThreeOne() {
        Formation f = new Formation("4-2-3-1");
        addGoalkeeper(f);
        addBackFour(f);
        addDefensiveMidfieldTwo(f);
        addAttackingMidfieldThree(f);
        addLoneStriker(f);
        return f;
    }

    // ---------------------------------------------------------------- 4-1-2-1-2 family

    // EFFECTS: returns a new 4-1-2-1-2 Narrow: back four, narrow diamond (CDM, CM, CM, CAM), two strikers
    public static Formation fourOneTwoOneTwoNarrow() {
        Formation f = new Formation("4-1-2-1-2 Narrow");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldFour(f, MidfieldFourRole.NARROW_DIAMOND);
        addForwardTwo(f);
        return f;
    }

    // EFFECTS: returns a new 4-1-2-1-2 Wide: back four, wide diamond (CDM, LM, RM, CAM), two strikers
    public static Formation fourOneTwoOneTwoWide() {
        Formation f = new Formation("4-1-2-1-2 Wide");
        addGoalkeeper(f);
        addBackFour(f);
        addMidfieldFour(f, MidfieldFourRole.DIAMOND);
        addForwardTwo(f);
        return f;
    }

    // ---------------------------------------------------------------- back three

    // EFFECTS: returns a new 3-4-3: three CBs, flat midfield four, LW-ST-RW
    public static Formation threeFourThree() {
        Formation f = new Formation("3-4-3");
        addGoalkeeper(f);
        addBackThree(f);
        addMidfieldFour(f, MidfieldFourRole.DEFAULT);
        addForwardThree(f, false);
        return f;
    }

    // EFFECTS: returns a new 3-4-3 Diamond: three CBs, wide diamond midfield, LW-ST-RW
    public static Formation threeFourThreeDiamond() {
        Formation f = new Formation("3-4-3 Diamond");
        addGoalkeeper(f);
        addBackThree(f);
        addMidfieldFour(f, MidfieldFourRole.DIAMOND);
        addForwardThree(f, false);
        return f;
    }

    // ---------------------------------------------------------------- back five

    // EFFECTS: returns a new 5-3-2: back five, three CMs, two strikers
    public static Formation fiveThreeTwo() {
        Formation f = new Formation("5-3-2");
        addGoalkeeper(f);
        addBackFive(f);
        addMidfieldThree(f, MidfieldThreeRole.DEFAULT);
        addForwardTwo(f);
        return f;
    }

    // EFFECTS: returns a new 5-4-1: back five, flat midfield four, a lone striker
    public static Formation fiveFourOne() {
        Formation f = new Formation("5-4-1");
        addGoalkeeper(f);
        addBackFive(f);
        addMidfieldFour(f, MidfieldFourRole.DEFAULT);
        addLoneStriker(f);
        return f;
    }

    // EFFECTS: returns a new 5-2-1-2: back five, two CMs behind a CAM, two strikers
    public static Formation fiveTwoOneTwo() {
        Formation f = new Formation("5-2-1-2");
        addGoalkeeper(f);
        addBackFive(f);
        addMidfieldThree(f, MidfieldThreeRole.ATTACKING);
        addForwardTwo(f);
        return f;
    }

    // ---------------------------------------------------------------- goalkeeper

    // EFFECTS: adds a goalkeeper on the centre of the goal line
    private static void addGoalkeeper(Formation formation) {
        add(formation, Position.GK, 0.05, 0.50);
    }

    // ---------------------------------------------------------------- defence

    // EFFECTS: adds three CBs, left to right, with the middle one slightly deeper
    private static void addBackThree(Formation formation) {
        add(formation, Position.CB, 0.22, 0.25);
        add(formation, Position.CB, 0.20, 0.50);
        add(formation, Position.CB, 0.22, 0.75);
    }

    // EFFECTS: adds LB, CB, CB, RB (left to right) around x = 0.25, with the centre-backs slightly deeper
    private static void addBackFour(Formation formation) {
        add(formation, Position.LB, 0.25, 0.12);
        add(formation, Position.CB, 0.22, 0.38);
        add(formation, Position.CB, 0.22, 0.62);
        add(formation, Position.RB, 0.25, 0.88);
    }

    // EFFECTS: adds LWB, CB, CB, CB, RWB (left to right); the wing-backs sit higher and wider than full-backs
    private static void addBackFive(Formation formation) {
        add(formation, Position.LWB, 0.30, 0.08);
        add(formation, Position.CB, 0.20, 0.30);
        add(formation, Position.CB, 0.18, 0.50);
        add(formation, Position.CB, 0.20, 0.70);
        add(formation, Position.RWB, 0.30, 0.92);
    }

    // ---------------------------------------------------------------- midfield

    // EFFECTS: adds two CMs side by side, left to right
    private static void addMidfieldTwo(Formation formation) {
        add(formation, Position.CM, 0.48, 0.38);
        add(formation, Position.CM, 0.48, 0.62);
    }

    // EFFECTS: adds two CDMs side by side (a double pivot), left to right, in front of the back line
    private static void addDefensiveMidfieldTwo(Formation formation) {
        add(formation, Position.CDM, 0.40, 0.38);
        add(formation, Position.CDM, 0.40, 0.62);
    }

    // EFFECTS: adds three CAMs, left to right, in the band behind the striker; the central one slightly higher
    private static void addAttackingMidfieldThree(Formation formation) {
        add(formation, Position.CAM, 0.62, 0.20);
        add(formation, Position.CAM, 0.64, 0.50);
        add(formation, Position.CAM, 0.62, 0.80);
    }

    // EFFECTS: adds a midfield three, left to right: a CM on each side (x = 0.50) and, in the middle,
    //          a CM (DEFAULT, x = 0.47), a CAM pushed forward (ATTACKING, x = 0.60), or a CDM sitting deep (HOLDING, x = 0.38)
    private static void addMidfieldThree(Formation formation, MidfieldThreeRole role) {
        add(formation, Position.CM, 0.50, 0.28);
        switch (role) {
            case ATTACKING:
                add(formation, Position.CAM, 0.60, 0.50);
                break;
            case HOLDING:
                add(formation, Position.CDM, 0.38, 0.50);
                break;
            default:
                add(formation, Position.CM, 0.47, 0.50);
                break;
        }
        add(formation, Position.CM, 0.50, 0.72);
    }

    // EFFECTS: adds a midfield four, left to right, shaped by role (see MidfieldFourRole)
    private static void addMidfieldFour(Formation formation, MidfieldFourRole role) {
        switch (role) {
            case ATTACK:
                add(formation, Position.LM, 0.60, 0.12);
                add(formation, Position.CM, 0.52, 0.38);
                add(formation, Position.CM, 0.52, 0.62);
                add(formation, Position.RM, 0.60, 0.88);
                break;
            case HOLDING:
                add(formation, Position.LM, 0.50, 0.12);
                add(formation, Position.CDM, 0.40, 0.38);
                add(formation, Position.CDM, 0.40, 0.62);
                add(formation, Position.RM, 0.50, 0.88);
                break;
            case DIAMOND:
                add(formation, Position.LM, 0.50, 0.18);
                add(formation, Position.CDM, 0.38, 0.50);
                add(formation, Position.CAM, 0.62, 0.50);
                add(formation, Position.RM, 0.50, 0.82);
                break;
            case NARROW_DIAMOND:
                add(formation, Position.CM, 0.50, 0.32);
                add(formation, Position.CDM, 0.38, 0.50);
                add(formation, Position.CAM, 0.62, 0.50);
                add(formation, Position.CM, 0.50, 0.68);
                break;
            default:
                add(formation, Position.LM, 0.50, 0.12);
                add(formation, Position.CM, 0.48, 0.38);
                add(formation, Position.CM, 0.48, 0.62);
                add(formation, Position.RM, 0.50, 0.88);
                break;
        }
    }

    // ---------------------------------------------------------------- attack

    // EFFECTS: adds a single ST in the centre
    private static void addLoneStriker(Formation formation) {
        add(formation, Position.ST, 0.85, 0.50);
    }

    // EFFECTS: adds two STs side by side, left to right
    private static void addForwardTwo(Formation formation) {
        add(formation, Position.ST, 0.82, 0.40);
        add(formation, Position.ST, 0.82, 0.60);
    }

    // EFFECTS: adds a front three, left to right: LW, a central forward, RW. The central forward is an ST (x = 0.85),
    //          or, if falseNine is true, a CF dropping deeper (x = 0.78) to link with the midfield
    private static void addForwardThree(Formation formation, boolean falseNine) {
        add(formation, Position.LW, 0.80, 0.12);
        if (falseNine) {
            add(formation, Position.CF, 0.78, 0.50);
        } else {
            add(formation, Position.ST, 0.85, 0.50);
        }
        add(formation, Position.RW, 0.80, 0.88);
    }

    // EFFECTS: adds a front four, left to right: LW, ST, ST, RW
    private static void addForwardFour(Formation formation) {
        add(formation, Position.LW, 0.78, 0.12);
        add(formation, Position.ST, 0.84, 0.40);
        add(formation, Position.ST, 0.84, 0.60);
        add(formation, Position.RW, 0.78, 0.88);
    }

    // ---------------------------------------------------------------- shared

    // EFFECTS: creates a slot at the given fractional location and adds it to the formation
    private static void add(Formation formation, Position position, double x, double y) {
        formation.addSlot(new Slot(position, new Coordinates(x, y)));
    }
}
