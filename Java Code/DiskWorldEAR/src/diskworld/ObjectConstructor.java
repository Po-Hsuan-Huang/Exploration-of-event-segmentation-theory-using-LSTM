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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import diskworld.actions.Joint;
import diskworld.actions.JointActionType;
import diskworld.actions.JointActionType.ActionType;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.Sensor;

/**
 * Class that allows the construction of one or multiple DiskComplexes according to a blue print (consisting of a list of DescriptionItems).
 * 
 * @author Jan
 *
 */
public class ObjectConstructor {

	private final Map<String, DiskType> diskTypes;
	private boolean fixedRoot;
	private DescriptionItem rootDescription;
	private final List<DescriptionItem> childrenDescriptions;
	private final Environment env;

	private static class DescriptionItem {
		private int parentIndex;
		private double positionAngle;
		private double rotationAngle;
		private double radius;
		private DiskType diskType;
		private Map<JointActionType.ActionType, double[]> jointActionRange;

		DescriptionItem(int parentIndex,
				double positionAngle,
				double rotationAngle,
				double radius,
				DiskType diskType) {
			this.positionAngle = positionAngle;
			this.rotationAngle = rotationAngle;
			this.parentIndex = parentIndex;
			this.radius = radius;
			this.diskType = diskType;
			this.jointActionRange = new HashMap<JointActionType.ActionType, double[]>();
		}

		void createDisk(ArrayList<Disk> disks, double radiusFactor) {
			int index = disks.size();
			Disk parent;
			if (parentIndex < 0) {
				parent = disks.get(index + parentIndex);
			} else {
				parent = disks.get(parentIndex);
			}
			if (parent == null)
				throw new IllegalArgumentException("Index not in existing nodes vector range");
			if (diskType.isJoint()) {
				Joint joint = parent.attachJoint(positionAngle, radiusFactor * radius, rotationAngle, diskType);
				for (Entry<ActionType, double[]> e : jointActionRange.entrySet()) {
					joint.setRange(e.getKey(), e.getValue());
				}
				disks.add(joint);
			} else {
				disks.add(parent.attachDisk(positionAngle, radiusFactor * radius, rotationAngle, diskType));
			}
		}

		public void setRange(ActionType actionType, double minValue, double maxValue) {
			jointActionRange.put(actionType, new double[] { minValue, maxValue });
		}
	}

	public ObjectConstructor(Environment env) {
		this.diskTypes = new TreeMap<String, DiskType>();
		this.env = env;
		this.rootDescription = null;
		this.childrenDescriptions = new LinkedList<ObjectConstructor.DescriptionItem>();
	}

	public void createDiskType(String name, DiskMaterial material, Actuator actuator, Sensor[] sensors) {
		DiskType dt = new DiskType(material, actuator, sensors);
		if (diskTypes.containsKey(name.toLowerCase())) {
			throw new IllegalArgumentException("DiskType with this name already exists: " + name);
		}
		diskTypes.put(name.toLowerCase(), dt);
	}

	public void createDiskType(String name, DiskMaterial material, Sensor sensor) {
		createDiskType(name, material, null, new Sensor[] { sensor });
	}

	public void createDiskType(String name, DiskMaterial material, Actuator actuator) {
		createDiskType(name, material, actuator, null);
	}

	public void createDiskType(String name, DiskMaterial material) {
		createDiskType(name, material, null, null);
	}

	public void setRoot(double rootRadius, boolean fixed, DiskType rootType) {
		rootDescription = new DescriptionItem(0, 0, 0, rootRadius, rootType);
		fixedRoot = fixed;
	}

	public void setRootActionRange(ActionType actionType, double minValue, double maxValue) {
		rootDescription.setRange(actionType, minValue, maxValue);
	}

	public void setRoot(double rootRadius, DiskType rootType) {
		setRoot(rootRadius, false, rootType);
	}

	public void setRoot(double rootRadius, boolean fixedRoot, String rootTypeName) {
		setRoot(rootRadius, fixedRoot, getType(rootTypeName));
	}

