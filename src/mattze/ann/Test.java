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
import java.util.Random;

/**
 * the xor example
 *
 * @author Mattias Ulbrich
 * @version 2006-05-10
 */
class Test {

	private static double[][] traindata = {{1, 0}, {0, 1}, {1, 1}, {0, 0}
	};
	// Targets are: xor, and, or
	private static double[][] target = {{1, 0, 1}, {1, 0, 1}, {0, 1, 1}, {0, 0, 0}
	};


	public static void main(String[] args) throws IOException {

		SimpleNeuralNet nn = new SimpleNeuralNet(2, 3, 3);

		for (int i = 0; i < 4; i++) {
//            traindata[i][0] = Layer.sigmoid(traindata[i][0]);
//            traindata[i][1] = Layer.sigmoid(traindata[i][1]);
		}

		nn.randomize(5);
		nn.setLearningRate(.8);

		Random r = new Random();

		for (int i = 0; i < 1000000; i++) {
			int j = r.nextInt(4);
			nn.train(traindata[j], target[j]);
			if (i % 10000 == 0) {
				double mse = 0;
				for (int k = 0; k < 4; k++) {
					mse += mse(nn.applyTo(traindata[k]), target[k]);
				}
				System.out.println(mse);
			}
		}

		double mse = 0;
		for (int i = 0; i < 4; i++) {
			mse += mse(nn.applyTo(traindata[i]), target[i]);
		}

		System.out.println(mse);

		for (int i = 0; i < 3; i++) {
			System.out.println();
			System.out.println(nn.applyTo(traindata[0])[i]);
			System.out.println(nn.applyTo(traindata[1])[i]);
			System.out.println(nn.applyTo(traindata[2])[i]);
			System.out.println(nn.applyTo(traindata[3])[i]);
		}
	}

	static public double mse(double[] v1, double[] v2) {
		double mse = 0;
		for (int i = 0; i < v2.length; i++) {
			mse += (v1[i] - v2[i]) * (v1[i] - v2[i]);
		}
		return mse;
	}
}
