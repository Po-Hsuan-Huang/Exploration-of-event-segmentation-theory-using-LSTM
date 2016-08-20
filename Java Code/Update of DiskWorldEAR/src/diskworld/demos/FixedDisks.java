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

public class FixedDisks implements Demo {

	// provide disk types 
	private DiskType rootType = new DiskType(DiskMaterial.METAL.withColor(Color.BLUE));
	private DiskType limbType = new DiskType(DiskMaterial.METAL);
	private DiskType jointType = new DiskType(DiskMaterial.METAL.withColor(Color.RED));
	private DiskType endEffectorType = new DiskType(DiskMaterial.METAL.withColor(Color.GREEN));

	private Joint[][] joints;

	@Override
	public String getTitle() {
		return "Fixed Disks";
	}

	@Override
	public long getMiliSecondsPerTimeStep() {
		return 5;
	}

	@Override
	public Environment getEnvironment() {
		// create environment
		int sizex = 50;
		int sizey = 50;
		Environment env = new Environment(sizex, sizey);

		// construct arms
		joints = new Joint[2][];
		joints[0] = createRobotArm(env, sizex / 4, sizey / 3, false);
		joints[1] = createRobotArm(env, (sizex * 3) / 4, sizey / 3, true);
		return env;
	}

	public Joint[] createRobotArm(Environment env, int x, int y, boolean fixedRoot) {
		Disk root;
		if (fixedRoot)
			root = env.newFixedRoot(x, y, 0.5, Math.toRadians(90), rootType);
		else
			root = env.newRootDisk(x, y, 0.5, Math.toRadians(90), rootType);
		int numLimbs = 3;
		int numPerLimb = 5;
		Disk d = root;
		Joint[] res = new Joint[numLimbs - 1];
		for (int i = 0; i < numLimbs; i++) {
			if (i > 0) {
				res[i - 1] = d.attachJoint(0, 0.5, 0, jointType);
				// limit all joints to the range -60 to +30 degrees 
				res[i - 1].setRange(JointActionType.ActionType.SPIN, Math.toRadians(-90), Math.toRadians(90));
				d = res[i - 1];
			}
			for (int j = 0; j < numPerLimb; j++) {
				d = d.attachDisk(0, 0.5, limbType);
			}
		}
		d.attachDisk(0, 0.75, endEffectorType);
		return res;
	}

	@Override
	public AgentMapping[] getAgentMappings() {
		AgentMapping[] res = new AgentMapping[joints.length];
		for (int i = 0; i < joints.length; i++) {
			// create possible ego-motion actions
			List<DiskAction> actions = new LinkedList<DiskAction>();
			// set max angular speed of joint rotations
			double maxAngularChange = 0.01;
			// first joint is controlled by target angle
			actions.add(joints[i][0].createJointAction("joint#0", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.TARGET));
			// other joints are controlled by angle change
			for (int j = 1; j < joints[i].length; j++) {
				actions.add(joints[i][j].createJointAction("arm#" + i + ".joint#" + j, maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
			}
			res[i] = new AgentMapping(actions);
		}
		return res;
	}

	@Override
	public AgentController[] getControllers() {
		// the controller of the agent
		AgentController controller = new AgentController() {
			int time = 0;

			@Override
			public void doTimeStep(double[] sensorValues, double[] actuatorValues) {
				int sign = ((time / 600) % 2) * 2 - 1;
				actuatorValues[0] = sign * Math.toRadians(60);
				actuatorValues[1] = sign * 0.01;
				time++;
			}
		};
		return new AgentController[] { controller, controller };
	}

	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		return true;
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new FixedDisks());
	}

}