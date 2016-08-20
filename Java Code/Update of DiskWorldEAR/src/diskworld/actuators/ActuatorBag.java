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

import java.awt.Graphics2D;

import diskworld.Disk;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.interfaces.Actuator;
import diskworld.visualization.VisualisationItem;
import diskworld.visualization.VisualizationSettings;

/**
 * Actuuator that combines multiple actuators into one.
 * 
 * @author Jan
 *
 */
public class ActuatorBag implements Actuator {

	
	private Actuator[] components;

	public ActuatorBag(Actuator[] components) {
		this.components = components;
	}
	
	@Override
	public int getDim() {
		int sum = 0;
		for (int i = 0; i < components.length; i++) {
			sum += components[i].getDim();
		}
		return sum;
	}

	@Override
	public boolean isAlwaysNonNegative(int index) {
		int splitIndex[] = splitIndex(index); 
		return components[splitIndex[0]].isAlwaysNonNegative(splitIndex[1]);
	}

	@Override
	public boolean isBoolean(int index) {
		int splitIndex[] = splitIndex(index); 
		return components[splitIndex[0]].isBoolean(splitIndex[1]);
	}
	
	/**
	 * Calculates the index of a activity value in the joint activity array (where all activity values of all actuators in the bag are concatenated)
	 * 
	 * @param actuatorInBagIndex index of the target actuator in the bag
	 * @param componentOfActuatorValueIndex index of the component of the target value (in the actuators own counting scheme, i.e. 0 = first activity value)
	 * @return index in the joint activity array
	 */
	public int jointActuatorArrayIndex(int actuatorInBagIndex, int componentOfActuatorValueIndex) {
		if ((actuatorInBagIndex < 0) || (actuatorInBagIndex >= components.length))
			throw new IllegalArgumentException("illegal actuatorInBagIndex: "+actuatorInBagIndex);
		if ((componentOfActuatorValueIndex < 0) || (componentOfActuatorValueIndex >= components[actuatorInBagIndex].getDim()))
			throw new IllegalArgumentException("illegal componentOfActuatorValueIndex: "+actuatorInBagIndex);
		int sum = 0;
		for (int i = 0; i < actuatorInBagIndex; i++) {
			sum += components[i].getDim();
		}
		return sum + componentOfActuatorValueIndex;
	}


	@Override
	public double getActivityFromRealWorldData(double realWorldValue, int index) {
		int splitIndex[] = splitIndex(index); 
		return components[splitIndex[0]].getActivityFromRealWorldData(realWorldValue, splitIndex[1]);
	}

	@Override
	public String getRealWorldMeaning(int index) {
		int splitIndex[] = splitIndex(index); 
		return components[splitIndex[0]].getRealWorldMeaning(splitIndex[1]);
	}
	

	private int[] splitIndex(int index) {
		int i1 = 0;
		int i = 0;
		while (index >= i1 + components[i].getDim()) {
			i++;
			i1 += components[i].getDim();
		}
		return new int[] { i1, index - i1 };
	}

	@Override
	public double evaluateEffect(Disk disk, Environment environment, double[] activity, double partial_dt, double total_dt, boolean firstSlice) {
		double sum = 0.0;
		for (Actuator c : components) {
			sum += c.evaluateEffect(disk, environment, activity, partial_dt, total_dt, firstSlice);
		}
		return sum;
	}

	@Override
	public VisualisationItem getVisualisationItem() {
		final VisualisationItem[] items = new VisualisationItem[getDim()];
		for (int i = 0; i < components.length; i++) {
			items[i] = components[i].getVisualisationItem();
		}
		return new VisualisationItem() {
			@Override
			public void draw(Graphics2D g, double centerx, double centery,
					double radius, double angle, double[] activity,
					double[] measurement, VisualizationSettings settings,
					DiskType diskType) {
				for (VisualisationItem item : items) {
					if (item != null)
						item.draw(g, centerx, centery, radius, angle, activity, measurement, settings, diskType);
				}
			}
		};
	}

}
