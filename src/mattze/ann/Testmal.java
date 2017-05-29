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
public class Testmal {
	public Testmal() {
		super();
	}

	private static void setupLayer(Layer layer, double offset) {
		layer.setWeight(0, 0, .1 + offset);
		layer.setWeight(0, 1, .2 + offset);
		layer.setWeight(1, 0, -.3 + offset);
		layer.setWeight(1, 1, .4 + offset);
	}

	public static void main(String[] args) {
		Layer layer1 = new SigmoidLayer(2, 2);
		Layer layer2 = new SigmoidLayer(2);
		MultiLayerNeuralNet nn = new MultiLayerNeuralNet();
		nn.setInputLayer(layer1);
		nn.addLayer(layer2);
		setupLayer(layer1, 0);
		setupLayer(layer2, -.3);
		layer1.debugOut();
		layer2.debugOut();

		double[] v = {1, .5};
		double[] t = {.3, .8};

		double[] r = nn.applyTo(v);

		nn.train(v, t);
		System.out.println();
		layer1.debugOut();
		System.out.println();
		layer2.debugOut();
	}


}
