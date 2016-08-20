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

public class RobotHand implements Demo {

	DiskType rootType = new DiskType(DiskMaterial.METAL.withColor(Color.BLUE));
	DiskType limbType = new DiskType(DiskMaterial.METAL);
	DiskType jointType = new DiskType(DiskMaterial.METAL.withColor(Color.RED));
	DiskType endEffectorType = new DiskType(DiskMaterial.METAL.withColor(Color.GREEN));
	DiskType objectType = new DiskType(DiskMaterial.RUBBER.withColor(Color.YELLOW));

	private Joint joints[][];

	@Override
	public String getTitle() {
		return "Robot Arm";
	}

	public Joint[] createRobotHand(Environment env, double x, double y) {
		int numPerLimb = 5;
		Joint joint[] = new Joint[3];
		joint[0] = env.newRootJoint(x, y, 1, 0, rootType, true);
		Disk d = joint[0];
		for (int j = 0; j < numPerLimb; j++) {
			d = d.attachDisk(0, 1, limbType);
		}
		Joint fork = d.attachJoint(0, 1, 0, jointType);

		joint[1] = fork.attachJoint(Math.toRadians(90), 1, Math.toRadians(-60), jointType);
		d = joint[1].attachDisk(0, 1, Math.toRadians(15), limbType);
		d = d.attachDisk(0, 1, limbType);
		d = d.attachDisk(0, 1, limbType);
		d.attachDisk(0, 1, endEffectorType);

		joint[2] = fork.attachJoint(Math.toRadians(-90), 1, Math.toRadians(60), jointType);
		d = joint[2].attachDisk(0, 1, -Math.toRadians(15), limbType);
		d = d.attachDisk(0, 1, limbType);
		d = d.attachDisk(0, 1, limbType);
		d.attachDisk(0, 1, endEffectorType);
		return joint;
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

		// construct arms
		joints = new Joint[4][];
		double offsx = -10;
		double offsy = -10;
		double x1 = sizex / 4 + offsx;
		double x2 = (sizex * 3) / 4 + offsx;
		double y1 = sizey / 4 + offsy;
		double y2 = (sizey * 3) / 4 + offsy;
		joints[0] = createRobotHand(env, x1, y1);
		joints[1] = createRobotHand(env, x2, y1);
		joints[2] = createRobotHand(env, x1, y2);
		joints[3] = createRobotHand(env, x2, y2);

		offsx = +18;
		offsy = -1;
		env.newRootDisk(x1 + offsx, y1 + offsy, 1.6, objectType);
		env.newRootDisk(x2 + offsx, y1 + offsy, 2.5, objectType);
		env.newRootDisk(x1 + offsx + 2, y2 + offsy, 4, objectType);
		env.newRootDisk(x2 + offsx + 4, y2 + offsy, 6, objectType);

		return env;
	}

	@Override
	public AgentMapping[] getAgentMappings() {
		// set max angular speed of joint rotations
		double maxAngularChange = 0.5;
		// first joint is controlled by target angle
		AgentMapping res[] = new AgentMapping[joints.length];
		for (int i = 0; i < joints.length; i++) {
			// create possible ego-motion actions
			List<DiskAction> actions = new LinkedList<DiskAction>();
			actions.add(joints[i][0].createJointAction("joint#0", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
			actions.add(joints[i][1].createJointAction("joint#1", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
			actions.add(joints[i][2].createJointAction("joint#2", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
			res[i] = new AgentMapping(actions);
		}
		return res;
	}

	@Override
	public AgentController[] getControllers() {
		AgentController res[] = new AgentController[joints.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = new AgentController() {
				int time = 0;

				@Override
				public void doTimeStep(double[] sensorValues, double[] actuatorValues) {
					time++;
					if (time < 500) {
						actuatorValues[0] = Math.toRadians(0);
						actuatorValues[1] = Math.toRadians(-20);
						actuatorValues[2] = Math.toRadians(20);
					} else if (time < 1400) {
						actuatorValues[0] = Math.toRadians(20);
						actuatorValues[1] = Math.toRadians(-20);
						actuatorValues[2] = Math.toRadians(20);
					} else if (time < 1800) {
						actuatorValues[0] = Math.toRadians(0);
						actuatorValues[1] = Math.toRadians(10);
						actuatorValues[2] = Math.toRadians(-10);
					} else {
						actuatorValues[0] = Math.toRadians(0);
						actuatorValues[1] = Math.toRadians(0);
						actuatorValues[2] = Math.toRadians(0);
					}

				}
			};
		}
		return res;
	}

	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		return true;
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new RobotHand());
	}

}