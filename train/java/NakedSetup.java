import jgam.game.*;

/**
 * remove Player and dice information from a setup,
 * so two setups are considered equal if even the dice are different!
 */
class NakedSetup extends BoardSetup {
    private BoardSetup wrapped;

    NakedSetup(BoardSetup wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * get the number of the player who may make the next move or decision.
     * no player!
     */
    public int getActivePlayer() {
        return 0;
    }

    /**
     * no dice!
     */
    public int[] getDice() {
        return null;
    }

    /**
     * get the value of the doubling cube.
     *
     * No double cube.
     *
     * @return 1, 2, 4, 8, 16, 32, 64
     */
    public int getDoubleCube() {
        return 1;
    }

    /**
     * get the number of checkers on a specific point for a player.
     *
     * delegate to wrapped
     *
     * @param player Player to check for (1 or 2)
     * @param pointnumber Point to check (1-24 for points, 0 for off, 25 for
     *   bar)
     * @return a value between 0 and 15
     */
    public int getPoint(int player, int pointnumber) {
        return wrapped.getPoint(player, pointnumber);
    }

    /**
     * may a player double the game value.
     * no cube!
     */
    public boolean mayDouble(int playerno) {
        return true;
    }
}
