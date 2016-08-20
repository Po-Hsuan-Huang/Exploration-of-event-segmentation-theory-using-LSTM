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

import diskworld.Disk;

/**
 * Encapsulates the modification of a disk (in a DiskComplexEnsemble).
 * This may involve x,y coordinates, rotation angle or radius.
 */
public class DiskModification {
	private final Disk disk;
	private final double newx, newy;
	private final double newangle;
	private final double newradius;
	private final double oldx, oldy;
	private final double oldangle;
	private final double oldradius;

	/**
	 * Constructor specifying changes to a disk.
	 * 
	 * @param disk
	 *            the disk to be changed
	 * @param x
	 *            the new absolute x coordinate
	 * @param y
	 *            the new absolute y coordinate
	 * @param angle
	 *            the new absolute angle
	 * @param radius
	 *            the new radius
	 */
	public DiskModification(Disk disk, double x, double y, double angle, double radius) {
		this.disk = disk;
		oldx = disk.getX();
		oldy = disk.getY();
		oldangle = disk.getAngle();
		oldradius = disk.getRadius();
		newx = x;
		newy = y;
		assert (!Double.isNaN(newx));
		assert (!Double.isNaN(newy));
		newangle = angle;
		newradius = radius;
	}

	/**
	 * Convenience constructor letting the radius unchanged.
	 * 
	 * @param disk
	 *            the disk to be changed
	 * @param x
	 *            the new absolute x coordinate
	 * @param y
	 *            the new absolute y coordinate
	 * @param angle
	 *            the new absolute angle
	 */
	public DiskModification(Disk disk, double x, double y, double angle) {
		this(disk, x, y, angle, disk.getRadius());
	}

	/**
	 * Convenience constructor letting the angle and radius unchanged.
	 * 
	 * @param disk
	 *            the disk to be changed
	 * @param x
	 *            the new absolute x coordinate
	 * @param y
	 *            the new absolute y coordinate
	 */
	public DiskModification(Disk disk, double x, double y) {
		this(disk, x, y, disk.getAngle(), disk.getRadius());
	}

	/**
	 * Convenience constructor changing only the radius
	 * 
	 * @param disk
	 *            the disk to be changed
	 * @param radius
	 *            the new radius
	 */
	public DiskModification(Disk disk, double radius) {
		this(disk, disk.getX(), disk.getY(), disk.getAngle(), radius);
	}

	public Disk getDisk() {
		return disk;
	}

	public boolean changesPosition() {
		return (oldx != newx) || (oldy != newy);
	}

	public boolean changesAngle() {
		return (oldangle != newangle);
	}

	public boolean changesRadius() {
		return (oldradius != newradius);
	}

	public double getOldX() {
		return oldx;
	}

	public double getOldY() {
		return oldy;
	}

	public double getOldRadius() {
		return oldradius;
	}

	public double getNewX() {
		return newx;
	}

	public double getNewY() {
		return newy;
	}

	public double getDeltaX() {
		return newx - oldx;
	}

	public double getDeltaY() {
		return newy - oldy;
	}

	public double getNewRadius() {
		return newradius;
	}

	public double getNewAngle() {
		return newangle;
	}

	public double getOldAngle() {
		return oldangle;
	}

	public double getDisplacement() {
		double dx = newx - oldx;
		double dy = newy - oldy;
		return Math.sqrt(dx * dx + dy * dy);
	}

}
