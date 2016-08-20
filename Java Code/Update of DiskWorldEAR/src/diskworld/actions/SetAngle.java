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
import diskworld.linalg2D.AngleUtils;

/**
 * Set the position of a neighbouring disk (orientation changes accordingly). All other neighbours of that neighbour keep their relative position.
 * All disks except the neighbour branch attached to the disk will remain.
 * 
 * @author Jan
 * 
 */
class SetAngle extends ChangeAngle {

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of the operation
	 * @param disk
	 *            the disk to be rotated
	 * @param refDisk
	 *            the disk that is the center of the rotation and relative to the orientation of which angles are measured
	 * @param neighbor
	 *            the neighbour relative to which the disk rotates
	 * @param maxAngularSpeed
	 *            the maximum speed by which angles can be increased/decreased in a time step
	 * @param range
	 *            the angular range to which the resulting angle will be clipped, or null for no limitation of range
	 * @param rootSpin
	 *            true if the change of angle affects the root disk
	 */
	public SetAngle(String name, Disk disk, Disk neighbor, Disk refDisk, double maxAngularSpeed, double[] range, boolean rootSpin) {
		super(name, disk, neighbor, refDisk, maxAngularSpeed, range, rootSpin);
	}

	@Override
	public Map<Disk, DiskModification> translateIntoDiskModifications(double targetAngle, double timestep) {
		double angleChange = determineDirection(targetAngle, getAngle());
		double maxAbsAngleChange = maxAngularSpeed * timestep;
		// check max change limit
		if (angleChange > maxAbsAngleChange) {
			angleChange = maxAbsAngleChange;
		}
		if (angleChange < -maxAbsAngleChange) {
			angleChange = -maxAbsAngleChange;
		}
		return rotate(angleChange);
	}

	/**
	 * Determine in which of the two possible directions to move from currentAngle to targetAngle
	 */
	private double determineDirection(double targetAngle, double currentAngle) {
		if (range == null) {
			// no range specified, go the shorter way
			return AngleUtils.mod2PI(targetAngle - currentAngle);
		} else {
			double targetInRange = AngleUtils.closestBigger(targetAngle, range[0]);
			return targetInRange - currentAngle;
		}
	}

	@Override
	public double getMinActionValue() {
		return getRangeMin();
	}

	@Override
	public double getMaxActionValue() {
		return getRangeMax();
	}

}
