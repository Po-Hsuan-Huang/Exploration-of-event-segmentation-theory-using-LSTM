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
import java.awt.geom.Ellipse2D;

import diskworld.visualization.VisualizationSettings;

public class CircleShape implements Shape {

	private double cx;
	private double cy;
	private final double radius;

	public CircleShape(double x, double y, double radius) {
		this.cx = x;
		this.cy = y;
		this.radius = radius;
	}

	@Override
	public boolean isInside(double x, double y) {
		double dx = x - cx;
		double dy = y - cy;
		return dx * dx + dy * dy <= radius * radius;
	}

	@Override
	public double[] getBoundingBox() {
		return new double[] { cx - radius, cy - radius, cx + radius, cy + radius };
	}

	@Override
	public boolean intersectsDisk(double x, double y, double r) {
		double dx = x - cx;
		double dy = y - cy;
		double sr = radius + r;
		return dx * dx + dy * dy <= sr * sr;
	}

	@Override
	public boolean intersectsRectangle(double x1, double y1, double x2, double y2) {
		// test if the circle's bounding box is completely inside the rectangle
		double bounds[] = getBoundingBox();
		if ((bounds[0] >= x1) && (bounds[2] <= x2) && (bounds[1] >= y1) && (bounds[3] <= y2))
			return true;
		// test if one of the corners is inside the circle
		if (isInside(x1, y1))
			return true;
		if (isInside(x1, y2))
			return true;
		if (isInside(x2, y1))
			return true;
		if (isInside(x2, y2))
			return true;
		// test if one of borders of the rectangle intersects twice with the circle
		if (multipleIntersectionsHorizontal(x1, x2, y1))
			return true;
		if (multipleIntersectionsHorizontal(x1, x2, y2))
			return true;
		if (multipleIntersectionsVertictal(x1, y1, y2))
			return true;
		if (multipleIntersectionsVertictal(x2, y1, y2))
			return true;
		return false;
	}

	@Override
	public void fill(Graphics2D g, Color fillcol, VisualizationSettings settings) {
		g.setColor(fillcol);
		g.fill(getCircle(settings));
	}

	@Override
	public void drawBorder(Graphics2D g, Color fillcol, VisualizationSettings settings) {
		g.setColor(fillcol);
		g.draw(getCircle(settings));
	}

	// private methods

	private Ellipse2D getCircle(VisualizationSettings settings) {
		int x1 = settings.mapX(cx - radius);
		int x2 = settings.mapX(cx + radius);
		int y1 = settings.mapY(cy + radius);
		int y2 = settings.mapY(cy - radius);
		return new Ellipse2D.Double(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	private boolean multipleIntersectionsHorizontal(double x1, double x2, double y) {
		return (cx >= x1) && (cx <= x2) && (Math.abs(y - cy) <= radius);
	}

	private boolean multipleIntersectionsVertictal(double x, double y1, double y2) {
		return (cy >= y1) && (cy <= y2) && (Math.abs(x - cx) <= radius);
	}

	@Override
	public double[] referencePoint() {
		return new double[] { cx, cy };
	}

	@Override
	public double[] referenceAngles() {
		return null;
	}

	@Override
	public double[] referenceValues() {
		return new double[] { radius };
	}

}
