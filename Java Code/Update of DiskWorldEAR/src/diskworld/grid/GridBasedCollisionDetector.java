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
package diskworld.grid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.collisions.Collision;
import diskworld.environment.Wall;
import diskworld.interfaces.CollisionDetector;
import diskworld.shape.WallShape;

public class GridBasedCollisionDetector implements CollisionDetector {

	private final GridWithDiskMap grid;
	private final Map<Wall, List<Cell>> wallCells;

	public GridBasedCollisionDetector(GridWithDiskMap grid) {
		this.grid = grid;
		this.wallCells = new HashMap<Wall, List<Cell>>();
	}

	@Override
	public LinkedList<Collision> getCollisions(DiskComplex dc) {
		// check collisions between disks
		LinkedList<Collision> res = new LinkedList<Collision>();
		for (Iterator<Disk> i = dc.getDisks().iterator(); i.hasNext();) {
			Disk d1 = i.next();
			List<Cell> cells = grid.getCellsIntersectingWithDisk(d1);
			for (Disk d2 : grid.getDisksIntersectingWithCells(cells)) {
				if (d1 != d2) {
					Collision c = Collision.diskCollision(d1, d2);
					if (c != null) {
						res.add(c);
					}
				}
			}
		}
		// check collisions with walls
		for (Entry<Wall, List<Cell>> e : wallCells.entrySet()) {
			Wall wall = e.getKey();
			List<Cell> cells = e.getValue();
			for (Disk d : grid.getDisksIntersectingWithCells(cells)) {
				if (d.belongsTo(dc)) {
					Collision c = Collision.wallCollision(d, wall);
					if (c != null) {
						res.add(c);
					}
				}
			}
		}
		return res;
	}

	@Override
	public boolean canAddWall(Wall wall) {
		List<Cell> cells = grid.getCellsIntersectingWithShape(new WallShape(wall));
		for (Disk d : grid.getDisksIntersectingWithCells(cells)) {
			if (Collision.wallCollision(d, wall) != null) {
				return false;
			}
		}
		wallCells.put(wall, cells);
		return true;
	}

	@Override
	public boolean removeWall(Wall wall) {
		return wallCells.remove(wall) != null;
	}

	@Override
	public Collection<Wall> getWalls() {
		return wallCells.keySet();
	}

}
