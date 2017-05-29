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

public interface NeuralNet {
	/**
	 * apply the net on a vector of doubles.
	 *
	 * @param value parameters, length must be input length of the input layer
	 * @return the result of the net, length is the output length of the output
	 * layer.
	 */
	public double[] applyTo(double[] value);

	/**
	 * get the square of the euclidean distance between the net's result and the target.
	 *
	 * @param data   double[]
	 * @param target double[]
	 * @return double
	 */
	public double error(double[] data, double[] target);

	public void train(double[] data, double[] target);

	/**
	 * set the learning rate for the following trainings
	 *
	 * @param rate double
	 */
	public void setLearningRate(double rate);
}
