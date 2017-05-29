
import java.awt.event.*;
import java.io.*;
import java.util.*;
import jgam.ai.*;

import javax.swing.*;

import jgam.game.*;
import java.awt.GridLayout;
import java.awt.Dimension;
import mattze.ann.*;
import java.text.*;
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
public class FeatureTrainer extends JFrame {

    private JLabel label = new JLabel();
    private JProgressBar progress = new JProgressBar();
    private JButton interrupt = new JButton();
    private static DateFormat df = new SimpleDateFormat("MMddHHmm");
    private static NumberFormat nf = new DecimalFormat("#0,0 %");

    private static class State implements Serializable {
        // number of positions to check
        int numberOfBoards;
        // number of rollouts/position
        int numberOfRollouts;
        int maxIterations;
        int numberOfMoves;
        // mse to achieve
        double maxMSE;
        Set boardSet = new HashSet();
        LinkedList featureValues = new LinkedList();
        LinkedList targetValues = new LinkedList();
        NeuralNet neuralNet = new MultiLayerNeuralNet();

        int generation = 0;

        // 0 = gather samples
        // 1 = evalute samples
        // 2 = train network
        int current_job = 0;
        String serfile;
    };

    private CombiAI ai;
    private State state;
    private boolean interrupted = false;
    private Random random = new Random();
    private GridLayout gridLayout1 = new GridLayout();
    private JProgressBar progress2 = new JProgressBar();

