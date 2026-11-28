package main.model;

/**
 * The live match: the single owner of the pitch, both teams, the ball, the score, the clock and the
 * period. It also decides which way each team attacks: home attacks toward x = length in the first
 * half, and the teams swap ends for the second half.
 *
 * Match holds state and validates changes to it. It does not contain football rules or set-piece
 * arrangements; those live in the simulation package and act on a Match through its methods.
 * Player locations stay on Player (and the held ball's location on its holder), so Match never
 * stores a second copy of them.
 *
 * Time is in seconds of simulated match time.
 */
public class Match {

    public static final double DEFAULT_HALF_LENGTH_SECONDS = 45 * 60;

    private final Pitch pitch;
    private final Team home;
    private final Team away;
    private final Ball ball;
    private final double halfLengthSeconds;

    private int homeGoals;
    private int awayGoals;
    private MatchPeriod period;
    private double elapsedInPeriodSeconds;

    // EFFECTS: Constructs a match with the default half length; see the main constructor
    public Match(Pitch pitch, Team home, Team away) {
        this(pitch, home, away, DEFAULT_HALF_LENGTH_SECONDS);
    }

    // REQUIRES: pitch, home and away are not null; home and away are different teams with no player in common;
    //           halfLengthSeconds is positive and finite
    // EFFECTS: Constructs a match that has not started, 0-0, with the ball on the centre spot;
    //          throws IllegalArgumentException if any requirement is violated
    public Match(Pitch pitch, Team home, Team away, double halfLengthSeconds) {
        if (pitch == null || home == null || away == null) {
            throw new IllegalArgumentException("Pitch and teams must not be null");
        }
        if (home == away) {
            throw new IllegalArgumentException("A team cannot play itself");
        }
        for (Player player : home.getSquad()) {
            if (away.getSquad().contains(player)) {
                throw new IllegalArgumentException("Player " + player.getName() + " is in both squads");
            }
        }
        if (!(halfLengthSeconds > 0) || Double.isInfinite(halfLengthSeconds)) {
            throw new IllegalArgumentException("Half length must be positive and finite, got " + halfLengthSeconds);
        }
        this.pitch = pitch;
        this.home = home;
        this.away = away;
        this.halfLengthSeconds = halfLengthSeconds;
        this.ball = new Ball(getCentreSpot());
        this.period = MatchPeriod.NOT_STARTED;
    }

    public Pitch getPitch() {
        return pitch;
    }

    public Team getHome() {
        return home;
    }

    public Team getAway() {
        return away;
    }

    public Ball getBall() {
        return ball;
    }

    public double getHalfLengthSeconds() {
        return halfLengthSeconds;
    }

    // EFFECTS: returns the centre of the pitch, in metres
    public Coordinates getCentreSpot() {
        return new Coordinates(pitch.getLength() / 2.0, pitch.getWidth() / 2.0);
    }

    // REQUIRES: team is home or away
    // EFFECTS: returns the other team; throws IllegalArgumentException otherwise
    public Team opponentOf(Team team) {
        requireTeam(team);
        return team == home ? away : home;
    }

    // ---------------------------------------------------------------- direction

    // REQUIRES: team is home or away
    // EFFECTS: returns the end the team attacks: home attacks toward x = length until the second half
    //          starts, then the teams swap; throws IllegalArgumentException otherwise
    public AttackDirection getAttackDirection(Team team) {
        requireTeam(team);
        boolean secondHalfEnds = period == MatchPeriod.SECOND_HALF || period == MatchPeriod.FULL_TIME;
        AttackDirection homeDirection = secondHalfEnds ? AttackDirection.TOWARD_MIN_X : AttackDirection.TOWARD_MAX_X;
        return team == home ? homeDirection : homeDirection.opposite();
    }

    // ---------------------------------------------------------------- score

    // REQUIRES: team is home or away
    // EFFECTS: returns the team's goals; throws IllegalArgumentException otherwise
    public int getGoals(Team team) {
        requireTeam(team);
        return team == home ? homeGoals : awayGoals;
    }

    // REQUIRES: team is home or away, and the ball is in play (first or second half)
    // MODIFIES: this
    // EFFECTS: adds a goal for the team; throws IllegalArgumentException for an unknown team,
    //          IllegalStateException if the match is not in play
    public void recordGoal(Team scoringTeam) {
        requireTeam(scoringTeam);
        if (!period.isInPlay()) {
            throw new IllegalStateException("Goals can only be scored while the match is in play, not during " + period);
        }
        if (scoringTeam == home) {
            homeGoals++;
        } else {
            awayGoals++;
        }
    }

    // ---------------------------------------------------------------- period and clock

    public MatchPeriod getPeriod() {
        return period;
    }

    // EFFECTS: returns the seconds played in the current half (0 outside the halves' play)
    public double getElapsedInPeriodSeconds() {
        return elapsedInPeriodSeconds;
    }

    // EFFECTS: returns true if the current half has run its full length
    public boolean isPeriodTimeUp() {
        return period.isInPlay() && elapsedInPeriodSeconds >= halfLengthSeconds;
    }

    // MODIFIES: this
    // EFFECTS: NOT_STARTED -> FIRST_HALF with the clock at 0; throws IllegalStateException from any other period
    public void startFirstHalf() {
        transition(MatchPeriod.NOT_STARTED, MatchPeriod.FIRST_HALF);
    }

    // MODIFIES: this
    // EFFECTS: HALF_TIME -> SECOND_HALF with the clock at 0 (ends are swapped from now on);
    //          throws IllegalStateException from any other period
    public void startSecondHalf() {
        transition(MatchPeriod.HALF_TIME, MatchPeriod.SECOND_HALF);
    }

    // MODIFIES: this
    // EFFECTS: FIRST_HALF -> HALF_TIME or SECOND_HALF -> FULL_TIME, with the clock at 0;
    //          throws IllegalStateException if no half is being played
    public void endHalf() {
        if (period == MatchPeriod.FIRST_HALF) {
            transition(MatchPeriod.FIRST_HALF, MatchPeriod.HALF_TIME);
        } else {
            transition(MatchPeriod.SECOND_HALF, MatchPeriod.FULL_TIME);
        }
    }

    // REQUIRES: a half is being played; seconds is positive and finite
    // MODIFIES: this
    // EFFECTS: advances the clock. It does not end the half on its own: the match loop checks isPeriodTimeUp
    //          and calls endHalf, so it can later allow stoppage time. Throws IllegalStateException outside a half,
    //          IllegalArgumentException for an invalid duration.
    public void advanceClock(double seconds) {
        if (!period.isInPlay()) {
            throw new IllegalStateException("The clock only runs during a half, not during " + period);
        }
        if (!(seconds > 0) || Double.isInfinite(seconds)) {
            throw new IllegalArgumentException("Seconds must be positive and finite, got " + seconds);
        }
        elapsedInPeriodSeconds += seconds;
    }

    private void transition(MatchPeriod from, MatchPeriod to) {
        if (period != from) {
            throw new IllegalStateException("Cannot move to " + to + " from " + period);
        }
        period = to;
        elapsedInPeriodSeconds = 0;
    }

    private void requireTeam(Team team) {
        if (team != home && team != away) {
            throw new IllegalArgumentException("Team is not playing in this match");
        }
    }
}
