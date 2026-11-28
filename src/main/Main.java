package main;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

import main.model.Formation;
import main.model.Formations;
import main.model.Slot;

/**
 * A small console app: pick a formation from a menu and see it drawn on a pitch in the terminal.
 *
 * The pitch is drawn top to bottom, with the team attacking upward: the goalkeeper is at the bottom and
 * the opponent's goal at the top. Facing up, the team's left (small y) is on the left of the screen.
 * This is presentation only; it reads the model and never changes it.
 */
public class Main {

    // Drawing size in characters. Terminal characters are roughly twice as tall as they are wide,
    // so the column count is larger than the row count would suggest from the pitch's 105 x 68 proportions.
    private static final int ROWS = 33; // along the pitch length (x)
    private static final int COLS = 45; // along the pitch width (y)
    private static final int GOAL_WIDTH = 9;

    private static final class Option {
        final String name;
        final Supplier<Formation> factory;

        Option(String name, Supplier<Formation> factory) {
            this.name = name;
            this.factory = factory;
        }
    }

    private static final List<Option> OPTIONS = Arrays.asList(
            new Option("4-3-3", Formations::fourThreeThree),
            new Option("4-3-3 Attack", Formations::fourThreeThreeAttack),
            new Option("4-3-3 Holding", Formations::fourThreeThreeHolding),
            new Option("4-3-3 False 9", Formations::fourThreeThreeFalseNine),
            new Option("4-4-2", Formations::fourFourTwo),
            new Option("4-4-2 Attack", Formations::fourFourTwoAttack),
            new Option("4-4-2 Holding", Formations::fourFourTwoHolding),
            new Option("4-2-4", Formations::fourTwoFour),
            new Option("4-2-3-1", Formations::fourTwoThreeOne),
            new Option("4-1-2-1-2 Narrow", Formations::fourOneTwoOneTwoNarrow),
            new Option("4-1-2-1-2 Wide", Formations::fourOneTwoOneTwoWide),
            new Option("3-4-3", Formations::threeFourThree),
            new Option("3-4-3 Diamond", Formations::threeFourThreeDiamond),
            new Option("5-3-2", Formations::fiveThreeTwo),
            new Option("5-4-1", Formations::fiveFourOne),
            new Option("5-2-1-2", Formations::fiveTwoOneTwo));

    public static void main(String[] args) {
        try (Scanner in = new Scanner(System.in)) {
            while (true) {
                printMenu();
                if (!in.hasNextLine()) {
                    return;
                }
                String input = in.nextLine().trim();
                if (input.equalsIgnoreCase("q")) {
                    return;
                }
                Integer choice = parseChoice(input);
                if (choice == null) {
                    System.out.println("Please enter a number from 1 to " + OPTIONS.size() + ", or q to quit.\n");
                    continue;
                }
                System.out.println();
                System.out.println(render(OPTIONS.get(choice - 1).factory.get()));
            }
        }
    }

    private static void printMenu() {
        System.out.println("Pick a formation:");
        for (int i = 0; i < OPTIONS.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, OPTIONS.get(i).name);
        }
        System.out.print("Number (or q to quit): ");
    }

    // EFFECTS: returns the 1-based menu choice, or null if the input is not a valid option number
    private static Integer parseChoice(String input) {
        try {
            int n = Integer.parseInt(input);
            return (n >= 1 && n <= OPTIONS.size()) ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // EFFECTS: returns the formation drawn on a vertical pitch, attacking upward
    static String render(Formation formation) {
        char[][] grid = emptyPitch();
        for (Slot slot : formation.getSlots()) {
            place(grid, slot);
        }

        StringBuilder out = new StringBuilder();
        out.append(center(formation.getName(), COLS)).append('\n');
        out.append(center("^ attacking ^", COLS)).append('\n');
        for (char[] row : grid) {
            out.append(row).append('\n');
        }
        return out.toString();
    }

    // EFFECTS: returns a blank pitch with touchlines, goal lines, goals and a halfway line
    private static char[][] emptyPitch() {
        char[][] grid = new char[ROWS][COLS];
        for (char[] row : grid) {
            Arrays.fill(row, ' ');
        }
        for (int c = 0; c < COLS; c++) {
            grid[0][c] = '-';
            grid[ROWS - 1][c] = '-';
            grid[ROWS / 2][c] = '-';
        }
        for (int r = 0; r < ROWS; r++) {
            grid[r][0] = '|';
            grid[r][COLS - 1] = '|';
        }
        int goalStart = (COLS - GOAL_WIDTH) / 2;
        for (int c = goalStart; c < goalStart + GOAL_WIDTH; c++) {
            grid[0][c] = '=';
            grid[ROWS - 1][c] = '=';
        }
        grid[0][0] = '+';
        grid[0][COLS - 1] = '+';
        grid[ROWS - 1][0] = '+';
        grid[ROWS - 1][COLS - 1] = '+';
        return grid;
    }

    // EFFECTS: writes the slot's position label onto the grid at its location, centred on its column
    //          and kept inside the touchlines
    private static void place(char[][] grid, Slot slot) {
        double x = slot.getCoordinates().getX();
        double y = slot.getCoordinates().getY();
        int innerRows = ROWS - 2;
        int innerCols = COLS - 2;

        int row = 1 + (int) Math.round((1.0 - x) * (innerRows - 1));
        int col = 1 + (int) Math.round(y * (innerCols - 1));

        String label = slot.getPosition().name();
        int start = col - label.length() / 2;
        start = Math.max(1, Math.min(start, COLS - 1 - label.length()));
        for (int i = 0; i < label.length(); i++) {
            grid[row][start + i] = label.charAt(i);
        }
    }

    private static String center(String text, int width) {
        int pad = Math.max(0, (width - text.length()) / 2);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pad; i++) {
            sb.append(' ');
        }
        return sb.append(text).toString();
    }
}
