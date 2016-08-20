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
import java.util.Vector;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actions.DiskModification;
import diskworld.collisions.Collision;

class TestCollisionDetector {

	private static final int NUM_TESTS = 5;
	private static final int NUM_TEST_STEPS = 10000;
	private static final int NUM_DISKS_MIN = 145;
	private static final int NUM_DISKS_MAX = 155;
	private static final double MAX_RADIUS = 0.05; // Floor has size 1x1 
	private static final double CELL_SIZE = 0.1; // Floor has size 1x1 
	private static final long RAND_SEED = 0;

	private static int[] testCollisions(Environment env) {
		long timeStart = System.currentTimeMillis();
		DiskType dt = new DiskType(DiskMaterial.METAL);
		Random rand = new Random(RAND_SEED);
		int res[] = new int[NUM_TEST_STEPS];
		int sumDisks = 0;
		int sumCollisions = 0;
		Vector<Disk> disks = new Vector<Disk>();
		for (int i = 0; i < NUM_TEST_STEPS; i++) {
			double rnd = rand.nextDouble();
			int numDisks = disks.size();
			sumDisks += numDisks;
			if ((rnd < 0.25) || (numDisks < NUM_DISKS_MIN)) {
				// add a new Disk 
				disks.add(env.newRootDisk(rand.nextDouble() * env.getMaxX(), rand.nextDouble() * env.getMaxY(), rand.nextDouble() * MAX_RADIUS, dt));
			} else {
				// choose a random disk
				Disk chosen = disks.get(rand.nextInt(numDisks));
				if ((rnd < 0.5) || (numDisks > NUM_DISKS_MAX)) {
					// remove it
					env.getDiskComplexesEnsemble().removeDiskComplex(chosen.getDiskComplex());
					disks.remove(chosen);
				} else if (rnd < 0.75) {
					// move it
					chosen.getDiskComplex().performModification(new DiskModification(chosen, rand.nextDouble() * env.getMaxX(), rand.nextDouble() * env.getMaxY()));
				} else {
					// change radius 
					chosen.getDiskComplex().performModification(new DiskModification(chosen, rand.nextDouble() * MAX_RADIUS));
				}
			}
			// count how many collisions
			res[i] = 0;
			for (DiskComplex dc : env.getDiskComplexes()) {
				LinkedList<Collision> collisions = env.getCollisionDetector().getCollisions(dc);
				res[i] += collisions == null ? 0 : collisions.size();
			}
			sumCollisions += res[i];
			//System.out.print(res[i] + ",");
		}
		long timeStop = System.currentTimeMillis();
		double timePerStep = ((double) (timeStop - timeStart) * 1000.0 / (double) NUM_TEST_STEPS);
		System.out.println("Avg. number of disks: " + (double) sumDisks / (double) NUM_TEST_STEPS + ", avg. number of collisions: " + (double) sumCollisions / (double) NUM_TEST_STEPS
				+ ", time per step [mu_s]: " + timePerStep);
		return res;
	}

	public static void main(String args[]) {
		System.out.println("Testing collision detector.");
		System.out.println("Number of Tests = " + NUM_TEST_STEPS);
		System.out.println("Size of cells (relative to floor size) = " + CELL_SIZE);
		System.out.println("Max. disk Radius (relative to floor size) = " + MAX_RADIUS);

		for (int i = 0; i < NUM_TESTS; i++) {
			System.out.println();
			System.out.println("Test #" + i + ":");
			System.out.println("Testing FullSearchCollisionDetector: ");
			Environment e1 = new Environment(1, 1, 1, null, CELL_SIZE, CELL_SIZE, false);
			int[] res1 = testCollisions(e1);
			System.out.println();
			System.out.println("Testing GridBasedCollisionDetector: ");
			Environment e2 = new Environment(1, 1, 1, null, CELL_SIZE, CELL_SIZE, true);
			int[] res2 = testCollisions(e2);
			if (equal(res1, res2)) {
				System.out.println("Test was successfull");
			} else {
				System.out.println("Test failed");
				System.exit(1);
			}
		}
	}

	private static boolean equal(int[] res1, int[] res2) {
		if (res1.length != res2.length) {
			return false;
		}
		for (int i = 0; i < res1.length; i++) {
			if (res1[i] != res2[i]) {
				return false;
			}
		}
		return true;
	}
}