	public int addItem(int parentIndex, double positionAngle, double rotationAngle, double radius, String diskTypeName) {
		childrenDescriptions.add(new DescriptionItem(parentIndex, positionAngle, rotationAngle, radius, getType(diskTypeName)));
		return childrenDescriptions.size();
	}

	public int addItem(int parentIndex, double positionAngle, double rotationAngle, double radius, DiskType diskType) {
		childrenDescriptions.add(new DescriptionItem(parentIndex, positionAngle, rotationAngle, radius, diskType));
		return childrenDescriptions.size();
	}

	public void setActionRange(int index, ActionType actionType, double minValue, double maxValue) {
		childrenDescriptions.get(index - 1).setRange(actionType, minValue, maxValue);
	}

	private DiskType getType(String diskTypeName) {
		DiskType res = diskTypes.get(diskTypeName.toLowerCase());
		if (res == null)
			throw new IllegalArgumentException("No disktype with this name defined: " + diskTypeName);
		return res;
	}

	public void readFromString(String descriptionString) {
		childrenDescriptions.clear();
		String split[] = descriptionString.split(";");
		if (split.length == 0) {
			throw new IllegalArgumentException("expected at least one item in " + descriptionString);
		}
		String splitRoot[] = split[0].split(":");
		if (splitRoot.length != 3) {
			throw new IllegalArgumentException("expected f/m:rootRadius:rootTypeIndex instead of " + split[0]);
		}
		try {
			boolean fixed = splitRoot[0].trim().toLowerCase().equals("f");
			double rootRadius = Double.parseDouble(splitRoot[1].trim());
			String rootType = splitRoot[2].trim();
			setRoot(rootRadius, fixed, rootType);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("number format incorrect in " + split[0]);
		}
		for (int i = 1; i < split.length; i++) {
			parseItem(split[i], i);
		}
	}

	private void parseItem(String string, int index) {
		String split[] = string.split(":");
		if (split.length != 5) {
			throw new IllegalArgumentException("expected parentIndex:positionAngle:rotationAngle:radius:diskTypeIndex instead of " + string);
		}
		int parentIndex;
		double positionAngle;
		double rotationAngle;
		double radius;
		String diskTypeName;
		try {
			parentIndex = Integer.parseInt(split[0].trim());
			if (Math.abs(parentIndex) >= index)
				throw new IllegalArgumentException("Illegal parent index: " + parentIndex);
			positionAngle = Math.toRadians(Double.parseDouble(split[1].trim()));
			rotationAngle = Math.toRadians(Double.parseDouble(split[2].trim()));
			radius = Double.parseDouble(split[3].trim());
			diskTypeName = split[4].trim();
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("number format incorrect in " + string);
		}
		addItem(parentIndex, positionAngle, rotationAngle, radius, diskTypeName);
	}

	public DiskComplex createDiskComplex(double rootx, double rooty, double rootOrientation, double scaleFactor, ArrayList<Disk> disks) {
		if (rootDescription.diskType.isJoint()) {
			Joint joint = env.newRootJoint(rootx, rooty, rootDescription.radius * scaleFactor, rootOrientation, rootDescription.diskType, fixedRoot);
			for (Entry<ActionType, double[]> e : rootDescription.jointActionRange.entrySet()) {
				joint.setRange(e.getKey(), e.getValue());
			}
			disks.add(joint);
		} else {
			disks.add(env.newRootDisk(rootx, rooty, rootDescription.radius * scaleFactor, rootOrientation, rootDescription.diskType, fixedRoot));
		}
		DiskComplex res = disks.get(0).getDiskComplex();
		for (DescriptionItem di : childrenDescriptions) {
			di.createDisk(disks, scaleFactor);
		}
		if (env.withdrawDueToCollisions(res)) {
			res = null;
		}
		return res;
	}

	public DiskComplex createDiskComplex(double rootx, double rooty, double rootOrientation, double scaleFactor) {
		ArrayList<Disk> disks = new ArrayList<Disk>();
		return createDiskComplex(rootx, rooty, rootOrientation, scaleFactor, disks);
	}

	public boolean createObject(double rootx, double rooty, double rootOrientation, double scaleFactor) {
		return createDiskComplex(rootx, rooty, rootOrientation, scaleFactor) != null;
	}

}
