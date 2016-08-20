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

import diskworld.Disk;
import diskworld.grid.Grid;
import diskworld.interfaces.FrictionModel;

public abstract class AnisotropicStokesFrictionModel implements FrictionModel {

	private static final double DEFAULT_RESISTANCE_MASS_COEFFICIENT = 0.1;

	private final Floor floor;
	private final Grid floorGrid;
	private double resistanceMassCoefficient;

	public AnisotropicStokesFrictionModel(Floor floor, Grid floorGrid, double resistanceMassCoefficient) {
		this.floor = floor;
		this.floorGrid = floorGrid;
		this.resistanceMassCoefficient = resistanceMassCoefficient;
	}

	public AnisotropicStokesFrictionModel(Floor floor, Grid floorGrid) {
		this(floor, floorGrid, DEFAULT_RESISTANCE_MASS_COEFFICIENT);
	}

	private double getFloorViscosity(Disk d) {
		int x = floorGrid.getCellxIndex(d.getX());
		int y = floorGrid.getCellyIndex(d.getY());
		return floor.getType(x, y).getFrictionCoefficient();
	}

	abstract protected double getLongitudalFrictionCoefficient(Disk d);

	abstract protected double getTransversalFrictionCoefficient(Disk d);

	@Override
	public double[] getFrictionForce(Disk d, double speedx, double speedy) {
		double a = d.getAngle();
		double s = Math.sin(a);
		double c = Math.cos(a);
		double longitudal = speedx * c + speedy * s;
		double transversal = -speedx * s + speedy * c;
		double viscosity = getFloorViscosity(d);
		longitudal *= -getLongitudalFrictionCoefficient(d) * viscosity;
		transversal *= -getTransversalFrictionCoefficient(d) * viscosity;
		double force[] = new double[2];
		force[0] = longitudal * c - transversal * s;
		force[1] = longitudal * s + transversal * c;
		return force;
	}

	@Override
	public double getDisplacementResistance(Disk d) {
		return d.getRadius() * getFloorViscosity(d) + d.getMass() * resistanceMassCoefficient;
	}

}
