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

import java.awt.Color;
import java.awt.Graphics2D;

import diskworld.shape.Shape;
import diskworld.visualization.VisualizationSettings;

public abstract class ShapeVisualisationVariant extends VisualisationVariant {

	/**
	 * Creates settings for visualising shapes.
	 * 
	 * @param variantName
	 *            the name of the variant (not used if only one variant exists)
	 */
	public ShapeVisualisationVariant(String variantName) {
		super(variantName);
	}

	/**
	 * Provide the color for border.
	 * 
	 * @param measurement
	 *            the measurement or null 
	 * @param settings
	 *            visualization settings
	 * @return
	 *         color in which the border shall be painted, or null if no border shall be painted
	 */
	public abstract Color getBorderColor(double[] measurement, VisualizationSettings settings);

	/**
	 * Provide the color used to fill the shape.
	 * 
	 * @param measurement
	 *            the measurement or null if not applicable 
	 * @param settings
	 *            visualization settings
	 * @return
	 *         color in which the shape shall be filled, or null if filling shall occur
	 */
	public abstract Color getFillColor(double[] measurement, VisualizationSettings settings);

	/**
	 * Perform additional visualizations
	 * 
	 * @param g
	 *            the graphics object used to draw
	 * @param measurement
	 *            the measurement or null if not applicable 
	 * @param sensorShape
	 *            the current sensor shape
	 * @param settings
	 *            visualization settings
	 */
	public abstract void additionalVisualisation(Graphics2D g, double[] measurement, Shape sensorShape, double cx, double cy, double r, double angle, VisualizationSettings settings);

	
	@Override
	public void visualisation(Graphics2D g, double[] measurement, Object visualisationObject, double cx, double cy, double r, double angle, VisualizationSettings settings) {
		additionalVisualisation(g, measurement, (Shape)visualisationObject, cx, cy, r, angle, settings);
	}

}
