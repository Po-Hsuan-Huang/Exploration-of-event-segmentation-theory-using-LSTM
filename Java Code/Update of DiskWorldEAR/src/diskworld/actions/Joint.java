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
package diskworld.actions;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.DiskType;

/**
 * A special disk that is marked upon creation as controllable and that keeps a reference to its parent
 * (the Disk it was attached to). This serves as reference for ego motions (rotations or radius changes)
 * Optionally a range can be specified to which possible angles/radius are restricted.
 * Initially the angular range is not set. Note that an angular range of [-PI,PI] is not the same as
 * no angular range: in the first case, the disk cannot turn further when it reaches +/-PI, in the second case
 * multiple rotations can be performed without limitations.
 * 
 * @author Jan
 * 
 */
public class Joint extends Disk {

	private final Disk parent;
	private double ranges[][];
	public static boolean targetActionValueInterpretedLinearly = false;

	/**
	 * Create a new root joint (parent == null) in the specified disk complex
	 * 
	 * @param diskComplex
	 * @param x
	 * @param y
	 * @param radius
	 * @param angle
	 * @param diskType
	 */
	public Joint(DiskComplex diskComplex, double x, double y, double radius, double angle, DiskType diskType) {
		super(diskComplex, x, y, radius, angle, diskType);
		this.ranges = new double[JointActionType.ActionType.values().length][];
		this.parent = null;
		// angle ranges are null by default
	}

	/**
	 * Create a new joint connected to the specified parent disk
	 * 
	 * @param parent
	 * @param x
	 * @param y
	 * @param radius
	 * @param angle
	 * @param diskType
	 */
	public Joint(Disk parent, double x, double y, double radius, double angle, DiskType diskType) {
		super(parent.getDiskComplex(), x, y, radius, angle, diskType);
		this.parent = parent;
		this.ranges = new double[JointActionType.ActionType.values().length][];
		// angle ranges are null by default
	}

	/**
	 * Set the range
	 * 
	 * @param actionType
	 *            type of the action
	 * @param minValue
	 *            minimum Angle/Radius, must be between -2*Pi and +2*Pi for angles and > 0 for radius range
	 * @param maxValue
	 *            maximum Angle/Radius, must be between minValue and minValue+2*Pi for angles and > minValue for radius
	 */
	public void setRange(JointActionType.ActionType actionType, double minValue, double maxValue) {
		setRange(actionType, new double[] { minValue, maxValue });
	}

	/**
	 * Set the range
	 * 
	 * @param actionType
	 *            type of the action
	 * @param range
	 *            an array of length 2 with minimum and maximum value for the specified action
	 */
	public void setRange(JointActionType.ActionType actionType, double[] range) {
		if (range.length != 2)
			throw new IllegalArgumentException("Illegal length of range array");
		if (actionType == JointActionType.ActionType.RESIZE) {
			if (range[0] <= 0)
				throw new IllegalArgumentException("Illegal value for min radius");
			if (range[1] <= range[0])
				throw new IllegalArgumentException("Illegal value for max radius");
		} else {
			if ((range[0] < -2 * Math.PI) || (range[0] > 2 * Math.PI))
				throw new IllegalArgumentException("Illegal value for min angle");
			if ((range[1] < range[0]) || (range[1] > range[0] + 2 * Math.PI))
				throw new IllegalArgumentException("Illegal value for max angle");
		}
		ranges[actionType.ordinal()] = range;
	}