    public FeatureTrainer() {
        super("Training features");
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            FeatureTrainer featuretrainer = new FeatureTrainer();

            if(args[0].equals("start")) {
                Properties prop = new Properties();
                prop.load(new FileInputStream(args[1]));
                featuretrainer.state = new State();
                featuretrainer.state.numberOfBoards = Integer.parseInt(prop.getProperty("numberofboards"));
                featuretrainer.state.numberOfRollouts = Integer.parseInt(prop.getProperty("numberofrollouts"));
                featuretrainer.state.maxMSE = Double.parseDouble(prop.getProperty("maxmse"));
                featuretrainer.state.numberOfMoves = Integer.parseInt(prop.getProperty("numberofmoves"));
                featuretrainer.state.maxIterations = Integer.parseInt(prop.getProperty("numberofiterations"));
                featuretrainer.state.serfile = prop.getProperty("serfile");
                NormalNeuralNetAI nne = new NormalNeuralNetAI();
                nne.init();
                featuretrainer.state.neuralNet = nne.getNeuralNet();
            } else if (args[0].equals("cont")) {
                featuretrainer.recover(args[1]);
            } else {
                System.out.println("Arguments:");
                System.out.println(" start <prop file>");
                System.out.println(" cont <ser file>");
                return;
            }

            featuretrainer.setVisible(true);
            while (!featuretrainer.interrupted) {
                featuretrainer.go();

                ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(featuretrainer.state.serfile));
                os.writeObject(featuretrainer.state);
                os.close();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /**
     * go
     */
    private void go() throws Exception {
        switch (state.current_job) {
        case 0:
            gatherSamples();
            break;
        case 1:
            rolloutSamples();
            break;
        case 2:
            trainNetwork();
            break;
        }
    }

    /**
     * trainNetwork
     */
    private void trainNetwork() throws IOException {
        label.setText("training network ...");
        progress.setMaximum(state.maxIterations);
        progress2.setMaximum(1000);
        int iteration = 0;
        double mse = 1;

        while (mse > state.maxMSE && !interrupted && iteration < state.maxIterations) {

            progress.setValue(iteration);
            progress2.setValue((int) (mse * 1000));
            progress2.setString(nf.format(mse));

            Iterator featIt = state.featureValues.iterator();
            Iterator targetIt = state.targetValues.iterator();
            while (featIt.hasNext()) {
                double[] features = (double[]) featIt.next();
                double[] target = (double[]) targetIt.next();
                state.neuralNet.train(features, target);
            }
            featIt = state.featureValues.iterator();
            targetIt = state.targetValues.iterator();
            mse = 0;
            while (featIt.hasNext()) {
                double[] features = (double[]) featIt.next();
                double[] target = (double[]) targetIt.next();
                mse += mse(state.neuralNet.applyTo(features), target);
            }

            mse /= state.featureValues.size();
            iteration++;
        }

        progress2.setString(null);

        if (!interrupted) {
            state.current_job = 0;
            state.generation ++;
            state.featureValues.clear();
            state.targetValues.clear();
            System.out.println("Generation "+state.generation+" written.");

            // xML Version
            File temp = new File("neuralnet" + df.format(new Date()) + ".xml");
            XMLObjectWriter out = new XMLObjectWriter(new FileWriter(temp));
            out.writeObject(state.neuralNet);
            out.close();
            temp = new File("neuralnet" + df.format(new Date()) + ".ser");
            ObjectOutputStream serout = new ObjectOutputStream(new FileOutputStream(temp));
            serout.writeObject(state.neuralNet);
            serout.close();
        }

    }

    private static double mse(double[] v1, double[] v2) {
        double mse = 0;
        for (int i = 0; i < v2.length; i++) {
            mse += (v1[i] - v2[i]) * (v1[i] - v2[i]);
        }
        return mse;
    }

    /**
     * rolloutSamples
     */
    private void rolloutSamples() throws Exception {
        label.setText("rolling out the boards ...");
        progress.setMaximum(state.numberOfBoards);
        progress2.setMaximum(state.numberOfRollouts);
        Writer log = new FileWriter("rolledOut.dat", true);
        log.write(new Date().toString() + "\n");

        ai = new CombiAI();
        ai.init();
        ((NormalNeuralNetAI) ai.getAIForCategory(CombiAI.NORMAL)).setNeuralNet(state.neuralNet);

        while (!state.boardSet.isEmpty() && !interrupted) {
            progress.setValue(state.numberOfBoards - state.boardSet.size());
            setTitle("" + (100 * progress.getValue() / state.numberOfBoards) + "% Training Features");
            double wincount = 0;
            BoardSetup board = (BoardSetup) state.boardSet.iterator().next();
            if (board.getActivePlayer() == 2) {
                board = new InvertedBoardSetup(board);
            }

            for (int i = 0; i < state.numberOfRollouts; i++) {
                progress2.setValue(i);
                double result = rollout(new TrainingSetup(board), false);
                wincount += result;
            }

            double[] features = NormalNeuralNetAI.extractFeatures(board, null);
            double target = wincount / state.numberOfRollouts;

            state.featureValues.add(features);
            state.targetValues.add(new double[] {target});
            state.boardSet.remove(board);

            // log
            log.write(ArrayBoardSetup.encodeBase64(board)+" "+target+"\n");
        }

        log.flush();
        log.close();

        if (!interrupted) {
            state.current_job = 2;
            state.boardSet.clear();
        }
    }
    
    /**
     * gatherSamples
     */
    private void gatherSamples() throws Exception {
        label.setText("gathering samples ...");
        progress.setMaximum(state.numberOfBoards);
        ai = new CombiAI();
        ai.init();
        ((NormalNeuralNetAI) ai.getAIForCategory(CombiAI.NORMAL)).setNeuralNet(state.neuralNet);

        while (state.boardSet.size() < state.numberOfBoards && !interrupted) {
            TrainingSetup setup = new TrainingSetup();
            progress.setValue(state.boardSet.size());
            rollout(setup, true);
        }

        if (!interrupted) {
            state.current_job = 1;
        }

    }


    /**
     * rollout
     *
     * Roll any further only if the board is not separated yet, for, if it is,
     * RaceAI and BearOffAI estimate rather nicely.
     *
     * Do at max state.numberOfMoves moves then use the ai.
     *
     *
     * @param trainingSetup TrainingSetup
     */
    private double rollout(TrainingSetup trainingSetup, boolean record) throws CannotDecideException {

        int cnt = 0;

        while (trainingSetup.winstatus() == 0
                && cnt < state.numberOfMoves
                && !trainingSetup.isSeparated()) {

            if (++cnt % 100 == 0) {
                System.out.println("after " + cnt + " moves:");
                trainingSetup.debugOut();
            }

            // Only add "Normal" positions, no separated ones.
            if (record && state.boardSet.size() < state.numberOfBoards) {
                state.boardSet.add(new BoardSnapshot(new NakedSetup(trainingSetup)));
            }

            do {
                trainingSetup.roll(random.nextInt(6) + 1, random.nextInt(6) + 1);
            } while (trainingSetup.undecidedPlayer());

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
        } else if (trainingSetup.winstatus() == 2) {
            return 0;
        } else {
            int cat = ai.getCategory(trainingSetup, trainingSetup.getPlayerAtMove());
            try {
                return ((EvaluatingAI) ai.getAIForCategory(cat)).propabilityToWin(trainingSetup);
            }catch(CannotDecideException ex) {
                return ((EvaluatingAI) ai.getAIForCategory(ai.NORMAL)).propabilityToWin(trainingSetup);
            }
        }

    }

    private void recover(String filename) throws IOException, ClassNotFoundException, Exception {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(filename));
        state = (State) is.readObject();
    }

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(gridLayout1);
        interrupt.setText("Interrupt");
        interrupt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                label.setText("interrupted ... hold on");
                interrupted = true;
            }
        });
        label.setMinimumSize(new Dimension(150, 20));
        label.setText(" ");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        progress.setStringPainted(true);
        progress2.setStringPainted(true);
        this.getContentPane().add(label, null);
        gridLayout1.setColumns(1);
        gridLayout1.setRows(4);
        gridLayout1.setVgap(10);
        this.getContentPane().add(progress, null);
        this.getContentPane().add(progress2);
        this.getContentPane().add(interrupt, null);
        pack();
        setLocationRelativeTo(null);
    }
}


