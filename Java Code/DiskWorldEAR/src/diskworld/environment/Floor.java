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
package diskworld.environment;

public class Floor {

	private final int numx, numy;
	private final byte[][] type;
	private final double scale;

	public Floor(int numColumns, int numRows, double scale) {
		numx = numColumns;
		numy = numRows;
		type = new byte[numx][numy];
		this.scale = scale;
	}

	public int[] getSize() {
		return new int[] { numx, numy };
	}

	public int getNumX() {
		return numx;
	}

	public int getNumY() {
		return numy;
	}

	public double getMaxX() {
		return numx * scale;
	}

	public double getMaxY() {
		return numy * scale;
	}

	public double getPosX(int x) {
		return x * scale;
	}

	public double getPosY(int y) {
		return y * scale;
	}

	public int getTypeIndex(int x, int y) {
		return (x < 0) || (x >= numx) || (y < 0) || (y >= numy) ? 0 : type[x][y] & 0xff;
	}

	public FloorCellType getType(int x, int y) {
		return FloorCellType.getCellType(getTypeIndex(x, y));
	}

	public int getTypeIndex(double x, double y) {
		return getTypeIndex((int) Math.floor(x / scale), (int) Math.floor(y / scale));
	}

	public FloorCellType getType(double x, double y) {
		return FloorCellType.getCellType(getTypeIndex(x, y));
	}

	public void setTypeIndex(int x, int y, int typeIndex) {
		this.type[x][y] = (byte) (typeIndex & 0xff);
	}

	public void setType(int x, int y, FloorCellType type) {
		setTypeIndex(x, y, type.getTypeIndex());
	}
	
	public void setType(int x, int y, int z) {
		setTypeIndex(x, y, z);
	}

	public void fill(FloorCellType type) {
		for (int i = 0; i < numx; i++)
			for (int j = 0; j < numy; j++)
				setType(i, j, type);
	}

	public void clear() {
		fill(FloorCellType.EMPTY);
	}

	public Floor createClone() {
		Floor clone = new Floor(numx, numy, scale);
		for (int i = 0; i < numx; i++)
			clone.type[i] = type[i].clone();
		return clone;
	}

}
