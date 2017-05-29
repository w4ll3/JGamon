import java.io.*;
import java.util.*;

import jgam.*;
import jgam.ai.*;
import jgam.game.*;
import mattze.ann.*;

/**
 * <p>
 * Title: JGam - Java Backgammon
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class ReinforcementTraining extends Thread implements ConsoleApp {

    boolean interrupted = false;

    AskGnuBG askGnu = null;

    SimpleNeuralNet normalneuralnet;

    SimpleNeuralNet raceneuralnet;

    CombiAI ai;

    RandomAI randomAi;

    PrintWriter dbgWriter;

    PrintWriter logWriter;

    boolean suspendWish = false;

    BoardSetup startSetup = BoardSnapshot.INITIAL_SETUP;

    public int debuglevel = 0;

    public boolean useDeterministic = true;

    int maxgenerations = 100;

    // initial learning rate
    double alpha = .1;

    // learning rate annealing
    double anneal = .3;

    // rate to make random moves.
    double exploration = .1;

    Timer timer;

    private double[] features = new double[NormalNeuralNetAI.NUMBER_OF_NORMAL_FEATURES];

    private double[] racefeatures = new double[ProbRaceNeuralNetAI.NUMBER_OF_RACE_FEATURES];

    public ReinforcementTraining() throws Exception {
        super("Normal Neural Net Trainer");
        randomAi = new RandomAI();
        randomAi.init();
        ai = new CombiAI();

        // prevent from reading a net from resources.
        ai.setAIForCategory(ai.NORMAL, new NormalNeuralNetAI());
        ai.setAIForCategory(ai.RACE, new ProbRaceNeuralNetAI());
        ai.init();
    }

    public void run() {

        InitialAI detai = new InitialAI();
        Random random = new Random();
        TrainingSetup lastSetup = new TrainingSetup();
        int games = 0;

        try {
            while (true) {

                TrainingSetup setup = new TrainingSetup(startSetup);
                int gamelength = 0;

                debugout(1, "Games " + games + "  Setups "
                        + normalneuralnet.getTrainCount() + "/"
                        + raceneuralnet.getTrainCount());

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
                        debugout(3, "Category: "
                                + ai
                                        .getCategory(setup, setup
                                                .getPlayerAtMove()));
                        debugout(3, "Win Chance: " + ai.propabilityToWin(setup));
                        lastSetup.copyFrom(setup);

                        if (useDeterministic) {
                            setup.performMoves(detai.makeMoves(setup));
                        } else {
                            setup.performMoves(ai.makeMoves(setup));
                        }

                        setup.switchPlayers();
                        double otherswin = ai.propabilityToWin(setup);
                        double[] mywin = { 1 - otherswin };

                        train(lastSetup, mywin);

                        checkSuspendWish();
                    }
                    gamelength++;
                }
                debugout(2, "Moves in game: " + gamelength);
                games++;
            }
        } catch (CannotDecideException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * train
     */

    private void train(BoardSetup setup, double[] target) {
        if (setup.isSeparated())
            trainRace(setup, target);
        else
            trainNormal(setup, target);
    }

    private void trainNormal(BoardSetup setup, double[] target) {

        NormalNeuralNetAI.extractFeatures(setup, features);

        synchronized (normalneuralnet) {
            double rate = alpha
                    / Math.pow(normalneuralnet.getTrainCount() / 1000. + 1.,
                        anneal);
            normalneuralnet.setLearningRate(rate);

            debugout(3, "Learning rate: " + rate);
            debugout(2, "Train N" + normalneuralnet.applyTo(features)[0]
                    + " -> " + target[0]);
            normalneuralnet.train(features, target);
            debugout(2, "    ->" + normalneuralnet.applyTo(features)[0]);

            if (logWriter != null) {
                logWriter.println("" + normalneuralnet.applyTo(features)[0]
                        + " " + target[0] + " " + rate);
            }
        }
    }

    private void trainRace(BoardSetup setup, double[] target) {

        ProbRaceNeuralNetAI.extractFeatures(setup, racefeatures);

        synchronized (raceneuralnet) {
            double rate = alpha
                    / Math.pow(raceneuralnet.getTrainCount() / 1000. + 1.,
                        anneal);
            normalneuralnet.setLearningRate(rate);

            debugout(3, "Learning rate: " + rate);
            debugout(2, "Train R" + raceneuralnet.applyTo(features)[0] + " -> "
                    + target[0]);
            normalneuralnet.train(features, target);
            debugout(2, "    ->" + raceneuralnet.applyTo(features)[0]);

            if (logWriter != null) {
                logWriter.println("" + raceneuralnet.applyTo(features)[0] + " "
                        + target[0] + " " + rate);
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

    void save(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream os = new ObjectOutputStream(fos);

            synchronized (normalneuralnet) {
                os.writeObject(normalneuralnet);
            }

            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        ReinforcementTraining t = new ReinforcementTraining();
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
        dbgWriter.println("Reinforcement JGAMMON Trainer NORMAL AI");
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
     */
    public void newNet(double range, double range2) {
        // New net! NORMAL
        SimpleNeuralNet neuralnet = new SimpleNeuralNet(
                NormalNeuralNetAI.NUMBER_OF_NORMAL_FEATURES, 40, 1);
        neuralnet.randomize(range);

        SimpleNeuralNet racenet = new SimpleNeuralNet(
                ProbRaceNeuralNetAI.NUMBER_OF_RACE_FEATURES, 20, 1);
        racenet.randomize(range2);

        setNormalNet(neuralnet);
        setRaceNet(racenet);
    }

    public void setNormalNet(SimpleNeuralNet neuralNetwork) {
        normalneuralnet = neuralNetwork;
        ai.setAIForCategory(ai.NORMAL, new NormalNeuralNetAI(normalneuralnet));
    }

    public void setRaceNet(SimpleNeuralNet neuralNetwork) {
        raceneuralnet = neuralNetwork;
        ai.setAIForCategory(ai.RACE, new ProbRaceNeuralNetAI(raceneuralnet));
    }

    /**
     * getNeuralNet
     * 
     * @return Object
     */
    public Object getNeuralNet() {
        return normalneuralnet;
    }

    synchronized void checkSuspendWish() {
        if (suspendWish) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    /* (non-Javadoc)
     * @see ConsoleApp#command(java.lang.String)
     */
    public boolean command(String command) throws Exception {
        String args[] = command.split(" +", 3);

        if (command.equals("exit") || command.equals("quit")) {
            System.exit(0);
        } else

        if (args[0].equals("newnet")) {
            newNet(Double.parseDouble(args[1]), Double.parseDouble(args[2]));
        } else

        if (args[0].equals("loadnormalnet")) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                    args[1]));
            setNormalNet((SimpleNeuralNet) ois.readObject());
            ois.close();
        } else

        if (args[0].equals("loadracenet")) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                    args[1]));
            setRaceNet((SimpleNeuralNet) ois.readObject());
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

        if (command.startsWith("alpha ")) {
            alpha = Double.parseDouble(command.substring(6));
        } else

        if (args[0].equals("anneal")) {
            anneal = Double.parseDouble(args[1]);
        } else

        if (args[0].startsWith("explor")) {
            exploration = Double.parseDouble(args[1]);
        } else

        if (args[0].equals("maxgen")) {
            maxgenerations = Integer.parseInt(args[1]);
        } else

        if (args[0].equals("learnrate")) {
            normalneuralnet.setLearningRate(Double.parseDouble(args[1]));
        } else

        if (args[0].equals("det")) {
            useDeterministic = args[1].equals("1");
        } else

        if (args[0].equals("gnubg")) {
            askGnu = new AskGnuBG(args[1]);
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
                    save("autosave.ser");
                    debugout(1, ">> autosave");
                }
            }, sec, sec);
        } else

        {
            return false;
        }

        return true;
    }
}
