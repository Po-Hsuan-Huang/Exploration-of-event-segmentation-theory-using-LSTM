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

import diskworld.DiskMaterial;
import diskworld.grid.Grid;
import diskworld.interfaces.FrictionModel;
import diskworld.interfaces.PhysicsParameters;

/**
 * The default physics parameters implementations:
 * Elasticities of disk-to-disk collisions is calculated from the materials inherent elasticities.
 * Disk-to-wall elasticity is determined by the disk material elasticity alone.
 * Frictions are floor type dependent and given by the .
 * 
 * @author Jan
 * 
 */
public class DefaultPhysicsParameters implements PhysicsParameters {

	private final FrictionModel frictionModel;
	private static final double DEFAULT_SHIFT_RESISTANCE_ENERGY_FACTOR = 0.1;
	protected double shiftResistanceEnergyConsumptionFactor = DEFAULT_SHIFT_RESISTANCE_ENERGY_FACTOR;

	public DefaultPhysicsParameters(Floor floor, Grid floorGrid) {
		this.frictionModel = new SlidingFrictionModel(floor, floorGrid);
	}

	public DefaultPhysicsParameters(FrictionModel frictionModel) {
		this.frictionModel = frictionModel;
	}

	/* (non-Javadoc)
	 * @see diskworld.environment.PhysicsParametersI#getDisk2DiskElasticty(diskworld.DiskMaterial, diskworld.DiskMaterial)
	 */
	@Override
	public double getDisk2DiskElasticty(DiskMaterial diskMaterial1, DiskMaterial diskMaterial2) {
		double el1 = diskMaterial1.getElasticity();
		double el2 = diskMaterial2.getElasticity();
		// combined elasticity is given by 1-elGes = (1-el1)*(1-el2)
		return el1 + el2 - el1 * el2;
	}

	/* (non-Javadoc)
	 * @see diskworld.environment.PhysicsParametersI#getDisk2WallElasticty(diskworld.DiskMaterial)
	 */
	@Override
	public double getDisk2WallElasticty(DiskMaterial diskMaterial) {
		return diskMaterial.getElasticity();
	}

	/* (non-Javadoc)
	 * @see diskworld.environment.PhysicsParametersI#getFrictionModel()
	 */
	@Override
	public FrictionModel getFrictionModel() {
		return frictionModel;
	}

	@Override
	public double getDisk2DiskGripCoefficient(DiskMaterial diskMaterial1, DiskMaterial diskMaterial2) {
		double gc1 = diskMaterial1.getGripCoefficient();
		double gc2 = diskMaterial2.getGripCoefficient();
		return gc1 * gc2;
	}

	@Override
	public double getShiftResistanceEnergyConsumptionFactor() {
		return shiftResistanceEnergyConsumptionFactor;
	}

	@Override
	public void setShiftResistanceEnergyConsumptionFactor(double factor) {
		shiftResistanceEnergyConsumptionFactor = factor;
	}

}
