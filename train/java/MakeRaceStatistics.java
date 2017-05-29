import java.util.*;

import javax.swing.ProgressMonitor;

import jgam.ai.*;
import jgam.game.SingleMove;

/**
 * 
 * Bearoff rollouts.
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class MakeRaceStatistics {
    public MakeRaceStatistics() {
        super();
    }

    static int RUNS = 200;
    
    public static void main(String[] args) throws Exception {

        BearOffAI boAI = new BearOffAI();
        boAI.init();
        RaceNeuralNetAI ai = new RaceNeuralNetAI(boAI);
        ai.init();

        List setups = makeSetups();
        ProgressMonitor pm = new ProgressMonitor(null, "Rolling out ...", null,
                0, setups.size());

        int progress = 0;
        for (Iterator it = setups.iterator(); it.hasNext() & !pm.isCanceled();) {
            TrainingSetup s = (TrainingSetup) it.next();
            pm.setProgress(progress++);

            // s.debugOut(new PrintWriter(System.err));
            System.out.print(ai.getEstimatedMoves(s, 1));
            System.out.print(" " + ai.getEstimatedMoves(s, 2));

            TrainingSetup t = new TrainingSetup();
            int wincount = 0;
            int roundcount[] = {0,0};

            for (int j = 0; j < RUNS; j++) {
                t.copyFrom(s);
                t.setPlayerAtMove(1);
                int rounds1 = 0; 
                while (t.getMaxPoint(1) > 0) {
                    SingleMove moves[];
                    t.roll();
                    try {
                        moves = boAI.makeMoves(t);
                    } catch (CannotDecideException ex) {
                        moves = ai.makeMoves(t);
                    }
                    t.performMoves(moves);                        
                    rounds1 ++;
                }
                
                int rounds2 = 0;
                t.setPlayerAtMove(2);
                while (t.getMaxPoint(2) > 0) {
                    SingleMove moves[];
                    t.roll();
                    try {
                        moves = boAI.makeMoves(t);
                    } catch (CannotDecideException ex) {
                        moves = ai.makeMoves(t);
                    }
                    t.performMoves(moves);
                    rounds2++;
                }
                
                if(rounds1 <= rounds2)
                    wincount++;

                roundcount[0] += rounds1;
                roundcount[1] += rounds2;
            }

            System.out.println(" " + wincount / (double)RUNS + " "
                    + ai.propabilityToWin(s) + " " + roundcount[0] / (double)RUNS
                    + " " + roundcount[1] / (double)RUNS);
            System.out.flush();
        }
        System.exit(0);
    }

    private static List makeSetups() throws Exception {
        List ret = new ArrayList();
        AI ai = new InitialAI();
        ai.init();
        
        ProgressMonitor pm = new ProgressMonitor(null, "Finding ...", null,
            0, 1000);

        while (ret.size() < 1000) {
            TrainingSetup t = new TrainingSetup();
            pm.setProgress(ret.size());
            
            while (t.winstatus() == 0) {

                do {
                    t.roll();
                } while (t.getPlayerAtMove() == 0);

                t.performMoves(ai.makeMoves(t));

                if (t.isSeparated() && t.getActivePlayer() == 1) {
                    ret.add(new TrainingSetup(t));                    
                }

                t.switchPlayers();

            }

        }
        pm.setProgress(ret.size());
        return ret;
    }
}