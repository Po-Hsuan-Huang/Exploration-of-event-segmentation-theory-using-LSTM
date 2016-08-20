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

import java.awt.Graphics2D;

import diskworld.visualization.VisualizationSettings;

public abstract class VisualisationVariant {
	private String variantName;

	/**
	 * Creates settings for visualising sensors.
	 * 
	 * @param variantName
	 *            the name of the variant (not used if only one variant exists)
	 */
	public VisualisationVariant(String variantName) {
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
	 * Perform visualization of sensor/measurement 
	 * 
	 * @param g
	 *            the graphics object used to draw
	 * @param measurement
	 *            the measurement or null if not applicable 
	 * @param visualisationObject
	 *            an object to be visualized (e.g. a Shape, DiskSymbol), can be null 
	 * @param settings
	 *            visualization settings
	 */
	public abstract void visualisation(Graphics2D g, double[] measurement, Object visualisationObject, double cx, double cy, double r, double angle, VisualizationSettings settings);
}
