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
import java.util.Collection;
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
import diskworld.environment.Wall;
import diskworld.interfaces.AgentController;
import diskworld.linalg2D.Line;
import diskworld.linalg2D.Point;
import diskworld.visualization.VisualizationSettings;

/*
 * demo to check the interaction of robot arms with two types of disks (blue: fixed, turquois: flexible)
 */
public class RobotHitsBall implements Demo {

	// provide disk types 
	private DiskType rootType = new DiskType(DiskMaterial.METAL.withColor(Color.BLUE));
	private DiskType limbType = new DiskType(DiskMaterial.METAL);
	private DiskType jointType = new DiskType(DiskMaterial.METAL.withColor(Color.RED));
	private DiskType endEffectorType = new DiskType(DiskMaterial.METAL.withColor(Color.GREEN));

	private DiskType movableBallType = new DiskType(DiskMaterial.RUBBER.withColor(Color.CYAN));
	private DiskType fixedBallType = new DiskType(DiskMaterial.RUBBER.withColor(Color.YELLOW));

	private Joint[][] joints;

	@Override
	public String getTitle() {
		return "Robot Hits Ball";
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
		Collection<Wall> walls = new LinkedList<Wall>();
		walls.add(new Wall(new Line(new Point(45, 5), new Point(45, 49)), 1));
		Environment env = new Environment(sizex, sizey, walls);
		env.getFloor().fill(FloorCellType.EMPTY);

		// construct arms
		joints = new Joint[6][];
		int x1 = sizex / 6;
		int x2 = (sizex * 3) / 6;
		int x3 = (sizex * 5) / 6;
		int y1 = 3;
		int y2 = 28;
		joints[0] = createRobotArm(env, x1, y1, false); // lower left
		joints[1] = createRobotArm(env, x1, y2, true); // lower right
		joints[2] = createRobotArm(env, x2, y1, false); // upper right
		joints[3] = createRobotArm(env, x2, y2, true); // upper left
		joints[4] = createRobotArm(env, x3, y1, false); // upper right
		joints[5] = createRobotArm(env, x3, y2, true); // upper left

		// construct balls
		int xball1 = x1 + 4;
		int xball2 = x2 + 4;
		int yball1 = y1 + 19;
		int yball2 = y2 + 19;
		double r = 1;
		env.newRootDisk(xball1, yball1, r, 0, fixedBallType, true);
		env.newRootDisk(xball2, yball1, r, 0, movableBallType, false);
		env.newRootDisk(xball1, yball2, r, 0, fixedBallType, true);
		env.newRootDisk(xball2, yball2, r, 0, movableBallType, false);

		return env;
	}

	public Joint[] createRobotArm(Environment env, int x, int y, boolean fixedRoot) {
		Disk root;
		root = env.newRootDisk(x, y, 2, Math.toRadians(90), rootType, fixedRoot);

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
			double maxAngularChange = 0.1;
			// first joint is controlled by target angle
			actions.add(joints[i][0].createJointAction("joint#0", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.TARGET));
			// other joints are controlled by angle change
			for (int j = 1; j < joints[i].length; j++) {
				actions.add(joints[i][j].createJointAction("arm#" + i + ".joint#" + j, maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.TARGET));
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
				int sign = (time / 4000) % 2 - 1;
				actuatorValues[0] = sign * Math.toRadians(60);
				actuatorValues[1] = sign * Math.toRadians(60);
				time++;
			}
		};

		AgentController[] cont = new AgentController[joints.length];
		for (int i = 0; i < joints.length; i++) {
			cont[i] = controller;
		}
		return cont;
	}

	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		return true;
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new RobotHitsBall());
	}

}
