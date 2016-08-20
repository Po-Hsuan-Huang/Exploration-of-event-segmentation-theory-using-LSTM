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
package diskworld.interfaces;

import diskworld.DiskMaterial;

public interface PhysicsParameters {

	public abstract double getDisk2DiskElasticty(DiskMaterial diskMaterial1, DiskMaterial diskMaterial2);

	public abstract double getDisk2WallElasticty(DiskMaterial diskMaterial);

	public abstract FrictionModel getFrictionModel();

	public abstract double getDisk2DiskGripCoefficient(DiskMaterial diskMaterial1, DiskMaterial diskMaterial2);

	public abstract double getShiftResistanceEnergyConsumptionFactor();

	public abstract void setShiftResistanceEnergyConsumptionFactor(double factor);
}