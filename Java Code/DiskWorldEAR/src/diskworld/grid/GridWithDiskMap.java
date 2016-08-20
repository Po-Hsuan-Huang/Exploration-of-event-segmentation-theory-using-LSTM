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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import diskworld.Disk;
import diskworld.interfaces.DiskChangeListener;
import diskworld.shape.Shape;

/**
 * A grid with additional information:
 * - Array of Cell objects for all cells (each holding a set of Disks that intersect the cell)
 * - Map from Disks to lists of cell objects (for quickly looking up all cells that intersect with the given disk)
 * 
 * This class implements the DiskChangeListener, in order to get informed of changes to the disks.
 * 
 * @author Simon Schlegel, Jan Kneissler
 */

public class GridWithDiskMap extends Grid implements DiskChangeListener {

	private Cell[][] cells; // hold all cell objects
	private HashMap<Disk, LinkedList<Cell>> cellMap; // maps disk to list of cell objects 

	/**
	 * Constructs a grid specifying the approximate grid cell sizes
	 * 
	 * @param gridSizeX
	 *            width of the grid area (maximum x coordinate)
	 * @param gridSizeY
	 *            height of the grid area (maximum y coordinate)
	 * @param approxCellSizeX
	 *            approximate width of cells (actual size might be a bit smaller)
	 * @param approxCellSizeY
	 *            approximate height of cells (actual size might be a bit smaller)
	 */
	public GridWithDiskMap(double gridSizeX, double gridSizeY, double approxCellSizeX, double approxCellSizeY) {
		super(gridSizeX, gridSizeY, approxCellSizeX, approxCellSizeY);
		this.cells = new Cell[getNumColumns()][getNumRows()];
		// initialise the 2d array
		for (int col = 0; col < cells.length; col++)
			for (int row = 0; row < cells[col].length; row++) {
				cells[col][row] = new Cell(col, row);
			}
		this.cellMap = new HashMap<Disk, LinkedList<Cell>>();
	}

	/**
	 * Constructs a grid specifying the number of grid cells
	 * 
	 * @param gridSizeX
	 *            width of the grid area (maximum x coordinate)
	 * @param gridSizeY
	 *            height of the grid area (maximum y coordinate)
	 * @param numCellsX
	 *            number of columns
	 * @param numCellsY
	 *            number of rows
	 */
	public GridWithDiskMap(double gridSizeX, double gridSizeY, int numCellsX, int numCellsY) {
		super(gridSizeX, gridSizeY, numCellsX, numCellsY);
		this.cells = new Cell[getNumColumns()][getNumRows()];
		// initialise the 2d array
		for (int col = 0; col < cells.length; col++)
			for (int row = 0; row < cells[col].length; row++) {
				cells[col][row] = new Cell(col, row);
			}
		this.cellMap = new HashMap<Disk, LinkedList<Cell>>();
	}
	// Public methods

	/**
	 * Provide all disks that intersect the cells with given indices
	 * 
	 * @param cellIndices
	 *            a collection of arrays, all of length 2, holding pairs (colIndex,rowIndex)
	 * @return a set of all disks that intersect at least one of the given cells
	 */
	public Set<Disk> getDisksIntersectingWithCellsByIndex(Collection<int[]> cellIndices) {
		Set<Disk> res = new HashSet<Disk>();
		for (int[] idx : cellIndices) {
			res.addAll(cells[idx[0]][idx[1]].getIntersectingDisks());
		}
		return res;
	}

	/**
	 * Provide all disks that interest the given cells
	 * 
	 * @param cells
	 *            a collection of cell objects
	 * @return a set of all disks that intersect at least one of the given cells
	 */
	public Set<Disk> getDisksIntersectingWithCells(Collection<Cell> cells) {
		Set<Disk> res = new HashSet<Disk>();
		for (Cell cell : cells) {
			res.addAll(cell.getIntersectingDisks());
		}
		return res;
	}

	/**
	 * Provide all disks that intersect the given shape.
	 * 
	 * @param shape
	 *            the shape to be tested
	 * @return a set holding all disks that intersect the shape
	 */
	public Set<Disk> getDisksIntersectingWithShape(Shape shape) {
		// get all disks that intersect with cells that intersect with shape
		Set<Disk> res = getDisksIntersectingWithCellsByIndex(getCellIndicesIntersectingWithShape(shape));
		// remove the ones that do not intersect with shape
		for (Iterator<Disk> di = res.iterator(); di.hasNext();) {
			Disk d = di.next();
			if (!shape.intersectsDisk(d.getX(), d.getY(), d.getRadius())) {
				di.remove();
			}
		}
		return res;
	}

