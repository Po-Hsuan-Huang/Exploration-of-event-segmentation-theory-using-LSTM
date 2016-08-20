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
import diskworld.environment.FloorCellType;
import diskworld.interfaces.AgentController;
import diskworld.visualization.VisualizationSettings;

public class Octopus implements Demo {
	// provide disk types 
	private DiskType rootType = new DiskType(DiskMaterial.METAL.withColor(Color.MAGENTA));
	private DiskType jointType = new DiskType(DiskMaterial.METAL.withColor(Color.MAGENTA));

	private Joint[][] joints;

	@Override
	public String getTitle() {
		return "Octopus arm";
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
		env.getFloor().fill(FloorCellType.GRASS);

		// construct octopus arm
		joints = new Joint[1][];
		joints[0] = createOctopusArm(env, sizex / 6, sizey / 2);

		return env;
	}

	public Joint[] createOctopusArm(Environment env, int x, int y) {

		double size = 2.5;
		Disk root = env.newFixedRoot(x, y, size * 2.0, Math.toRadians(0), rootType);

		int numLimbs = 10;

		Disk d = root;
		Joint[] res = new Joint[numLimbs];
		for (int i = 0; i <= numLimbs; i++) {
			if (i > 0) {
				size *= 0.9;
				res[i - 1] = d.attachJoint(0, size, Math.toRadians(0), jointType);
				// limit all joints to the range -60 to +30 degrees 
				res[i - 1].setRange(JointActionType.ActionType.SPIN, Math.toRadians(-15), Math.toRadians(15));
				d = res[i - 1];
			}
		}

		return res;
	}

	@Override
	public AgentMapping[] getAgentMappings() {
		AgentMapping[] res = new AgentMapping[joints.length];
		for (int i = 0; i < joints.length; i++) {
			// create possible ego-motion actions
			List<DiskAction> actions = new LinkedList<DiskAction>();
			// set max angular speed of joint rotations
			double maxAngularChange = 1;
			// first joint is controlled by angle change
			actions.add(joints[i][0].createJointAction("joint#0", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
			// other joints are controlled by angle change as well
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
				for (int i = 0; i < 10; i++)
					actuatorValues[i] = 0.01 * (20 + i * i) * Math.cos(0.2 * (time * 0.00001) * (5 + i / 2) * 1000);
				time++;
			}
		};

		AgentController[] cont = new AgentController[joints.length];
		for (int i = 0; i < joints.length; i++) {
			cont[i] = controller;
		}
		return cont;
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new Octopus());
	}

	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		// TODO Auto-generated method stub
		return false;
	}
}