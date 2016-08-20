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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import diskworld.Disk;
import diskworld.actions.DiskModification;

/**
 * 
 * Each DiskComplex has a a skeleton. It is used to change disk positions/sizes in the disk complex (ego motion) and
 * to determine when a DiskComplex is split into two or more smaller DiskComplexes.
 * 
 * A skeleton is an undirected, connected graph. Each disk of the DiskComplex corresponds to one vertex. Neighbouring relationships
 * correspond to edges. There are two types of edges:
 * - transient edges (may be removed by ego motions)
 * - permanent edges (are persistent, can only removed explicitly from the outside).
 * 
 * The connection components that remain when all transient edges are removed are called "islands". A transient edge is not allowed
 * to have both end points in the same island (each transient edge must thus connect two different islands).
 * 
 * Furthermore some of the vertices may be labelled "control" vertices.
 * In order to allow a vertex to become a control vertex, it must have the following property (but not each vertex with
 * that property is necessarily a declared as control vertex):
 * All permanent edges of a control vertex must be "bridges". An permanent edge is called "bridge" if by removing it, the island it is part of
 * in becomes disconnected (where only permanent edges are considered to determine connectivity). In other words, a control
 * vertex is an place where the island has the shape of a star of multiple disconnected "sub-islands".
 * 
 * Moving set:
 * ===========
 * 
 * For a subset of the edges of a control vertex, we define the "moving set". This is the set of vertices corresponding to disks that
 * move together when ego-motion is applied at the place of the control vertex.
 * 
 * The moving set is determined as follows:
 * - include all sub-islands corresponding to the selected bridges
 * - now check for all other islands (the ones not containing the control vertex) if there is a path from that island to a
 * sub-island in the moving set and if there is a path from that island to a sub-island not in the moving set (where the path may
 * contain transient and permanent edges, but may not pass the control vertex). If this is not the case, the island is added to the moving set.
 * 
 * There are two types of ego motion:
 * Rotation: There are two moving sets: one containing one sub-island, the other containing the rest.
 * Growing/Shrinking: There are as many moving sets as sub-islands (every moving set contains one sub-island).
 * 
 * By definition the moving sets are not connected to each other (even when transient edges are allowed). All islands
 * that are in neither of the moving sets are called "resolvable".
 * 
 * Allowable ego motions:
 * =======================
 * 
 * The two disks corresponding to the end points of permanent edges are never overlapping.
 * The two disks corresponding to the end points of permanent edges can be overlapping (and should do so, most of the time).
 * 
 * An ego-motion is performed by moving all disks of each moving set "together" (translation and/or rotation). An ego-motion is allowable
 * if it does not lead to any collisions between disks of different moving sets (existing overlaps are irrelevant) and
 * if it does not contract along any of the edges between moving set and resolvable islands ("contraction" meaning that the distance
 * between the disks in question is shorter after the ego-motion).
 * 
 * Removal of transient edges by ego motions:
 * ==========================================
 * 
 * A transient edge that is a bridge (when allowing both transient and permanent vertices) is called "island bridge".
 * Transient edges are always introduced in pairs (see below). The pairing is maintained during the lifetime of each transient edge.
 * A pair of transient edges is introduced when two different DiskComplexes are merged. They both connect the two skeletons.
 * In this way, transient edges are never island bridges when they are introduced (but the may become island edges later in their life).
 * If a transient edge gets removed then, its creation partner is also removed immediately. This may lead to removal of other
 * pairs of transient edges in a chain reaction.
 * 
 * Transient edge pairs are removed when:
 * a) one of them is expanded (distance gets larger) due to an ego motion
 * b) one of them has become an island bridge
 * 
 * Merging of Disk Complexes:
 * ==========================
 * 
 * Two disk complexes can be merged in two ways:
 * 
 * a) Gluing: two colliding disks of the two disk complexes are selected. One or both of two disk complexes are moved such that
 * the distance between the disk complexes agrees with the radius sum. A permanent edge between the corresponding vertices is
 * introduced, merging the two skeletons into one.
 * 
 * b) Two collision pairs between DiskComplexes are chosen and transient edges are introduced between them, merging the two skeletons
 * into one.
 * 
 * 
 * Splitting of Disk Complexes:
 * =============================
 * 
 * A DiskComplex may be split in two ways:
 * 
 * a) The removal of transient edges (due to ego motion) leads to a disconnected skeleton graph.
 * 
 * b) a permanent edge gets removed (external decision): This may lead to a disconnected skeleton graph (either directly or
 * indirectly by the triggered removal of transient edges).
 * 
 * in Both cases, connection components are identified and grouped into multiple separate new DiskComplexes.
 * 
 * @author Jan
 */
