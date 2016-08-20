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
package diskworld.actuators;

import java.awt.Graphics2D;

import diskworld.interfaces.DiskSymbol;
import diskworld.visualization.VisualizationSettings;

public abstract class ActuatorVisualisationVariant {

	private String variantName;

	/**
	 * Creates settings for visualising actuators.
	 * 
	 * @param variantName
	 *            the name of the variant (not used if only one variant exists)
	 */
	public ActuatorVisualisationVariant(String variantName) {
		this.variantName = variantName;
	}

	/**
	 * Provides the name
	 * 
	 * @return name of variant
	 */
	public String getVariantName() {
		return variantName;
	}

	/**
	 * Perform additional visualisations
	 * 
	 * @param g
	 *            the graphics object used to draw
	 * @param centerx
	 *            x coordinate of disk
	 * @param centery
	 *            y coordinate of disk
	 * @param radius
	 *            radius of disk
	 * @param angle
	 *            orientation of disk
	 * @param activity
	 *            current activity of the actuator
	 * @param diskSymbol
	 *            DiskSymbol object to be displayed or null
	 * @param settings
	 *            visualisation settings (use to map absolute coordinates to screen coordinates)
	 */
	public abstract void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, DiskSymbol diskSymbol, VisualizationSettings settings);

}
