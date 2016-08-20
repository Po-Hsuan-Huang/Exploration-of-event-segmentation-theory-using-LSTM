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
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * Object to hold a connected set of vertices and their permanent edges. Transient edges are not store in this class.
 * The island also holds a marker integer used for flood filling across islands along transient edges.
 * 
 * @author Jan
 */
public class Island {

	private final Set<Vertex> vertices;
	private int marker;

	/**
	 * Create a new island object without any vertices
	 */
	public Island() {
		vertices = new HashSet<Vertex>();
		marker = 0;
	}

	/**
	 * Add a vertex to the island
	 * 
	 * @param newVertex
	 *            the vertex to be added
	 */
	public void addVertex(Vertex newVertex) {
		vertices.add(newVertex);
	}

	/**
	 * Access to the set of vertices
	 * 
	 * @return set of all vertices in this island
	 */
	public Set<Vertex> getVertices() {
		return vertices;
	}

	/**
	 * Get the marker
	 * 
	 * @return previously stored marker value
	 */
	public int getMarker() {
		return marker;
	}

	/**
	 * Set the marker
	 * 
	 * @param marker
	 *            new marker value
	 */
	public void setMarker(int marker) {
		this.marker = marker;
	}

	/**
	 * Merge all vertices of the other island with this island
	 * 
	 * @param otherIsland
	 *            other island to be merged, will be cleared
	 */
	public void mergeWith(Island otherIsland) {
		vertices.addAll(otherIsland.vertices);
		for (Vertex v : otherIsland.vertices) {
			v.moveToIsland(this);
		}
		otherIsland.vertices.clear();
	}

	/**
	 * Determine of the given edge is a bridge (island splits into two disconnected parts when the edge is removed)
	 * 
	 * @param edge
	 *            the edge to be tested
	 * @return true if the edge is a bridge
	 */
	public boolean isBridge(PermanentEdge edge) {
		clearMarks();
		floodFill(edge.getVertex1(), edge);
		return !edge.getVertex2().isMarked();
	}

	/**
	 * Clear the mark of all vertices
	 */
	private void clearMarks() {
		for (Vertex v : vertices) {
			v.clearMark();
		}
	}

	/**
	 * Flood fill from vertex v, not using the excluded edge
	 */
	private void floodFill(Vertex v, PermanentEdge excludedEdge) {
		if (!v.isMarked()) {
			v.setMark();
			Set<PermanentEdge> edges = v.getEdges();
			for (PermanentEdge e : edges) {
				if (e != excludedEdge) {
					floodFill(e.getOtherVertex(v), excludedEdge);
				}
			}
		}
	}

	/**
	 * Split the Island into two parts by removing the given edge, assuming that isBridge() has been called and has returned true.
	 * One part remains in this island object, the other part is returned as a new island object.
	 * 
	 * @param bridge
	 *            a bridge edge, for which the method isBridge() has been called prior to this method call.
	 * @return
	 *         sub-island after split that has been removed from this island object
	 */
	public Island split(PermanentEdge bridge) {
		Island other = new Island();
		Iterator<Vertex> i = vertices.iterator();
		while (i.hasNext()) {
			Vertex v = i.next();
			if (v.isMarked()) {
				other.addVertex(v);
				v.moveToIsland(other);
				i.remove();
			}
		}
		bridge.getVertex1().detachEdge(bridge);
		bridge.getVertex2().detachEdge(bridge);
		return other;
	}
}
