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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.actions.DiskAction;
import diskworld.actions.Joint;
import diskworld.actions.JointActionType;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.Sensor;
import diskworld.linalg2D.Utils;

/**
 * Provides the interface to a controller that controls an agent. Sensor and actuator values are passed to and from the controller
 * in form of double arrays. This class maps the values to the corresponding actions and reads from the corresponding sensors.
 * For debugging purposes, each of the IO channels can be equipped with a name (a string).
 * 
 * @author Jan
 * 
 */
/**
 * @author Jan
 * 
 */
public class AgentMapping {

	/**
	 * interface for a method that extracts a value from an array (possibly applying some transformation)
	 * 
	 * @author Jan
	 */
	public static interface ValueExtractor {
		/**
		 * Extract a value (possibly performing some transformation) from the array
		 * 
		 * @param array
		 *            array of doubles
		 * @return value extracted
		 */
		public double extractValue(double[] array);
	}

	/**
	 * Not a map! Like this the sequence of entries stays deterministic!
	 */
	private ArrayList<Map.Entry<DiskAction, ValueExtractor>> actions;

	/**
	 * Not a map! Like this the sequence of entries stays deterministic!
	 */
	private ArrayList<Map.Entry<Disk, Map.Entry<Integer, ValueExtractor>>> sensors;

	private List<String> sensorNames;

	private double actuatorValues[];
	private double sensorValues[];
	private int maxActionValueIndex;

	/**
	 * Construct an empty instance
	 */
	public AgentMapping() {
		actions = new ArrayList<Map.Entry<DiskAction, ValueExtractor>>();
		sensors = new ArrayList<Map.Entry<Disk, Entry<Integer, ValueExtractor>>>();
		sensorNames = new LinkedList<String>();
		sensorValues = null;
		actuatorValues = null;
		maxActionValueIndex = 0;
	}

	/**
	 * Adds a new action together with an extractor.
	 * 
	 * @param diskAction
	 *            the action to be performed
	 * @param extractor
	 *            the extractor to be used to obtain the action value
	 */
	public void addAction(DiskAction diskAction, ValueExtractor extractor) {
		actions.add(new AbstractMap.SimpleEntry<DiskAction, AgentMapping.ValueExtractor>(diskAction, extractor));
	}

	/**
	 * Convenience method: Adds an action with an extractor that looks up the i-th value in the action value array
	 * 
	 * @param diskAction
	 *            the action to be performed
	 * @param actionValueIndex
	 *            the index at which the action value shall be taken from
	 */
	public void addAction(DiskAction diskAction, final int actionValueIndex) {
		maxActionValueIndex = Math.max(maxActionValueIndex, actionValueIndex);
		actions.add(new AbstractMap.SimpleEntry<DiskAction, AgentMapping.ValueExtractor>(diskAction, new ValueExtractor() {
			@Override
			public double extractValue(double[] array) {
				return array[actionValueIndex];
			}
		}));
	}

	/**
	 * Convenience constructor: constructs mapping using a list of DiskActions
	 * 
	 * @param diskActions
	 *            list of DiskActions to be performed, each will correspond to one actuator value
	 * 
	 */
	public AgentMapping(List<DiskAction> diskActions) {
		this();
		if (diskActions != null) {
			int count = 0;
			for (DiskAction action : diskActions) {
				addAction(action, count);
				count++;
			}
		}
	}

	/**
	 * Convenience constructor: constructs mapping using a diskComplex
	 * 
	 * @param diskComplex
	 *            all disk actions, actuators and sensors of this complex will be added to the mapping
	 */
	public AgentMapping(DiskComplex diskComplex) {
		this();
		addJointActions(diskComplex);
		addActuatorActions(diskComplex);
		addAllSensorValues(diskComplex);
	}

	private void addJointActions(DiskComplex diskComplex) {
		int count = actions.size();
		for (Disk d : diskComplex.getDisks()) {
			if (d.getDiskType().isJoint()) {
				String name = d.getName();
				if (name == null)
					name = "disk" + count;
				for (JointActionType jat : d.getDiskType().getJointActions()) {
					DiskAction diskAction = ((Joint) d).createJointAction(name, jat.maxChangePerTimeStep(), jat.createActionType(), jat.controlType());
					addAction(diskAction, count);
				}
				count++;
			}
		}
	}

	private void addActuatorActions(DiskComplex diskComplex) {
		int count = actions.size();
		for (Disk d : diskComplex.getDisks()) {
			if (d.getDiskType().hasActuator()) {
				String name = d.getName();
				if (name == null)
					name = "disk" + count;
				Actuator actuator = d.getDiskType().getActuator();
				name += ":" + actuator.getClass().getSimpleName();
				int dim = actuator.getDim();
				for (int i = 0; i < dim; i++) {
					addAction(new SetActivation(name + (dim > 1 ? "." + i : ""), d, i), count);
					count++;
				}
			}
		}
	}

	private void addAllSensorValues(DiskComplex diskComplex) {
		int count = 0;
		for (Disk d : diskComplex.getDisks()) {
			if (d.getDiskType().hasSensors()) {
				String name = d.getName();
				addAllSensorValues(name == null ? "disk" + count : name, d);
			}
		}
	}

