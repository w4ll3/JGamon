
import javax.swing.ProgressMonitor;

import jgam.ai.BearOffAI;
import jgam.game.SingleMove;

/**
 * 
 * Bearoff rollouts.
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class MakeBOStatistics {
    public MakeBOStatistics() {
        super();
    }

    public static void main(String[] args) throws Exception {

        ProgressMonitor pm = new ProgressMonitor(null, "Rolling out ...", null,
                0, 1000);
        BearOffAI ai = new BearOffAI();
        ai.init();

        for (int i = 0; i < 400 && !pm.isCanceled(); i++) {
            pm.setProgress(i);
            StatSetup s = new StatSetup();
            // s.debugOut(new PrintWriter(System.err));
            System.out.print(ai.lookup(ai.makePrint(s, 1)));
            System.out.print(" " + ai.lookup(ai.makePrint(s, 2)));
            TrainingSetup t = new TrainingSetup();

            int wincount = 0;
            int roundcount = 0;

            for (int j = 0; j < 300; j++) {
                t.copyFrom(s);
                int rounds = 0;
                while (t.winstatus() == 0) {
                    t.roll();
                    SingleMove moves[] = ai.makeMoves(t);
                    t.performMoves(moves);
                    if(t.getActivePlayer() == 1)
                        rounds++;
                    t.switchPlayers();                    
                }

                if (t.winstatus() == 1)
                    wincount++;

                t.setPlayerAtMove(1);
                while (t.getMaxPoint(1) > 0) {
                    SingleMove moves[] = ai.makeMoves(t);
                    t.performMoves(moves);
                    rounds++;
                }
                roundcount += rounds;
            }

            System.out.println(" " + wincount / 300. + " "
                    + ai.propabilityToWin(s) + " " + roundcount / 300.);
        }
        System.exit(0);
    }
}

class StatSetup extends TrainingSetup {

    StatSetup() {
        super(true);
        make(checkers1, random.nextInt(14) + 1);
        make(checkers2, random.nextInt(14) + 1);
        activePlayer = 1;
    }

    void make(byte[] board, int cnt) {
        while (cnt-- > 0) {
            board[random.nextInt(6) + 1]++;
        }
    }

}
