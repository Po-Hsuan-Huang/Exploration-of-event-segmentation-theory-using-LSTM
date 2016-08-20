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

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.environment.Epsilons;
import diskworld.environment.Wall;
import diskworld.interfaces.CollidableObject;
import diskworld.linalg2D.Point;

public class Collision {
	private final Disk obj1;
	private final CollidableObject obj2; // disk or wall
	private final double collisionPointx, collisionPointy; // touching point 
	private final double rx1, ry1; // distance from object 1 center to collision point 
	private final double rx2, ry2; // distance from object 1 center to collision point 
	private final double directionx, directiony; // direction from disk1 to disk2, or along the normal in case of wall, length 1

	/**
	 * Constructor for a Collision-Object
	 * 
	 * @param x1
	 *            x-coordinate
	 * @param y1
	 *            y-coordinate
	 * @param dx
	 *            distance in x-direction
	 * @param dy
	 *            distance in y-direction
	 * @param obj1
	 *            Disk colliding
	 * @param obj2
	 *            disk or wall obj1 is colliding with
	 * @param factorCollisionPoint
	 *            set between 0.0 and 1.0, defines the collision point along the distance vector
	 */
	private Collision(double x1, double y1, double dx, double dy, Disk obj1, CollidableObject obj2, double factorCollisionPoint) {
		this.obj1 = obj1;
		this.obj2 = obj2;
		collisionPointx = x1 + dx * factorCollisionPoint;
		collisionPointy = y1 + dy * factorCollisionPoint;
		DiskComplex dc1 = obj1.getDiskComplex();
		rx1 = collisionPointx - dc1.getCenterx();
		ry1 = collisionPointy - dc1.getCentery();
		if (obj2 instanceof Disk) {
			DiskComplex dc2 = ((Disk) obj2).getDiskComplex();
			rx2 = collisionPointx - dc2.getCenterx();
			ry2 = collisionPointy - dc2.getCentery();
		} else {
			rx2 = 0.0;
			ry2 = 0.0;
		}
		double f = 1 / Math.sqrt(dx * dx + dy * dy);
		directionx = f * dx;
		directiony = f * dy;
	}

	/**
	 * Constructor for Collision, calculating the factorCollisionPoint from radii and distances from the coordinates
	 * 
	 * @param disk1
	 *            colliding disk
	 * @param x1
	 *            x-position disk1
	 * @param y1
	 *            y-position disk1
	 * @param r1
	 *            radius disk1
	 * @param obj2
	 *            disk or wall disk1 collides with (has to have radius - be round)
	 * @param x2
	 *            x-position obj2
	 * @param y2
	 *            y-position obj2
	 * @param r2
	 *            radius obj2
	 */
	private Collision(Disk disk1, double x1, double y1, double r1, CollidableObject obj2, double x2, double y2, double r2) {
		this(x1, y1, x2 - x1, y2 - y1, disk1, obj2, r1 / (r1 + r2));
	}

	/**
	 * Collision of two disks
	 * 
	 * @param disk1
	 * @param disk2
	 * @return Collision-Object if intersecting, null otherwise
	 */
	public static Collision diskCollision(Disk disk1, Disk disk2) {
		return disk1.getZLevel() == disk2.getZLevel() ? roundObjectCollision(disk1, disk1.getX(), disk1.getY(), disk1.getRadius(), disk2, disk2.getX(), disk2.getY(), disk2.getRadius()) : null;
	}

	/**
	 * Calulates Collison for round objects
	 * 
	 * @param disk1
	 *            colliding disk
	 * @param x1
	 *            x-position disk1
	 * @param y1
	 *            y-position disk1
	 * @param r1
	 *            radius disk1
	 * @param obj2
	 *            disk or wall disk1 collides with (has to have radius - be round)
	 * @param x2
	 *            x-position obj2
	 * @param y2
	 *            y-position obj2
	 * @param r2
	 *            radius obj2
	 * @return Collision-Object if intersecting, null otherwise
	 */
	public static Collision roundObjectCollision(Disk disk1, double x1, double y1, double r1, CollidableObject obj2, double x2, double y2, double r2) {
		if (Collision.intersecting(x1, y1, r1, x2, y2, r2))
			return new Collision(disk1, x1, y1, r1, obj2, x2, y2, r2);
		else
			return null;
	}

	/**
	 * Calls {@link #roundObjectCollision(Disk, double, double, double, CollidableObject, double, double, double)} reading the parameters from disk1
	 * 
	 * @return Collision-Object if intersecting, null otherwise
	 */
	private static Collision roundObjectCollision(Disk disk1, CollidableObject obj2, double x2, double y2, double r2) {
		return roundObjectCollision(disk1, disk1.getX(), disk1.getY(), disk1.getRadius(), obj2, x2, y2, r2);
	}

