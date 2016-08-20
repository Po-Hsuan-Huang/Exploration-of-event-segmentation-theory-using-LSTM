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
package diskworld;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates material properties needed for Disk:
 * - density
 * - color
 * - elasticity
 * - friction
 */
public class DiskMaterial {

	private double density;
	private double elasticity;
	private double frictionCoefficient;
	private double gripCoefficient;
	private Color displayColor; // used only for visualization

	private static final Set<DiskMaterial> materials = new HashSet<DiskMaterial>();

	/**
	 * Predefined material: metal
	 */
	public final static DiskMaterial METAL = new DiskMaterial(1.0, 0.8, 0.5, 0.5, Color.LIGHT_GRAY);

	/**
	 * Predefined material: rubber
	 */
	public final static DiskMaterial RUBBER = new DiskMaterial(0.3, 1.0, 0.8, 0.9, Color.CYAN);
	
	public final static DiskMaterial ORGANIC = new DiskMaterial(1.0, 1.0, 0.0, 0.9, Color.CYAN); //0.7 density
	
	/**
	 * Predefined material: dough
	 */
	public final static DiskMaterial DOUGH = new DiskMaterial(0.5, 0, 1.0, 1.0, Color.YELLOW);;//new DiskMaterial(1.0, 0, 1.0, 1.0, Color.YELLOW);
	public final static DiskMaterial HEAVYDOUGH = new DiskMaterial(1.0, 0, 1.0, 1, Color.YELLOW);

	public DiskMaterial(double density, double elasticity, double frictionCoefficient, double gripCoefficient, Color displayColor) {
		this.density = density;
		this.elasticity = elasticity;
		this.frictionCoefficient = frictionCoefficient;
		this.gripCoefficient = gripCoefficient;
		this.displayColor = displayColor;
		materials.add(this);
	}

	/**
	 * Creates a new material by cloning this material properties, but giving it another color index and setting the default color for the new color index
	 * 
	 * @param defaultColor
	 * @return new material with same properties but modified color
	 */
	public DiskMaterial withColor(Color defaultColor) {
		return new DiskMaterial(density, elasticity, frictionCoefficient, gripCoefficient, defaultColor);
	}

	/**
	 * Provides the density, used to calculate the mass of disks
	 * 
	 * @return positive number
	 */
	public double getDensity() {
		return density;
	}

	public double getElasticity() {
		return elasticity;
	}

	public double getFrictionCoefficient() {
		return frictionCoefficient;
	}

	public static Set<DiskMaterial> getAllMaterials() {
		return materials;
	}

	public Color getDisplayColor() {
		return displayColor;
	}

	public void setFrictionCoefficient(double frictionCoefficient) {
		this.frictionCoefficient = frictionCoefficient;
	}

	public double getGripCoefficient() {
		return gripCoefficient;
	}
}
