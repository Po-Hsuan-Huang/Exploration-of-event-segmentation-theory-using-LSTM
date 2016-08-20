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

import java.util.LinkedList;

import diskworld.Disk;
import diskworld.shape.CircleShape;
import diskworld.shape.Shape;

/**
 * @author Simon Schlegel, Jan Kneissler
 * 
 *          Implements a rectangular grid. Cells are represented as pair of integers (colIndex,rowIndex), the lower left corner (with coordinates 0,0) belongs to the cell (0,0).
 */
public class Grid {

	private final int numX, numY; // #of columns and rows; declares the partition of the grid
	private final double cellSizeX; // defines the x range of a cell
	private final double cellSizeY; // defines the y range of a cell

	/**
	 * Constructs a grid specifying the number of rows and columns
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
	public Grid(double gridSizeX, double gridSizeY, int numCellsX, int numCellsY) {
		this.numX = numCellsX;
		this.numY = numCellsY;
		this.cellSizeX = gridSizeX / numX; // range of a column
		this.cellSizeY = gridSizeY / numY; // range of a row
	}

	/**
	 * Constructs a grid
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
	public Grid(double gridSizeX, double gridSizeY, double approxCellSizeX, double approxCellSizeY) {
		this.numX = (int) Math.ceil(gridSizeX / approxCellSizeX); //# of columns, ciel to avoid division by zero
		this.numY = (int) Math.ceil(gridSizeY / approxCellSizeY); //# of rows,  ciel to avoid division by zero
		this.cellSizeX = gridSizeX / numX; // range of a column
		this.cellSizeY = gridSizeY / numY; // range of a row
	}

	// Public methods

	/**
	 * Number of cells in a row
	 * 
	 * @return number of columns
	 */
	public int getNumColumns() {
		return this.numX;
	}

	/**
	 * Number of cells in a column
	 * 
	 * @return number of rows
	 */
	public int getNumRows() {
		return this.numY;
	}

	/**
	 * Width of cells
	 * 
	 * @return width of a column
	 */
	public double getCellWidth() {
		return this.cellSizeX;
	}

	/**
	 * Height of cells
	 * 
	 * @return height of a row
	 */
	public double getCellHeight() {
		return this.cellSizeY;
	}

	/**
	 * Determines if the cell (cellx,celly) intersects with the given shape
	 * 
	 * @param cellx
	 *            column index of cell
	 * @param celly
	 *            row index of cell
	 * @param shape
	 *            the shape to be tested
	 * @return true if the cell and the shape intersect
	 */
	public boolean cellIntersectsShape(int cellx, int celly, Shape shape) {
		double x1 = cellSizeX * cellx;
		double y1 = cellSizeY * celly;
		double x2 = x1 + cellSizeX;
		double y2 = y1 + cellSizeY;
		return shape.intersectsRectangle(x1, y1, x2, y2);
	}

	/**
	 * Provide all cells in the grid that intersect the given shape
	 * 
	 * @param shape
	 *            the shape to be tested
	 * @return list of integer arrays, all of length 2, of the form { col, row }
	 */
	public LinkedList<int[]> getCellIndicesIntersectingWithShape(Shape shape) {
		double bounds[] = shape.getBoundingBox();
		int xmin = Math.max(0, getCellxIndex(bounds[0]));
		int ymin = Math.max(0, getCellyIndex(bounds[1]));
		int xmax = Math.min(numX - 1, getCellxIndex(bounds[2]));
		int ymax = Math.min(numY - 1, getCellyIndex(bounds[3]));
		LinkedList<int[]> res = new LinkedList<int[]>();
		for (int x = xmin; x <= xmax; x++) {
			for (int y = ymin; y <= ymax; y++) {
				if (cellIntersectsShape(x, y, shape)) {
					res.add(new int[] { x, y });
				}
			}
		}
		return res;
	}

	/**
	 * Provide all cells in the grid that intersect the given disk
	 * 
	 * @param disk
	 *            the disk to be tested
	 * @return list of integer arrays, all of length 2, of the form { col, row }
	 */
	public LinkedList<int[]> getCellIndicesIntersectingWithDisk(Disk disk) {
		return getCellIndicesIntersectingWithShape(new CircleShape(disk.getX(), disk.getY(), disk.getRadius()));
	}

	public int getCellxIndex(double x) {
		return (int) Math.floor(x / cellSizeX);
	}

	public int getCellyIndex(double y) {
		return (int) Math.floor(y / cellSizeY);
	}

}
