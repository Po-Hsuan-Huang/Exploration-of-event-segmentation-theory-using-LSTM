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
import diskworld.visualization.AbstractDiskSymbol;
import diskworld.visualization.CircleDiskSymbol;

/**
 * Actuator that moves and turns the disk complex. The actuator value array has two components.
 * There are two principally different ways of controlling the movement:
 * - displacement control: component [0] holds the distance by which the disk moves forward,
 * component [1] holds the angle by which the disk rotates (all in the next time step)
 * - speed control: component [0] holds the speed by which the disk
 * moves forward (in the current direction, backwards if negative),
 * component [1] holds the angular speed by which the DiskComplex rotates (in radians per time unit).
 * 
 * @author Jan
 */
public class Mover extends ActuatorWithVisualisation {

	public static final String ACTUATOR_NAME = "Mover";

	private static final AbstractDiskSymbol DISK_SYMBOL = new CircleDiskSymbol(0.3);

	// indices of activity values:
	public static final int DISTANCE = 0;
	public static final int ANGLE = 1;
	
	private final boolean displacementControl;
	private final double maxForwardValue;
	private final double maxBackwardValue;
	private final double maxRotationValue;
	private final double rotationEnergyConsumption;
	private final double moveEnergyConsumption;

	@Override
	public int getDim() {
		return 2;
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
	public Mover(boolean displacementControl, double maxBackwardValue, double maxForwardValue, double maxRotationValue, double moveEnergyConsumptionConstant, double rotationEnergyConsumptionConstant) {
		super(ACTUATOR_NAME, DISK_SYMBOL);
		this.displacementControl = displacementControl;
		this.maxBackwardValue = maxBackwardValue;
		this.maxForwardValue = maxForwardValue;
		this.maxRotationValue = maxRotationValue;
		this.rotationEnergyConsumption = rotationEnergyConsumptionConstant / 2 / Math.PI;
		this.moveEnergyConsumption = moveEnergyConsumptionConstant;
	}

	/**
	 * Convenience constructor. Operates in displacementControl mode, allows only forward movement
	 * and rotation by full angle (+/-Pi) in one time step.
	 * 
	 * @param maxForwardDistance
	 *            maximum distance translated in one time step
	 * @param moveEnergyConsumption
	 *            energy consumption required for motion by 1 distance unit and per mass unit, must be non-negative
	 * @param rotationEnergyConsumption
	 *            energy consumption required for a full 360 rotation per mass momentum unit, must be non-negative
	 */
	public Mover(double maxForwardDistance, double rotationEnergyConsumption, double moveEnergyConsumption) {
		this(true, 0.0, maxForwardDistance, Math.PI, rotationEnergyConsumption, moveEnergyConsumption);
	}

	/**
	 * Convenience constructor. Operates in displacementControl mode.
	 * 
	 * @param maxBackwardDistance
	 *            maximum distance translated backwards in one time step
	 * @param maxForwardDistance
	 *            maximum distance translated forwards in one time step
	 * @param moveEnergyConsumption
	 *            energy consumption required for motion by 1 distance unit and per mass unit, must be non-negative
	 * @param rotationEnergyConsumption
	 *            energy consumption required for a full 360 rotation per mass momentum unit, must be non-negative
	 */
	public Mover(double maxBackwardDistance, double maxForwardDistance, double maxRotationValue, double rotationEnergyConsumption, double moveEnergyConsumption) {
		this(true, maxBackwardDistance, maxForwardDistance, maxRotationValue, rotationEnergyConsumption, moveEnergyConsumption);
	}

	@Override
	public double evaluateEffect(Disk disk, Environment environment, double[] activity, double partial_dt, double total_dt, boolean firstSlice) {
		if (firstSlice) {
			// set velocity and angular speed in first time slice
			double translationValue = activity[0] >= 0 ? activity[0] * maxForwardValue : activity[0] * maxBackwardValue;
			double rotationValue = activity[1] * maxRotationValue;
			double translationSpeed, rotationSpeed;
			if (displacementControl) {
				translationSpeed = translationValue / total_dt;
				rotationSpeed = rotationValue / total_dt;
			} else {
				translationSpeed = translationValue;
				rotationSpeed = rotationValue;
			}
			disk.getDiskComplex().setAngularSpeed(rotationSpeed);
			// move in direction of the new angle
			double newAngle = disk.getAngle() + rotationSpeed * total_dt;
			double vx = translationSpeed * Math.cos(newAngle);
			double vy = translationSpeed * Math.sin(newAngle);
			disk.getDiskComplex().setVelocity(vx, vy);
			return (Math.abs(rotationSpeed) * rotationEnergyConsumption * disk.getDiskComplex().getMass()
					+ Math.abs(translationSpeed) * moveEnergyConsumption * disk.getDiskComplex().getMassMomentum()) * total_dt;
		} else {
			// in other time slices: do nothing
			return 0.0;
		}
	}

	@Override
	protected ActuatorVisualisationVariant[] getVisualisationVariants() {
		return new ActuatorVisualisationVariant[] {
				getDiskSymbolVisualization(0),
				getDiskSymbolVisualization(1),
				getDiskSymbolVisualization(),
				ACTIVITY_AS_TEXT,
				NO_VISUALISATION,
		};
	}

	@Override
	public boolean isAlwaysNonNegative(int index) {
		return (index == 0) && (maxBackwardValue >= 0.0);
	}

	@Override
	public boolean isBoolean(int index) {
		return false;
	}

	@Override
	public double getActivityFromRealWorldData(double realWorldValue, int index) {
		return index == 0 ? getSpeedActivity(realWorldValue) : getAngleActivity(realWorldValue);
	}

	private double getAngleActivity(double realWorldValue) {
		return realWorldValue / maxRotationValue;
	}

	private double getSpeedActivity(double realWorldValue) {
		return realWorldValue >= 0 ? realWorldValue / maxForwardValue : realWorldValue / maxBackwardValue;
	}

	@Override
	public String getRealWorldMeaning(int index) {
		String txt = displacementControl ? "distance" : "speed";
		return index == 0 ? "translation " + txt : "rotation " + txt;
	}
}