public class Skeleton {

	private final Set<Island> islands;
	private final Set<TransientEdge> transientEdges;
	private final Map<Vertex, Map<PermanentEdge, MovingSet>> controlVertices;
	private final Map<Disk, Map<Disk, Set<Disk>>> movingSetMap;

	/**
	 * Creates an empty skeleton
	 */
	public Skeleton() {
		controlVertices = new HashMap<Vertex, Map<PermanentEdge, MovingSet>>();
		islands = new HashSet<Island>();
		transientEdges = new HashSet<TransientEdge>();
		movingSetMap = new HashMap<Disk, Map<Disk, Set<Disk>>>();
	}

	/**
	 * Creates first vertex
	 * 
	 * @param isControlPoint
	 * @return the newly created Vertex
	 */
	public Vertex createFirstVertex(Disk disk, boolean isControlPoint) {
		Island island = new Island();
		islands.add(island);
		Vertex newVertex = new Vertex(disk, island);
		if (isControlPoint) {
			HashMap<PermanentEdge, MovingSet> branchMap = new HashMap<PermanentEdge, MovingSet>();
			controlVertices.put(newVertex, branchMap);
		}
		island.addVertex(newVertex);
		return newVertex;
	}

	/**
	 * Creates a new vertex in the skeleton and connects it to an existing vertex.
	 * It can be specified if the new vertex counts as control vertex or not.
	 * 
	 * @param newDisk
	 *            a newly created disk for which a skeleton vertex is required
	 * @param connectedTo
	 *            the disk to which the new disk shall be connected
	 * @param isControlPoint
	 *            if true, the new vertex will be marked as controllable vertex
	 * @return the newly created Vertex
	 */
	public Vertex createVertex(Disk newDisk, Disk connectedTo, boolean isControlPoint) {
		Vertex oldVertex = connectedTo.getSkeletonVertex();
		if (oldVertex == null)
			throw new SkeletonException("unknown disk");
		Island island = oldVertex.getIsland();
		Vertex newVertex = new Vertex(newDisk, island);
		PermanentEdge newEdge = new PermanentEdge(oldVertex, newVertex);
		extendMovingSets(island, oldVertex, newVertex, newEdge);
		if (isControlPoint) {
			HashMap<PermanentEdge, MovingSet> branchMap = new HashMap<PermanentEdge, MovingSet>();
			branchMap.put(newEdge, new MovingSet(island.getVertices()));
			controlVertices.put(newVertex, branchMap);
		}
		island.addVertex(newVertex);
		return newVertex;
	}

	/**
	 * Loops over all moving sets of island. Those that contain oldVertex get extended by newVertex
	 * If oldVertex is a control vertex, create a new moving set
	 * 
	 * @param newEdge
	 */
	private void extendMovingSets(Island island, Vertex oldVertex, Vertex newVertex, PermanentEdge newEdge) {
		for (Entry<Vertex, Map<PermanentEdge, MovingSet>> e : controlVertices.entrySet()) {
			Vertex controlVertex = e.getKey();
			invalidateMovingSets(controlVertex);
			if (island.getVertices().contains(controlVertex)) {
				for (MovingSet ms : e.getValue().values()) {
					Set<Vertex> vertices = ms.getIslandVertices();
					if (vertices.contains(oldVertex)) {
						vertices.add(newVertex);
					}
				}
			}
		}
		Map<PermanentEdge, MovingSet> oldVertexMovingSets = controlVertices.get(oldVertex);
		if (oldVertexMovingSets != null) {
			oldVertexMovingSets.put(newEdge, new MovingSet(newVertex));
		}
	}

