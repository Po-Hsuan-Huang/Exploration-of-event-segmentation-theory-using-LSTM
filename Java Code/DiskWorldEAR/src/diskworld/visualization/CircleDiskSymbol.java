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

public class CircleDiskSymbol extends AbstractDiskSymbol {

	private double relRadius;

	public CircleDiskSymbol(double relativeRadius) {
		this.relRadius = relativeRadius;
	}

	@Override
	public void draw(Graphics2D graphics, int screenx, int screeny, int halfwidth, int halfheight, double angle) {
		int rx = (int) Math.round(halfwidth * relRadius);
		int ry = (int) Math.round(halfheight * relRadius);
		graphics.fillArc(screenx - rx, screeny - ry, 2 * rx + 1, 2 * ry + 1, 0, 360);
	}

}
