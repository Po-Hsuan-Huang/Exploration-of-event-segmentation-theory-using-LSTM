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
package diskworld.tests;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.LinkedList;

import javax.swing.JFrame;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.environment.Wall;
import diskworld.linalg2D.Line;
import diskworld.linalg2D.Point;
import diskworld.visualization.EnvironmentPanel;
import diskworld.visualization.VisualisationItem;
import diskworld.visualization.VisualizationSettings;

class TestVisualisation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Wall w = new Wall(new Line(new Point(0.1, 0.1), new Point(0.9, 0.3)), 0.1);
		LinkedList<Wall> walls = new LinkedList<Wall>();
		walls.add(w);
		Environment env = new Environment(10, 10, 0.1, walls);
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setType(i, j, (byte) ((i + j) % 2));
		DiskComplex d1 = env.getDiskComplexesEnsemble().createNewDiskComplex();
		DiskType dt1 = new DiskType(DiskMaterial.METAL);
		//dt1.addVisualisationItem(PolygonDiskSymbol.getTriangleSymbol(0.5));
		DiskType dt2 = new DiskType(DiskMaterial.RUBBER);
		Disk d = env.newRootDisk(0.5, 0.45, 0.1, 0, dt1);
		d = env.newRootDisk(0.2, 0.7, 0.2, 0, dt2);
		DiskType dt3 = new DiskType(DiskMaterial.METAL);
		//dt3.addVisualisationItem(PolygonDiskSymbol.getSquareSymbol(0.75));

		EnvironmentPanel ep = new EnvironmentPanel();

		// my own visualization, drawing an ellipse with radius 80,50 (not scaled or rotated with disk) 
		ep.getSettings().getOptions().addOption("MyOwnDebugOptions", "FixedEllipses");
		dt3.addVisualisationItem(new VisualisationItem() {
			@Override
			public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, double measurement[], VisualizationSettings settings, DiskType diskType) {
				if (settings.getOptions().getOption("MyOwnDebugOptions", "FixedEllipses").isEnabled()) {
					int x = settings.mapX(centerx);
					int y = settings.mapY(centery);
					int w = 80;
					int h = 50;
					g.setColor(Color.YELLOW);
					g.drawArc(x - w, y - h, w * 2 + 1, h * 2 + 1, 0, 360);
				}
			}
		});

		env.newRootDisk(0.5, 0.6, 0.05, 0, dt3);

		// rotate a little bit
		env.doTimeStep(0.03);
		System.out.println("Angle before rotation: " + d.getAngle());
		d1.applyImpulse(0, 0.1, 0.4, 0.45);
		d1.applyImpulse(0, -0.1, 0.6, 0.45);
		env.doTimeStep(0.2);
		System.out.println("Angle after rotation: " + d.getAngle());

		ep.setEnvironment(env);
		//dt2.addVisualisationItem(new CircleDiskSymbol(0.3));
		ep.getSettings().setViewedRect(0, 0, 1, 1);
		frame.add(ep);
		frame.setVisible(true);
		frame.setSize(new Dimension(600, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
