
import jgam.game.SingleMove;
import jgam.game.BoardSetup;
import jgam.game.BoardSnapshot;
import jgam.game.ArrayBoardSetup;
import java.util.*;
import jgam.ai.*;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class BearOffStreuung {
    public BearOffStreuung() {
        super();
    }

    static BearOffAI ai = new BearOffAI();

    public static void main(String[] args) throws Exception {

        ai.init();

        for (int i = 0; i < 300; i++) {
            TrainingSetup board = new TrainingSetup();
            float est1 = ai.lookup(ai.makePrint(board, 1));
            float est2 = ai.lookup(ai.makePrint(board, 2));
            int count = 0;
            for (int j = 0; j < 300; j++) {
                TrainingSetup p = new TrainingSetup(board);
                count += rollout(p);
            }
            System.out.println((est1 - est2) + " " + count);
        }

    }

    private static int rollout(TrainingSetup trainingSetup) throws CannotDecideException {

        int cnt = 0;

        while (trainingSetup.winstatus() == 0) {

            if (++cnt % 100 == 0) {
                System.out.println("after " + cnt + " moves:");
                trainingSetup.debugOut();
            }

            trainingSetup.roll();

            SingleMove[] moves = ai.makeMoves(trainingSetup);
            for (int i = 0; i < moves.length; i++) {
                trainingSetup.performMove(moves[i]);
                /*                trainingSetup.debugOut();
                                System.out.println("after doing "+moves[i]);*/
            }

            trainingSetup.switchPlayers();
        }

        if (trainingSetup.winstatus() == 1) {
            return 1;
        } else {
            return 0;
        }

    }
}
