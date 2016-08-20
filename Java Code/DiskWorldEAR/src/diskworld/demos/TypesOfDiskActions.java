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
import diskworld.visualization.VisualizationOptions;
import diskworld.visualization.VisualizationSettings;

public class TypesOfDiskActions implements Demo {

	private Joint[][] joint;

	@Override
	public String getTitle() {
		return "Types of Disk Actions";
	}

	@Override
	public long getMiliSecondsPerTimeStep() {
		return 5;
	}

	@Override
	public Environment getEnvironment() {
		// create environment
		int sizex = 120;
		int sizey = 120;
		Environment env = new Environment(sizex, sizey);

		// provide disk types 
		DiskType jointType = new DiskType(DiskMaterial.METAL.withColor(Color.BLUE));
		DiskType limbType = new DiskType(DiskMaterial.METAL);

		// construct agents
		joint = new Joint[3][2];
		for (int i = 0; i < 3; i++) {
			Disk root = env.newFixedRoot((sizex * (2 * i + 1)) / 6, sizey / 2, 3, 0, limbType);
			for (int j = 0; j < 2; j++) {
				joint[i][j] = root.attachJoint(Math.toRadians(90 + (1 - 2 * j) * 40), i == 2 ? 5 : 2, 0, jointType);
				int num = 5;
				Disk d = joint[i][j];
				for (int k = 0; k < num; k++) {
					d = d.attachDisk(Math.toRadians((1 - 2 * j) * 10), 1, limbType);
				}
			}
		}
		return env;
	}

	@Override
	public AgentMapping[] getAgentMappings() {
		// create possible ego-motion actions
		List<DiskAction> actions = new LinkedList<DiskAction>();
		// set max angular speed of joint rotations
		double maxAngularChange = 10;
		double maxRadiusFactor = 2;
		// first joint is controlled by target angle
		actions.add(joint[0][0].createJointAction(maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
		actions.add(joint[0][1].createJointAction(maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
		actions.add(joint[1][0].createJointAction(maxAngularChange, JointActionType.ActionType.SLIDE, JointActionType.ControlType.CHANGE));
		actions.add(joint[1][1].createJointAction(maxAngularChange, JointActionType.ActionType.SLIDE, JointActionType.ControlType.CHANGE));
		actions.add(joint[2][0].createJointAction(maxRadiusFactor, JointActionType.ActionType.RESIZE, JointActionType.ControlType.CHANGE));
		actions.add(joint[2][1].createJointAction(maxRadiusFactor, JointActionType.ActionType.RESIZE, JointActionType.ControlType.CHANGE));
		return new AgentMapping[] { new AgentMapping(actions) };
	}

	@Override
	public AgentController[] getControllers() {
		// the controller of the agent
		AgentController controller = new AgentController() {
			private int time = 0;

			@Override
			public void doTimeStep(double[] sensorValues, double[] actuatorValues) {
				time++;
				double ampl[] = new double[] { 2, -2, 1.3, -1.3, -5, -5 };
				double sin = Math.sin(time * 2 * Math.PI / 500);
				for (int i = 0; i < actuatorValues.length; i++) {
					actuatorValues[i] = 0.1 * ampl[i] * sin;
				}
			}
		};
		return new AgentController[] { controller };
	}

	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		settings.getOptions().getOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_GRID).setEnabled(false);
		return true;
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new TypesOfDiskActions());
	}

}