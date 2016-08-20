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

import java.util.Random;

/**
 * Obtains a set of input positions (x,y) and desired target output positions (X,Y).
 * Calculates an offset (deltax,deltay) and an angle phi that minimizes the
 * squared distance between the shifted&rotated input positions and the targets.
 * This works only under the assumption that the rotation angle phi is small.
 * Optionally, the samples can be weighted. Samples with a higher weight get more
 * importance.
 * 
 * @author Jan
 * 
 */
public class OptimalShiftAndSmallRotation {

	private double sw, sx, sy, sX, sY, sxx, syy, sXy, sxY;
	private double phi, xOff, yOff;
	private boolean uptodate;

	/**
	 * Create a new, empty instance
	 */
	public OptimalShiftAndSmallRotation() {
		sw = 0;
		sx = 0;
		sy = 0;
		sX = 0;
		sY = 0;
		sxx = 0;
		syy = 0;
		sXy = 0;
		sxY = 0;
		phi = 0;
		xOff = 0;
		yOff = 0;
		uptodate = true;
	}

	/**
	 * Add a weighted sample
	 * 
	 * @param w
	 *            the weight > 0.0
	 * @param x
	 *            x position of input
	 * @param y
	 *            y position of input
	 * @param X
	 *            x position of output target
	 * @param Y
	 *            y position of output target
	 */
	public void addWeightedSample(double w, double x, double y, double X, double Y) {
		uptodate = false;
		sw += w;
		sx += w * x;
		sy += w * y;
		sX += w * X;
		sY += w * Y;
		sxx += w * x * x;
		syy += w * y * y;
		sXy += w * X * y;
		sxY += w * x * Y;
	}

	/**
	 * Add a sample with weight 1.0
	 * 
	 * @param x
	 *            x position of input
	 * @param y
	 *            y position of input
	 * @param X
	 *            x position of output target
	 * @param Y
	 *            y position of output target
	 */
	public void addSample(double x, double y, double X, double Y) {
		addWeightedSample(1.0, x, y, X, Y);
	}

	/**
	 * Provide the rotation angle
	 * 
	 * @return phi in radian
	 */
	public double getPhi() {
		updateParameters();
		return phi;
	}

	/**
	 * Provide the x offset
	 * 
	 * @return shift in x direction
	 */
	public double getOffsetx() {
		updateParameters();
		return xOff;
	}

	/**
	 * Provide the y offset
	 * 
	 * @return shift in y direction
	 */
	public double getOffsety() {
		updateParameters();
		return yOff;
	}

	private void updateParameters() {
		if (!uptodate) {
			double dxx = sxx * sw - sx * sx;
			double dyy = syy * sw - sy * sy;
			double dXy = sXy * sw - sX * sy;
			double dxY = sxY * sw - sx * sY;
			double nominator = dxY - dXy;
			double denominator = dxx + dyy;
			if (denominator == 0.0) {
				phi = 0.0;
				System.out.println("phi=0");
			} else {
				double sinApprox = nominator / denominator;
				//System.out.println("sinApprox: " + sinApprox + "=" + nominator + "/" + denominator);
				if (sinApprox > 1.0) {
					phi = Math.PI / 2;
				} else if (sinApprox < -1.0) {
					phi = -Math.PI / 2;
				} else {
					phi = Math.asin(sinApprox);
				}
			}
			assert (!Double.isNaN(phi));
			double sin = Math.sin(phi);
			double cos = Math.cos(phi);
			xOff = (sX - sx * cos + sy * sin) / sw;
			yOff = (sY - sx * sin - sy * cos) / sw;
			uptodate = true;
		}
	}

	public static void main(String args[]) {
		double p[][] = new double[][] {
				{ 0, 0 },
				{ 1, 2 },
				{ 2, 2 },
				{ 2, 3 },
		};
		double phi = Math.toRadians(30);
		double offx = 1;
		double offy = 2;

		double sin = Math.sin(phi);
		double cos = Math.cos(phi);
		double p2[][] = new double[p.length][2];
		double dev = 0.1;
		Random rand = new Random(0);
		for (int i = 0; i < p.length; i++) {
			p2[i][0] = cos * p[i][0] - sin * p[i][1] + offx + dev * rand.nextDouble();
			p2[i][1] = sin * p[i][0] + cos * p[i][1] + offy + dev * rand.nextDouble();
		}
		OptimalShiftAndSmallRotation osr = new OptimalShiftAndSmallRotation();
		for (int i = 0; i < p.length; i++) {
			osr.addSample(p[i][0], p[i][1], p2[i][0], p2[i][1]);
		}
		System.out.println("Phi estimated: " + osr.getPhi() + " (true: " + phi + ")");
		System.out.println("offx estimated: " + osr.getOffsetx() + " (true: " + offx + ")");
		System.out.println("offy estimated: " + osr.getOffsety() + " (true: " + offy + ")");
	}

}
