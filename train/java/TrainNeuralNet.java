
import mattze.ann.*;
import jgam.ai.*;
import java.io.*;
import java.util.*;
import jgam.game.*;
import mattze.xmlize.*;

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
public class TrainNeuralNet {

    static double train[][];
    static double target[][];


    public static void main(String[] args) throws Exception {

        // set up neural net.
        MultiLayerNeuralNet nn = new MultiLayerNeuralNet();
        nn.setInputLayer(new SigmoidLayer(NormalNeuralNetAI.NUMBER_OF_NORMAL_FEATURES, 10));
        nn.addLayer(new SigmoidLayer(1));
        nn.setLearningRate(0.7);
        nn.setMomentum(0.3);
        nn.randomize(.5);

        // load trainingsdata
        load();

        double mse = 1000;
        int round = 0;
        for (; round < 10000 && mse > .008 * train.length; round++) {
            for (int j = 0; j < train.length; j++) {
                nn.train(train[j], target[j]);
            }

            mse = 0;
            for (int j = 0; j < train.length; j++) {
                mse += mse(nn.applyTo(train[j]), target[j]);
            }
            System.out.println("" + round + " " + mse / train.length);

        }

        System.out.println("#Rounds: " + round);

        XMLObjectWriter out = new XMLObjectWriter(new FileWriter("neuralnet.xml"));
        out.writeObject(nn);
        out.close();
    }

    static void load() throws IOException {
        List entries = new ArrayList();
        List targetlist = new ArrayList();
        BufferedReader br = new BufferedReader(new FileReader("trainingdata.dat"));
        while (br.ready()) {
            String tok[] = br.readLine().split(" ");
            BoardSetup b = ArrayBoardSetup.decodeBase64(tok[0]);
            entries.add(NormalNeuralNetAI.extractFeatures(b, null));
            targetlist.add(new double[] {Double.parseDouble(tok[1])});
        }
        train = (double[][]) entries.toArray(new double[0][]);
        target = (double[][]) targetlist.toArray(new double[0][]);
        br.close();
    }

    static public double mse(double[] v1, double[] v2) {
        double mse = 0;
        for (int i = 0; i < v2.length; i++) {
            mse += (v1[i] - v2[i]) * (v1[i] - v2[i]);
        }
        return mse;
    }

}
