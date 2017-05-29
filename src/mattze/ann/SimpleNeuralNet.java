/*
 * JGammon: A backgammon client written in Java
 * Copyright (C) 2005/06 Mattias Ulbrich
 *
 * JGammon includes: - playing over network
 *                   - plugin mechanism for graphical board implementations
 *                   - artificial intelligence player
 *                   - plugin mechanism for AI players
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package mattze.ann;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

/**
 * <p>Title: JGam - Java Backgammon</p>
 * <p>
 * <p>Description: </p>
 * <p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class SimpleNeuralNet implements NeuralNet, Serializable {

	private int insize, outsize, hiddensize;

	private double[][] hiddenweights;
	private double[][] outweights;
	private double[] hiddenRegister;
	private double[] outRegister;
	private double[] outError = new double[outsize];
	private double[] hiddenError = new double[hiddensize];
	private double learnrate = .5;
	private int numberOfTrained;
	private static Random random = new Random();

	public SimpleNeuralNet(int insize, int hiddensize, int outsize) {
		this.insize = insize;
		this.outsize = outsize;
		this.hiddensize = hiddensize;

		hiddenweights = new double[hiddensize][insize + 1];
		outweights = new double[outsize][hiddensize + 1];
		hiddenRegister = new double[hiddensize];
		outRegister = new double[outsize];
		outError = new double[outsize];
		hiddenError = new double[hiddensize];
	}

	/**
	 * apply the net on a vector of doubles.
	 *
	 * @param data parameters, length must be input length of the input
	 *             layer
	 * @return the result of the net, length is the output length of the
	 * output layer.
	 */
	public double[] applyTo(double[] data) {
		return applyTo(data, new double[outsize]);
	}


	public synchronized double[] applyTo(double[] data, double[] target) {
		double sum;

		for (int j = 0; j < hiddensize; j++) {
			sum = hiddenweights[j][insize];

			// Calculate the output value
			for (int i = 0; i < insize; i++) {
				sum += hiddenweights[j][i] * data[i];
			}

			hiddenRegister[j] = sigmoid(sum);
		}

		for (int k = 0; k < outsize; k++) {
			sum = outweights[k][outsize];

			// Calculate the output value

			for (int j = 0; j < hiddensize; j++) {
				sum += outweights[k][j] * hiddenRegister[j];
			}

			target[k] = sigmoid(sum);
		}

		return target;
	}

	/**
	 * sigmoid
	 *
	 * @param sum double
	 * @return double
	 */
	private static double sigmoid(double val) {
		return 1 / (1 + Math.exp(-val));
	}

	/**
	 * get the square of the euclidean distance between the net's result and
	 * the target.
	 *
	 * @param data   double[]
	 * @param target double[]
	 * @return double
	 * @todo Implement this mattze.ann.NeuralNet method
	 */
	public synchronized double error(double[] data, double[] target) {
		return 0.0;
	}

	/**
	 * train
	 *
	 * @param data   double[]
	 * @param target double[]
	 * @todo Implement this mattze.ann.NeuralNet method
	 */
	public synchronized void train(double[] data, double[] target) {

		applyTo(data, outRegister);

        /* Calculate error at output nodes */
		for (int i = 0; i < outsize; i++) {
			outError[i] = (target[i] - outRegister[i]) * outRegister[i] * (1 - outRegister[i]);
		}

        /* Calculate error at hidden nodes */
		for (int i = 0; i < hiddensize; i++) {
			hiddenError[i] = 0.0;
		}

		for (int i = 0; i < outsize; i++) {
			for (int j = 0; j < hiddensize; j++) {
				hiddenError[j] += outError[i] * outweights[i][j];
			}
		}

		for (int i = 0; i < hiddensize; i++) {
			hiddenError[i] *= hiddenRegister[i] * (1 - hiddenRegister[i]);
		}

        /* Adjust weights at output nodes */
		for (int i = 0; i < outsize; i++) {
			for (int j = 0; j < hiddensize; j++) {
				outweights[i][j] += learnrate * outError[i] * hiddenRegister[j];
			}
			outweights[i][hiddensize] += learnrate * outError[i] /* * 1 */;
		}

        /* Adjust weights at hidden nodes */
		for (int i = 0; i < insize; i++) {
			for (int j = 0; j < hiddensize; j++) {
				hiddenweights[j][i] += learnrate * hiddenError[j] * data[i];
			}
		}

		for (int i = 0; i < hiddensize; i++) {
			hiddenweights[i][insize] += learnrate * hiddenError[i];
		}

		numberOfTrained++;

	}

	/**
	 * write random values into the weight arrays.
	 *
	 * @param range the std deviation for the normal distribution
	 */
	public synchronized void randomize(double range) {

		for (int i = 0; i < outsize; i++) {
			for (int j = 0; j <= hiddensize; j++) {
				outweights[i][j] = random.nextGaussian() * range;
			}
		}

		for (int i = 0; i < hiddensize; i++) {
			for (int j = 0; j <= insize; j++) {
				hiddenweights[i][j] = random.nextGaussian() * range;
			}
		}

	}

	/**
	 * setLearningRate
	 *
	 * @param d double
	 */
	public void setLearningRate(double d) {
		learnrate = d;
	}

	public int getTrainCount() {
		return numberOfTrained;
	}

	// I/O stuff
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeInt(insize);
		out.writeInt(hiddensize);
		out.writeInt(outsize);
		out.writeObject(hiddenweights);
		out.writeObject(outweights);
		out.writeInt(numberOfTrained);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		insize = in.readInt();
		hiddensize = in.readInt();
		outsize = in.readInt();
		hiddenweights = (double[][]) in.readObject();
		outweights = (double[][]) in.readObject();
		numberOfTrained = in.readInt();

		outRegister = new double[outsize];
		outError = new double[outsize];
		hiddenError = new double[hiddensize];
		hiddenRegister = new double[hiddensize];
	}


}
