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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import diskworld.visualization.VisualizationSettings;

/**
 * @author Jan
 * 
 *         Implementation of intersection methods and boundingBox(). Only the isInside() method still needs to implemented by subclasses.
 */
public class ArbitraryShape implements Shape {

	BoundaryElement boundary[];

	public ArbitraryShape(BoundaryElement[] boundary) {
		this.boundary = boundary;
	}

	@Override
	public double[] getBoundingBox() {
		double[] res = null;
		for (BoundaryElement be : boundary) {
			if (res == null) {
				res = new double[4];
				res[0] = be.getMinx();
				res[1] = be.getMiny();
				res[2] = be.getMaxx();
				res[3] = be.getMaxy();
			} else {
				res[0] = Math.min(res[0], be.getMinx());
				res[1] = Math.min(res[1], be.getMiny());
				res[2] = Math.max(res[2], be.getMaxx());
				res[3] = Math.max(res[3], be.getMaxy());
			}
		}
		return res;
	}

	@Override
	public boolean intersectsDisk(double x, double y, double r) {
		// check if the center of the circle is inside this shape 
		if (isInside(x, y))
			return true;
		// check if an arbitrary point of the boundary is inside the circle
		double p[] = boundary[0].getAnyPoint();
		double dx = p[0] - x;
		double dy = p[1] - y;
		if (dx * dx + dy * dy <= r * r)
			return true;
		// now check if the boundary intersects with the disk boundary
		for (BoundaryElement be : boundary) {
			if (be.intersectsCircle(x, y, r))
				return true;
		}
		return false;
	}

	@Override
	public boolean intersectsRectangle(double x1, double y1, double x2, double y2) {
		// check if the center of the circle is inside this shape 
		if (isInside((x1 + x2) * 0.5, (y1 + y2) * 0.5))
			return true;
		// check if an arbitrary point of the boundary is inside the rectangle
		double p[] = boundary[0].getAnyPoint();
		if ((p[0] >= x1) && (p[0] <= x2) && (p[1] >= y1) && (p[1] <= y2))
			return true;
		// now check if the boundary intersects with the rectangle boundary
		for (BoundaryElement be : boundary) {
			if (intersectsRectangle(be, x1, y1, x2, y2))
				return true;
		}
		return false;
	}

	@Override
	public boolean isInside(double x, double y) {
		int[] count1 = new int[3];
		int[] count2 = new int[3];
		for (BoundaryElement be : boundary) {
			be.countIntersectionsWithHorizontalLine(x, x, y, count1);
			be.countIntersectionsWithVerticalLine(x, y, y, count2);
		}
		if (count1[1] > 0)
			return true;
		if (count2[1] > 0)
			return true;
		// if inside, [0] and [2] should all be odd, decide by voting
		int vote = 0;
		if (count1[0] % 2 == 1)
			vote++;
		if (count1[2] % 2 == 1)
			vote++;
		if (count2[0] % 2 == 1)
			vote++;
		if (count2[2] % 2 == 1)
			vote++;
		return vote >= 2; // should be either 0 or 4, but who knows what rounding errors can do?
	}

	@Override
	public void fill(Graphics2D g, Color color, VisualizationSettings settings) {
		g.setColor(color);
		g.fillPolygon(getPolygon(settings));
	}

	@Override
	public void drawBorder(Graphics2D g, Color color, VisualizationSettings settings) {
		g.setColor(color);
		g.drawPolygon(getPolygon(settings));
	}

	//private methods

	private Polygon getPolygon(VisualizationSettings settings) {
		Polygon poly = new Polygon();
		double dp[] = new double[2];
		for (BoundaryElement be : boundary) {
			int num = be.getNumDrawingPoints();
			for (int i = 0; i < num; i++) {
				be.getDrawingPoint(i, num, dp);
				poly.addPoint(settings.mapX(dp[0]), settings.mapY(dp[1]));
			}
		}
		return poly;
	}

	private boolean intersectsRectangle(BoundaryElement be, double x1, double y1, double x2, double y2) {
		int[] count = new int[3];
		be.countIntersectionsWithHorizontalLine(x1, x2, y1, count);
		if (count[1] > 0)
			return true;
		be.countIntersectionsWithHorizontalLine(x1, x2, y2, count);
		if (count[1] > 0)
			return true;
		be.countIntersectionsWithVerticalLine(x1, y1, y2, count);
		if (count[1] > 0)
			return true;
		be.countIntersectionsWithVerticalLine(x2, y1, y2, count);
		return (count[1] > 0);
	}

	@Override
	public double[] referencePoint() {
		// center of gravity
		double sx = 0.0;
		double sy = 0.0;
		int n = 0;
		double dp[] = new double[2];
		for (BoundaryElement be : boundary) {
			int num = be.getNumDrawingPoints();
			for (int i = 0; i < num; i++) {
				be.getDrawingPoint(i, num, dp);
				sx += dp[0];
				sy += dp[1];
				n++;
			}
		}
		return new double[] { sx / n, sy / n };
	}

	@Override
	public double[] referenceAngles() {
		return null;
	}

	@Override
	public double[] referenceValues() {
		return null;
	}

}
