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

import diskworld.Environment;
import diskworld.shape.ArbitraryShape;
import diskworld.shape.BoundaryElement;
import diskworld.shape.LineBoundary;
import diskworld.shape.Shape;

public abstract class LineSensor extends ShapeBasedSensor {

	/**
	 * Sensor with a (narrow) rectangular receptive field.
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param angle
	 *            the angle of the line relative to the disk orientation
	 * @param widthAbsolute
	 *            the width (absolute) of the rectangle
	 * @param minRangeRelativeToRadius
	 *            minimum distance (inner radius), specified in multiples of the disk radius
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @param sensorName
	 *            a name for the sensor (used in options pop-up menu)
	 */
	public LineSensor(
			Environment environment,
			double angle,
			double widthAbsolute,
			double minRangeRelativeToRadius,
			double maxRangeAbsolute,
			String sensorName) {
		super(environment, getLineTemplate(angle, widthAbsolute, minRangeRelativeToRadius, maxRangeAbsolute), sensorName);
	}

	private static ShapeTemplate getLineTemplate(final double angle, final double widthAbsolute, final double minRangeRelativeToRadius, final double maxRangeAbsolute) {
		return new ShapeTemplate() {
			@Override
			public Shape getShape(double centerx, double centery, double radius, double angle) {
				double s = Math.sin(angle);
				double c = Math.cos(angle);
				double d1 = minRangeRelativeToRadius * radius;
				double d2 = maxRangeAbsolute;
				double m1x = centerx + c * d1;
				double m1y = centery + s * d1;
				double m2x = centerx + c * d2;
				double m2y = centery + s * d2;
				double dx = -s * widthAbsolute;
				double dy = c * widthAbsolute;
				BoundaryElement[] boundary = new BoundaryElement[4];
				boundary[0] = new LineBoundary(m1x + dx, m1y + dy, m1x - dx, m1y - dy);
				boundary[1] = new LineBoundary(m1x - dx, m1y - dy, m2x - dx, m2y - dy);
				boundary[2] = new LineBoundary(m2x - dx, m2y - dy, m2x + dx, m2y + dy);
				boundary[3] = new LineBoundary(m2x + dx, m2y + dy, m1x + dx, m1y + dy);
				return new ArbitraryShape(boundary);
			}
		};
	}

	// visualization options to be used by subclasses 

}
