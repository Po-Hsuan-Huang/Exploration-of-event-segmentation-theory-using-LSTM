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

import diskworld.DiskType;

/**
 * Interface for additional visualization gadgets assigned to disks (e.g. DiskSymbol, Sensor Range, Debugging info, ...)
 * 
 * @author Jan
 * 
 */
public interface VisualisationItem {
	/**
	 * Draws the item, given absolute coordinates of the disk. Note: use {@link VisualizationSettings#mapX(double)} and {@link VisualizationSettings#mapY(double)} to map the absolute
	 * coordinates to screen coordinates
	 * 
	 * @param g
	 *            the Graphics object used to draw
	 * @param centerx
	 *            x coordinate of disk center
	 * @param centery
	 *            y coordinate of disk center
	 * @param radius
	 *            radius of disk
	 * @param angle
	 *            absolute angle
	 * @param activity
	 *            the disks activity values (used for actuators disks)
	 * @param measurement
	 *            for sensor visualization items: array of values measured by the corresponding sensor; otherwise: null
	 * @param settings
	 *            the VisualizationSettings object used to control the drawing
	 * @param diskType
	 *            type of the disk to which this item belongs
	 */
	public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, double measurement[], VisualizationSettings settings, DiskType diskType);
}
