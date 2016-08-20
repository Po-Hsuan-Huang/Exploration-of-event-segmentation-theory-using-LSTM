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

import java.util.HashMap;
import java.util.Map.Entry;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.PhysicsParameters;
import diskworld.linalg2D.Point;

class CollisionAccu {

	private HashMap<Collision, Integer> collisions;
	private final boolean blocked;

	public CollisionAccu(Collision c, boolean blocked) {
		collisions = new HashMap<Collision, Integer>();
		this.blocked = blocked;
		add(c, 1);
	}

	public Point getCollisionPoint() {
		double sumx = 0.0;
		double sumy = 0.0;
		int num = 0;
		for (Collision c : collisions.keySet()) {
			Point p = c.getCollisionPoint();
			sumx += p.x;
			sumy += p.y;
			num++;
		}
		return new Point(sumx / num, sumy / num);
	}

	public void add(Collision c, int directionSign) {
		collisions.put(c, directionSign);
	}

	public void exchangeImpulse(Disk obj1, CollidableObject obj2, PhysicsParameters param) {
		double elasticity = obj2 instanceof Disk ?
				param.getDisk2DiskElasticty(obj1.getDiskType().getMaterial(), ((Disk) obj2).getDiskType().getMaterial()) :
				param.getDisk2WallElasticty(obj1.getDiskType().getMaterial());
		double impulses[] = new double[4];
		double sum[] = new double[4];
		double sumx = 0.0;
		double sumy = 0.0;
		int num = 0;
		for (Entry<Collision, Integer> e : collisions.entrySet()) {
			Collision c = e.getKey();
			int sign = e.getValue();
			c.computeCollisionImpulses(elasticity, impulses);
			sum[0] += sign * impulses[0];
			sum[1] += sign * impulses[1];
			if (sign == 1) {
				sum[2] += impulses[2];
				sum[3] += impulses[3];
			} else {
				sum[2] += impulses[3];
				sum[3] += impulses[2];
			}
			Point p = c.getCollisionPoint();
			sumx += p.x;
			sumy += p.y;
			num++;
		}
		exchangeCollisionImpulse(obj1, obj2, sumx / num, sumy / num, sum[0] / num, sum[1] / num, sum[2] / num, sum[3] / num);
	}

	public boolean isBlocked() {
		return blocked;
	}

	private void exchangeCollisionImpulse(Disk obj1, CollidableObject obj2, double pointx, double pointy, double impulsex, double impulsey, double angularImpulse1, double angularImpulse2) {
		DiskComplex dc1 = obj1.getDiskComplex();
		if (!dc1.isFixed())
			dc1.applyImpulse(impulsex, impulsey, angularImpulse1);
		double absImpulse = Math.sqrt(impulsex * impulsex + impulsey * impulsey);
		obj1.callEventHandler(obj2, pointx, pointy, absImpulse);
		if (obj2 instanceof Disk) {
			Disk d2 = (Disk) obj2;
			DiskComplex dc2 = d2.getDiskComplex();
			if (!dc2.isFixed()) {
				dc2.applyImpulse(-impulsex, -impulsey, angularImpulse2);
			}
			d2.callEventHandler(obj1, pointx, pointy, absImpulse);
		}
	}

}
