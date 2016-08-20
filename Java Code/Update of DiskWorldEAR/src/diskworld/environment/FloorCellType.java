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

import java.awt.Color;

public class FloorCellType {

	/**
	 * The cell types are stored as single bytes, so 256 is the maximum number of possible floor types!
	 */
	public static final int NUM_FLOOR_TYPES = 256;
	private static final FloorCellType[] FLOOR_TYPES = new FloorCellType[NUM_FLOOR_TYPES];
	private static int numUsed = 0;

	private final static double FRICTION_FACTOR = 0.3;

	/**
	 * Empty cell type (outer space), drawn in black, no friction
	 */
	public static final FloorCellType EMPTY = new FloorCellType(0.0 * FRICTION_FACTOR, Color.BLACK);

	
	/**
	 * Opposite of empty cell type, very high friction
	 */
	public static final FloorCellType FULL = new FloorCellType(2 * FRICTION_FACTOR, Color.BLACK);
	
	public static final FloorCellType BURRKILLER = new FloorCellType(2 * FRICTION_FACTOR, Color.RED.darker().darker().darker());
	
	/**
	 * Grass, high friction
	 */
	public static final FloorCellType GRASS = new FloorCellType(1.0 * FRICTION_FACTOR, Color.GREEN.darker());

	/**
	 * Water, low friction
	 */
	public static final FloorCellType WATER = new FloorCellType(0.1 * FRICTION_FACTOR, Color.BLUE);

	/**
	 * Stone, medium friction
	 */
	public static final FloorCellType STONE = new FloorCellType(0.3 * FRICTION_FACTOR, Color.DARK_GRAY);

	/**
	 * Ice, very low friction
	 */
	public static final FloorCellType ICE = new FloorCellType(0.01 * FRICTION_FACTOR, Color.WHITE);

	private final int index;
	private final Color displayColor;
	private final double frictionCoefficient;

	public static FloorCellType getCellType(int typeIndex) {
		if (typeIndex < 0)
			throw new IllegalArgumentException("negative index");
		if (typeIndex >= numUsed)
			throw new IllegalArgumentException("floor cell type was not defined: " + typeIndex + " vs. " + numUsed);
		return FLOOR_TYPES[typeIndex];
	}

	public static int numFloorCellTypesDefined() {
		return numUsed;
	}

	private static int nextIndex() {
		int res = numUsed;
		numUsed++;
		return res;
	}

	public FloorCellType(double frictionCoefficient, Color displayColor) {
		this.index = nextIndex();
		this.displayColor = displayColor;
		this.frictionCoefficient = frictionCoefficient;
		FLOOR_TYPES[index] = this;
	}

	public Color getDisplayColor() {
		return displayColor;
	}

	/**
	 * Provides the friction coefficient (for SlidingFrictionModel) or viscosity (for StokesFrictionModel)
	 * 
	 * @return a value >= 0 (0 means no friction)
	 */
	public double getFrictionCoefficient() {
		return frictionCoefficient;
	}

	public int getTypeIndex() {
		return index;
	}

}
