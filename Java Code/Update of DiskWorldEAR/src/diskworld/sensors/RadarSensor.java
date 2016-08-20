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

import diskworld.Disk;
import diskworld.Environment;

public class RadarSensor extends LineSensor {

	private static final double MIN_RANGE_RELATIVE = 1; // starts at disk radius
	private static final String SENSOR_NAME = "Radar";

	private double maxRange;

	public RadarSensor(Environment environment, double angle, double width, double maxRangeAbsolute) {
		super(environment, angle, width, MIN_RANGE_RELATIVE, maxRangeAbsolute, SENSOR_NAME);
		this.maxRange = maxRangeAbsolute;
	}

	@Override
	public int getDimension() {
		return 1;
	}

	@Override
	protected void doMeasurement(double measurement[]) {
		double centerx = getDisk().getX();
		double centery = getDisk().getY();
		double min = maxRange;
		for (Disk d : getDisksInShape()) {
			if (d != getDisk()) {
				double dx = d.getX() - centerx;
				double dy = d.getY() - centery;
				double dist = Math.sqrt(dx * dx + dy * dy) - d.getRadius();
				if (dist < min)
					min = dist;
			}
		}
		// TODO: implementation is not correct if disk is large 
		measurement[0] = min / maxRange;
	}

	@Override
	public double getRealWorldInterpretation(double[] measurement, int index) {
		return measurement[index] * maxRange;
	}

	@Override
	public String getRealWorldMeaning(int index) {
		return "distance to obstacle";
	}
}
