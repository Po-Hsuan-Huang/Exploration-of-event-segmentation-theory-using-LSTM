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

/**
 * Interface for classes modelling the handling of friction
 * 
 * @author Jan
 */
public interface FrictionModel {

	/**
	 * Calculate the force that acts on a disk due to friction
	 * 
	 * @param disk
	 *            the disk to be considered
	 * @param velocityx
	 *            the x component of the speed vector
	 * @param velocityy
	 *            the y component of the speed vector
	 * @return
	 *         an array of length 2 containing x and y component of the force
	 */
	public double[] getFrictionForce(Disk disk, double velocityx, double velocityy);

	/**
	 * Provides a measure for the resistance of a disk against displacement. This is used for
	 * determining which part of the agent moves how much in case of ego-motion: If not
	 * a part of an agent is fixed, the sum of all displacement resistances is minimised
	 * to determine position and orientation after an ego-motion.
	 * In order to be able to resolve ego motions in friction-free scenarios, the returned value should
	 * not be 0.
	 * 
	 * @param disk
	 *            the disk to be considered
	 * @return a value > 0 specifying the resistance of the disk against displacement. Typically this is a
	 *         combination of mass and floor contact.
	 */
	public double getDisplacementResistance(Disk disk);

}