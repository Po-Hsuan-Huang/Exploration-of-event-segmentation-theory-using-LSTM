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
 *         Boundary that is given by a circular arc.
 */
public class ArcBoundary implements BoundaryElement {

	private static final double MAX_INTERPOL_ANGLE = 5.0 / 180.0 * Math.PI; //  user for drawing only, maximum angle deviation 5 degrees between consecutive interpolation points

	private double cx, cy;
	private double angle1, angle2;
	private double radius;

	public ArcBoundary(double centerx, double centery, double radius, double angle1, double angle2) {
		this.cx = centerx;
		this.cy = centery;
		this.angle1 = angle1;
		this.angle2 = angle2;
		this.radius = radius;
	}

	@Override
	public double getMinx() {
		if (included(Math.PI)) {
			return cx - radius;
		} else {
			double c1 = Math.cos(angle1);
			double c2 = Math.cos(angle2);
			return cx + Math.min(c1, c2) * radius;
		}
	}

	@Override
	public double getMaxx() {
		if (included(0)) {
			return cx + radius;
		} else {
			double c1 = Math.cos(angle1);
			double c2 = Math.cos(angle2);
			return cx + Math.max(c1, c2) * radius;
		}
	}

	@Override
	public double getMiny() {
		if (included(Math.PI * 1.5)) {
			return cy - radius;
		} else {
			double s1 = Math.sin(angle1);
			double s2 = Math.sin(angle2);
			return cy + Math.min(s1, s2) * radius;
		}
	}

	@Override
	public double getMaxy() {
		if (included(Math.PI * 0.5)) {
			return cy + radius;
		} else {
			double s1 = Math.sin(angle1);
			double s2 = Math.sin(angle2);
			return cy + Math.max(s1, s2) * radius;
		}
	}

	@Override
	public boolean intersectsCircle(double circx, double circy, double circr) {
		double dx = circx - cx;
		double dy = circy - cy;
		double d2 = dx * dx + dy * dy;
		double sr = radius + circr;
		if (d2 > sr * sr)
			return false;
		double dr = radius - circr;
		if (d2 < dr * dr)
			return false;
		return included(Math.atan2(dy, dx));
	}

	@Override
	public void countIntersectionsWithHorizontalLine(double x1, double x2, double y, int[] count) {
		double dy = y - cy;
		if (Math.abs(dy) >= radius)
			return;
		// (x-cx)^2 + dy^2 = r^2 ==> x = cx +/- sqrt(r^2-dy^2) 
		double pm = Math.sqrt(radius * radius - dy * dy);
		incCount(-pm, dy, cx - pm, x1, x2, count);
		incCount(pm, dy, cx + pm, x1, x2, count);
	}

	@Override
	public void countIntersectionsWithVerticalLine(double x, double y1, double y2, int[] count) {
		double dx = x - cx;
		if (Math.abs(dx) >= radius)
			return;
		// dx^2 + (y-cy)^2 = r^2 ==> y = cy +/- sqrt(r^2-dx^2) 
		double pm = Math.sqrt(radius * radius - dx * dx);
		incCount(dx, -pm, cy - pm, y1, y2, count);
		incCount(dx, pm, cy + pm, y1, y2, count);
	}

	private void incCount(double dx, double dy, double intersect, double min, double max, int[] count) {
		if (included(Math.atan2(dy, dx))) {
			if (intersect < min) {
				count[0]++;
			} else if (intersect > max) {
				count[2]++;
			} else {
				count[1]++;
			}
		}
	}

	@Override
	public double[] getAnyPoint() {
		double[] res = new double[2];
		getDrawingPoint(0, 2, res);
		return res;
	}

	@Override
	public int getNumDrawingPoints() {
		return 1 + (int) Math.ceil(Math.abs(angle2 - angle1) / MAX_INTERPOL_ANGLE);
	}

	@Override
	public void getDrawingPoint(int i, int num, double[] interpolationPoint) {
		double angle = angle1 + (double) i / (double) (num - 1) * (angle2 - angle1);
		interpolationPoint[0] = cx + Math.cos(angle) * radius;
		interpolationPoint[1] = cy + Math.sin(angle) * radius;

	}

	private boolean included(double angle) {
		double minAngle = Math.min(angle1, angle2);
		double maxAngle = Math.max(angle1, angle2);
		while (angle > minAngle + 2 * Math.PI)
			angle -= 2 * Math.PI;
		while (angle < minAngle)
			angle += 2 * Math.PI;
		return angle <= maxAngle;
	}

}
