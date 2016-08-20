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
package diskworld.collisions;

import java.util.LinkedList;
import java.util.List;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.DiskMaterial;
import diskworld.environment.Epsilons;
import diskworld.interfaces.PhysicsParameters;
import diskworld.linalg2D.Point;

class MergingDetector {

	private List<Collision> collisions;
	private Collision best1, best2;
	private double bestRatio;
	private final PhysicsParameters physicsParameters;

	public MergingDetector(DiskComplex dc1, DiskComplex dc2, Collision c, PhysicsParameters physicsParameters) {
		collisions = new LinkedList<Collision>();
		collisions.add(c);
		bestRatio = 0.0;
		best1 = null;
		best2 = null;
		this.physicsParameters = physicsParameters;
	}

	public void add(Collision c) {
		for (Collision c2 : collisions) {
			double ratio = getGripRatio(c, c2);
			if (ratio > bestRatio) {
				bestRatio = ratio;
				best1 = c;
				best2 = c2;
			}
		}
		collisions.add(c);
	}

	private double getGripRatio(Collision c1, Collision c2) {
		Point p1 = c1.getCollisionPoint();
		Point p2 = c2.getCollisionPoint();
		double deltax = p2.getX() - p1.getX();
		double deltay = p2.getY() - p1.getY();
		double ratio1 = getRatio(deltax, deltay, c1);
		double ratio2 = getRatio(deltax, deltay, c2);
		return Math.max(ratio1, ratio2);
	}

	private double getRatio(double deltax, double deltay, Collision c) {
		double dirx = c.getDirX();
		double diry = c.getDirY();
		double sin = deltax * dirx + deltay * diry;
		double cos = -deltax * diry + deltay * dirx;
		DiskMaterial diskMaterial1 = c.getObj1().getDiskType().getMaterial();
		DiskMaterial diskMaterial2 = ((Disk) c.getObj2()).getDiskType().getMaterial();
		double gripCoefficient = physicsParameters.getDisk2DiskGripCoefficient(diskMaterial1, diskMaterial2);
		if (Math.abs(cos) < Epsilons.EPSILON_GRIP) {
			cos = Epsilons.EPSILON_GRIP;
		}
		return Math.abs(gripCoefficient * sin / cos);
	}

	public boolean decideMerge() {
		return (bestRatio >= 1.0);
	}

	public void performMerge(DiskComplex newdc1, DiskComplex newdc2) {
		newdc1.mergeTransiently(newdc2, best1.getObj1(), (Disk) best1.getObj2(), best2.getObj1(), (Disk) best2.getObj2());
	}

}
