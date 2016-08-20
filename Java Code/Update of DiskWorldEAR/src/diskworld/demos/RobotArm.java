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
package diskworld.demos;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actions.DiskAction;
import diskworld.actions.Joint;
import diskworld.actions.JointActionType;
import diskworld.demos.DemoLauncher.Demo;
import diskworld.environment.AgentMapping;
import diskworld.interfaces.AgentController;
import diskworld.visualization.VisualizationSettings;

public class RobotArm implements Demo {

	private Joint[] joints;

	@Override
	public String getTitle() {
		return "Robot Arm";
	}

	@Override
	public long getMiliSecondsPerTimeStep() {
		return 5;
	}

	@Override
	public Environment getEnvironment() {
		// create environment
		int sizex = 100;
		int sizey = 100;
		Environment env = new Environment(sizex, sizey);

		// provide disk types 
		DiskType rootType = new DiskType(DiskMaterial.METAL.withColor(Color.BLUE));
		DiskType limbType = new DiskType(DiskMaterial.METAL);
		DiskType jointType = new DiskType(DiskMaterial.METAL.withColor(Color.RED));
		DiskType endEffectorType = new DiskType(DiskMaterial.METAL.withColor(Color.GREEN));

		// construct arm
		int numLimbs = 3;
		int numPerLimb = 5;
		joints = new Joint[numLimbs];
		Joint root = env.newRootJoint(sizex / 2, sizey / 2, 1, 0, rootType, true);
		joints[0] = root;
		joints[0].setRange(JointActionType.ActionType.SPIN, Math.toRadians(0.1), Math.toRadians(360));
		Disk d = root;
		for (int j = 0; j < numPerLimb; j++) {
			d = d.attachDisk(0, 1, limbType);
		}
		for (int i = 0; i < numLimbs; i++) {
			if (i > 0) {
				joints[i] = d.attachJoint(0, 1, 0, jointType);
				// limit all joints to the range -60 to +30 degrees 
				joints[i].setRange(JointActionType.ActionType.SPIN, Math.toRadians(-160), Math.toRadians(160));
				d = joints[i];
			}
			for (int j = 0; j < numPerLimb; j++) {
				d = d.attachDisk(0, 1, limbType);
			}
		}
		d.attachDisk(0, 2, endEffectorType);

		return env;
	}

	@Override
	public AgentMapping[] getAgentMappings() {
		// create possible ego-motion actions
		List<DiskAction> actions = new LinkedList<DiskAction>();
		// set max angular speed of joint rotations
		double maxAngularChange = 0.5;
		// first joint is controlled by target angle
		actions.add(joints[0].createJointAction("joint#0", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
		// other joints are controlled by angle change
		for (int i = 1; i < joints.length; i++) {
			actions.add(joints[i].createJointAction("joint#" + i, maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
		}
		return new AgentMapping[] { new AgentMapping(actions) };
	}

	@Override
	public AgentController[] getControllers() {
		// the controller of the agent
		AgentController controller = new AgentController() {
			@Override
			public void doTimeStep(double[] sensorValues, double[] actuatorValues) {
				actuatorValues[0] = 0;
				actuatorValues[1] = 0.5;
				actuatorValues[2] = 0.2;
			}
		};
		return new AgentController[] { controller };
	}

	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		return true;
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new RobotArm());
	}

}