	/**
	 * Adds a permanent edge between two vertices. This is only allowed if the two vertices are in the same island,
	 * and if there is not yet a edge between the two vertices. Also it is not allowed that the edge connects
	 * two different MovingSets.
	 * 
	 * @param disk1
	 *            disk belonging to the first end point of the to be created permanent edge
	 * @param disk2
	 *            disk belonging to the second end point of the to be created permanent edge
	 */
	public void addPermanentEdge(Disk disk1, Disk disk2) {
		Vertex vertex1 = disk1.getSkeletonVertex();
		if (vertex1 == null)
			throw new SkeletonException("unknown disk");
		Vertex vertex2 = disk2.getSkeletonVertex();
		if (vertex2 == null)
			throw new SkeletonException("unknown disk");
		if (vertex1.getEdge(vertex2) != null) {
			throw new SkeletonException("edge between vertices exists already");
		}
		if (vertex1 == vertex2) {
			throw new SkeletonException("identical vertices");
		}
		Island island = vertex1.getIsland();
		if (island != vertex2.getIsland())
			throw new SkeletonException("tried to make a permanent connection between different islands (use glueTogether to merge Skeletons)");
		if (connectionMergesMovingSets(vertex1, vertex2, island))
			throw new SkeletonException("not allowed to connect different moving sets with a new edge");
		new PermanentEdge(vertex1, vertex2);
	}

