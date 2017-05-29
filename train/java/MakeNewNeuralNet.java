import jgam.ai.*;
import mattze.ann.*;
import jgam.game.*;
import mattze.xmlize.*;
import java.io.*;

/**
 * Start a new neural net from scratch.
 *
 * play a couple of games and evaluate every positition via
 *
 * my_needed_turns = .1142 * mypip + 2.3041;   [also his]
 * diff = my_needed_turns - his_needed_turns
 * propab_to_win = 1 / (1 + Math.exp(1.3 * diff - 0.65));
 *
 * that's a good point to start learning at. ...
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class MakeNewNeuralNet {

    final static int HIDDEN_LAYER_SIZE = 30;
    final static int POPULATION = 1000;

    public static void main(String[] args) throws CannotDecideException, IOException {

        MultiLayerNeuralNet nn = new MultiLayerNeuralNet();
        // we don't have a CombiAI, yet. Possibly
        // doesn't matter: only needed to create positions
        InitialAI ai = new InitialAI();
        ai.init();

        nn.setInputLayer(new SigmoidLayer(NormalNeuralNetAI.NUMBER_OF_NORMAL_FEATURES, HIDDEN_LAYER_SIZE));
        nn.addLayer(new SigmoidLayer(1));
        nn.setLearningRate(.8);
        nn.setMomentum(.3);
        nn.randomize(1);

        double[][] features = new double[2 * POPULATION][];
        double[][] target = new double[2 * POPULATION][1];

        TrainingSetup s = new TrainingSetup();

        for (int i = 0; i < POPULATION / 2; i++) {

            if (s.isSeparated()) {
                s = new TrainingSetup();
            }

            s.roll();
            s.performMoves(ai.makeMoves(s));

            features[2 * i] = NormalNeuralNetAI.extractFeatures(s, null);
            target[2 * i][0] = eval(s);

            // view both sides of the story!
            s.switchPlayers();

            features[2 * i + 1] = NormalNeuralNetAI.extractFeatures(s, null);
            target[2 * i + 1][0] = eval(s);

        }

        System.out.println("Population made, Training");

        double mse = 1000;
        int generation = 0;
        double bestmse = 1000;
        MultiLayerNeuralNet bestNet = null;

        while (mse > .0025 && generation < 250) {

            for (int i = 0; i < POPULATION; i++) {
                nn.train(features[i], target[i]);
            }

            mse = 0;
            double absdiff = 0;
            for (int i = 0; i < POPULATION; i++) {
                double sqerr = nn.error(features[i], target[i]);
                mse += sqerr;
                absdiff += Math.sqrt(sqerr);
            }
            mse /= POPULATION;
            absdiff /= POPULATION;

            double noise = (mse - .001) * (mse - .001);
            nn.addNoise(noise);

            if (generation % 20 == 0) {
                nn.addNoise(.1);
            }

            if (mse < bestmse) {
                bestmse = mse;
                bestNet = (MultiLayerNeuralNet) nn.clone();
            }

            System.out.println("After " + (++generation) + " generations MSE=" +
                    mse + " ; Absdiff=" + absdiff + " ; Noise=" + noise);

        }

        System.out.println("Best netowrk: MSE="+bestmse);

        XMLObjectWriter o = new XMLObjectWriter(new FileWriter("neuralnet_new.xml"));
        o.writeObject(bestNet);
        o.flush();
        o.close();

        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("neuralnet_new.ser"));
        os.writeObject(bestNet);
        os.flush();
        os.close();

    }

    static double eval(BoardSetup s) {

        // could be done easier ... better to check like this

        int me = s.getPlayerAtMove();
        int mypip = s.calcPip(me);
        int hispip = s.calcPip(3 - me);

        double myturns = .1142 * mypip + 2.3041;
        double histurns = .1142 * hispip + 2.3041;

        double diff = myturns - histurns;

        double val = 1 / (1 + Math.exp(1.3 * diff - 0.65));

        return val;
    }
}
