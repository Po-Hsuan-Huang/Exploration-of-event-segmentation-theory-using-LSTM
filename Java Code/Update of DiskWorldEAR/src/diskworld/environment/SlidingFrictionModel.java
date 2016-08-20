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
import diskworld.DiskMaterial;
import diskworld.actuators.Sucker;
import diskworld.grid.Grid;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.FrictionModel;

public class SlidingFrictionModel implements FrictionModel {

	private static final double DEFAULT_RESISTANCE_OFFSET = 0.01;

	private final Floor floor;
	private final Grid floorGrid;
	private double resistanceOffset;

	public SlidingFrictionModel(Floor floor, Grid floorGrid, double resistanceOffset) {
		this.floor = floor;
		this.floorGrid = floorGrid;
		this.resistanceOffset = resistanceOffset;
	}

	public SlidingFrictionModel(Floor floor, Grid floorGrid) {
		this(floor, floorGrid, DEFAULT_RESISTANCE_OFFSET);
	}

	private double getFloorContactCoefficient(Disk d) {
		int x = floorGrid.getCellxIndex(d.getX());
		int y = floorGrid.getCellyIndex(d.getY());
		DiskMaterial dm = d.getDiskType().getMaterial();
		double fc1 = dm.getFrictionCoefficient();
		double fc2 = floor.getType(x, y).getFrictionCoefficient();
		return fc1 * fc2;
	}

	private double getEffectiveMass(Disk d) {
		double res = d.getMass();
		Actuator actuator = d.getDiskType().getActuator();
		if (actuator instanceof Sucker) {
			res += ((Sucker) actuator).getMaxSuckingForce() * d.getActivity()[0];
		}
		return res;
	}

	@Override
	public double[] getFrictionForce(Disk d, double speedx, double speedy) {
		double f = -getEffectiveMass(d) * getFloorContactCoefficient(d) / Math.sqrt(speedx * speedx + speedy * speedy); // friction constant * mass of disk d divided by the sqrt of speedx^2 and speedy^2
		double force[] = new double[2];
		force[0] = f * speedx;
		force[1] = f * speedy;
		return force;
	}

	@Override
	public double getDisplacementResistance(Disk d) {
		return getEffectiveMass(d) * (getFloorContactCoefficient(d) + resistanceOffset);
	}

}
