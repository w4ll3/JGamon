
import jgam.game.SingleMove;
import jgam.game.BoardSetup;
import jgam.game.ArrayBoardSetup;
import java.util.Random;
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
public class Pip2WinsData {

    static CombiAI ai = new CombiAI();

    public static void main(String[] args) throws Exception {
        ai.init();

        for (int i = 0; i < 100; i++) {
            RandomSetup inst = new RandomSetup();
            int count = 0;
            for (int j = 0; j < 50; j++) {
                TrainingSetup s = new TrainingSetup(inst);
                count += rollout(s);
            }
        }

    }

    private static int rollout(TrainingSetup trainingSetup) throws CannotDecideException {

        int cnt = 0;

        while (trainingSetup.winstatus() == -1) {

            if (++cnt % 100 == 0) {
                System.out.println("after " + cnt + " moves:");
                trainingSetup.debugOut();
            }

            trainingSetup.roll();

            SingleMove[] moves = ai.makeMoves(trainingSetup);
            trainingSetup.performMoves(moves);


            trainingSetup.switchPlayers();
        }

        return trainingSetup.winstatus();
    }


    static class RandomSetup extends TrainingSetup {

        Random random = new Random();

        RandomSetup() {
            super();
            checkers1[0] = 9;
            int sep = random.nextInt(12)+7;

            for (int i = 0; i < 15; i++) {
                checkers1[random.nextInt(sep)]++;
            }

            for (int i = 0; i < 15; i++) {
                checkers2[random.nextInt(25-sep)]++;
            }

            activePlayer = 1;
        }
    }

}
