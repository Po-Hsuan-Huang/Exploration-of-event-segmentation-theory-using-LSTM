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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import diskworld.Disk;

/**
 * Changes the radius of a disk by a factor. The range for the action value is [-1,1] and is mapped exponentially
 * to the interval [1/maxGrowingFactor,maxGrowingFactor]. Thus performing the action with -x as action value parameter
 * exactly cancels the operation with +x as action value.
 * All disks attached to the disk that is grown/shrunk will be shifted in position accordingly.
 * 
 * @author Jan
 * 
 */
/**
 * @author Jan
 * 
 */
class ChangeRadius implements DiskAction {

	protected Disk disk;
	protected double range[];
	protected double logMaxGrowingRate;
	private String name;
	private double energyConsumed;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of the operation
	 * @param disk
	 *            the disk to be grown/shrunk
	 * @param maxGrowingRate
	 *            a number > 1.0 that gives the maximum growing factor per time unit
	 * @param range
	 *            the range [minRadius,maxRadius] of possible radius values
	 */
	public ChangeRadius(String name, Disk disk, double maxGrowingRate, double range[]) {
		if (!disk.getDiskComplex().isControllable(disk))
			throw new IllegalArgumentException("disk must be controllable!");
		if (maxGrowingRate <= 1.0)
			throw new IllegalArgumentException("max growing rate must be > 1!");
		this.disk = disk;
		this.logMaxGrowingRate = Math.log(maxGrowingRate);
		this.range = range;
		if (range != null) {
			if (range[0] > range[1])
				throw new IllegalArgumentException("min and max radius not valid");
			if (range[0] <= 0.0)
				throw new IllegalArgumentException("min radius not valid");
		}
	}

	@Override
	public Disk targetDisk() {
		return disk;
	}

	@Override
	public Map<Disk, DiskModification> translateIntoDiskModifications(double actionValue, double timestep) {
		return multiplyRadius(Math.exp(actionValue * timestep * logMaxGrowingRate));
	}

	public Map<Disk, DiskModification> multiplyRadius(double scalefactor) {
		double oldRadius = disk.getRadius();
		double newRadius = scalefactor * oldRadius;
		//System.out.println("Change radius: " + oldRadius + " --> " + newRadius);
		if (range != null) {
			if (newRadius > range[1]) {
				newRadius = range[1];
			}
			if (newRadius < range[0]) {
				newRadius = range[0];
			}
		}

		// get moving sets
		Map<Disk, Set<Disk>> movingSets = disk.getDiskComplex().getMovingSets(disk);

		// check if all fixed disks are inside the same moving set
		Set<Disk> fixedDisks = disk.getDiskComplex().getFixedDisks();
		for (Set<Disk> s : movingSets.values()) {
			int count = 0;
			for (Disk d : fixedDisks) {
				if (s.contains(d)) {
					count++;
				}
			}
			if ((count != 0) && (count != fixedDisks.size())) {
				return null;
			}
		}
		// construct disk modification list
		Map<Disk, DiskModification> dm = new HashMap<Disk, DiskModification>();
		dm.put(disk, new DiskModification(disk, newRadius));
		for (Entry<Disk, Set<Disk>> e : movingSets.entrySet()) {
			Disk d = e.getKey();
			double dx = d.getX() - disk.getX();
			double dy = d.getY() - disk.getY();
			double dist = Math.sqrt(dx * dx + dy * dy);
			double factor = (newRadius - oldRadius) / dist;
			dx *= factor;
			dy *= factor;
			for (Disk d2 : e.getValue()) {
				dm.put(d2, new DiskModification(d2, d2.getX() + dx, d2.getY() + dy));
			}
		}
		return dm;
	}

	@Override
	public double getMinActionValue() {
		return -1.0;
	}

	@Override
	public double getMaxActionValue() {
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
		this.energyConsumed = energyConsumed;
	}

	@Override
	public double getEnergyConsumedByActionsAndActuators() {
		return energyConsumed;
	}
}
