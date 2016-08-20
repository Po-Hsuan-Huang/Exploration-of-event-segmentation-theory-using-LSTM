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
import diskworld.Environment;
import diskworld.visualization.VisualisationItem;

/**
 * Interface for actuators. An actuator should be generic, which means it should not hold any reference to the Disk to which it is attached to.
 * In particular, it should be possible to use the same Actuator object shared for multiple Disks.
 * The control value send to the actuator is called "activity". The activity is a property of the disk object
 * and is passed to the evaluateEffect() method to perform the actuators effect on the disk.
 * 
 * Actuators obtain activity values as input that are given as array of specific length with values in the interval [-1,1].
 * However some actuator implementations may choose to use only the positive subrange [0,1] or only boolean results.
 * In the first case negative activities are handled as if 0.0 was passed. In the second case values < 0.5 are treated as 0.0 (=false)
 * and values >= 0.5 are treated as 1.0 (= true).
 * 
 * If the activity values need to be determined in "real world units", the corresponding helper method can be used. Example
 * of "real world units" are angles, speeds, discrete values (e.g. FloorType index) etc.
 * The real world interpretation of changes (velocities, angular speeds, etc.) should be understood "per simulation time unit"
 * and thus be independent of the step size of simulation steps.
 * 
 * Note that the default setting for all activities of an actuator is defined to 0.0. This is the so-called "do-nothing" setting,
 * in which the actuator shows no (or minimal) effect. Also the energy consumption is supposed to be smallest for the do-nothing
 * setting (it is recommended to set the "do-nothing" energy consumption to 0.0 for all actuators and pack all constant energy
 * consumption contributions into a single constant (e.g. called "constant metabolism costs").
 * 
 */
public interface Actuator {

	/**
	 * The dimensionality of the actuator values.
	 * 
	 * @return number of actuator values expected in evaluateEffect
	 */
	public int getDim();

	/**
	 * indicates if the activity value in the given component of the activity array is always in [0,1]
	 * 
	 * @param index
	 *            the index of the activity value in the activity array
	 * @return
	 *         true if the activity value is limited to the non-negative range
	 */
	public boolean isAlwaysNonNegative(int index);

	/**
	 * indicates if the activity value in the given component of the activity array is boolean
	 * 
	 * @param index
	 *            the index of the activity value in the activity array
	 * @return
	 *         true if the activity value is limited to the values 0.0 and 1.0
	 */
	public boolean isBoolean(int index);

	/**
	 * Obtain the activity value of a "real world" control value in the given component of the measurement array
	 * 
	 * @param realWorldValue
	 *            the data to be translated
	 * @param index
	 *            the index in the activity array
	 * @return
	 *         corresponding activity value (a value in [-1,1])
	 */
	public double getActivityFromRealWorldData(double realWorldValue, int index);

	/**
	 * Obtain the meaning and unit of the "real world" interpretation of the activity value in the given component of the activity array
	 * 
	 * @param index
	 *            the index in the activity array
	 * @return
	 *         string describing meaning and unit of real world interpretation of this activity value component (e.g. "disk orientation change [degrees]")
	 */
	public String getRealWorldMeaning(int index);

	/**
	 * Perform the effect of the Actuator on the given disk (possibly depending on the floor state).
	 * A time step calculation may be split up into several time slices. Thus this method may be
	 * called several times for a single time step (once per time slice). Both the size of the
	 * current time slice and the total time step size (= sum of time slice sizes) is provided.
	 * 
	 * @param disk
	 *            the Disk to which the sensor is supposed to be attached to
	 * @param environment
	 *            the environment in which the actuator lives
	 * @param activity
	 *            the current activation values, an array of doubles in [getRangeMin(),getRangeMax()] of size getDim()
	 * @param partial_dt
	 *            length of the time slice for which the effect shall be calculated
	 * @param total_dt
	 *            total length of the time step (sum of all time slices)
	 * @param firstSlice
	 *            true if this is the first call of this method (in the current time step)
	 * @return amount of energy consumed by the actuator in this time slice
	 */
	public double evaluateEffect(Disk disk, Environment environment, double[] activity, double partial_dt, double total_dt, boolean firstSlice);

	/**
	 * Creates a visualization item, to be attached to DiskTypes that use the sensor
	 * 
	 * @return
	 *         a new visualization item, or null if no visualization supported
	 */
	public VisualisationItem getVisualisationItem();
}
