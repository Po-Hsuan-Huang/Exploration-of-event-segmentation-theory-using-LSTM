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

public class JointActionType {

	/**
	 * Type of the joint action (spinning or sliding or resizing)
	 * 
	 * @author Jan
	 * 
	 */
	public static enum ActionType {
		/**
		 * Spin the joint around its axis (all its children move relative to its parent)
		 */
		SPIN,
		/**
		 * Slide the joint along its parent's perimeter
		 */
		SLIDE,
		/**
		 * Change radius of disk
		 */
		RESIZE

	}

	/**
	 * Action value meaning (set angle or angle change)
	 */
	public static enum ControlType {
		/**
		 * Try to set the angle/radius to the specified value
		 */
		TARGET,
		/**
		 * Try to change the angle/radius by the specified value (in case of radius change exponentially)
		 */
		CHANGE
	}

	private final double maxChangePerTimeStep;
	private final JointActionType.ControlType controlType;
	private final JointActionType.ActionType actionType;

	public JointActionType(double maxChangePerTimeStep, JointActionType.ControlType controlType, JointActionType.ActionType actionType) {
		this.maxChangePerTimeStep = maxChangePerTimeStep;
		this.controlType = controlType;
		this.actionType = actionType;
	}

	public double maxChangePerTimeStep() {
		return maxChangePerTimeStep;
	}

	public JointActionType.ControlType controlType() {
		return controlType;
	}

	public JointActionType.ActionType createActionType() {
		return actionType;
	}

}
