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
import java.util.Random;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.ObjectConstructor;
import diskworld.demos.DemoLauncher.Demo;
import diskworld.environment.AgentMapping;
import diskworld.environment.FloorCellType;
import diskworld.interfaces.AgentController;
import diskworld.visualization.VisualizationOptions;
import diskworld.visualization.VisualizationSettings;

public class DifferentZLevels implements Demo {

	@Override
	public String getTitle() {
		return "Different z-Levels";
	}

	@Override
	public long getMiliSecondsPerTimeStep() {
		return 5;
	}

	@Override
	public Environment getEnvironment() {
		// create environment
		int sizex = 20;
		int sizey = 20;
		Environment env = new Environment(sizex, sizey);

		// provide disk types 
		DiskType diskType1 = new DiskType(DiskMaterial.METAL.withColor(Color.BLUE));
		DiskType diskType2 = new DiskType(DiskMaterial.METAL.withColor(Color.RED));

		env.getFloor().fill(FloorCellType.GRASS);

		//construct DiskComplexes with Disks at two different zLevels
		ObjectConstructor oc1 = env.createObjectConstructor();
		oc1.setRoot(0.5, diskType1);
		oc1.addItem(0, 0, 0, 0.1, diskType2);

		ObjectConstructor oc2 = env.createObjectConstructor();
		oc2.setRoot(0.2, diskType2);

		double maximpulse = 3;
		int numDisks = 5;
		Random rand = new Random(6);
		for (int i = 0; i < numDisks; i++) {
			DiskComplex dc = oc1.createDiskComplex(rand.nextDouble() * env.getMaxX(), rand.nextDouble() * env.getMaxX(), rand.nextDouble() * 2.0 * Math.PI, (rand.nextDouble() + 1.0));
			if (dc != null) {
				Disk d1 = dc.getDisks().get(0);
				Disk d2 = dc.getDisks().get(1);
				System.out.println("d1: " + d1.getRadius() + " d2: " + d2.getRadius());
				d1.setZLevel(2);
				d2.setZLevel(1);
				d1.applyImpulse(maximpulse * rand.nextDouble(), maximpulse * rand.nextDouble());
			}
		}
		for (int i = 0; i < numDisks; i++) {
			DiskComplex dc = oc2.createDiskComplex(rand.nextDouble() * env.getMaxX(), rand.nextDouble() * env.getMaxX(), rand.nextDouble() * 2.0 * Math.PI, (rand.nextDouble() + 1.0));
			if (dc != null) {
				Disk d1 = dc.getDisks().get(0);
				d1.setZLevel(1);
				d1.applyImpulse(maximpulse * rand.nextDouble(), maximpulse * rand.nextDouble());
			}
		}

		return env;
	}

	@Override
	public AgentMapping[] getAgentMappings() {
		return new AgentMapping[0];
	}

	@Override
	public AgentController[] getControllers() {
		return new AgentController[0];
	}

	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		settings.getOptions().getOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_GRID).setEnabled(false);
		settings.getOptions().getOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_SKELETON).setEnabled(false);
		return true;
	}

	public static void main(String[] args) {
		DemoLauncher.runDemo(new DifferentZLevels());
	}

}