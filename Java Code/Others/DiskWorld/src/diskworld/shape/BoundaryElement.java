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
 * Defines pieces of the boundary of shapes. Examples lines and arcs (segments of circles)
 */
public interface BoundaryElement {

	/**
	 * Counts number of intersections with horizontal line. 
	 * The Boundary element is intersected with an (infinite) horizontal line. The intersections 
	 * are counted in an integer array of length 3. Intersections with x coordinate < x1
	 * are counted in [0], intersections with x1 <= x coordinate smaller <= x2 are counted in [1]
	 * and intersections with x > x2 are counted in [2]. 
	 * 
	 * @param x1
	 *            x coordinate of first point
	 * @param x2
	 *            x coordinate of second point
	 * @param y
	 *            y coordinate of line
	 * @param count
	 *            integer array (will be incremented depending on intersection x coordinates)
	 */
	public void countIntersectionsWithHorizontalLine(double x1, double x2, double y, int count[]);

	/**
	 * Counts number of intersections with horizontal line. 
	 * The Boundary element is intersected with an (infinite) horizontal line. The intersections 
	 * are counted in an integer array of length 3. Intersections with x coordinate smaller than
	 * x1 are counted in [0], intersections with x1 <= x coordinate smaller <= x2 are counted in [1]
	 * and intersections with x > x2 are counted in [2]. 
	 * 
	 * @param x
	 *            x coordinate of line
	 * @param y1
	 *            y coordinate of first point
	 * @param y2
	 *            y coordinate of second point
	 * @param count
	 *            integer array (will be incremented depending on intersection y coordinates)
	 */
	public void countIntersectionsWithVerticalLine(double x, double y1, double y2, int count[]);

	/**
	 * Determines if there is an intersection with a circle
	 * 
	 * @param x
	 *            x coordinate of center
	 * @param y
	 *            y coordinate of center
	 * @param r
	 *            radius
	 * @return true if the circle intersects this boundary element
	 */
	public boolean intersectsCircle(double x, double y, double r);

	/**
	 * Minimum x coordinate
	 * 
	 * @return smallest x coordinate of all points on the curve
	 */
	public double getMinx();

	/**
	 * Maximum x coordinate
	 * 
	 * @return largest x coordinate of all points on the curve
	 */
	public double getMaxx();

	/**
	 * Minimum y coordinate
	 * 
	 * @return smallest y coordinate of all points on the curve
	 */
	public double getMiny();

	/**
	 * Maximum y coordinate
	 * 
	 * @return largest y coordinate of all points on the curve
	 */
	public double getMaxy();

	/**
	 * Provides any point (chosen arbitrarily) on this boundary element (e.g. the start point)
	 * 
	 * @return double array of length 2 of form {x,y}
	 */
	public double[] getAnyPoint();

	/**
	 * Propose a number of points to interpolate the boundary by lines (this is used for drawing only)
	 * 
	 * @return suitable number of points (at least 2) to interpolate the curve
	 */
	public int getNumDrawingPoints();

	/**
	 * Calculate a interpolation point (this is used for drawing only)
	 * 
	 * @param i index of the drawing point, must be in 0..num-1
	 * @param num number of interpolation points, including end points, must be at least 2
	 * @param interpolationPoint array of length 2 into which x and y coordinate of the interpolation point shall be written
	 */
	public void getDrawingPoint(int i, int num, double[] interpolationPoint);

}
