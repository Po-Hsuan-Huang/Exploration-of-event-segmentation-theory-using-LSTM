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

/**
 * 
 * Class for an non-oriented edge between two vertices (can be permanent or transient).
 * 
 * @author Jan
 */
public class PermanentEdge {

	//code to produce deterministic hash values 
	private static int instanceCount = 0;
	private final int instanceID = ++instanceCount;

	@Override
	public int hashCode() {
		return instanceID;
	}

	private Vertex v1, v2;

	/**
	 * Creates edge object between two vertices and attaches itself to the two vertices.
	 * 
	 * @param vertex1
	 *            first vertex
	 * @param vertex2
	 *            second vertex
	 */
	public PermanentEdge(Vertex vertex1, Vertex vertex2) {
		if (vertex1 == vertex2)
			throw new SkeletonException("cannot have edge between identical vertices");
		v1 = vertex1;
		v2 = vertex2;
		v1.attachEdge(this);
		v2.attachEdge(this);
	}

	/**
	 * Given one vertex of the edge, determine the other.
	 * 
	 * @param vertex
	 *            one vertex of this edge
	 * @return the other vertex of this edge
	 */
	public Vertex getOtherVertex(Vertex vertex) {
		if (v1 == vertex) {
			return v2;
		}
		if (v2 == vertex) {
			return v1;
		}
		throw new SkeletonException("edge does not contain vertex");
	}

	/**
	 * Access to the first vertex
	 * 
	 * @return first vertex object
	 */
	public Vertex getVertex1() {
		return v1;
	}

	/**
	 * Access to the second vertex
	 * 
	 * @return second vertex object
	 */
	public Vertex getVertex2() {
		return v2;
	}

}
