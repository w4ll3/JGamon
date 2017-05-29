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
 * Hidden-layer-feed-forward neural network with
 * backpropagation.
 *
 * @author Mattias Ulbrich
 * @version 2006-05-19
 */
public class MultiLayerNeuralNet implements Serializable, NeuralNet {

	// the topmost layer
	private Layer inLayer;

	// the one at the bottom
	private Layer outLayer;

	public MultiLayerNeuralNet() {
		// for serialisation
	}

	/**
	 * apply the net on a vector of doubles.
	 *
	 * @param value parameters, length must be input length of the input layer
	 * @return the result of the net, length is the output length of the output
	 * layer.
	 */
	public double[] applyTo(double[] value) {
		return inLayer.forward(value);
	}

	/**
	 * set the topMost layer.
	 * <p>
	 * This layer must be completely initialized (use the (int,int)-constructor).
	 *
	 * @param layer Layer to be set as topLayer
	 * @throws IllegalStateException if there is already an input layer.
	 */
	public void setInputLayer(Layer layer) {
		if (inLayer != null) {
			throw new IllegalStateException("InputLayer already set");
		}

		outLayer = inLayer = layer;
	}

	/**
	 * add a layer to the end of the chain.
	 * The layer must not be completely initialized yet.
	 *
	 * @param layer Layer to be added.
	 * @throws IllegalStateException if it has already been connected or
	 *                               finished.
	 */
	public void addLayer(Layer layer) throws IllegalStateException {
		layer.connectTo(outLayer);
		outLayer = layer;
	}

	public void train(double[] data, double target[]) {
		inLayer.forward(data);
		outLayer.backward(target);
	}


	/**
	 * get the square of the euclidean distance between the net's result and the target.
	 *
	 * @param data   double[]
	 * @param target double[]
	 * @return double
	 */
	public double error(double[] data, double target[]) {
		double[] is = inLayer.forward(data);
		double mse = 0;
		for (int i = 0; i < is.length; i++) {
			mse += (is[i] - target[i]) * (is[i] - target[i]);
		}
		return mse;
	}

	public void setLearningRate(double v) {
		Layer lay = inLayer;
		while (lay != null) {
			lay.setLearningRate(v);
			lay = lay.getBottomLayer();
		}
	}

	public void randomize(double range) {
		Random r = new Random();
		Layer lay = inLayer;
		while (lay != null) {
			lay.randomize(r, range);
			lay = lay.getBottomLayer();
		}
	}


	public Object clone() {
		MultiLayerNeuralNet nn = new MultiLayerNeuralNet();
		nn.inLayer = inLayer.deepclone();
		Layer lay = nn.inLayer;
		while (lay != null) {
			nn.outLayer = lay;
			lay = lay.getBottomLayer();
		}
		return nn;
	}

	/**
	 * add gaussian noise to each weight
	 *
	 * @param range the standard deviation of the gaussian noise.
	 */
	public void addNoise(double range) {
		Random r = new Random();
		Layer lay = inLayer;
		while (lay != null) {
			lay.addNoise(r, range);
			lay = lay.getBottomLayer();
		}

	}


	/**
	 * get a layer from this network
	 *
	 * @param no number of the layer
	 */
	public Layer getLayer(int no) {
		Layer ret = inLayer;
		for (int i = 0; i < no; i++) {
			ret = inLayer.getBottomLayer();
		}

		return ret;
	}

	/**
	 * Delegate method void setMomentum(double momentum) to all layers
	 *
	 * @param momentum double
	 */
	public void setMomentum(double momentum) {
		Layer lay = inLayer;
		while (lay != null) {
			lay.setMomentum(momentum);
			lay = lay.getBottomLayer();
		}
	}

}
