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

import diskworld.environment.Wall;

public class WallShape extends ArbitraryShape {

	public WallShape(Wall wall) {
		super(getWallBoundary(wall));
	}

	private static BoundaryElement[] getWallBoundary(Wall wall) {
		double x1 = wall.getX1();
		double y1 = wall.getY1();
		double x2 = wall.getX2();
		double y2 = wall.getY2();
		double dx = wall.getHalfThicknessX();
		double dy = wall.getHalfThicknessY();
		double r = wall.getHalfThicknessX();
		double angle = Math.atan2(dy, dx);
		return new BoundaryElement[] { 
				new LineBoundary(x1-dx, y1-dy, x2-dy, y2-dy),
				new ArcBoundary(x2, y2, r, angle-Math.PI, angle),
				new LineBoundary(x2+dx, y2+dy, x1+dy, y1+dy),
				new ArcBoundary(x1, y1, r, angle, angle+Math.PI)			
		};
	}

}
