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
 * Interface for a Lookuptable that shall be called when the configuration of disks has 
 * changed. Methods are called for each changed disk individually.
 */
public interface DiskChangeListener {
	/**
	 * Called when a disk has changed its position
	 * 
	 * @param d the disk that has changed its place
	 */
	public void diskHasMoved(Disk d);

	/**
	 * Called when a disk was created
	 * 
	 * @param d the disk that has appeared
	 */
	public void diskWasAdded(Disk d);

	/**
	 * Called when a disk was removed
	 * 
	 * @param d the disk that has disappeared
	 */
	public void diskWasRemoved(Disk d);

	/**
	 * Called when a disk has changed its size
	 * 
	 * @param d the disk that has changed its radius
	 */
	public void diskHasChangedRadius(Disk d);	
	
}
