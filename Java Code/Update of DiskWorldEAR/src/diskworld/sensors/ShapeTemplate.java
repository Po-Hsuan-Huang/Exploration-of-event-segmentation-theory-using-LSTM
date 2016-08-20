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
package diskworld.sensors;

import diskworld.shape.Shape;

/**
 * @author Jan
 * 
 *         Holds a template for shapes. It can generate shape when provided with the relevant disk parameters (coordinates, angle, radius).
 */
interface ShapeTemplate {
	/**
	 * Generates a shape relative to the provided disk parameters.
	 * 
	 * @param centerx
	 *            x coordinate of disk centre
	 * @param centery
	 *            y coordinate of disk centre
	 * @param radius
	 *            radius of the disk
	 * @param angle
	 *            absolute orientation of the disk
	 * @return
	 *         a new Shape based on this template
	 */
	public Shape getShape(double centerx, double centery, double radius, double angle);
}