	/**
	 * Tests if there is a control vertex such that vertex1 and vertex2 lie in two different moving sets.
	 * It can be assumed that vertex1 and vertex2 lie in the same island (that is also given as argument).
	 * If vertex1 or vertex2 is a control vertex the method also returns true.
	 */
	private boolean connectionMergesMovingSets(Vertex vertex1, Vertex vertex2, Island island) {
		// loop over all control vertices belonging to the island
		for (Entry<Vertex, Map<PermanentEdge, MovingSet>> e : controlVertices.entrySet()) {
			if (island.getVertices().contains(e.getKey())) {
				// loop over all moving sets of the current control vertex
				for (MovingSet ms : e.getValue().values()) {
					// check if one of the vertices is contained, and the other not (in that case the other must be either
					// in another moving set or is the control vertex itself)
					int count = 0;
					if (ms.getIslandVertices().contains(vertex1))
						count++;
					if (ms.getIslandVertices().contains(vertex2))
						count++;
					if (count == 1) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Merges another Skeleton permanently to this skeleton by building a permanent connection between two disks.
	 * All vertices, edges, islands of the other skeleton will be placed in this Skeleton object.
	 * The other skeleton object will be emptied and should not used any longer.
	 * 
	 * @param thisDisk
	 *            the disk in this skeleton, which will be glued
	 * @param otherDisk
	 *            the disk in the other skeleton, which will be glued
	 * @param otherSkeleton
	 *            the skeleton containing the other disk. Will be emptied by this method.
	 */
	public void mergePermanently(Disk thisDisk, Disk otherDisk, Skeleton otherSkeleton) {
		Vertex thisVertex = thisDisk.getSkeletonVertex();
		if (thisVertex == null)
			throw new SkeletonException("unknown disk");
		Vertex otherVertex = otherDisk.getSkeletonVertex();
		if (otherVertex == null)
			throw new SkeletonException("unknown disk");
		controlVertices.putAll(otherSkeleton.controlVertices);
		Island thisIsland = thisVertex.getIsland();
		Island otherIsland = otherVertex.getIsland();
		extendMovingSets(thisIsland, thisVertex, otherIsland);
		extendMovingSets(otherIsland, otherVertex, thisIsland);
		islands.addAll(otherSkeleton.islands);
		islands.remove(otherIsland);
		thisIsland.mergeWith(otherIsland);
		new PermanentEdge(thisVertex, otherVertex);
		otherSkeleton.clear();
		updateMovingSetAttachments();
	}

	/**
	 * Loop over all control vertices of thisIsland that contain thisVertex and add all vertices of otherIsland.
	 */
	private void extendMovingSets(Island thisIsland, Vertex thisVertex, Island otherIsland) {
		for (Entry<Vertex, Map<PermanentEdge, MovingSet>> e : controlVertices.entrySet()) {
			Vertex controlVertex = e.getKey();
			invalidateMovingSets(controlVertex);
			if (thisIsland.getVertices().contains(controlVertex)) {
				for (MovingSet ms : e.getValue().values()) {
					Set<Vertex> vertices = ms.getIslandVertices();
					if (vertices.contains(thisVertex)) {
						vertices.addAll(otherIsland.getVertices());
					}
				}
			}
		}
	}

	/**
	 * Merge this skeleton with another skeleton by building two transient connections.
	 * The other skeleton object will be emptied and should not used any longer.
	 * 
	 * @param thisDisk1
	 *            first disk of first edge (corresponding to vertex in this skeleton)
	 * @param otherDisk1
	 *            second disk of first edge (corresponding to vertex in other skeleton)
	 * @param thisDisk2
	 *            first disk of second edge (corresponding to vertex in this skeleton)
	 * @param otherDisk2
	 *            second disk of second edge (corresponding to vertex in other skeleton)
	 * @param otherSkeleton
	 *            the other skeleton to be merged with this skeleton. Will be emptied by this method.
	 */
	public void mergeTransiently(Disk thisDisk1, Disk otherDisk1, Disk thisDisk2, Disk otherDisk2, Skeleton otherSkeleton) {
		if (this == otherSkeleton)
			throw new SkeletonException("not allowed to merge skeleton with itself");
		Vertex thisVertex1 = thisDisk1.getSkeletonVertex();
		Vertex thisVertex2 = thisDisk2.getSkeletonVertex();
		Vertex otherVertex1 = otherDisk1.getSkeletonVertex();
		Vertex otherVertex2 = otherDisk2.getSkeletonVertex();
		if (thisVertex1 == null)
			throw new SkeletonException("unknown disk");
		TransientEdge[] te = TransientEdge.createEdgePair(thisVertex1, otherVertex1, thisVertex2, otherVertex2);
		transientEdges.add(te[0]);
		transientEdges.add(te[1]);
		controlVertices.putAll(otherSkeleton.controlVertices);
		islands.addAll(otherSkeleton.islands);
		otherSkeleton.clear();
		updateMovingSetAttachments();
	}

	/**
	 * Provides a map that gives for each of the neighbour disks of a control vertex the set of disks that move together with that disk
	 * 
	 * @param controlDisk
	 *            the disk corresponding to the control vertex
	 * @return map from neighbour disks to the moving set containing that neighbour disk
	 */
	public Map<Disk, Set<Disk>> getMovingSets(Disk controlDisk) {
		// fast lookup if set is already computed and has not changed
		Map<Disk, Set<Disk>> res = movingSetMap.get(controlDisk);
		if (res != null) {
			return res;
		}
		// have to construct it
		Vertex vertex = controlDisk.getSkeletonVertex();
		if (vertex == null)
			throw new SkeletonException("unknown disk");
		Map<PermanentEdge, MovingSet> branches = controlVertices.get(vertex);
		if (branches == null)
			throw new SkeletonException("vertex is not marked as control vertex");
		res = new HashMap<Disk, Set<Disk>>();
		for (Entry<PermanentEdge, MovingSet> e : branches.entrySet()) {
			Vertex neighbor = e.getKey().getOtherVertex(vertex);
			Set<Disk> movingDiskSet = e.getValue().getAllMovingDisks();
			res.put(neighbor.getDisk(), movingDiskSet);
		}
		movingSetMap.put(controlDisk, res);
		return res;
	}

	/**
	 * Mark the given control vertex as changed. The entry in the movingSetMap of the corresponding disk is removed, in order
	 * to force re-computation in the next call to getMovingSets().
	 */
	private void invalidateMovingSets(Vertex controlVertex) {
		movingSetMap.remove(controlVertex.getDisk());
	}

	/**
	 * Mark the all control vertices as changed. All entries in the movingSetMap are removed, in order
	 * to force re-computation subsequent calls to getMovingSets().
	 */
	private void invalidateAllMovingSets() {
		movingSetMap.clear();
	}

	/**
	 * Removes a permanent edge. This may trigger removal of transient edges and split the skeleton into sub-skeletons.
	 * 
	 * @param disk1
	 *            disk belonging to first end point of to be removed edge
	 * @param disk2
	 *            disk belonging to second end point of to be removed edge
	 * @return true if the operation leads to a split of the skeleton
	 */
	public boolean removalOfPermanentEdgeSplitsSkeleton(Disk disk1, Disk disk2) {
		Vertex vertex1 = disk1.getSkeletonVertex();
		if (vertex1 == null)
			throw new SkeletonException("unknown disk");
		Vertex vertex2 = disk2.getSkeletonVertex();
		if (vertex2 == null)
			throw new SkeletonException("unknown disk");
		PermanentEdge edge = vertex1.getEdge(vertex2);
		if (edge == null) {
			throw new SkeletonException("edge does not exists");
		}
		Island island = vertex1.getIsland();
		if (island.isBridge(edge)) {
			Island newIsland = island.split(edge);
			islands.add(newIsland);
			removeIslandBridges();
			updateMovingSetAttachments();
			return isSkeletonSplit();
		} else {
			vertex1.detachEdge(edge);
			vertex2.detachEdge(edge);
			return false;
		}
	}

	/**
	 * Determines length relative to radius sum.
	 * 
	 * @param distance
	 *            between corresponding disk centers / (r1 + r2)
	 * @return length relative to radius sum
	 */
	private double relativeLength(TransientEdge edge) {
		Disk d1 = edge.getVertex1().getDisk();
		Disk d2 = edge.getVertex2().getDisk();
		double x1 = d1.getX();
		double y1 = d1.getY();
		double r1 = d1.getRadius();
		double x2 = d2.getX();
		double y2 = d2.getY();
		double r2 = d2.getRadius();
		return distanceFactor(x1, y1, r1, x2, y2, r2);
	}

	public void storeTransientEdgesRelativeLengths() {
		for (TransientEdge te : transientEdges) {
			te.setLength(relativeLength(te));
		}
	}

	/**
	 * Determines which transient edge pairs are removed by a set of disk modifications.
	 * 
	 * @param diskModifications
	 * @return true if the diskcomplex' skeleton was split into multiple connected components by the modifications
	 */
	public boolean diskModificationsSplitSkeleton(Collection<DiskModification> diskModifications) {
		Set<TransientEdge> remove = new HashSet<TransientEdge>();
		for (TransientEdge e : transientEdges) {
			double distBefore = e.getLength();
			double distAfter = relativeLength(e);
			if (distAfter > distBefore) {
				remove.add(e);
				remove.add(e.getPartner());
			}
		}
		if (!remove.isEmpty()) {
			transientEdges.removeAll(remove);
			removeIslandBridges();
			updateMovingSetAttachments();
			return isSkeletonSplit();
		} else {
			return false;
		}
	}

	/**
	 * Computes (distance / (r1+r2))^2
	 */
	private double distanceFactor(double x1, double y1, double r1, double x2, double y2, double r2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double sr = r1 + r2;
		return (dx * dx + dy * dy) / (sr * sr);
	}

	/**
	 * Determine if the skeleton is disconnected (considering transient edges)
	 * 
	 * @return true of there are more than one connection component
	 */
	private boolean isSkeletonSplit() {
		clearIslandMarkers();
		if (islands.isEmpty())
			return false;
		floodFill(islands.iterator().next(), 0);
		for (Island island : islands) {
			if (island.getMarker() != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine an array of skeletons, one for each connection component. This skeleton is emptied and should not be used any longer.
	 * 
	 * @return array of connected sub-skeletons
	 */
	public Skeleton[] getSplittedSkeletons() {
		clearIslandMarkers();
		int count = 0;
		Island island = getUnmarkedIsland();
		while (island != null) {
			floodFill(island, count);
			count++;
			island = getUnmarkedIsland();
		}
		Skeleton[] res = new Skeleton[count];
		decomposeAccordingToMarkerValues(res);
		clear();
		return res;
	}

	/**
	 * Decompose this skeleton into sub-skeletons according to marker values
	 */
	private void decomposeAccordingToMarkerValues(Skeleton[] subSkeletons) {
		for (int i = 0; i < subSkeletons.length; i++) {
			subSkeletons[i] = new Skeleton();
		}
		for (Island island : islands) {
			subSkeletons[island.getMarker()].islands.add(island);
		}
		for (TransientEdge te : transientEdges) {
			subSkeletons[te.getVertex1().getIsland().getMarker()].transientEdges.add(te);
		}
		for (Entry<Vertex, Map<PermanentEdge, MovingSet>> e : controlVertices.entrySet()) {
			Vertex cv = e.getKey();
			subSkeletons[cv.getIsland().getMarker()].controlVertices.put(cv, e.getValue());
		}
	}

	/**
	 * Find an island with the marker value -1
	 */
	private Island getUnmarkedIsland() {
		for (Island island : islands) {
			if (island.getMarker() == -1)
				return island;
		}
		return null;
	}

	/**
	 * Remove iteratively all island bridges
	 */
	private void removeIslandBridges() {
		TransientEdge bridge = findIslandBridge();
		while (bridge != null) {
			transientEdges.remove(bridge);
			transientEdges.remove(bridge.getPartner());
			bridge = findIslandBridge();
		}
	}

	/**
	 * Finds an island bridge
	 * 
	 * @return island bridge, or null if none exist
	 */
	private TransientEdge findIslandBridge() {
		for (TransientEdge edge : transientEdges) {
			clearIslandMarkers();
			edge.getVertex2().getIsland().setMarker(0);
			floodFill(edge.getVertex1().getIsland(), 1);
			if (edge.getVertex2().getIsland().getMarker() == 0) {
				return edge;
			}
		}
		return null;
	}

	/**
	 * Updates the attached islands for all moving sets
	 */
	private void updateMovingSetAttachments() {
		invalidateAllMovingSets();
		for (Entry<Vertex, Map<PermanentEdge, MovingSet>> e : controlVertices.entrySet()) {
			clearIslandMarkers();
			Island controlVertexIsland = e.getKey().getIsland();
			// mark the island of control vertex with -2 
			controlVertexIsland.setMarker(-2);
			ArrayList<MovingSet> ms = new ArrayList<MovingSet>();
			ms.addAll(e.getValue().values());
			for (int i = 0; i < ms.size(); i++) {
				MovingSet m = ms.get(i);
				startIslandFloodFill(m.getIslandVertices(), i);
				m.clearAttachedIslands();
			}
			for (Island island : islands) {
				if (island.getMarker() >= 0) {
					ms.get(island.getMarker()).addAttachedIsland(island);
				}
			}
		}
	}

	private void startIslandFloodFill(Set<Vertex> islandVertices, int mark) {
		for (TransientEdge e : transientEdges) {
			Vertex v1 = e.getVertex1();
			Vertex v2 = e.getVertex2();
			if (islandVertices.contains(v1)) {
				floodFill(v2.getIsland(), mark);
			}
			if (islandVertices.contains(v2)) {
				floodFill(v2.getIsland(), mark);
			}
		}
	}

	/**
	 * Fill all unmarked islands connected via transient edges with the mark. Islands that have already another marker get the marker value -2.
	 */
	private void floodFill(Island island, int mark) {
		int prevmark = island.getMarker();
		if (prevmark == -1) {
			island.setMarker(mark);
			for (TransientEdge e : transientEdges) {
				Island island1 = e.getVertex1().getIsland();
				Island island2 = e.getVertex2().getIsland();
				if (island1 == island) {
					floodFill(island2, mark);
				}
				if (island2 == island) {
					floodFill(island1, mark);
				}
			}
		} else {
			if (prevmark != mark) {
				// already having a different mark: mark with -2 
				island.setMarker(-2);
			}
		}
	}

	/**
	 * Set all island markers to -1
	 */
	private void clearIslandMarkers() {
		for (Island island : islands) {
			island.setMarker(-1);
		}
	}

	/**
	 * Clear all entries of the skeleton
	 */
	private void clear() {
		islands.clear();
		transientEdges.clear();
		controlVertices.clear();
		movingSetMap.clear();
	}

	/**
	 * Determines if a disk is marked as controllable
	 * 
	 * @param disk
	 *            the disk to be tested
	 * @return true if the disk is marked controllable
	 */
	public boolean isControllable(Disk disk) {
		Vertex vertex = disk.getSkeletonVertex();
		if (vertex == null)
			throw new SkeletonException("unknown disk");
		return controlVertices.containsKey(vertex);
	}

	public Set<Island> getIslands() {
		return islands;
	}

	/**
	 * Provide set of transient edges
	 * 
	 * @return a collection containing all transient edges
	 */
	public Collection<TransientEdge> getTransientEdges() {
		return transientEdges;
	}

}