	/**
	 * Creates a new sensor value
	 * 
	 * @param name
	 *            name of the sensor value
	 * @param disk
	 *            disk to which the sensor is attached
	 * @param sensorIndex
	 *            index of sensor in DiskType.getSensors()
	 * @param extractor
	 *            value extractor that is used to obtain the sensor measurement from the sensor's value array
	 */
	public void addSensorValue(String name, Disk disk, int sensorIndex, ValueExtractor extractor) {
		if (name == null) {
			name = disk.getName();
			if (name == null) {
				name = "sensor";
			}
		}
		sensorNames.add(name);
		Entry<Integer, ValueExtractor> pair = new AbstractMap.SimpleEntry<Integer, ValueExtractor>(sensorIndex, extractor);
		sensors.add(new AbstractMap.SimpleEntry<Disk, Map.Entry<Integer, ValueExtractor>>(disk, pair));
	}

	/**
	 * Convenience method. Creates a new sensor value using an extractor that simply takes the i-th value of the sensor measurement array
	 * 
	 * @param name
	 *            name of the sensor value
	 * @param disk
	 *            disk to which the sensor is attached
	 * @param sensorIndex
	 *            index of sensor in DiskType.getSensors()
	 * @param valueIndex
	 *            index of desired measurement in the measurement array of the sensor
	 */
	public void addSensorValue(String name, Disk disk, int sensorIndex, final int valueIndex) {
		addSensorValue(name, disk, sensorIndex, new ValueExtractor() {
			@Override
			public double extractValue(double[] array) {
				return array[valueIndex];
			}
		});
	}

	/**
	 * Convenience method. Creates a new sensor value using all values of the sensor measurement array
	 * 
	 * @param name
	 *            name of the sensor value (will be appended by index if more than 1
	 * @param disk
	 *            disk to which the sensor is attached
	 * @param sensorIndex
	 *            index of sensor in DiskType.getSensors()
	 */
	public void addSensorValues(String name, Disk disk, int sensorIndex) {
		if (name == null) {
			name = disk.getName();
			if (name == null) {
				name = "sensor";
			}
		}
		int numSensorChannels = disk.getSensorMeasurements()[sensorIndex].length;
		for (int i = 0; i < numSensorChannels; i++) {
			addSensorValue(name + (numSensorChannels == 1 ? "" : "." + i), disk, sensorIndex, i);
		}
	}

	/**
	 * Convenience method. Creates a new sensor value using all values all sensors
	 * 
	 * @param name
	 *            name of the disk (will be appended by sensor class name)
	 * @param disk
	 *            disk to which the sensor is attached
	 */
	public void addAllSensorValues(String name, Disk disk) {
		if (name == null) {
			name = disk.getName();
			if (name == null) {
				name = "sensor";
			}
		}
		Sensor sensors[] = disk.getDiskType().getSensors();
		for (int i = 0; i < sensors.length; i++) {
			addSensorValues(name + ":" + sensors[i].getClass().getSimpleName(), disk, i);
		}
	}

	/**
	 * Access to sensor values
	 * 
	 * @return array of sensor values
	 */
	public double[] getSensorValues() {
		if ((sensorValues == null) || (sensorValues.length != sensors.size())) {
			sensorValues = new double[sensors.size()];
		}
		return sensorValues;
	}

	/**
	 * Access to actuator values
	 * 
	 * @return array of actuator values
	 */
	public double[] getActuatorValues() {
		if ((actuatorValues == null) || (actuatorValues.length < maxActionValueIndex)) {
			actuatorValues = new double[maxActionValueIndex + 1];
		}
		return actuatorValues;
	}

	/**
	 * Access to the sensor names
	 * 
	 * @return array of strings holding the sensor value identifiers
	 */
	public String[] getSensorNames() {
		return sensorNames.toArray(new String[sensors.size()]);
	}

	/**
	 * Access to the actuator names. Can be also used to set the names.
	 * 
	 * @return array of strings holding the actuator value identifiers
	 */
	public String[] getActuatorNames() {
		int count = 0;
		String res[] = new String[actions.size()];
		for (Entry<DiskAction, ValueExtractor> e : actions) {
			res[count] = e.getKey().getName();
			count++;
		}
		return res;
	}

	public int getNumActions() {
		return actions.size();
	}

	public DiskAction getAction(int index) {
		return actions.get(index).getKey();
	}

	public double getClippedTranslatedActionValue(int index) {
		Entry<DiskAction, ValueExtractor> e = actions.get(index);
		DiskAction action = e.getKey();
		ValueExtractor valueExtractor = e.getValue();
		double actionValue = valueExtractor.extractValue(getActuatorValues());
		double min = action.getMinActionValue();
		double max = action.getMaxActionValue();
		actionValue = (Utils.clip_pm1(actionValue) * (max - min) + (max + min)) / 2.0;
		return actionValue;
	}

	/**
	 * Read all sensor values by extracting a value from the corresponding sensor reading array
	 */
	public void readSensorValues() {
		int count = 0;
		if ((sensorValues == null) || (sensorValues.length != sensors.size())) {
			sensorValues = new double[sensors.size()];
		}
		for (Entry<Disk, Entry<Integer, ValueExtractor>> e : sensors) {
			Disk disk = e.getKey();
			int sensorIndex = e.getValue().getKey();
			ValueExtractor extractor = e.getValue().getValue();
			double sensorMeasurement[] = disk.getSensorMeasurements()[sensorIndex];
			sensorValues[count] = extractor.extractValue(sensorMeasurement);
			count++;
		}
	}
}
