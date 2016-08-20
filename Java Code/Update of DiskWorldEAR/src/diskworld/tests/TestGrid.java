/*******************************************************************************
 *     DiskWorld - a simple 2D physics simulation environment, 
 *     Copyright (C) 2014  Jan Kneissler
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program in the file "License.txt".  
 *     If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package diskworld.tests;

import java.util.LinkedList;
import java.util.Random;

import diskworld.grid.Grid;
import diskworld.shape.CircleShape;
import diskworld.shape.RingSegmentShape;
import diskworld.shape.Shape;

/**
 * @author Simon Schlegel
 */

class TestGrid {

	static double sizex = 100;
	static double sizey = 50;
	static int numx = 30;
	static int numy = 20;

	static Grid grid = new Grid(100, 100, numx, numy);

	public static void main(String[] args) {
		double maxRadius = 20;
		Grid grid = new Grid(sizex, sizey, numx, numy);
		System.out.println("CircleShape");
		for (int i = 0; i < 200; i++) {
			Random rand = new Random(i);
			System.out.println("<" + i + "> ");
			Shape shape = new CircleShape(rand.nextDouble() * sizex, rand.nextDouble() * sizey, rand.nextDouble() * maxRadius);
			test(grid, shape);
		}
		System.out.println("RingSegmentShape");
		for (int i = 0; i < 50; i++) {
			Random rand = new Random(i);
			double r2 = rand.nextDouble() * maxRadius;
			double r1 = rand.nextDouble() * r2;
			Shape shape = new RingSegmentShape(rand.nextDouble() * sizex, rand.nextDouble() * sizey, r1, r2, rand.nextDouble() * 20 - 10, rand.nextDouble() * 2 * Math.PI);
			test(grid, shape);
		}
	}

	private static void test(Grid grid, Shape shape) {
		Random rand = new Random(0);
		boolean found[][] = new boolean[numx][numy];
		for (int i = 0; i < 10000000; i++) {
			double x = rand.nextDouble() * sizex;
			double y = rand.nextDouble() * sizey;
			if (shape.isInside(x, y)) {
				found[(int) (x / sizex * numx)][(int) (y / sizey * numy)] = true;
			}
		}
		LinkedList<int[]> res = grid.getCellIndicesIntersectingWithShape(shape);
		int num1 = res.size();
		int num2 = 0;
		for (boolean[] ba : found) {
			for (boolean b : ba) {
				if (b)
					num2++;
			}
		}
		if (num1 != num2) {
			System.out.println("Mismatch " + num1 + " != " + num2);
		}
		for (int[] i : res) {
			if (!found[i[0]][i[1]]) {
				System.out.println("Possibly a false positive: " + i[0] + "," + i[1] + " searching more...");
				for (int j = 0; (j < 1000000000) && !found[i[0]][i[1]]; j++) {
					double x = (i[0] + rand.nextDouble()) * sizex / numx;
					double y = (i[1] + rand.nextDouble()) * sizey / numy;
					if (shape.isInside(x, y)) {
						found[i[0]][i[1]] = true;
						num2++;
					}
				}
			}
			if (!found[i[0]][i[1]]) {
				System.out.println(shape);
				throw new RuntimeException("Very likely a false positive: " + i[0] + "," + i[1] + "!");
			}
			found[i[0]][i[1]] = false;
		}
		for (int i = 0; i < found.length; i++) {
			for (int j = 0; j < found[i].length; j++) {
				if (found[i][j]) {
					throw new RuntimeException("Definitely a false negative: " + i + "," + j);
				}
			}
		}
		if (num1 != num2) {
			throw new RuntimeException("Mismatch " + num1 + " != " + num2);
		}
		System.out.print("Ok(" + num1 + ") ");
	}
}
