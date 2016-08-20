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
package diskworld.linalg2D;


public class Point {
	public double x;
	public double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double distanceSqr(double x, double y) {
		double dx = this.x - x;
		double dy = this.y - y;
		return dx * dx + dy * dy;
	}

	public double distance(double x, double y) {
		double dx = this.x - x;
		double dy = this.y - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public double distance(Point other) {
		double dx = this.x - other.x;
		double dy = this.y - other.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	/**
	 * returns the intersection point between two lines:
	 * one through this and this1, the other one through other1 and other2.
	 * 
	 * @param this1
	 * @param other1
	 * @param other2
	 * @return the intersection point
	 */
	public Point getIntersectionPoint(Point this1, Point other1, Point other2) {
		double intersectionx, intersectiony;
		double x1 = this.getX();
		double y1 = this.getY();
		double x2 = this1.getX();
		double y2 = this1.getY();
		double x3 = other1.getX();
		double y3 = other1.getY();
		double x4 = other2.getX();
		double y4 = other2.getY();

		//source formula: http://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
		intersectionx = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) /
				((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

		intersectiony = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) /
				((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

		return new Point(intersectionx, intersectiony);
	}

	/**
	 * returns the intersection point between two lines:
	 * one through this1 and this2, the other one through other1 and other2.
	 * 
	 * @param this1
	 * @param other1
	 * @param other2
	 * @return the intersection point
	 */
	public static Point getIntersectionPoint(Point this1, Point this2, Point other1, Point other2) {
		double intersectionx, intersectiony;
		double x1 = this1.getX();
		double y1 = this1.getY();
		double x2 = this2.getX();
		double y2 = this2.getY();
		double x3 = other1.getX();
		double y3 = other1.getY();
		double x4 = other2.getX();
		double y4 = other2.getY();

		//source formula: http://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
		intersectionx = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) /
				((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

		intersectiony = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) /
				((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

		return new Point(intersectionx, intersectiony);
	}

}
