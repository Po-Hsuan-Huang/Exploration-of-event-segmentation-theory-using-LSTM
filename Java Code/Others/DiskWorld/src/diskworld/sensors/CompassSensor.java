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

/**
 * Compass: measures sine and cosine of absolute orientation angle (0 = east)
 * 
 */
public class CompassSensor extends AbstractSensor {

	private static final String NAME = "Compass sensor";

	//default constructor
	public CompassSensor(Environment environment) {
		super(environment, NAME);
	}

	@Override
	public int getDimension() {
		return 2;
	}

	@Override
	public void doMeasurement(Disk disk, double pos[]) {
		double angle = disk.getAngle();
		pos[0] = Math.sin(angle);
		pos[1] = Math.cos(angle);
	}

	@Override
	public double getRealWorldInterpretation(double[] measurement, int index) {
		return measurement[index];
	}

	@Override
	public String getRealWorldMeaning(int index) {
		return (index == 0 ? "sine " : "cosine ") + " of absolute orientation angle";
	}

}
