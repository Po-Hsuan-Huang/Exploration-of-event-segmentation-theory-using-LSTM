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
 * Class for a vertex of the skeleton graph.
 * A vertex maintains a set of permanent edges it is attached to and the island to which it belongs and the disk it corresponds to.
 * Transient edges are not registered by vertex objects.
 * A vertex also carries a boolean marker for flood filling.
 * 
 * @author Jan
 */
public class Vertex {

	//code to produce deterministic hash values 
	private static int instanceCount = 0;
	private final int instanceID = ++instanceCount;

	@Override
	public int hashCode() {
		return instanceID;
	}

	private final Disk disk;
	private final Set<PermanentEdge> edges;
	private boolean mark;
	private Island island;

	/**
	 * Creates a vertex, giving its island and disk.
	 * 
	 * @param disk
	 *            the Disk object to which this vertex belongs
	 * @param island
	 *            the island to which this vertex belongs (initially)
	 */
	public Vertex(Disk disk, Island island) {
		edges = new HashSet<PermanentEdge>();
		mark = false;
		this.disk = disk;
		this.island = island;
	}

	/**
	 * Assign this vertex to another island
	 * 
	 * @param island
	 *            the new island to which this vertex shall belong to from now on
	 */
	public void moveToIsland(Island island) {
		this.island = island;
	}

	/**
	 * Access to all edges
	 * 
	 * @return set of edges attached to this vertex
	 */
	public Set<PermanentEdge> getEdges() {
		return edges;
	}

	/**
	 * Detach edge from this vertex
	 * 
	 * @param edge
	 *            edge object to be removed from the set of edges
	 */
	public void detachEdge(PermanentEdge edge) {
		edges.remove(edge);
	}

	/**
	 * Attach edge to this vertex
	 * 
	 * @param edge
	 *            edge object to be added to the set of edges
	 */
	public void attachEdge(PermanentEdge edge) {
		edges.add(edge);
	}

	/**
	 * Access to the island object
	 * 
	 * @return the island which this vertex currently belongs to
	 */
	public Island getIsland() {
		return island;
	}

	/**
	 * The edge between this vertex and the other vertex
	 * 
	 * @param otherVertex
	 *            a neighbot of this vertex
	 * @return edge between this and otherVertex
	 */
	public PermanentEdge getEdge(Vertex otherVertex) {
		for (PermanentEdge e : edges) {
			if (e.getOtherVertex(this) == otherVertex) {
				return e;
			}
		}
		throw new SkeletonException("other vertex is not a neighbor");
	}

	/**
	 * The disk that corresponds to this vertex.
	 * 
	 * @return Disk object passed in the constructor
	 */
	public Disk getDisk() {
		return disk;
	}

	/**
	 * Set the flood fill marker
	 */
	public void setMark() {
		mark = true;
	}

	/**
	 * Clear the flood fill marker
	 */
	public void clearMark() {
		mark = false;
	}

	/**
	 * Test the flood fill marker
	 * 
	 * @return true if marker was set
	 */
	public boolean isMarked() {
		return mark;
	}

}
