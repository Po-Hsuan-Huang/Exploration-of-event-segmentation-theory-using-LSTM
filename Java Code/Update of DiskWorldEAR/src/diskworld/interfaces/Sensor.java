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
package diskworld.interfaces;

import diskworld.Disk;
import diskworld.visualization.VisualisationItem;

/**
 * Interface for sensors. A sensor should be generic, which means not hold any reference to the Disk to which it is attached to. In particular, it should be possible to use the same Sensor object
 * shared for multiple Disks. In order to compute the actual sensor measurement, the Disk and the Floor are passed as arguments.
 * 
 * Sensors produce measurements that are arrays of specific length with values in the interval [-1,1]. However some sensor implementations
 * may choose to use only the positive subrange [0,1] or may to produce only boolean results (values in {0,1}) in some of the
 * returned array components.
 * 
 * If the sensor measurements need to be interpreted in "real world units", the corresponding helper method can be used. Example
 * of "real world units" are angles (in degrees), distances, discrete values (e.g. FloorType index) etc.
 */
public interface Sensor {

	/**
	 * Dimensionality of the sensor measurement
	 * 
	 * @return the number of values produced by this sensor in every time step
	 */
	public int getDimension();

	/**
	 * Obtain the "real world" interpretation of the sensor measurement in the given component of the measurement array
	 * 
	 * @param disk
	 *            the Disk to which the sensor is supposed to be attached to
	 * @param values
	 *            array of doubles (length==getDim()) that is supposed to be filled with the measurement results in the interval [getMinValue(),getMaxValue()]
	 */

	public void doMeasurement(Disk disk, double values[]);

	/**
	 * Obtain the "real world" interpretation of the sensor measurement in the given component of the measurement array
	 * 
	 * @param measurement
	 *            the measurement array (all values are in [-1,1])
	 * @param index
	 *            the index of the requested component in the measurement array
	 * @return
	 *         real world interpretation of the measurement
	 */
	public double getRealWorldInterpretation(double measurement[], int index);

	/**
	 * Obtain the meaning and unit of the "real world" interpretation of the sensor measurement in the given component of the measurement array
	 * 
	 * @param index
	 *            the index of the measurement in the measurement array
	 * @return
	 *         string describing meaning and unit real world interpretation (e.g. "relative angle [degrees]")
	 */
	public String getRealWorldMeaning(int index);

	/**
	 * Creates a visualization item, to be attached to DiskTypes that use the sensor
	 * 
	 * @return
	 *         a new visualization item, or null if no visualization supported
	 */
	public VisualisationItem getVisualisationItem();

}
