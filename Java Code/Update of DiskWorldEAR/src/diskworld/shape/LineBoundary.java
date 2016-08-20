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
package diskworld.shape;

/**
 * @author Jan
 * 
 *         A straight line boundary
 */
public class LineBoundary implements BoundaryElement {

	private double x1, y1, x2, y2;

	public LineBoundary(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	@Override
	public double getMinx() {
		return Math.min(x1, x2);
	}

	@Override
	public double getMaxx() {
		return Math.max(x1, x2);
	}

	@Override
	public double getMiny() {
		return Math.min(y1, y2);
	}

	@Override
	public double getMaxy() {
		return Math.max(y1, y2);
	}

	@Override
	public void countIntersectionsWithHorizontalLine(double xa, double xb, double y, int[] count) {
		double dx = x2-x1;
		double dy = y2-y1;
		if (dy != 0.0) {
			// solve y1+c*dy = y ==> c = (y-y1)/dy  must be between 0 and 1
			double c = (y-y1)/dy;
			if ((c >= 0.0) && (c <= 1.0)) {
				double intersectx = x1 + c*dx;
				incCount(intersectx, xa, xb, count);
			}
		}
	}

	private static void incCount(double intersect, double min, double max, int[] count) {
		if (intersect < min) {
			count[0]++;
		} else if (intersect > max) {
			count[2]++;
		} else {
			count[1]++;					
		}		
	}
	
	@Override
	public void countIntersectionsWithVerticalLine(double x, double ya, double yb, int[] count) {
		double dx = x2-x1;
		double dy = y2-y1;
		if (dx != 0.0) {
			// solve x1+c*dx = x ==> c = (x-x1)/dx  must be between 0 and 1
			double c = (x-x1)/dx;
			if ((c >= 0.0) && (c <= 1.0)) {
				double intersecty = y1 + c*dy;
				incCount(intersecty, ya, yb, count);
			}
		}
	}

	@Override
	public boolean intersectsCircle(double cx, double cy, double r) {
		// check if end points are inside and outside
		double dx1 = cx - x1;
		double dx2 = cx - x2;
		double dy1 = cy - y1;
		double dy2 = cy - y2;
		double r2 = r * r;
		if ((dx1 * dx1 + dy1 * dy1 <= r2) != (dx2 * dx2 + dy2 * dy2 <= r2))
			return true;
		// check if circle intersects twice with line
		double dx = x2 - x1;
		double dy = y2 - y1;
		double d2 = dx * dx + dy * dy;
		double par = dx1 * dx + dy1 * dy;
		double orth = -dx1 * dy + dy1 * dx;
		return (par >= 0) && (par <= d2) && (orth * orth <= r2 * d2);
	}

	@Override
	public double[] getAnyPoint() {
		return new double[] { x1, y1 };
	}

	@Override
	public int getNumDrawingPoints() {
		return 2;
	}

	@Override
	public void getDrawingPoint(int i, int num, double[] interpolationPoint) {
		if (i == 0) {
			interpolationPoint[0] = x1;
			interpolationPoint[1] = y1;
		} else {
			interpolationPoint[0] = x2;
			interpolationPoint[1] = y2;
		} 
	}

}