	/**
	 * In a GridWithDiskMap, this method can be implemented more efficiently by a lookup in the cellMap.
	 * Still faster is {@link #getCellsIntersectingWithDisk(Disk)}
	 * 
	 * @see Grid#getCellIndicesIntersectingWithDisk(Disk)
	 * @see #getCellsIntersectingWithDisk(Disk)
	 */
	@Override
	public LinkedList<int[]> getCellIndicesIntersectingWithDisk(Disk disk) {
		LinkedList<int[]> res = new LinkedList<int[]>();
		for (Cell cell : cellMap.get(disk)) {
			res.add(cell.getIndex());
		}
		return res;
	}

	/**
	 * In a GridWithDiskMap, the cells can be simply looked up, this is faster than getting a list of cell indices
	 * 
	 * @see #getCellIndicesIntersectingWithDisk(Disk)
	 */
	public LinkedList<Cell> getCellsIntersectingWithDisk(Disk disk) {
		return cellMap.get(disk);
	}

	/**
	 * Transform a list of cell indices into a list of cells
	 * 
	 * @param cellIndices
	 *            a list of arrays, all of length 2, holding pairs (colIndex,rowIndex)
	 * @return a list of cell objects
	 */
	private LinkedList<Cell> getCells(LinkedList<int[]> cellIndices) {
		LinkedList<Cell> res = new LinkedList<Cell>();
		for (int[] idx : cellIndices) {
			res.add(cells[idx[0]][idx[1]]);
		}
		return res;
	}

	/**
	 * Provide all cells in the grid that intersect the given shape
	 * 
	 * @param shape
	 *            the shape to be tested
	 * @return list of cell objects
	 */
	public List<Cell> getCellsIntersectingWithShape(Shape shape) {
		return getCells(super.getCellIndicesIntersectingWithShape(shape));
	}

	/**
	 * Access to Cell (used for visualization)
	 * 
	 * @param indexx
	 *            column index
	 * @param indexy
	 *            row index
	 * @return cell at specified rwo and column
	 */
	public Cell getCell(int indexx, int indexy) {
		return cells[indexx][indexy];
	}

	// Implementation of DiskChangeListener interface

	@Override
	public void diskWasAdded(Disk d) {
		List<int[]> cellIndices = super.getCellIndicesIntersectingWithDisk(d); // have to call super method, in order to recalculate instead of looking up! 
		LinkedList<Cell> cellList = new LinkedList<Cell>();
		for (int[] idx : cellIndices) {
			Cell cell = cells[idx[0]][idx[1]];
			cell.getIntersectingDisks().add(d);
			cellList.add(cell);
		}
		cellMap.put(d, cellList);
	}

	@Override
	public void diskWasRemoved(Disk d) {
		for (Cell cell : cellMap.get(d)) {
			cell.getIntersectingDisks().remove(d);
		}
		cellMap.remove(d);
	}

	@Override
	public void diskHasChangedRadius(Disk d) {
		updateCells(d);
	}

	@Override
	public void diskHasMoved(Disk d) {
		updateCells(d);
	}

	// Private methods

	// checks if a moving disk changes the list of intersecting cells
	private void updateCells(Disk d) {
		LinkedList<Cell> oldList = cellMap.get(d);
		LinkedList<Cell> newList = new LinkedList<Cell>();
		LinkedList<int[]> newIndices = super.getCellIndicesIntersectingWithDisk(d); // have to call super method, in order to recalculate instead of looking up!

		// Speed-up heuristics: Most of the time, there should be only one cell in the list (Disks are small compared to Cells,
		// so most of the time they are completely inside one cell). Make a quick test if this is the case and compare cells.
		if ((oldList.size() == 1) && (newIndices.size() == 1)) {
			int[] oldIndex = oldList.getFirst().getIndex();
			int[] newIndex = newIndices.getFirst();
			if ((oldIndex[0] == newIndex[0]) && (oldIndex[1] == newIndex[1])) {
				// no change, can stop
				return;
			}
		}

		// okay, we have to do it the long way, compare the two lists
		for (int[] idx : newIndices) {
			Cell cell = cells[idx[0]][idx[1]];
			newList.add(cell);
			if (!oldList.contains(cell)) {
				// not in old list: add disk to set of intersecting disks of that cell 
				cell.getIntersectingDisks().add(d);
			}
		}
		// find out which elements of old list are not in new list
		oldList.removeAll(newList);
		for (Cell notAnyMore : oldList) {
			// not any longer in new list: remove disk from set of intersecting disks of that cell
			notAnyMore.getIntersectingDisks().remove(d);
		}
		cellMap.put(d, newList);
	}
}
