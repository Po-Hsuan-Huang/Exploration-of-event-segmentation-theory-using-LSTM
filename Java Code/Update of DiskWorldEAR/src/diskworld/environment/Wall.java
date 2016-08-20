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
package diskworld.environment;

import diskworld.interfaces.CollidableObject;
import diskworld.linalg2D.Line;

// a wall is represented as a line object witch a certain thickness
// it has parameters: thickness (d^2), thicknessX (dx^2) and thicknessY (dy^2)

public class Wall implements CollidableObject {

	//code to produce deterministic hash values 
	private static int instanceCount = 0;
	private final int instanceID = ++instanceCount;

	@Override
	public int hashCode() {
		// Note: Disk can be compared wit walls (both are CollidableObjects). We resolve this by iving disks a positive and walls a negative hasCode   
		return -instanceID;
	}

	private final Line line;
	private final double d, dx, dy;

	public Wall(Line line, double thickness) {
		this.line = line;
		// computes d,dy and dx
		d = thickness * 0.5;
		double f = d / line.getLength();
		dy = line.getDeltaX() * f;
		dx = -line.getDeltaY() * f;
	}

	public double getX1() {
		return line.getX1();
	}

	public double getY1() {
		return line.getY1();
	}

	public double getX2() {
		return line.getX2();
	}

	public double getY2() {
		return line.getY2();
	}

	public double getHalfThickness() {
		return d;
	}

	public double getHalfThicknessX() {
		return dx;
	}

	public double getHalfThicknessY() {
		return dy;
	}

}
