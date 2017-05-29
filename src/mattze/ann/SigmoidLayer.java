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

/**
 * A Layer for the ann, using the sigmoid function which is
 * <pre>
 *                    1
 *   sigmoid(v) = ----------
 *                 1+exp(-v)
 *
 *
 *   sigmoid'(v) = sigmoid(v) * (1-sigmoid(v))
 *
 * </pre>
 *
 * @author Mattias Ulbrich
 */
public class SigmoidLayer extends Layer {
	public SigmoidLayer() {
		super();
	}

	public SigmoidLayer(int in_size, int out_size) {
		super(in_size, out_size);
	}

	public SigmoidLayer(int out_size) {
		super(out_size);
	}

	/**
	 * activation function.
	 *
	 * @param v argument (arbitr.)
	 * @return double between 0 and 1
	 */
	protected double activation(double v) {
		return 1. / (Math.exp(-v) + 1.);
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
	protected double activationDiff(double a) {
		double v = a * (1 - a);
		return v;
	}

	protected Layer newInstance() {
		return new SigmoidLayer(in_size, out_size);
	}
}
