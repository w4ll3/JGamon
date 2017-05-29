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

import java.io.Serializable;
import java.util.Random;

/**
 * Layer of a hidden-layer neural network
 *
 * @author Mattias Ulbrich
 * @version 2006-05-19
 */
public abstract class Layer implements Serializable {

	public final static long serialVersionUID = -8564117356623579199L;

	protected int in_size, out_size;
	private double[][] weights;
	private double[][] update;
	private double[] in;
	private double[] out;
	private double[] delta;
	private double[] top_target;

	private Layer topLayer;
	private Layer bottomLayer;

	// the learning rate
	private double eta = 0.7;

	private double momentum = 0.3;

	public Layer() {
		// for serialized creation
	}

	/**
	 * set up a new layer in a forward ann.
	 *
	 * @param parent parent layer
	 * @param size   number of neurons
	 */
	public Layer(int in_size, int out_size) {
		setup(in_size, out_size);
	}

	/**
	 * create a new layer. do not set it up completely, but
	 * leave it open for connectTo.
	 * the input size is deteremined in connectTo.
	 *
	 * @param out_size the out size.
	 */
	public Layer(int out_size) {
		this.out_size = out_size;
	}


	/**
	 * create a new Layer behind another layer.
	 *
	 * @param topLayer Layer that is in front of this
	 * @param out_size number of neurons
	 */
	public Layer(Layer topLayer, int out_size) {
		this(topLayer.out_size, out_size);
		this.topLayer = topLayer;
		topLayer.bottomLayer = this;
	}

	/* make the arrays */
	private void setup(int in_size, int out_size) {
		this.in_size = in_size;
		this.out_size = out_size;
		this.weights = new double[out_size][in_size + 1];
		this.update = new double[out_size][in_size + 1];
		this.out = new double[out_size];
		this.delta = new double[out_size];
		this.top_target = new double[in_size];
	}


	/**
	 * connect this layer to a top layer.
	 * This may not have yet a topLayer.
	 *
	 * @param topLayer Layer
	 */
	/*package*/ void connectTo(Layer topLayer) {
		if (this.topLayer != null)
			throw new IllegalStateException("There is already a topLayer!");

		this.topLayer = topLayer;
		topLayer.bottomLayer = this;
		in_size = topLayer.out_size;
		setup(in_size, out_size);
	}

	/**
	 * feed forward the given data.
	 * Add the weighted data and apply the activator function on it.
	 *
	 * @param data array of inputsize
	 * @return output array of outputsize
	 */
	public double[] forward(double[] data) {
		if (data.length != in_size) {
			throw new IllegalArgumentException("wrong input size!");
		}
		in = data;

		for (int i = 0; i < out_size; i++) {
			out[i] = 0;
			for (int j = 0; j < in_size; j++) {
				out[i] += weights[i][j] * in[j];
			}
			out[i] += weights[i][in_size]; // the bias
			out[i] = activation(out[i]);
		}

		if (bottomLayer != null) {
			return bottomLayer.forward(out);
		} else {
			return out;
		}
	}

	/**
	 * activation function.
	 * Here sigmoid
	 *
	 * @param d argument (arbitr.)
	 * @return double between 0 and 1
	 */
	protected abstract double activation(double v);


	/**
	 * set the weight of a perceptron.
	 *
	 * @param perceptron the perceptron
	 * @param input      the number of the input. or the bias if inputsize+1
	 * @param val        the value to set
	 */
	public void setWeight(int perceptron, int input, double val) {
		weights[perceptron][input] = val;
	}


	/**
	 * use the backpropagation method to set the delta values.
	 *
	 * @param target the value that should be here
	 * @return array of inputsize
	 */
	public void backward(double[] target) {
		for (int i = 0; i < delta.length; i++) {
			delta[i] = target[i] - out[i];
			// the diff of sigmoid!
			delta[i] *= activationDiff(out[i]);
		}

//        adjustWeights();

		if (topLayer != null) {
			for (int j = 0; j < in_size; j++) {
				top_target[j] = 0;
				for (int k = 0; k < out_size; k++) {
					top_target[j] += delta[k] * weights[k][j];
				}
			}
			topLayer.backward(top_target);
		}

		adjustWeights();
	}

	/**
	 * differentation of the activation function.
	 * <p>
	 * Let a be the activation function then:
	 * <p>
	 * calc a'(v)  in terms of a(v)
	 *
	 * @param a the result of a(v)
	 * @return a'(v)
	 */
	protected abstract double activationDiff(double a);

	/**
	 * After propagating the delta adjust the weights.
	 */
	private void adjustWeights() {

		for (int i = 0; i < out_size; i++) {
			for (int j = 0; j < in_size; j++) {
				weights[i][j] += momentum * update[i][j];
				update[i][j] = eta * delta[i] * in[j];
				weights[i][j] += update[i][j];
			}

			weights[i][in_size] += momentum * update[i][in_size];
			update[i][in_size] = eta * delta[i];
			weights[i][in_size] += update[i][in_size];
		}
	}

	/**
	 * set the learning rate to be used
	 *
	 * @param eta the new rate (0 < eta <= 1)
	 */
	public void setLearningRate(double eta) {
		this.eta = eta;
	}

	/**
	 * randomize the weights.
	 * <p>
	 * Use values normally distributed between around 0 with std dev range
	 *
	 * @param random the Random to use
	 * @param range  the standard deviation
	 */
	public void randomize(Random random, double range) {
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				weights[i][j] = random.nextGaussian() * range;
			}
		}
	}

	/**
	 * getBottomLayer
	 *
	 * @return Layer
	 */
	public Layer getBottomLayer() {
		return bottomLayer;
	}

	/**
	 * use this <i>setter</i> method to set the value of the field <code>momentum</code>.
	 *
	 * @param momentum new value to be set for <code>momentum</code>
	 */
	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}

	/**
	 * add gaussian noise to all the weights in this layer.
	 *
	 * @param r     PRG to use
	 * @param range std deviation
	 */
	public void addNoise(Random r, double range) {
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				weights[i][j] += r.nextGaussian() * range;
			}
		}
	}


	protected abstract Layer newInstance();

	/**
	 * deepclone also clones the bottomLayer!
	 *
	 * @return Layer
	 */
	public Layer deepclone() {
		Layer layer = newInstance();
		layer.weights = (double[][]) weights.clone();
		if (bottomLayer != null)
			layer.bottomLayer = bottomLayer.deepclone();

		return layer;
	}

	void debugOut() {
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				System.out.print(" " + weights[i][j]);
			}
			System.out.println();
		}
	}

}
