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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.environment.Wall;
import diskworld.interfaces.CollisionDetector;
import diskworld.interfaces.DiskChangeListener;

public class FullSearchCollisionDetector implements CollisionDetector, DiskChangeListener {

	private Set<Disk> disks;
	private Set<Wall> walls;

	public FullSearchCollisionDetector() {
		disks = new HashSet<Disk>();
		this.walls = new HashSet<Wall>();
	}

	@Override
	public void diskHasMoved(Disk d) {
		// nothing to do
	}

	@Override
	public void diskWasAdded(Disk d) {
		disks.add(d);
	}

	@Override
	public void diskWasRemoved(Disk d) {
		disks.remove(d);
	}

	@Override
	public void diskHasChangedRadius(Disk d) {
		// nothing to do
	}

	@Override
	public LinkedList<Collision> getCollisions(DiskComplex dc) {
		LinkedList<Collision> res = new LinkedList<Collision>();
		// loop over all disks of the diskComplex
		Iterator<Disk> i = dc.getDisks().iterator();
		while (i.hasNext()) {
			// first check disks in same disk complex, stop when same disk was reached, in order to not count collisions twice  
			Disk d1 = i.next();
			Iterator<Disk> j = dc.getDisks().iterator();
			boolean cont = true;
			while (j.hasNext() && cont) {
				Disk d2 = j.next();
				if (d1 != d2) {
					Collision c = Collision.diskCollision(d1, d2);
					if (c != null) {
						res.add(c);
					}
				} else {
					cont = false;
				}
			}
			// no check all disks in other DiskComplexes
			j = disks.iterator();
			while (j.hasNext()) {
				Disk d2 = j.next();
				if (!d2.belongsToSame(d1)) {
					Collision c = Collision.diskCollision(d1, d2);
					if (c != null) {
						res.add(c);
					}
				}
			}
			// now check collisions with walls
			for (Wall wall : walls) {
				Collision c = Collision.wallCollision(d1, wall);
				if (c != null) {
					res.add(c);
				}
			}
		}
		return res;
	}

	@Override
	public boolean canAddWall(Wall wall) {
		for (Disk d : disks) {
			if (Collision.wallCollision(d, wall) != null) {
				return false;
			}
		}
		walls.add(wall);
		return true;
	}

	@Override
	public boolean removeWall(Wall wall) {
		return walls.remove(wall);
	}

	@Override
	public Collection<Wall> getWalls() {
		return walls;
	}

}
