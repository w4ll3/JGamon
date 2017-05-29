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

import java.io.PrintWriter;
import jgam.ai.*;
import mattze.ann.*;

import java.text.*;
import java.util.*;
import jgam.game.*;
import java.io.*;

public class RaceTrainer extends Thread implements ConsoleApp {

    private static final DecimalFormat nf = new DecimalFormat("#0.00");

    boolean debug = true;

    double alpha = .2;

    double anneal = .3;

    boolean suspendWish = false;

    int gameNo = 0;

    public PrintWriter err = new PrintWriter(System.err);

    public PrintWriter log = null;

    public BearOffAI bearoffAi = new BearOffAI();

    public RaceNeuralNetAI ai = new RaceNeuralNetAI(bearoffAi);

    public AI deterAi = new GnuBgAI(); //  new InitialAI();

    Timer timer;

    public RaceTrainer() throws Exception {
        super("Neural net RaceTrainer");
        bearoffAi.init();
        deterAi.init();
        // prevent from loading non-existant file!
        ai.setNeuralNet(new SimpleNeuralNet(0, 0, 0));
        ai.init();
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to
     * create a thread, starting the thread causes the object's <code>run</code>
     * method to be called in that separately executing thread.
     */
    public void run() {

        try {

            while (true) {
                TrainingSetup s = new TrainingSetup();
                while (!s.isSeparated()) {
                    s.roll();
                    s.performMoves(deterAi.makeMoves(s));
                    s.switchPlayers();
                }

                // player1 first
                apply(1, s);
                apply(2, s);

                checkSuspendWish();
                if (debug) {
                    err.println("Game#: " + (gameNo++));
                }
            }

        } catch (Exception ex) {
            err.println("Thread stopped!");
            ex.printStackTrace(err);
        }
    }

    double[] features = new double[RaceNeuralNetAI.NUMBER_OF_RACE_FEATURES];

    public void apply(int player, BoardSetup s) throws CannotDecideException {
        LinkedList moveList = new LinkedList();
        TrainingSetup s2 = new TrainingSetup(s);
        s2.setPlayerAtMove(player);

        while (CombiAI.getCategory(s2, player) == CombiAI.RACE) {
            moveList.addLast(new BoardSnapshot(s2));
            s2.roll();
            SingleMove[] moves = ai.makeMoves(s2);
            s2.performMoves(moves);
            // s2.debugOut();
        }

        double val = bearoffAi.getEstimatedMoves(s2, player);

        while (!moveList.isEmpty()) {
            BoardSetup bs = (BoardSetup) moveList.removeLast();
            ai.extractFeatures(bs, features, player);
            double[] target = { (++val) / 30. };
            SimpleNeuralNet nn = (SimpleNeuralNet) ai.getNeuralNet();
            double rate = alpha
                    / Math.pow(nn.getTrainCount() / 1000. + 1, anneal);

            if (debug) {
                err.println();
                bs.debugOut(err);
                err.println(" Value: " + ai.useNeuralNet(features) * 30);
                err.println(" Target: " + target[0] * 30);
                err.println(" Rate: " + rate);
            }
            if (log != null) {
                log
                        .println(("" + ai.useNeuralNet(features) * 30 + "\t" + target[0] * 30));
            }

            nn.train(features, target);

        }
    }

    public void newNet() {
        ai.newNeuralNet();
    }

    public void setNet(NeuralNet n) {
        ai.setNeuralNet(n);
    }

    synchronized void checkSuspendWish() {
        try {
            if (suspendWish) {
                wait();
                suspendWish = false;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace(err);
        }
    }

    void eval() {
        try {
            TrainingSetup setup = new TrainingSetup();

            //
            // play till separated using the predefined player
            while (!setup.isSeparated()) {
                do {
                    setup.roll();
                } while (setup.undecidedPlayer());
                setup.performMoves(deterAi.makeMoves(setup));
                setup.switchPlayers();
            }

            double errorProb = 0;
            double errorThrows = 0;
            int cnt = 0;
            while (setup.winstatus() == 0) {
                double roll[] = rollout(setup, 50);
                double net = ai.propabilityToWin(setup);
                double netthrow = ai.getEstimatedMoves(setup, setup
                        .getPlayerAtMove());

                err.println(nf.format(roll[0]) + " " + nf.format(net) + " "
                        + nf.format(roll[1]) + " " + nf.format(netthrow));
                errorProb += (roll[0] - net) * (roll[0] - net);
                errorThrows += (roll[1] - netthrow) * (roll[1] - netthrow);
                cnt++;
                setup.roll();
                setup.performMoves(ai.makeMoves(setup));
                setup.switchPlayers();
            }

            err.println("MSE: " + nf.format(errorProb / cnt) + "     "
                    + nf.format(errorThrows / cnt));

        } catch (CannotDecideException ex) {
            ex.printStackTrace(err);
        }
    }

    private double[] rollout(TrainingSetup trainingSetup, int rounds)
            throws CannotDecideException {

        TrainingSetup t = new TrainingSetup();
        double wincount = 0;
        double throwscount = 0;
        int i;
        for (i = 0; i < rounds; i++) {
            t.copyFrom(trainingSetup);
            int initialplayer = t.getPlayerAtMove();
            while (t.winstatus() == 0) {
                // t.debugOut();
                t.roll();
                if (t.getMaxPoint(t.getPlayerAtMove()) <= 6)
                    t.performMoves(bearoffAi.makeMoves(t));
                else
                    t.performMoves(ai.makeMoves(t));

                if (t.getActivePlayer() == initialplayer)
                    throwscount++;

                t.switchPlayers();
            }

            if (t.winstatus() == initialplayer) {
                wincount++;
            }

            while (t.getMaxPoint(initialplayer) > 0) {
                t.roll();
                if (t.getMaxPoint(t.getPlayerAtMove()) <= 6)
                    t.performMoves(bearoffAi.makeMoves(t));
                else
                    t.performMoves(ai.makeMoves(t));

                throwscount++;

                t.switchPlayers();

            }

        }

        return new double[] { wincount / i, throwscount / i };

    }

    public void log(String s) {
        if (debug) {
            err.println(s);
        }
    }

    /**
     * to commands ...
     * 
     * @param comm String to be parsed
     * @return true if successfull else false
     * @throws Exception which is caught somewhere
     * @todo Implement this ConsoleApp method
     */
    public boolean command(String comm) throws Exception {
        String args[] = comm.split(" +", 2);

        if (comm.equals("exit") || comm.equals("quit")) {
            System.exit(0);
        } else

        if (comm.equals("newnet")) {
            newNet();
        } else

        if (args[0].equals("loadnet")) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                    args[1]));
            setNet((NeuralNet) ois.readObject());
            ois.close();
        } else

