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
package diskworld.actions;

import java.util.Map;

import diskworld.Disk;

/*
 * Interface that is implemented by the three different actions that an agent can perform:
 * - changing the activation of a disk (only effective if an actuator is attached to the disk, depending on its DiskType)
 * - ego motion (change angular position of two neighbouring disks)
 * - growing/shrinking (change of radius of a disk)
 * 
 * @author Jan
 *
 */
public interface DiskAction {
	/**
	 * Disk that is targeted by the action (although also other disk's positions may be affected by the action)
	 */
	public Disk targetDisk();

	/**
	 * Carries out the action
	 * 
	 * @param actionValue
	 *            the value that determines how much of the specific action will be executed
	 * @param timestep
	 *            length of the action (dt)
	 * 
	 * @return Map of DiskModifications, if no DiskModifications are invoked an empty map is returned, if the action failed, null is returned
	 */
	public Map<Disk, DiskModification> translateIntoDiskModifications(double actionValue, double timestep);

	/**
	 * Provide the smallest allowed action value
	 * 
	 * @return minimum action value
	 */
	public double getMinActionValue();

	/**
	 * Provide the greatest allowed action value
	 * 
	 * @return maximum action value
	 */
	public double getMaxActionValue();

	/**
	 * Provides the name of the action
	 * 
	 * @return name, indicating place of the disk and type of the action
	 */
	public String getName();

	/**
	 * Determines if the orientation angle is corrected.
	 * 
	 * @return true if the orientation angle shall be corrected
	 */
	public boolean correctAngle();

	/**
	 * Stores the amount of energy consumed by this action.
	 * 
	 * @param energyConsumed
	 *            a value >= 0 giving amount of energy consumed by trying to perform this action (can be > 0 even if the action has failed)
	 */
	public void setEnergyConsumedByAction(double energyConsumed);

	/**
	 * Provides the previously store energy consumption.
	 * 
	 * @return energy consumed by last execution trial of this action
	 */
	public double getEnergyConsumedByActionsAndActuators();

}
