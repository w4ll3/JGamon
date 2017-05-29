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
public class LinearLayer extends Layer {
	public LinearLayer() {
	}

	public LinearLayer(int in_size, int out_size) {
		super(in_size, out_size);
	}

	public LinearLayer(int out_size) {
		super(out_size);
	}

	/**
	 * activation function.
	 * <p>
	 * Identity
	 *
	 * @param v argument (arbitr.)
	 * @return double v itsself
	 */
	protected double activation(double v) {
		return v;
	}

	/**
	 * differentation of the activation function.
	 * <p>
	 * which is exactly 1, always ...
	 *
	 * @param a the result of a(v)
	 * @return a'(v)
	 */
	protected double activationDiff(double a) {
		return 1.;
	}

	/**
	 * newInstance. Do not copy the weights.
	 *
	 * @return Layer
	 */
	protected Layer newInstance() {
		return new LinearLayer(in_size, out_size);
	}
}
