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
package diskworld;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import diskworld.actions.JointActionType;
import diskworld.actions.JointActionType.ActionType;
import diskworld.actions.JointActionType.ControlType;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.Sensor;
import diskworld.visualization.VisualisationItem;
import diskworld.visualization.VisualizationSettings;

/**
 * The DiskType determines the static properties of a disk (the material from which the disk is made, the color index used for visualization and sensors, whether it has a sensor attached (and which
 * type of sensor) to it, whether it acts as actuator (and which type of actuator it is).
 * 
 * The mass and radius are supposed to be dynamic (may change during the simulation) and are thus not part of disk type. It is allowable to create several Disks using the same DiskType object.
 */
public class DiskType {

	private final DiskMaterial material;
	private final Sensor[] sensors;
	private final Actuator actuator;
	private List<JointActionType> jointActionTypes;

	/**
	 * Only used for visualization: Additional items to be drawn (optionally).
	 */
	private final List<VisualisationItem> visualisationItems;

	/**
	 * Constructor for DiskType. Color is set to a default provided by material, but may be changed afterwards if needed.
	 * 
	 * @param material
	 *            the material properties
	 * @param actuator
	 *            Actuator attached to the disk or null
	 * @param sensors
	 *            Sensors attached to the disk or null
	 */
	public DiskType(DiskMaterial material, Actuator actuator, Sensor[] sensors) {
		this.material = material;
		this.sensors = sensors;
		this.actuator = actuator;
		// NOTE: the sensors must be first in the list, in order to be able to assign the correct measurement values to them 
		this.visualisationItems = createSensorVisualisationItems(sensors);
		VisualisationItem actuatorVisualisation = (actuator != null) ? actuator.getVisualisationItem() : null;
		if (actuatorVisualisation != null)
			this.visualisationItems.add(actuatorVisualisation);
		this.jointActionTypes = null;
	}

	private static List<VisualisationItem> createSensorVisualisationItems(Sensor[] sensors) {
		List<VisualisationItem> list = new LinkedList<VisualisationItem>();
		if (sensors != null) {
			for (Sensor s : sensors) {
				VisualisationItem vi = s.getVisualisationItem();
				if (vi == null) {
					// createDummyVisualisation
					vi = new VisualisationItem() {
						@Override
						public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, double[] measurement, VisualizationSettings settings,
								DiskType diskType) {
						}
					};
				}
				list.add(vi);
			}
		}
		return list;
	}

	/**
	 * Convenience constructor without sensor
	 * 
	 * @param material
	 *            the material properties
	 * @param actuator
	 *            Actuator attached to the disk or null
	 */
	public DiskType(DiskMaterial material, Actuator actuator) {
		this(material, actuator, null);
	}

	/**
	 * Convenience constructor without actuator
	 * 
	 * @param material
	 *            the material properties
	 * @param sensors
	 *            Sensor attached to the disk or null
	 */
	public DiskType(DiskMaterial material, Sensor[] sensors) {
		this(material, null, sensors);
	}

	/**
	 * Convenience constructor without sensor and actuator
	 * 
	 * @param material
	 *            the material properties
	 */
	public DiskType(DiskMaterial material) {
		this(material, null, null);
	}

	/**
	 * Convenience constructor without actuator and only one sensor
	 * 
	 * @param material
	 *            the material properties
	 * @param sensor
	 *            Sensor attached to the disk or null
	 */
	public DiskType(DiskMaterial material, Sensor sensor) {
		this(material, null, new Sensor[] { sensor });
	}

	/**
	 * Material properties
	 * 
	 * @return material for disks of this type
	 */
	public DiskMaterial getMaterial() {
		return material;
	}

	/**
	 * Is a sensor attached?
	 * 
	 * @return true if a sensor is attached to this disk
	 */
	public boolean hasSensors() {
		return (sensors != null) && (sensors.length != 0);
	}

	/**
	 * Access to the sensors
	 * 
	 * @return array of Sensor objects attached to this type of disks or null if none
	 */
	public Sensor[] getSensors() {
		return sensors;
	}

	/**
	 * Is an actuator attached?
	 * 
	 * @return true if a sensor is attached to this disk
	 */
	public boolean hasActuator() {
		return actuator != null;
	}

	/**
	 * Access to the Sensor type
	 * 
	 * @return Actuator attached to this type of disks or null if none
	 */
	public Actuator getActuator() {
		return actuator;
	}

	/**
	 * Access to the Joint actions list
	 * 
	 * @return list of possible joint actions (or null if the disk type is not considered a joint)
	 */
	public List<JointActionType> getJointActions() {
		return jointActionTypes;
	}

	/**
	 * Add a joint action to the disk type
	 * 
	 * @param jointActionType
	 *            the JointActionType to be added
	 */
	public void addJointAction(JointActionType jointActionType) {
		if (jointActionTypes == null) {
			jointActionTypes = new LinkedList<JointActionType>();
		}
		jointActionTypes.add(jointActionType);
	}

	/**
	 * Add a joint action to the disk type
	 * 
	 * @param maxChangePerTimeStep
	 * @param controlType
	 * @param actionType
	 *            the ActionType to be added
	 */
	public void addJointAction(double maxChangePerTimeStep, ControlType controlType, ActionType actionType) {
		addJointAction(new JointActionType(maxChangePerTimeStep, controlType, actionType));
	}

	/**
	 * Determine if disks of this type are joints or un-controllable disks
	 * 
	 * @return true if there are disk actions defined for this type
	 */
	public boolean isJoint() {
		return jointActionTypes != null;
	}

	/**
	 * Provide list of additional items for visualization
	 * 
	 * @return list of visualization items to be drawn
	 */
	public List<VisualisationItem> getAdditionalVisualisationItems() {
		return visualisationItems;
	}

	/**
	 * Add visualization item
	 * 
	 * @param item
	 *            visualization items to be added to the list
	 */
	public void addVisualisationItem(VisualisationItem item) {
		visualisationItems.add(item);
	}

}
