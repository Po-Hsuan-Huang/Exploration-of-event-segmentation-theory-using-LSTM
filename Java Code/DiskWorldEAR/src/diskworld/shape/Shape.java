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

import diskworld.visualization.VisualizationSettings;

/**
 * Defines a 2D shape. A shape is a closed curve without self-intersections.
 */
public interface Shape {

	/**
	 * Determines if a given rectangle (parallel to the axis) has any points
	 * in common with this shape. This also includes the cases that the rectangle is
	 * completely inside the shape or the shape is completely inside the rectangle.
	 * 
	 * @param x1
	 *            left x coordinate
	 * @param y1
	 *            lower y coordinate
	 * @param x2
	 *            right x coordinate
	 * @param y2
	 *            upper y coordinate
	 * @return true if rectangle and shape intersect
	 */
	public boolean intersectsRectangle(double x1, double y1, double x2, double y2);

	/**
	 * Determines if a given point is inside the interior of the shape (or on the boundary).
	 * 
	 * @param x
	 *            x coordinate of point to be tested
	 * @param y
	 *            y coordinate of point to be tested
	 * @return true if given point is inside the interior of the shape including boundary.
	 */
	public boolean isInside(double x, double y);

	/**
	 * Obtain rectangle that completely contains the shape.
	 * 
	 * @return array with 4 elements: [minx,miny,maxx,maxy]
	 */
	public double[] getBoundingBox();

	/**
	 * Tests if a disk intersects with this shape. Returns true if either:
	 * <ul>
	 * <li>the shape lies fully inside the disk</li>
	 * <li>the disk lies fully inside the shape</li>
	 * <li>the disk intersects partially with the shape</li>
	 * </ul>
	 * 
	 * @param x
	 *            x coordinate of centre
	 * @param y
	 *            y coordinate of centre
	 * @param r
	 *            radius
	 * @return true if the shapes intersects the disk
	 */
	public boolean intersectsDisk(double x, double y, double r);

	/**
	 * Fill the shape on the screen.
	 * 
	 * @param g
	 *            graphics object to paint
	 * @param color
	 *            color to be used for filling
	 * @param settings
	 *            provides the maps from absolute to screen coordinates
	 */
	public void fill(Graphics2D g, Color color, VisualizationSettings settings);

	/**
	 * Draw the border of the shape on the screen.
	 * 
	 * @param g
	 *            graphics object to paint
	 * @param color
	 *            color to be used for border
	 * @param settings
	 *            provides the maps from absolute to screen coordinates
	 */
	public void drawBorder(Graphics2D g, Color color, VisualizationSettings settings);

	/**
	 * Used for visualisation: provides a reference point of the shape
	 * (which one depends on the shape implementation, for example the center in cones, circles, rings)
	 * 
	 * @return x,y coordinate of reference point
	 */
	public double[] referencePoint();

	/**
	 * Used for visualisation: provides a set of reference angles (depending on shape, may be null)
	 * 
	 * @return array of angles (in rad) or null
	 */
	public double[] referenceAngles();

	/**
	 * Used for visualisation: provides a set of reference values (for instance distances, depending depending on shape implementation, may be null)
	 * 
	 * @return array of values or null
	 */
	public double[] referenceValues();

}
