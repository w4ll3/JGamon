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
public class ProbRaceTraining extends Thread implements ConsoleApp {

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

    public ProbRaceTraining() throws Exception {
        super("Probability Race Neural Net Trainer");
        randomAi = new RandomAI();
        randomAi.init();
        boAI = new BearOffAI();
        boAI.init();
        detai = new InitialAI();
        detai.init();
    }

    public void run() {

        
        Random random = new Random();
        TrainingSetup lastSetup = new TrainingSetup();
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
                        debugout(3, "Win Chance: "
                                + raceAI.propabilityToWin(setup));
                        lastSetup.copyFrom(setup);

                        if (setup.getMaxPoint(setup.getPlayerAtMove()) <= 6)
                            setup.performMoves(boAI.makeMoves(setup));
                        else
                            setup.performMoves(raceAI.makeMoves(setup));

                        setup.switchPlayers();
                        
                        double[] mywin = { 0 };
                        if (setup.winstatus() != 0) {
                            mywin[0] = 1.;
                        } else {
                            double otherswin = raceAI.propabilityToWin(setup);
                            mywin[0] = 1 - otherswin;
                        }

                        train(lastSetup, mywin);

                        checkSuspendWish();
                    }
                }
                games++;
                debugout(3, setup);
                debugout(3, "is lost, prob: "+raceAI.propabilityToWin(setup));
                train(setup, new double[] {0});
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
                    / Math.pow(neuralnet.getTrainCount() / 1000. + 1., anneal);
            neuralnet.setLearningRate(rate);

            debugout(2, "Learning rate: " + rate);
            debugout(2, "Train " + neuralnet.applyTo(features)[0] + " -> "
                    + target[0]);
            neuralnet.train(features, target);
            debugout(2, "    -> " + neuralnet.applyTo(features)[0]);

            if (logWriter != null) {
                logWriter.println("" + neuralnet.applyTo(features)[0] + " "
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

    void eval(int number) {
        try {
        TrainingSetup setup = new TrainingSetup(startSetup);
        TrainingSetup t = new TrainingSetup();
        
        //
        // play till separated using the predefined player
        while (!setup.isSeparated()) {
            do {
                setup.roll();
            } while (setup.undecidedPlayer());
            setup.performMoves(detai.makeMoves(setup));
            setup.switchPlayers();
        }
        
        
        while(setup.winstatus() == 0) {
            int wincount = 0;
            for(int i=0; i <100; i++) {
                t.copyFrom(setup);
                wincount += rollout(t);
            }
            debugout(0, "" + (wincount/100.) + " "+ raceAI.propabilityToWin(setup));
            
            setup.roll();
            setup.performMoves(raceAI.makeMoves(setup));
            setup.switchPlayers();
        }
        }catch(CannotDecideException ex) {
            ex.printStackTrace(dbgWriter);
        }
    }   

    private int rollout(TrainingSetup trainingSetup)
            throws CannotDecideException {

        int initialplayer = trainingSetup.getPlayerAtMove();

        while (trainingSetup.winstatus() == 0) {          
//            trainingSetup.debugOut();
            trainingSetup.roll();
            if (trainingSetup.getMaxPoint(trainingSetup.getPlayerAtMove()) <= 6)
                trainingSetup.performMoves(boAI.makeMoves(trainingSetup));
            else
                trainingSetup.performMoves(raceAI.makeMoves(trainingSetup));

            trainingSetup.switchPlayers();
        }

        if (trainingSetup.winstatus() == initialplayer) {
            return 1;
        } else {
            return 0;
        }

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
        ProbRaceTraining t = new ProbRaceTraining();
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
        // New net! NORMAL
        SimpleNeuralNet neuralnet = new SimpleNeuralNet(
                RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES, 20, 1);
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

        if (command.startsWith("alpha ")) {
            alpha = Double.parseDouble(command.substring(6));
        } else

        if (args[0].equals("anneal")) {
            anneal = Double.parseDouble(args[1]);
        } else

        if (args[0].startsWith("explor")) {
            exploration = Double.parseDouble(args[1]);
        } else

        if (args[0].equals("learnrate")) {
            neuralnet.setLearningRate(Double.parseDouble(args[1]));
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
                    save("probraceautosave.ser");
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
