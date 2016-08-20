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

import diskworld.environment.FloorCellType;

public class RGBColorFloorTypeResponse implements FloorTypeResponse {

	@Override
	public int getDim() {
		return 3;
	}

	@Override
	public void putResponse(FloorCellType cellType, double[] measurement, int startIndex) {
		Color col = cellType.getDisplayColor();
		float[] compArray = new float[3];
		col.getRGBColorComponents(compArray);
		measurement[startIndex] = compArray[0];
		measurement[startIndex+1] = compArray[1];
		measurement[startIndex+2] = compArray[2];
	}

}
