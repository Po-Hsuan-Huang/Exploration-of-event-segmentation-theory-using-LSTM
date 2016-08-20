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

public class Line {

	private final double x1, y1, x2, y2;

	public Line(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public Line(Point start, Point end) {
		this(start.x, start.y, end.x, end.y);
	}

	public double getX1() {
		return x1;
	}

	public double getY1() {
		return y1;
	}

	public double getX2() {
		return x2;
	}

	public double getY2() {
		return y2;
	}

	public double getDeltaX() {
		return x2 - x1;
	}

	public double getDeltaY() {
		return y2 - y1;
	}

	public double getLength() {
		double dx = getDeltaX();
		double dy = getDeltaY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	public Point getIntersectionPoint(Line other) {
		double intersectionx, intersectiony;
		double x3 = other.x1;
		double y3 = other.y1;
		double x4 = other.x2;
		double y4 = other.y2;

		//source formula: http://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
		intersectionx = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) /
				((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

		intersectiony = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) /
				((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

		return new Point(intersectionx, intersectiony);
	}

}
