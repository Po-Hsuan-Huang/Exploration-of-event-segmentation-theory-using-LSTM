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
import diskworld.DiskMaterial;
import diskworld.Environment;
import diskworld.visualization.AbstractDiskSymbol;
import diskworld.visualization.CircleDiskSymbol;

/**
 * Actuator that can consume disks that it overlaps/collides with.
 * 
 * @author Jan
 */
public class Mouth extends ActuatorWithVisualisation {

	public static final String ACTUATOR_NAME = "Mouth";

	private static final AbstractDiskSymbol DISK_SYMBOL = new CircleDiskSymbol(0.3);
	
	public interface ConsumableDecider {
		public boolean canConsume(DiskMaterial material);
	}

	public enum ConsumptionEventType {
		COLLISION, OVERLAP, COLLISION_OR__OVERLAP
	}
	
	private double consumptionRange;
	private ConsumptionEventType eventType;
	
	@Override
	public int getDim() {
		return 1;
	}

	/**
	 * Actuator that moves and rotates a disk (including its DiskComplex)
	 * 
	 * @param maxForwardValue
	 *            the maximum speed/distance with which the disk can move forwards, must be non-negative
	 * @param maxBackwardValue
	 *            the maximum speed/distance by which the disk can move backwards, must be non-negative
	 * @param maxRotationValue
	 *            the maximum angular speed/rotation angle by which the disk can rotate (in radian per time unit or radian), must be non-negative
	 * @param moveEnergyConsumptionConstant
	 *            energy consumption required for motion by 1 distance unit and per mass unit, must be non-negative
	 * @param rotationEnergyConsumptionConstant
	 *            energy consumption required for a full 360 rotation per mass momentum unit, must be non-negative
	 */
	public Mouth(boolean displacementControl, double maxBackwardValue, double maxForwardValue, double maxRotationValue, double moveEnergyConsumptionConstant, double rotationEnergyConsumptionConstant) {
		super(ACTUATOR_NAME, DISK_SYMBOL);
		// TODO
	}


	@Override
	public double evaluateEffect(Disk disk, Environment environment, double[] activity, double partial_dt, double total_dt, boolean firstSlice) {
		// TODO
		return 0;
	}

	@Override
	protected ActuatorVisualisationVariant[] getVisualisationVariants() {
		return new ActuatorVisualisationVariant[] {
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
		return true;
	}

	@Override
	public double getActivityFromRealWorldData(double realWorldValue, int index) {
		return realWorldValue;
	}

	@Override
	public String getRealWorldMeaning(int index) {
		return "consumption activation flag";
	}
}
