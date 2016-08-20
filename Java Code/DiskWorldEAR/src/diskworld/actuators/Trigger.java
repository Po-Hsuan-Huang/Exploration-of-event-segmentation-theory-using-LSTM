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

/**
 * Class holding a trigger value that can be incremented, that decays every time step, and can
 * be used to fire a discrete event.
 * 
 * @author Jan
 *
 */
public class Trigger {

	private final double triggerThreshold; 
	private final double triggerEnergy;
	private final double decayFactor;
	private double value;
	
	public Trigger(double triggerThreshold, double triggerEnergy, double decayFactor) {
		value = 0.0;
		this.triggerThreshold = triggerThreshold;
		this.triggerEnergy = triggerEnergy;
		this.decayFactor = decayFactor; 
	}

	/**
	 * Decays, then increments the value. Returns energy consumed. 
	 * 
	 * @param increment
	 * 		value by which the trigger value shall be incremented
	 * @return
	 * 		energy consumed by incrementing the trigger value (not by the triggered event)
	 */
	public double timeStep(double increment) {
		value = value*decayFactor+increment;
		return triggerEnergy*increment*increment;
	}

	public boolean isActivated() {
		return value >= triggerThreshold;
	}

	public void reset() {
		value = 0.0;
	}

}
