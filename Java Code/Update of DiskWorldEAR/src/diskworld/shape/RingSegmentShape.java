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

import diskworld.linalg2D.AngleUtils;

public class RingSegmentShape extends ArbitraryShape {

	private double cx;
	private double cy;
	private final double minr, maxr;
	private double startAngle;
	private final double endAngle;

	public RingSegmentShape(double x, double y, double minr, double maxr, double startAngle, double openingAngle) {
		super(new BoundaryElement[] {
				getRay(x, y, minr, maxr, startAngle),
				getArc(x, y, maxr, startAngle, startAngle + openingAngle),
				getRay(x, y, maxr, minr, startAngle + openingAngle),
				getArc(x, y, minr, startAngle + openingAngle, startAngle)
		});
		this.cx = x;
		this.cy = y;
		this.minr = minr;
		this.maxr = maxr;
		// need to normalize angles for the inside test
		while (startAngle < 0)
			startAngle += 2 * Math.PI;
		while (startAngle >= 2 * Math.PI)
			startAngle -= 2 * Math.PI;
		if ((openingAngle < 0) || (openingAngle > 2 * Math.PI))
			throw new IllegalArgumentException("");
		this.startAngle = startAngle;
		this.endAngle = startAngle + openingAngle;
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

	private static BoundaryElement getRay(double x, double y, double r1, double r2, double angle) {
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		return new LineBoundary(x + r1 * c, y + r1 * s, x + r2 * c, y + r2 * s);
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
		double angle = AngleUtils.closestBigger(Math.atan2(dy, dx), startAngle);
		return (d2 >= minr * minr) && (d2 <= maxr * maxr) && (angle < endAngle);
	}

	@Override
	public double[] referenceAngles() {
		return new double[] { startAngle, endAngle };
	}

	@Override
	public double[] referenceValues() {
		return new double[] { minr, maxr };
	}

}
