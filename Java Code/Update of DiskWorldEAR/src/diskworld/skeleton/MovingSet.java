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
package diskworld.skeleton;

import java.util.HashSet;
import java.util.Set;

import diskworld.Disk;

/**
 * Holds the vertices of one island that move together, plus the other islands that are connected exclusively to this moving set
 * 
 * @author Jan
 * 
 */
class MovingSet {

	private final Set<Vertex> islandVertices;
	private Set<Island> attachedIslands;

	/**
	 * Creates a new Moving set.
	 * 
	 * @param islandVertices
	 *            the vertices that are (initially) in this moving set. The set passed as argument will be cloned and can thus later be changed without affecting this MovingSet.
	 */
	public MovingSet(Set<Vertex> islandVertices) {
		this.islandVertices = new HashSet<Vertex>();
		this.islandVertices.addAll(islandVertices);
		this.attachedIslands = new HashSet<Island>();
	}

	/**
	 * Creates a new Moving set with a single vertex.
	 * 
	 * @param islandVertex
	 *            the only vertex that is put in the islandVertices set
	 */
	public MovingSet(Vertex islandVertex) {
		this.islandVertices = new HashSet<Vertex>();
		this.islandVertices.add(islandVertex);
		this.attachedIslands = new HashSet<Island>();
	}

	/**
	 * Provides access to the set vertices of the respective island that move together.
	 * 
	 * @return set of vertices of the island with the control vertex that move together
	 */
	public Set<Vertex> getIslandVertices() {
		return islandVertices;
	}

	/**
	 * Provides access to other islands (not containing the control vertex) that move together with this moving set
	 * 
	 * @param attachedIslands
	 *            set of islands not containing the control vertex that move together with the vertices returned by getIslandVertices()
	 */
	public void setAttachedIslands(Set<Island> attachedIslands) {
		this.attachedIslands = attachedIslands;
	}

	/**
	 * Clears the set of attached islands
	 */
	public void clearAttachedIslands() {
		attachedIslands.clear();
	}

	/**
	 * Add an island to the set of attached islands
	 */
	public void addAttachedIsland(Island island) {
		attachedIslands.add(island);
	}

	/**
	 * Create a list of all disks that move together. These correspond to the islandVertices and the attached islands
	 * 
	 * @return list of all Disk objects that move together in this MovingSet
	 */
	public Set<Disk> getAllMovingDisks() {
		Set<Disk> res = new HashSet<Disk>();
		for (Vertex v : islandVertices) {
			res.add(v.getDisk());
		}
		for (Island island : attachedIslands) {
			for (Vertex v : island.getVertices()) {
				res.add(v.getDisk());
			}
		}
		return res;
	}

}