	/**
	 * Create an action that changes the disk orientation. This is only possible for disks marked controllable.
	 * There are two possible modes:
	 * - absolute: the desired angle is specified, however it may take several time steps to reach that angle
	 * - relative: the desired change of angle is specified, the angle may however not leave the range limits
	 * 
	 * Depending on the mode, the control value has different role:
	 * - absolute: control value is mapped linearly to the angular range, where -1 becomes range[0] and +1 becomes range[1]
	 * - relative: control value is mapped linearly to the angular change, where -1 becomes -maxChangePerTimeStep and +1 becomes +maxChangePerTimeStep
	 * 
	 * @param maxChangePerTimeStep
	 *            the maximum value by which the angle will be increased or decreased in each time step, or maximum factor by which radius will be multiplied/divided
	 * @param actionType
	 *            if SPIN the joint rotates around its own axis
	 *            if SLIDE the joint slides along the perimeter of its parent (i.e. rotates around the parents axis)
	 * @param controlType
	 *            if TARGET_ANGLE, the action value indicates the angle to which the limb shall be set (possibly after several steps),
	 *            if ANGULAR_CHANGE the action value indicates the desired change of angle in multiples of the parameter maxChangePerTimeStep
	 * @return a DiskAction object, that can be used to perform the corresponding change of Disk positions in the DiskComplex
	 */
	public DiskAction createJointAction(String name, double maxChangePerTimeStep, JointActionType.ActionType actionType, JointActionType.ControlType controlType) {
		if (name == null) {
			name = getName();
			if (name == null) {
				name = "joint";
			}
		}
		Disk reference = null;
		double[] r = ranges[actionType.ordinal()];
		switch (actionType) {
		case SPIN:
			name += ":spin";
			reference = this;
			break;

		case SLIDE:
			if (parent == null) {
				throw new IllegalArgumentException("Cannot perform slide operation on root joint!");
			}
			reference = parent;
			name += ":slide";
			break;

		case RESIZE:
			name += ":resize";
			break;
		}
		if (controlType == JointActionType.ControlType.CHANGE) {
			name += "-change";
		} else {
			name += "-set";
		}
		boolean rootSpin = (actionType == JointActionType.ActionType.SPIN) && (parent == null);
		if (rootSpin && !isMarkedFixed()) {
			throw new IllegalArgumentException("Cannot perform spin operation not fixed root joint!");
		}

		if (actionType == JointActionType.ActionType.RESIZE) {
			if (controlType == JointActionType.ControlType.CHANGE)
				return new ChangeRadius(name, this, maxChangePerTimeStep, r);
			else
				return new SetRadius(name, this, maxChangePerTimeStep, r, targetActionValueInterpretedLinearly);

		} else {
			if (controlType == JointActionType.ControlType.CHANGE)
				return new ChangeAngle(name, this, parent, reference, maxChangePerTimeStep, r, rootSpin);
			else
				return new SetAngle(name, this, parent, reference, maxChangePerTimeStep, r, rootSpin);
		}
	}

	/**
	 * Convenience method: Create an action without specifying a name.
	 * 
	 * Create an action that changes the disk orientation. This is only possible for disks marked controllable.
	 * There are two possible modes:
	 * - absolute: the desired angle is specified, however it may take several time steps to reach that angle
	 * - relative: the desired change of angle is specified, the angle may however not leave the range limits
	 * 
	 * Depending on the mode, the control value has different role:
	 * - absolute: control value is mapped linearly to the angular range, where -1 becomes range[0] and +1 becomes range[1]
	 * - relative: control value is mapped linearly to the angular change, where -1 becomes -maxChangePerTimeStep and +1 becomes +maxChangePerTimeStep
	 * 
	 * @param maxChangePerTimeStep
	 *            the maximum value by which the angle will be increased or decreased in each time step, or maximum factor by which radius will be multiplied/divided
	 * @param actionType
	 *            if SPIN the joint rotates around its own axis
	 *            if SLIDE the joint slides along the perimeter of its parent (i.e. rotates around the parents axis)
	 * @param controlType
	 *            if TARGET_ANGLE, the action value indicates the angle to which the limb shall be set (possibly after several steps),
	 *            if ANGULAR_CHANGE the action value indicates the desired change of angle in multiples of the parameter maxChangePerTimeStep
	 * @return a DiskAction object, that can be used to perform the corresponding change of Disk positions in the DiskComplex
	 */
	public DiskAction createJointAction(double maxChangePerTimeStep, JointActionType.ActionType actionType, JointActionType.ControlType controlType) {
		return createJointAction(null, maxChangePerTimeStep, actionType, controlType);
	}
}
