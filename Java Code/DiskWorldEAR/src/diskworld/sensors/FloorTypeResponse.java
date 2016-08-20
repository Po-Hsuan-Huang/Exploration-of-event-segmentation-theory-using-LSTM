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

import diskworld.environment.FloorCellType;

/**
 * Interface for sensors that respond with floor type specific values (e.g. RGB values)
 * 
 * @author Jan
 * 
 */
public interface FloorTypeResponse {

	/**
	 * Provide the dimensionality of the response value array (i.e. 3 in case of RGB values)
	 * 
	 * @return number of components to be determined
	 */
	public int getDim();

	/**
	 * Store the response in the given array, starting at the given index. It can be assumed
	 * that array has sufficient length.
	 * 
	 * @param cellType
	 *            the type of the floor cell for which the measurement value is to be determined
	 * @param measurement
	 *            the measurement array into which values shall be written
	 * @param startIndex
	 *            start position in the measurement array, first component shall be written at this index
	 */
	public void putResponse(FloorCellType cellType, double[] measurement, int startIndex);

}
