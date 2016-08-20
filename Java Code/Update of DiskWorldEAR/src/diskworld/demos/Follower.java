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

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actuators.Mover;
import diskworld.demos.DemoLauncher.Demo;
import diskworld.environment.AgentMapping;
import diskworld.environment.FloorCellType;
import diskworld.interfaces.AgentController;
import diskworld.interfaces.Sensor;
import diskworld.sensors.ClosestDiskSensor;
import diskworld.visualization.VisualizationOption;
import diskworld.visualization.VisualizationOptions;
import diskworld.visualization.VisualizationSettings;

public class Follower implements Demo {

	private static final double SENSOR_OPENING_ANGLE = Math.toRadians(120);
	private static final double MAX_DISPLACEMENT_DISTANCE = 0.1;

	private DiskComplex agent;
	private Disk agentDisk;
	private ClosestDiskSensor sensor;
	private Mover mover;

	@Override
	public String getTitle() {
		return "Disk Follower";
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
		sensor = ClosestDiskSensor.getPositionAngleSensor(env, SENSOR_OPENING_ANGLE, 15);
		mover = new Mover(MAX_DISPLACEMENT_DISTANCE, 0.1, 0.1);
		DiskType agentDiskType = new DiskType(DiskMaterial.METAL, mover, new Sensor[] { sensor });

		// change the material properties: disable friction for rubber, set high friction for metal
		DiskMaterial.RUBBER.setFrictionCoefficient(0.0);
		DiskMaterial.METAL.setFrictionCoefficient(0.4);

		// construct balls (disks made of rubber)
		double x1 = 8;
		double y1 = 14;
		double r1 = 1;
		Disk d1 = env.newRootDisk(x1, y1, r1, ball);
		// give it a kick
		d1.applyImpulse(+6, -3);

		double x2 = 12;
		double y2 = 15;
		double r2 = 0.5;
		Disk d2 = env.newRootDisk(x2, y2, r2, ball);
		// give it a kick
		d2.applyImpulse(+4, -3);

		// construct agent (a sensor and two motors on the sides)
		double xa = 20;
		double ya = 25;
		double ra = 1;
		agentDisk = env.newRootDisk(xa, ya, ra, agentDiskType);
		// store the agent diskComplex for later use
		agent = agentDisk.getDiskComplex();

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

			private int count=0;
			
			@Override
			public void doTimeStep(double[] sensorValues, double[] actuatorValues) {
				count++;
				System.out.println(count);
				if (count == 1000) {
					System.out.println("Wait");
					
				}
				if (sensorValues[0] < 0.5) {
					// no disk seen, stop
					actuatorValues[0] = 0.0;
					actuatorValues[1] = 0.0;
				} else {
					// disk seen, move
					actuatorValues[0] = 1.0;
					double angle = sensor.getRealWorldInterpretation(sensorValues, 1);
					// and turn towards the seen disk
					actuatorValues[1] = mover.getActivityFromRealWorldData(angle, 1);
				}
			}
		};
		return new AgentController[] { controller };
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new Follower());
	}

	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		VisualizationOption opt = settings.getOptions().getOption(VisualizationOptions.GROUP_SENSORS, ClosestDiskSensor.SENSOR_NAME);
		if (opt != null) {
			opt.setChosenOption(1);
			return true;
		}
		return false;
	}

}