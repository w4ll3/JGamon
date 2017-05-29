
import java.util.*;

import jgam.ai.*;
import jgam.game.*;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *r
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class Pip2TurnsData {
    public Pip2TurnsData() {
        super();
    }

    static Random random = new Random();

    static CombiAI ai = new CombiAI();

    public static void main(String[] args) throws Exception {
        ai.init();

        for (int i = 0; i < 400; i++) {
            TrainingSetup inst = new TrainingSetup();
            rollon(inst);

            int count = 0;
            // mach noch mal 10 Züge
            while (!inst.winstatus() && count < 15) {
                inst.roll();
                SingleMove[] m = ai.makeMoves(inst);
                inst.performMoves(m);
                count++;
            }

            inst.setActivePlayer(1);
//            inst.debugOut();
            count = 0;
            for (int j = 0; j < 100; j++) {
                TrainingSetup s = new TrainingSetup(inst);
                int c = countMoves(s);
                count += c;
            }
            System.out.println("" + inst.pip() + " " + count / 100.);
        }

    }

    private static void rollon(TrainingSetup trainingSetup) throws Exception {
        while (!trainingSetup.isSeparated()) {

            do {
                trainingSetup.roll();
            } while (trainingSetup.undecidedPlayer());

            SingleMove[] moves = ai.makeMoves(trainingSetup);

            trainingSetup.performMoves(moves);

            trainingSetup.switchPlayers();
        }

    }


    static int countMoves(TrainingSetup s) throws Exception {

        int count = 0;

        while (!s.winstatus()) {
            s.roll();
            SingleMove[] m = ai.makeMoves(s);
            s.performMoves(m);
            count++;
        }

        return count;
    }

    /**
     * Training setups know about setting dice and performing single moves
     */
    static class TrainingSetup extends ArrayBoardSetup {

        Random random = new Random();

        TrainingSetup() {
            super(BoardSnapshot.INITIAL_SETUP);
        }

        TrainingSetup(BoardSetup setup) {
            super(setup);
        }

        int pip() {
            // inly player 1 interesting
            int sum = 0;
            for (int i = 0; i <= 25; i++) {
                sum += i * getPoint(1, i);
            }
            return sum;
        }


        void performMoves(SingleMove[] m) {
            for (int i = 0; i < m.length; i++) {
                if (m[i].player() == 1) {
                    checkers1[m[i].from()]--;
                    checkers1[m[i].to()]++;
                } else {
                    checkers2[m[i].from()]--;
                    checkers2[m[i].to()]++;
                }
            }
        }


        void roll() {
            if (dice == null) {
                dice = new int[2];
            }

            dice[0] = random.nextInt(6) + 1;
            dice[1] = random.nextInt(6) + 1;
        }

        boolean winstatus() {
            if (getMaxPoint(1) == 0) {
                return true;
            } else {
                return false;
            }
        }

        void switchPlayers() {
            activePlayer = 3 - getPlayerAtMove();
        }

        boolean undecidedPlayer() {
            return dice[0] == dice[1] && activePlayer == 0;
        }

        /**
         * setActivePlayer
         *
         * @param i int
         */
        private void setActivePlayer(int i) {
            activePlayer = i;
        }

    }

}
