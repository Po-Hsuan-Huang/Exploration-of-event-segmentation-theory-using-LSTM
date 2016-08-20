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
import java.util.Set;

import diskworld.Disk;
import diskworld.linalg2D.AngleUtils;

/**
 * Changes the position and orientation of a neighbouring disk. All other neighbours of that neighbour keep their relative position.
 * All disks except the neighbour branch attached to the disk will remain.
 * 
 * @author Jan
 * 
 */
class ChangeAngle implements DiskAction {

	private final Disk disk, neighbour, refDisk;
	protected final double maxAngularSpeed;
	private final String name;
	protected final double[] range;
	private final boolean correctAngle;
	private double energyConsumed;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of the operation
	 * @param disk
	 *            the disk that performs the action
	 * @param neighbour
	 *            the neighbour that is moved, may be null for a root joint (in which case only the disk orientation is changed)
	 * @param refDisk
	 *            the disk that is the center of the rotation and relative to the orientation of which angles are measured
	 * @param maxAngularSpeed
	 *            the maximum speed by which angles can be increased/decreased
	 * @param range
	 *            the angular range to which the resulting angle will be clipped, or null for no limitation of range
	 * @param rootSpin
	 *            true if the change of angle affects the root disk
	 */
	public ChangeAngle(String name, Disk disk, Disk neighbour, Disk refDisk, double maxAngularSpeed, double[] range, boolean rootSpin) {
		if (!disk.getDiskComplex().isControllable(disk))
			throw new IllegalArgumentException("disk must be controllable!");
		this.disk = disk;
		this.neighbour = neighbour;
		this.refDisk = refDisk;
		this.maxAngularSpeed = maxAngularSpeed;
		this.name = name;
		this.range = range;
		this.correctAngle = !rootSpin;
	}

	protected double getRangeMin() {
		return range == null ? -Math.PI : range[0];
	}

	protected double getRangeMax() {
		return range == null ? Math.PI : range[1];
	}

	@Override
	public Disk targetDisk() {
		return disk;
	}

	@Override
	public Map<Disk, DiskModification> translateIntoDiskModifications(double angularSpeed, double timestep) {
		// check angular range
		double angleChange = angularSpeed * timestep;
		if (range != null) {
			double currentAngle = getAngle();
			if (currentAngle + angleChange < range[0]) {
				angleChange = range[0] - currentAngle;
			}
			if (currentAngle + angleChange > range[1]) {
				angleChange = range[1] - currentAngle;
			}
		}
		return rotate(angleChange); // - sign since we rotate the parent not the disk itself
	}

	@Override
	public double getMinActionValue() {
		return -maxAngularSpeed;
	}

	@Override
	public double getMaxActionValue() {
		return maxAngularSpeed;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * The current angle relative to refDisk
	 * 
	 * @return Direction from neighbour to disk, relative to the orientation of refDisk
	 */
	protected double getAngle() {
		double angle = refDisk.getAngle();
		if (neighbour != null) {
			double dx = disk.getX() - neighbour.getX();
			double dy = disk.getY() - neighbour.getY();
			angle -= Math.atan2(dy, dx);
		}
		if (neighbour == refDisk) {
			// slide operation, angle changes with negative sign!
			angle = -angle;
		}
		return AngleUtils.closestBigger(angle, getMiddleRangeComplement());
	}

	private double getMiddleRangeComplement() {
		return range == null ? -Math.PI : (range[0] + range[1]) / 2.0 - Math.PI;
	}

	/**
	 * Rotate moving set around center of ref disk
	 * 
	 * @param refDisk
	 * @param angleChange
	 * @param frictionModel
	 * @param frictionModel
	 * @return map from Disk to DiskModifications
	 */
	protected Map<Disk, DiskModification> rotate(double angleChange) {
		if (neighbour != null) {
			// not the root disk: 
			// instead of rotating the disk itself by angleChange, we rotate its parent by -angleChange
			angleChange *= -1;
		}
		double x = refDisk.getX();
		double y = refDisk.getY();
		double sin = Math.sin(angleChange);
		double cos = Math.cos(angleChange);
		// TODO: make deterministic, provide deerministic hash value
		Map<Disk, DiskModification> dm = new HashMap<Disk, DiskModification>();
		if (neighbour == null) {
			// the disk is the root joint, rotating all disks 
			for (Disk d : disk.getDiskComplex().getDisks()) {
				double dx = d.getX() - x;
				double dy = d.getY() - y;
				dm.put(d, new DiskModification(d, x + dx * cos - dy * sin, y + dx * sin + dy * cos, d.getAngle() + angleChange));
			}
		} else {
			//get moving set of the neighbor
			Set<Disk> movingSet = disk.getDiskComplex().getMovingSets(disk).get(neighbour);
			if (movingSet == null)
				throw new RuntimeException("no moving set obtained");

			// check if all fixed disks are inside or outside, otherwise the action is not possible
			Set<Disk> fixedDisks = disk.getDiskComplex().getFixedDisks();
			int count = 0;
			for (Disk d : fixedDisks) {
				if (movingSet.contains(d))
					count++;
			}
			if ((count != 0) && (count != fixedDisks.size())) {
				return null;
			}

			// construct disk modification list
			for (Disk d : movingSet) {
				double dx = d.getX() - x;
				double dy = d.getY() - y;
				dm.put(d, new DiskModification(d, x + dx * cos - dy * sin, y + dx * sin + dy * cos, d.getAngle() + angleChange));
			}
		}
		return dm;
	}

	@Override
	public boolean correctAngle() {
		return correctAngle;
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
