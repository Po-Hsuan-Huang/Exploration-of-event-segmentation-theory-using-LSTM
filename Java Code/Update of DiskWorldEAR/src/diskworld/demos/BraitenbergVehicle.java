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

import java.util.HashSet;
import java.util.Set;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actuators.ImpulsDrive;
import diskworld.demos.DemoLauncher.Demo;
import diskworld.environment.AgentMapping;
import diskworld.environment.FloorCellType;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.AgentController;
import diskworld.interfaces.Sensor;
import diskworld.sensors.ClosestDiskSensor;
import diskworld.visualization.VisualizationSettings;

public class BraitenbergVehicle implements Demo {

	private static final int NUM_AGENTS = 1;
	private static final double IMPULSE_DRIVE_STRENGTH = 0.8;

	private DiskComplex agent;

	private ClosestDiskSensor sensor;

	@Override
	public String getTitle() {
		return "Braitenberg Vehicle";
	}

	@Override
	public long getMiliSecondsPerTimeStep() {
		return 5;
	}

	@Override
	public Environment getEnvironment() {
		// create environment
		int sizex = 30;
		int sizey = 30;
		Environment env = new Environment(sizex, sizey);
		env.getFloor().fill(FloorCellType.GRASS);

		// provide disk types (including sensors and actuators)
		DiskType ball = new DiskType(DiskMaterial.RUBBER);
		Set<DiskMaterial> invisibleMaterials = new HashSet<DiskMaterial>();
		invisibleMaterials.add(DiskMaterial.METAL);
		sensor = ClosestDiskSensor.getPositionAngleSensor(env, Math.toRadians(200), 15, invisibleMaterials);

		DiskType agentSensor = new DiskType(DiskMaterial.METAL, new Sensor[] { sensor });
		Actuator impulseDrive = new ImpulsDrive(IMPULSE_DRIVE_STRENGTH, 0.0);
		DiskType agentDrive = new DiskType(DiskMaterial.METAL, impulseDrive);

		// change the material properties: disable friction for rubber, set high friction for metal
		DiskMaterial.RUBBER.setFrictionCoefficient(0.0);
		DiskMaterial.METAL.setFrictionCoefficient(0.4);

		// construct ball (one disk made of rubber)
		double x1 = 10;
		double y1 = 10;
		double r1 = 1;
		Disk d1 = env.newRootDisk(x1, y1, r1, ball);
		// give it a kick
		d1.applyImpulse(+1, -3);

		// construct agent (a sensor and two motors on the sides)
		double x2 = 20;
		double y2 = 25;
		double r2 = 1;
		Disk d2 = env.newRootDisk(x2, y2, r2, Math.toRadians(271), agentSensor);
		d2.attachDisk(Math.toRadians(90), 0.5, Math.toRadians(90), agentDrive);
		d2.attachDisk(Math.toRadians(-90), 0.5, Math.toRadians(-90), agentDrive);

		// store the agent diskComplex for later use
		agent = d2.getDiskComplex();

		return env;
	}

	@Override
	public AgentMapping[] getAgentMappings() {
		// create the default mapping of sensors/actuators to a double array
		AgentMapping mapping = new AgentMapping(agent);
		return new AgentMapping[] { mapping };
	}

	@Override
	public AgentController[] getControllers() {
		// the controller of the agent
		AgentController controller = new AgentController() {
			@Override
			public void doTimeStep(double[] sensorValues, double[] actuatorValues) {
				// sensorValues has dim 2 (disk seen, angle to closest disk in view)
				boolean seen = sensorValues[0] > 0.5;
				double dir = sensor.getRealWorldInterpretation(sensorValues, 1);
				if (seen && (Math.abs(dir) < Math.toRadians(20))) {
					actuatorValues[0] = 1;
					actuatorValues[1] = 1;
				} else {
					actuatorValues[0] = -dir * 0.4;
					actuatorValues[1] = dir * 0.4;
				}
			}
		};
		AgentController[] cont = new AgentController[NUM_AGENTS];
		for (int i = 0; i < cont.length; i++) {
			cont[i] = controller;
		}
		return cont;
	}

	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		return true;
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new BraitenbergVehicle());
	}

}
