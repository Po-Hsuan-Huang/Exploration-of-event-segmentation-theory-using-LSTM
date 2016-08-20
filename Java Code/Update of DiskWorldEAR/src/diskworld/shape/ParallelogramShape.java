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

public class ParallelogramShape extends ArbitraryShape {

	private final double x,y;
	private final double dx1,dy1;
	private final double dx2,dy2;

	public ParallelogramShape(double x, double y, double dx1, double dy1, double dx2, double dy2) {
		super(new BoundaryElement[] { 
				new LineBoundary(x, y, x+dx1, y+dy1),
				new LineBoundary(x+dx1, y+dy1, x+dx1+dx2, y+dy1+dy2),
				new LineBoundary(x+dx1+dx2, y+dy1+dy2, x+dx2, y+dy2),
				new LineBoundary(x+dx2, y+dy2, x, y)
		});
		this.x = x;
		this.y = y;
		this.dx1 = dx1;
		this.dy1 = dy1;
		this.dx2 = dx2;
		this.dy2 = dy2;
	}

	public double getx() {
		return x;
	}

	public double gety() {
		return y;
	}
	
	public double getdx1() {
		return dx1;
	}

	public double getdy1() {
		return dy1;
	}

	public double getdx2() {
		return dx2;
	}

	public double getdy2() {
		return dy2;
	}

}
