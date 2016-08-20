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
package diskworld.collisions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.collisions.UnorderedPairMap.ElementMethod;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.PhysicsParameters;
import diskworld.linalg2D.Point;

public class CollisionTracker {

	private final UnorderedPairMap<CollidableObject, CollisionAccu> map;
	private final UnorderedPairMap<DiskComplex, MergingDetector> mergeMap;
	private final PhysicsParameters physicsParameters;

	public CollisionTracker(PhysicsParameters physicsParameters) {
		map = new UnorderedPairMap<CollidableObject, CollisionAccu>();
		mergeMap = new UnorderedPairMap<DiskComplex, MergingDetector>();
		this.physicsParameters = physicsParameters;
	}

	public void clear() {
		map.clear();
		mergeMap.clear();
	}

	public void addAll(LinkedList<Collision> collisions, boolean blocked) {
		for (Collision c : collisions) {
			add(c, blocked, physicsParameters);
		}
	}

	public void add(Collision c, boolean blocked, PhysicsParameters physicsParameters) {
		CollidableObject o1 = c.getObj1();
		CollidableObject o2 = c.getObj2();
		CollisionAccu ca;
		ca = map.sortedGet(o1, o2);
		boolean newCollision = false;
		if (ca != null) {
			ca.add(c, +1);
		} else {
			ca = map.sortedGet(o2, o1);
			if (ca != null) {
				ca.add(c, -1);
				return;
			} else {
				newCollision = true;
				map.sortedPut(o1, o2, new CollisionAccu(c, blocked));
			}
		}

		if (newCollision && (o2 instanceof Disk)) {
			DiskComplex dc1 = ((Disk) o1).getDiskComplex();
			DiskComplex dc2 = ((Disk) o2).getDiskComplex();
			if (dc1 != dc2) {
				MergingDetector md = mergeMap.get(dc1, dc2);
				if (md == null) {
					mergeMap.sortedPut(dc1, dc2, new MergingDetector(dc1, dc2, c, physicsParameters));
				} else {
					md.add(c);
				}
			}
		}

	}

	public List<Point> getCollisionPoints(final boolean blocked) {
		final List<Point> res = new LinkedList<Point>();
		map.doForAll(new ElementMethod<CollidableObject, CollisionAccu>() {
			@Override
			public void forEach(CollidableObject key1, CollidableObject key2, CollisionAccu value) {
				if (value.isBlocked() == blocked)
					res.add(value.getCollisionPoint());
			}
		});
		return res;
	}

	public void exchangeImpulses(final PhysicsParameters physicsParameters) {
		map.doForAll(new ElementMethod<CollidableObject, CollisionAccu>() {
			@Override
			public void forEach(CollidableObject key1, CollidableObject key2, CollisionAccu value) {
				value.exchangeImpulse((Disk) key1, key2, physicsParameters);
			}
		});
	}

	public void checkMerging() {
		// TODO: make deterministic, provide deerministic hash value
		final Map<DiskComplex, DiskComplex> mergedTo = new HashMap<DiskComplex, DiskComplex>();
		mergeMap.doForAll(new ElementMethod<DiskComplex, MergingDetector>() {
			@Override
			public void forEach(DiskComplex key1, DiskComplex key2, MergingDetector value) {
				if (value.decideMerge()) {
					DiskComplex newdc1 = mergedTo.get(key1);
					if (newdc1 == null) {
						newdc1 = key1;
					}
					DiskComplex newdc2 = mergedTo.get(key2);
					if (newdc2 == null) {
						newdc2 = key2;
					}
					if (newdc1 != newdc2) {
						value.performMerge(newdc1, newdc2);
						mergedTo.put(newdc2, newdc1);
						for (Entry<DiskComplex, DiskComplex> e : mergedTo.entrySet()) {
							if (e.getValue() == newdc2) {
								e.setValue(newdc1);
							}
						}
					}
				}
			}
		});
	}

}
