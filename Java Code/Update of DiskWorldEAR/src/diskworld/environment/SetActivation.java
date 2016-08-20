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
package diskworld.environment;

import java.util.HashMap;
import java.util.Map;

import diskworld.Disk;
import diskworld.actions.DiskAction;
import diskworld.actions.DiskModification;

/**
 * Changes the activity of a disk. This has only an effect if an Actuator is attached to the disk (depends on its DiskType)
 * 
 * @author Jan
 * 
 */
class SetActivation implements DiskAction {

	private final String name;
	private final Disk disk;
	private final int index;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of the operation
	 * @param disk
	 *            the disk to be affected
	 * @param index
	 *            the index in the activity array
	 */
	public SetActivation(String name, Disk disk, int index) {
		this.disk = disk;
		this.name = name;
		this.index = index;
	}

	@Override
	public Disk targetDisk() {
		return disk;
	}

	@Override
	public Map<Disk, DiskModification> translateIntoDiskModifications(double actionValue, double timestep) {
		disk.setActivity(index, actionValue);
		// no DiskModifications involved (setting activity does not count as modification)
		return new HashMap<Disk, DiskModification>();
	}

	@Override
	public double getMinActionValue() {
		// TODO this must be removed!
		return -1.0;
	}

	@Override
	public double getMaxActionValue() {
		// TODO this must be removed!
		return 1.0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean correctAngle() {
		return true;
	}

	@Override
	public void setEnergyConsumedByAction(double energyConsumed) {
		if (energyConsumed != 0.0)
			throw new IllegalArgumentException("Activation should never consume energy (energy consumption is handled by the corresponding actuator)");
	}

	@Override
	public double getEnergyConsumedByActionsAndActuators() {
		return disk.getEnergyConsumedByActuator();
	}

}
