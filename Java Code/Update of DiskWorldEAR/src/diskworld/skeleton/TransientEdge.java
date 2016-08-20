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
 * Class that holds a transient edge.
 * 
 * @author Jan
 * 
 */
public class TransientEdge {

	//code to produce deterministic hash values 
	private static int instanceCount = 0;
	private final int instanceID = ++instanceCount;

	@Override
	public int hashCode() {
		return instanceID;
	}

	private final Vertex v1, v2;
	private TransientEdge partner;
	private double length; // used to determine expansion

	/**
	 * private contructor
	 * 
	 * @param v1
	 *            first vertex
	 * @param v2
	 *            second vertex
	 */
	private TransientEdge(Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
		this.partner = null;
		this.length = 0;
	}

	/**
	 * Creates a pair of transient edges.
	 * 
	 * @param va1
	 *            first vertex of first edge
	 * @param va2
	 *            second vertex of first edge
	 * @param vb1
	 *            first vertex of second edge
	 * @param vb2
	 *            second vertex of second edge
	 * @return the two new TransientEdges as array of length 2
	 */
	public static TransientEdge[] createEdgePair(Vertex va1, Vertex va2, Vertex vb1, Vertex vb2) {
		TransientEdge e1 = new TransientEdge(va1, va2);
		TransientEdge e2 = new TransientEdge(va1, va2);
		e1.setPartner(e2);
		e2.setPartner(e1);
		return new TransientEdge[] { e1, e2 };
	}

	/**
	 * Get first vertex
	 * 
	 * @return first vertex
	 */
	public Vertex getVertex1() {
		return v1;
	}

	/**
	 * Get second vertex
	 * 
	 * @return second vertex
	 */
	public Vertex getVertex2() {
		return v2;
	}

	/**
	 * Get partner edge
	 * 
	 * @return partner edge
	 */
	public TransientEdge getPartner() {
		return partner;
	}

	/**
	 * Private method to set partner edge
	 * 
	 * @param partner
	 *            partner edge, created together with this
	 */
	public void setPartner(TransientEdge partner) {
		this.partner = partner;
	}

	/**
	 * Set the length
	 */
	public void setLength(double length) {
		this.length = length;
	}

	/**
	 * Get the length
	 */
	public double getLength() {
		return length;
	}

}