	/**
	 * Calculates Collision disk to wall
	 * 
	 * @param disk
	 * @param wall
	 * @return Collision-Object if intersecting, null otherwise
	 */
	public static Collision wallCollision(Disk disk, Wall wall) {
		Collision res;
		double dx = wall.getHalfThicknessX();
		double dy = wall.getHalfThicknessY();
		for (int i = 0; i < 2; i++) {
			res = lineCollision(disk, wall, wall.getX1() + dx, wall.getY1() + dy, wall.getX2() + dx, wall.getY2() + dy);
			if (res != null)
				return res;
			dx *= -1;
			dy *= -1;
		}
		double d = wall.getHalfThickness();
		res = roundObjectCollision(disk, wall, wall.getX1(), wall.getY1(), d);
		if (res != null)
			return res;
		res = roundObjectCollision(disk, wall, wall.getX2(), wall.getY2(), d);
		return res;
	}

	//	public void setEgoMotion(double egoMotionRelativeSpeed) {
	//		relativeSpeedByEgoMotion = egoMotionRelativeSpeed;
	//	}

	/**
	 * Calculates Collision disk to wall (line piece)
	 * 
	 * @return Collision-Object if intersecting, null otherwise
	 */
	private static Collision lineCollision(Disk disk, Wall wall, double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double vx = disk.getX() - x1;
		double vy = disk.getY() - y1;
		double proj = vx * dx + vy * dy;
		double f = proj / (dx * dx + dy * dy);
		if ((f < 0.0) || (f > 1.0))
			return null;
		double ox = -vx + dx * f;
		double oy = -vy + dy * f;
		double r = disk.getRadius() - Epsilons.EPSILON_WALL_COLLISION;
		if (ox * ox + oy * oy <= r * r) {
			return new Collision(disk.getX(), disk.getY(), ox, oy, disk, wall, 1.0);
		}
		return null;
	}

	public Point getCollisionPoint() {
		return new Point(collisionPointx, collisionPointy);
	}

	public Disk getObj1() {
		return obj1;
	}

	public CollidableObject getObj2() {
		return obj2;
	}

	public double getProjection(double dx, double dy) {
		return dx * directionx + dy * directiony;
	}

	public static boolean intersecting(double x1, double y1, double r1, double x2, double y2, double r2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double sr = r1 + r2 - Epsilons.EPSILON_DISK_INTERSECT;
		return dx * dx + dy * dy <= sr * sr;
	}

	public double getDirX() {
		return directionx;
	}

	public double getDirY() {
		return directiony;
	}

	public void computeCollisionImpulses(double elasticity, double impulses[]) {
		DiskComplex dc1 = obj1.getDiskComplex();
		double v1x = dc1.getSpeedxRelative(ry1) + obj1.getEgoMotionx();
		double v1y = dc1.getSpeedyRelative(rx1) + obj1.getEgoMotiony();
		double v2x, v2y;
		Disk d2;
		DiskComplex dc2;
		if (obj2 instanceof Disk) {
			d2 = (Disk) obj2;
			dc2 = d2.getDiskComplex();
			v2x = dc2.getSpeedxRelative(ry2) + d2.getEgoMotionx();
			v2y = dc2.getSpeedyRelative(rx1) + d2.getEgoMotiony();
		} else {
			d2 = null;
			dc2 = null;
			v2x = 0;
			v2y = 0;
		}
		double projspeed1 = v1x * directionx + v1y * directiony;
		double projspeed2 = v2x * directionx + v2y * directiony;
		double speedDiff = projspeed1 - projspeed2;
		if (speedDiff > Epsilons.EPSILON_SPEED_DIFF) {
			double change1, change2;
			change1 = dc1.getProjectedSpeedChangeByImpulseRelative(rx1, ry1, directionx, directiony);
			change2 = dc2 == null ? 0.0 : dc2.getProjectedSpeedChangeByImpulseRelative(rx2, ry2, directionx, directiony);
			double impulse = -speedDiff / (change1 + change2) * (1.0 + elasticity);
			impulses[0] = directionx * impulse;
			impulses[1] = directiony * impulse;
			impulses[2] = DiskComplex.angularMomentumRelative(rx1, ry1, impulses[0], impulses[1]);
			impulses[3] = DiskComplex.angularMomentumRelative(rx2, ry2, -impulses[0], -impulses[1]);
		} else {
			impulses[0] = 0.0;
			impulses[1] = 0.0;
			impulses[2] = 0.0;
			impulses[3] = 0.0;
		}
	}

}
