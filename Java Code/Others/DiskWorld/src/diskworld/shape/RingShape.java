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

public class RingShape extends ArbitraryShape {

	private double cx;
	private double cy;
	private final double minr, maxr;

	public RingShape(double x, double y, double minr, double maxr) {
		super(new BoundaryElement[] {
				getArc(x, y, minr, 0, 2 * Math.PI),
				getArc(x, y, maxr, 0, 2 * Math.PI),
		});
		this.cx = x;
		this.cy = y;
		this.minr = minr;
		this.maxr = maxr;
	}

	public double getCenterx() {
		return cx;
	}

	public double getCentery() {
		return cy;
	}

	private static BoundaryElement getArc(double x, double y, double r, double angle1, double angle2) {
		return new ArcBoundary(x, y, r, angle1, angle2);
	}

	/* (non-Javadoc)
	 * @see diskworld.shape.ArbitraryShape#isInside(double, double)
	 * 
	 * We have a better implementation than the generic implementation in super class!
	 */
	@Override
	public boolean isInside(double x, double y) {
		double dx = x - cx;
		double dy = y - cy;
		double d2 = dx * dx + dy * dy;
		return (d2 >= minr * minr) && (d2 <= maxr * maxr);
	}

	/**
	 * Need to override this, otherwise radius is drawn
	 * 
	 * @see diskworld.shape.ArbitraryShape#drawBorder(java.awt.Graphics2D, java.awt.Color, diskworld.visualization.VisualizationSettings)
	 */
	@Override
	public void drawBorder(Graphics2D g, Color color, VisualizationSettings settings) {
		g.setColor(color);
		drawCircle(g, minr, settings);
		drawCircle(g, maxr, settings);
	}

	private void drawCircle(Graphics2D g, double r, VisualizationSettings s) {
		int x1 = s.mapX(cx - r);
		int y1 = s.mapY(cy + r);
		int x2 = s.mapX(cx + r);
		int y2 = s.mapY(cy - r);
		g.draw(new Ellipse2D.Double(x1, y1, x2 - x1, y2 - y1));
	}

	@Override
	public double[] referenceValues() {
		return new double[] { minr, maxr };
	}

	@Override
	public double[] referenceAngles() {
		return new double[] { 0, 2 * Math.PI };
	}

}
