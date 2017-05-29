import java.io.*;
import java.util.*;

import jgam.*;
import jgam.ai.*;
import jgam.game.*;
import mattze.ann.*;

/**
 * <p> Title: JGam - Java Backgammon </p>
 * 
 * <p> Description: </p>
 * 
 * <p> Copyright: Copyright (c) 2005 </p>
 * 
 * <p> Company: </p>
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class ProbRaceRolloutTraining extends Thread implements ConsoleApp {

    boolean interrupted = false;

    SimpleNeuralNet neuralnet;

    RandomAI randomAi;

    PrintWriter dbgWriter;

    PrintWriter logWriter;

    boolean suspendWish = false;

    BoardSetup startSetup = BoardSnapshot.INITIAL_SETUP;

    public int debuglevel = 0;

    // initial learning rate
    double alpha = .1;

    // learning rate annealing
    double anneal = .3;

    // rate to make random moves.
    double exploration = .1;

    Timer timer;

    private double[] features = new double[NormalNeuralNetAI.NUMBER_OF_NORMAL_FEATURES];

    private BearOffAI boAI;

    private ProbRaceNeuralNetAI raceAI;

    private InitialAI detai;

    // how many rounds
    private int rounds = 20;

    // into which depth at max
    private int depth = 40;

    private int offset = 0;

    public ProbRaceRolloutTraining() throws Exception {
        super("Probability Race Rollout Trainer");
        randomAi = new RandomAI();
        randomAi.init();
        boAI = new BearOffAI();
        boAI.init();
        detai = new InitialAI();
        detai.init();
    }

    public void run() {

        Random random = new Random();
        int games = 0;

        try {
            while (true) {

                TrainingSetup setup = new TrainingSetup(startSetup);
                debugout(1, "Games " + games + "  Setups "
                        + neuralnet.getTrainCount());

                //
                // play till separated using the predefined player
                while (!setup.isSeparated()) {
                    do {
                        setup.roll();
                    } while (setup.undecidedPlayer());
                    setup.performMoves(detai.makeMoves(setup));
                    setup.switchPlayers();
                }

                //
                // play a game
                //
                while (setup.winstatus() == 0) {
                    do {
                        setup.roll();
                    } while (setup.undecidedPlayer());

                    if (random.nextDouble() < exploration) {
                        // exploration move!!
                        // which is not recorded!
                        setup.performMoves(randomAi.makeMoves(setup));
                        debugout(2, "Exploration step");
                    } else {
                        // exploit step

                        debugout(3, setup);
                        debugout(3, "Current Value: "
                                + raceAI.propabilityToWin(setup));

                        double[] mywin = { rollout(setup, rounds) };
                        train(setup, mywin);

                        if (setup.getMaxPoint(setup.getPlayerAtMove()) <= 6)
                            setup.performMoves(boAI.makeMoves(setup));
                        else
                            setup.performMoves(raceAI.makeMoves(setup));

                        setup.switchPlayers();

                        checkSuspendWish();
                    }
                }
                games++;
                debugout(3, setup);
                debugout(3, "is lost, prob: " + raceAI.propabilityToWin(setup));
                train(setup, new double[] { 0 });
            }
        } catch (CannotDecideException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * train
     */
    private void train(BoardSetup setup, double[] target) {

        raceAI.getFeatures(setup, features);

        synchronized (neuralnet) {
            double rate = alpha
                    / Math.pow(
                        (neuralnet.getTrainCount() - offset) / 1000. + 1.,
                        anneal);
            neuralnet.setLearningRate(rate);

            debugout(2, "Learning rate: " + rate);
            debugout(2, "Train " + neuralnet.applyTo(features)[0] + " -> "
                    + target[0]);
            neuralnet.train(features, target);
            debugout(2, "    -> " + neuralnet.applyTo(features)[0]);

            if (logWriter != null) {
                logWriter.println("" + neuralnet.applyTo(features)[0] + " "
                        + target[0] + " " + rate);
                // logWriter.flush();
            }
        }
    }

    public void setLog(PrintWriter w) {
        dbgWriter = w;
    }

    /**
     * debugout
     * 
     * @param i int
     * @param d double
     */
    private void debugout(int level, Object o) {
        if (debuglevel >= level) {
            if (o instanceof BoardSetup) {
                ((BoardSetup) o).debugOut(dbgWriter);
            } else {
                dbgWriter.println(o);
            }
            dbgWriter.flush();
        }
    }

    void eval(int number) {
        try {
            TrainingSetup setup = new TrainingSetup(startSetup);

            //
            // play till separated using the predefined player
            while (!setup.isSeparated()) {
                do {
                    setup.roll();
                } while (setup.undecidedPlayer());
                setup.performMoves(detai.makeMoves(setup));
                setup.switchPlayers();
            }

            while (setup.winstatus() == 0) {
                debugout(0, "" + rollout(setup, 25) + " "
                        + raceAI.propabilityToWin(setup));

                setup.roll();
                setup.performMoves(raceAI.makeMoves(setup));
                setup.switchPlayers();
            }
        } catch (CannotDecideException ex) {
            ex.printStackTrace(dbgWriter);
        }
    }

    private double rollout(TrainingSetup trainingSetup, int rounds)
            throws CannotDecideException {

        TrainingSetup t = new TrainingSetup();
        double wincount = 0;
        int i;
        for (i = 0; i < rounds; i++) {
            t.copyFrom(trainingSetup);
            int initialplayer = t.getPlayerAtMove();
            int cnt = 0;

            while (t.winstatus() == 0 && cnt < 2 * depth) {
                // t.debugOut();
                t.roll();
                if (t.getMaxPoint(t.getPlayerAtMove()) <= 6)
                    t.performMoves(boAI.makeMoves(t));
                else
                    t.performMoves(raceAI.makeMoves(t));

                t.switchPlayers();
                cnt++;
            }

            if (t.winstatus() == initialplayer) {
                wincount++;
            } else if (t.winstatus() != 3 - initialplayer) {
                assert t.getActivePlayer() == initialplayer;
                wincount += raceAI.propabilityToWin(t);
            }
        }

        return wincount / i;

    }

    void save(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream os = new ObjectOutputStream(fos);

            synchronized (neuralnet) {
                os.writeObject(neuralnet);
            }

            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        ProbRaceRolloutTraining t = new ProbRaceRolloutTraining();
        if (args.length > 0) {
            new ConsoleFrame(t).setVisible(true);
        } else {
            t.commandline();
        }
    }

    /**
     * commandline
     */
    private void commandline() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        dbgWriter = new PrintWriter(System.out, true);
        dbgWriter.println("Reinforcement JGAMMON Trainer Probability Race AI");
        while (true) {
            dbgWriter.flush();
            String c = br.readLine();
            try {
                if (command(c)) {
                    dbgWriter.println(" > " + c);
                } else {
                    dbgWriter.println("unknown: " + c);
                }
            } catch (Exception ex) {
                ex.printStackTrace(dbgWriter);
            }
        }
    }

    /**
     * newNet
     * 
     * @throws Exception
     */
    public void newNet(double range) throws Exception {
        // New net!
        // TODO soll das aussehen?
        SimpleNeuralNet neuralnet = new SimpleNeuralNet(
                ProbRaceNeuralNetAI.NUMBER_OF_RACE_FEATURES, 10, 1);
        neuralnet.randomize(range);
        setNet(neuralnet);
    }

    /**
     * setNet
     * 
     * @param neuralNetwork NeuralNetwork
     * @throws Exception by AI.init
     */
    public void setNet(SimpleNeuralNet neuralNetwork) throws Exception {
        neuralnet = neuralNetwork;
        raceAI = new ProbRaceNeuralNetAI(neuralNetwork);
        raceAI.init();
    }

    /**
     * getNeuralNet
     * 
     * @return Object
     */
    public Object getNeuralNet() {
        return neuralnet;
    }

    synchronized void checkSuspendWish() {
        if (suspendWish) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    public boolean command(String command) throws Exception {
        String args[] = command.split(" +", 2);

        if (command.equals("exit") || command.equals("quit")) {
            System.exit(0);
        } else

        if (args[0].equals("newnet")) {
            newNet(Double.parseDouble(args[1]));
        } else

        if (args[0].equals("loadnet")) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                    args[1]));
            setNet((SimpleNeuralNet) ois.readObject());
            ois.close();
        } else

        if (args[0].equals("savenet")) {
            ObjectOutputStream ois = new ObjectOutputStream(
                    new FileOutputStream(args[1]));
            ois.writeObject(getNeuralNet());
        } else

        if (args[0].equals("dbg")) {
            try {
                debuglevel = Integer.parseInt(args[1]);
            } catch (Exception ex) {
                debuglevel = 0;
            }
        } else

        if (args[0].equals("log")) {
            if (logWriter != null) {
                logWriter.close();
            }
            if (args.length > 1) {
                logWriter = new PrintWriter(new FileWriter(args[1]));
            } else {
                logWriter = null;
            }
        } else

        if (command.equals("flushlog")) {
            logWriter.flush();
        } else

        if (command.startsWith("alpha ")) {
            alpha = Double.parseDouble(command.substring(6));
        } else

        if (args[0].equals("offset")) {
            offset = Integer.parseInt(args[1]);
        } else

        if (args[0].equals("anneal")) {
            anneal = Double.parseDouble(args[1]);
        } else

        if (args[0].equals("rounds")) {
            rounds = Integer.parseInt(args[1]);
        } else

        if (args[0].equals("depth")) {
            depth = Integer.parseInt(args[1]);
        } else

        if (args[0].startsWith("explor")) {
            exploration = Double.parseDouble(args[1]);
        } else

        if (args[0].equals("eval")) {
            debugout(0, "hang on ... evaluating ...");
            new Thread() {
                public void run() {
                    eval(0);
                }
            }.start();

        } else

        if (command.startsWith("sus")) {
            suspendWish = true;
        } else

        if (command.equals("resume")) {
            synchronized (this) {
                suspendWish = false;
                notify();
            }
        } else

        if (command.equals("step") || command.equals("s")) {
            synchronized (this) {
                suspendWish = true;
                notify();
            }
        } else

        if (args[0].equals("setup")) {
            startSetup = new FileBoardSetup(new File(args[1]));
        } else

        if (command.equals("run")) {
            if (isAlive()) {
                dbgWriter.println("thread already running");
            } else {
                start();
            }
        } else

        if (args[0].equals("autosave")) {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            int sec = Integer.parseInt(args[1]) * 60000;
            timer.schedule(new TimerTask() {
                public void run() {
                    save("racerollout.autosave.ser");
                    debugout(1, ">> autosave " + new Date());
                }
            }, sec, sec);
        } else

        {
            return false;
        }

        return true;
    }
}
