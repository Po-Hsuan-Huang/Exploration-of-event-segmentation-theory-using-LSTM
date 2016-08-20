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
package diskworld.actuators;

import diskworld.Disk;
import diskworld.Environment;
import diskworld.linalg2D.Utils;
import diskworld.visualization.CircleDiskSymbol;

/**
 * Actuator that acts as a suction cup with the effect of increasing the pressure of the disk against the floor. If activity
 * is 0 or negative, the disk pressure corresponds to its normal mass. If activity is > 0 the effective mass is
 * given by mass + activity*maxSuckingForce.
 * This increases the effective friction forces (if a SlidingFrictionModel is used). For FrcitionModels that have no
 * dependency on the mass of the disk (like StokesFriction), this actuator is useless.
 * 
 * @author Jan
 * 
 */
public class Sucker extends ActuatorWithVisualisation {

	protected static final String ACTUATOR_NAME = "Sucker";

	private double maxSuckingForce, maxEnergyConsumption;

	public Sucker(double maxSuckingForce, double maxEnergyConsumption) {
		super(ACTUATOR_NAME, new CircleDiskSymbol(0.5));
		this.maxSuckingForce = maxSuckingForce;
		this.maxEnergyConsumption = maxEnergyConsumption;
	}

	@Override
	public double evaluateEffect(Disk disk, Environment environment, double[] activity, double partial_dt, double total_dt, boolean firstSlice) {
		// nothing to do, suction is taken into account automatically by friction computation, just return the energy consumption 
		return activity[0] * maxEnergyConsumption;
	}

	public double getMaxSuckingForce() {
		return maxSuckingForce;
	}

	@Override
	protected ActuatorVisualisationVariant[] getVisualisationVariants() {
		return new ActuatorVisualisationVariant[] {
				getDiskSymbolVisualization(0),
				getDiskSymbolVisualization(),
				ACTIVITY_AS_TEXT,
				NO_VISUALISATION,
		};
	}

	@Override
	public boolean isAlwaysNonNegative(int index) {
		return true;
	}

	@Override
	public boolean isBoolean(int index) {
		return false;
	}

	@Override
	public double getActivityFromRealWorldData(double suckingForce, int index) {
		return Utils.clip_pm1(suckingForce / maxSuckingForce);
	}

	@Override
	public String getRealWorldMeaning(int index) {
		return "sucking force";
	}

}
