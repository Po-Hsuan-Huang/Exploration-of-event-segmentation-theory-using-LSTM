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
package diskworld.visualization;

import java.awt.Graphics2D;
import java.awt.Polygon;

public class PolygonDiskSymbol extends AbstractDiskSymbol {

	private double[][] points;

	private static double[][] TRIANGLE_POINTS = new double[][] {
			{ 1.0, 0.0 },
			{ -0.5, 0.5 * Math.sqrt(3.0) },
			{ -0.5, -0.5 * Math.sqrt(3.0) }
	};

	private static double[][] SQUARE_POINTS = new double[][] {
			{ Math.sqrt(0.5), Math.sqrt(0.5) },
			{ -Math.sqrt(0.5), Math.sqrt(0.5) },
			{ -Math.sqrt(0.5), -Math.sqrt(0.5) },
			{ Math.sqrt(0.5), -Math.sqrt(0.5) },
	};

	public PolygonDiskSymbol(double[][] points) {
		this.points = points;
	}

	public static PolygonDiskSymbol getTriangleSymbol(double relativeRadius) {
		return new PolygonDiskSymbol(scaled(TRIANGLE_POINTS, relativeRadius));
	}

	public static PolygonDiskSymbol getSquareSymbol(double relativeRadius) {
		return new PolygonDiskSymbol(scaled(SQUARE_POINTS, relativeRadius));
	}

	private static double[][] scaled(double[][] p, double relativeRadius) {
		double res[][] = new double[p.length][2];
		for (int i = 0; i < p.length; i++) {
			res[i][0] = p[i][0] * relativeRadius;
			res[i][1] = p[i][1] * relativeRadius;
		}
		return res;
	}

	@Override
	public void draw(Graphics2D graphics, int screenx, int screeny, int halfwidth, int halfheight, double angle) {
		int xp[] = new int[points.length];
		int yp[] = new int[points.length];
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		for (int i = 0; i < points.length; i++) {
			double x = points[i][0] * c - points[i][1] * s;
			double y = points[i][0] * s + points[i][1] * c;
			xp[i] = screenx + (int) Math.round(x * halfwidth);
			yp[i] = screeny - (int) Math.round(y * halfheight); // note: screen y axis goes downwards 
		}
		Polygon p = new Polygon(xp, yp, points.length);
		graphics.fillPolygon(p);
	}

}