        if (args[0].equals("savenet")) {
            ObjectOutputStream ois = new ObjectOutputStream(
                    new FileOutputStream(args[1]));
            ois.writeObject(ai.getNeuralNet());
        } else

        if (comm.equals("dbg")) {
            debug = true;
        } else

        if (comm.equals("nodbg")) {
            debug = false;
        } else

        if (args[0].equals("log")) {
            if (log != null) {
                log.close();
            }

            if (args.length > 1) {
                log = new PrintWriter(new FileWriter(args[1]));
            } else {
                log = null;
            }
        } else

        if (comm.startsWith("anneal ")) {
            anneal = Double.parseDouble(comm.substring(6));
        } else

        if (comm.startsWith("alpha ")) {
            alpha = Double.parseDouble(comm.substring(6));
        } else

        if (comm.equals("suspend")) {
            suspendWish = true;
        } else

        if (comm.equals("resume")) {
            synchronized (this) {
                notify();
            }
        } else

        if (comm.equals("eval")) {
            new Thread() {
                public void run() {
                    eval();
                }
            }.start();
        } else

        if (args[0].equals("autosave")) {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            int sec = Integer.parseInt(args[1]) * 60000;
            timer.schedule(new TimerTask() {
                public void run() {
                    try {
                        ObjectOutputStream ois = new ObjectOutputStream(
                                new FileOutputStream("race.autosave.ser"));
                        ois.writeObject(ai.getNeuralNet());
                        err.println(">> autosave " + new Date());
                    } catch (Exception ex) {
                        ex.printStackTrace(err);
                    }
                }
            }, sec, sec);
        } else

        if (comm.equals("info")) {
            err.println("Alpha: " + alpha);
            err.println("Anneal: " + anneal);
            err.println("#Trained: "
                    + ((SimpleNeuralNet) ai.getNeuralNet()).getTrainCount());
        } else

        if (comm.equals("run")) {
            if (isAlive()) {
                err.println("thread already running");
            } else {
                start();
            }
        } else

        {
            return false;
        }

        return true;

    }

    /**
     * setLog
     */
    public void setLog(PrintWriter writer) {
        err = writer;
    }

    /**
     * commandline
     */
    private void commandline() throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        err = new PrintWriter(System.out, true);
        err.println("Reinforcement JGAMMON Trainer RACE AI");
        while (true) {
            err.flush();
            String c = br.readLine();
            try {
                if (command(c)) {
                    err.println(" > " + c);
                } else {
                    err.println("unknown: " + c);
                }
            } catch (Exception ex) {
                ex.printStackTrace(err);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RaceTrainer t = new RaceTrainer();
        if (args.length > 0) {
            new ConsoleFrame(t).setVisible(true);
        } else {
            t.commandline();
        }
    }

}
