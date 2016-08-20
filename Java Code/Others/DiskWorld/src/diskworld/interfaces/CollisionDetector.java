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

import java.util.Collection;
import java.util.LinkedList;

import diskworld.DiskComplex;
import diskworld.collisions.Collision;
import diskworld.environment.Wall;

public interface CollisionDetector {
	/**
	 * Determines collisions of disks in the specified DiskComplex.
	 * This may be collisions with other disks of the same DiskComplex, with disks of other
	 * DiskComplexes in the DiskComplexEnsemble or collisions with walls.
	 * 
	 * @param diskComplex
	 *            the DiskComplex whose disks are to be checked for collisions
	 * @return
	 *         a list of all collisions found
	 */
	public LinkedList<Collision> getCollisions(DiskComplex diskComplex);

	/**
	 * Determines if a wall can be added (i.e. dioes not collide with any existing disk)
	 * 
	 * @param wall the wall to be added
	 * @return true if the wall was added successfully
	 */
	public boolean canAddWall(Wall wall);

	/**
	 * Removes a wall from the list of walls maintained by this collision detector
	 * 
	 * @param wall the wall to be removed
	 * @return true if the wall existed in the list, false otherwise
	 */
	public boolean removeWall(Wall wall);
	
	
	/**
	 * Returns a list of all existing walls.
	 * 
	 * @return list of walls
	 */
	public Collection<Wall> getWalls();
	
	
}
