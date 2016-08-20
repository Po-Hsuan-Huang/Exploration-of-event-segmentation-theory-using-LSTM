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
import diskworld.environment.FloorCellType;

/**
 * Sensor that measures the floor type (of the cell in which the center of the disk is currently located).
 * 
 */
public class FloorSensor extends AbstractSensor {

	private static final String NAME = "FloorSensor";
	private FloorTypeResponse responseFunction;
	
	//default constructor
	public FloorSensor(Environment environment, FloorTypeResponse responseFunction) {
		super(environment, NAME);
		this.responseFunction = responseFunction;
	}

	@Override
	public int getDimension() {
		return responseFunction.getDim();
	}

	@Override
	public void doMeasurement(Disk disk, double measurement[]) {
		FloorCellType cellType = environment.getFloorCellTypeAtPosition(disk.getX(), disk.getY());
		responseFunction.putResponse(cellType, measurement, 0);
	}

	@Override
	public double getRealWorldInterpretation(double[] measurement, int index) {
		return measurement[index];
	}

	@Override
	public String getRealWorldMeaning(int index) {
		return "floor type response #" + index;
	}

}
