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
 * Actuator that teleports the disk complex.
 * The actuator value array has length 4. Component [0] holds the trigger controller,
 * [1] the x coordinate, [2] the y coordinate, [3] the orientation angle.
 * The teleporter can be operating in absolute coordinates or relative coordinates
 * (displacements and orientation changes are then relative to the disk to which the
 * teleporter actuator is attached).
 * Velocity and angular speed of the disk complex are set to 0 after the teleportation.
 * 
 * @author Jan
 */
public class Teleporter extends ActuatorWithVisualisation {

	public static final String ACTUATOR_NAME = "Teleporter";

	private static final AbstractDiskSymbol DISK_SYMBOL = new CircleDiskSymbol(0.3);

	private final boolean relativeCoordinates;
	private final double energyPerMass;
	private final double maxX, maxY;
	private final Trigger trigger;

	@Override
	public int getDim() {
		return 4;
	}

	/**
	 * Actuator that teleports the DiskComplex. Energy is consumed by activating a trigger,
	 * when trigger threshold is reached, teleportation is activated, trigger is reset to 0
	 * if teleportation was successful. In addition to trigger energy, a successful
	 * teleportation consumes energy proportional to the mass of the teleported DiskComplex.
	 * 
	 * @param relativeCoordinates
	 *            indicates if the coordinates given in the activity array are to be interpreted relative to the current position
	 * @param teleportationEnergyPerMass
	 *            energy used per mass unit used in trigger operation
	 */
	public Teleporter(Environment environment, boolean relativeCoordinates, double triggerThreshold, double triggerEnergy, double teleportationEnergyPerMass, double triggerDecayFactor) {
		super(ACTUATOR_NAME, DISK_SYMBOL);
		this.relativeCoordinates = relativeCoordinates;
		this.maxX = environment.getMaxX();
		this.maxY = environment.getMaxY();
		this.energyPerMass = teleportationEnergyPerMass;
		this.trigger = new Trigger(triggerThreshold, triggerEnergy, triggerDecayFactor);
	}

	@Override
	public double evaluateEffect(Disk disk, Environment environment, double[] activity, double partial_dt, double total_dt, boolean firstSlice) {
		double energyConsumed = trigger.timeStep(activity[0]);
		if (trigger.isActivated()) {
			double x = activity[1] * maxX;
			double y = activity[2] * maxY;
			double angle = activity[3] * Math.PI;
			double newx, newy, newAngle;
			if (relativeCoordinates) {
				double oldAngle = disk.getAngle();
				double cos = Math.cos(oldAngle);
				double sin = Math.sin(oldAngle);
				newx = disk.getX() + x * cos - y * sin;
				newy = disk.getY() + x * sin + y * cos;
				newAngle = oldAngle + angle;
			} else {
				newx = x;
				newy = y;
				newAngle = angle;
			}
			if ((newx >= 0) && (newx <= environment.getMaxX()) && (newy >= 0) && (newy <= environment.getMaxY())) {
				if (environment.canTeleport(disk, newx, newy, newAngle)) {
					energyConsumed += energyPerMass * disk.getDiskComplex().getMass();
					trigger.reset();
				}
			}
		}
		return energyConsumed;
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
		return (!relativeCoordinates) && (index >= 1) && (index <= 2);
	}

	@Override
	public boolean isBoolean(int index) {
		return false;
	}

	@Override
	public double getActivityFromRealWorldData(double realWorldValue, int index) {
		switch (index) {
		case 1:
			return realWorldValue / maxX;
		case 2:
			return realWorldValue / maxY;
		case 3:
			return realWorldValue / Math.PI;
		}
		return realWorldValue;
	}

	@Override
	public String getRealWorldMeaning(int index) {
		String s = relativeCoordinates ? " [relative]" : " [absolute]";
		switch (index) {
		case 0: return "trigger";
		case 1: return "x-coordinate"+s;
		case 2: return "y-coordinate"+s;
		case 3: return "angle"+s;	
		}
		return "";
	}
}
