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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actuators.ImpulsDrive;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.Sensor;
import diskworld.sensors.ClosestDiskSensor;
import diskworld.sensors.RadarSensor;
import diskworld.visualization.EnvironmentPanel;

class TestSensor {

	static JFrame frame = new JFrame();
	static Environment env;

	public static void main(String[] args) {
		env = new Environment(10, 10);
		setRandomFloor();
		sensor1();
		test("Sensor1: Disk Distance (Cone)", 600, 5);
		frame.dispose();
		System.exit(0);
	}

	public static void test(String title, int numsteps, int sleepTime) {
		System.out.println(title);
		env.setTime(0);
		EnvironmentPanel ep = new EnvironmentPanel();
		ep.setEnvironment(env);
		ep.getSettings().setFullView(env);
		//ep.getSettings().setCircleSymbol(2, 0.5);
		//ep.getSettings().setViewedRect(0.0,0.0,0.5,0.5);
		JPanel panel = new JPanel();
		frame.add(panel);
		frame.setTitle(title);
		frame.setVisible(true);
		frame.setSize(new Dimension(620, 700)); //620,700
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel label = new JLabel(title);
		label.setFont(new Font(Font.SERIF, Font.BOLD, 36));
		panel.add(label);
		ep.setPreferredSize(new Dimension(600, 600)); //600,600
		panel.add(ep);
		frame.add(panel);
		frame.validate();
		final char[] key = new char[1];
		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				key[0] = e.getKeyChar();
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		do {
			key[0] = 0;
			//		for (int i = 0; i < numsteps; i++) {
			long ts = System.currentTimeMillis();
			//env.doTimeStep(0.01);
			double dt = 0.01;
			env.doTimeStep(dt);
			ep.repaint();
			long time = System.currentTimeMillis() - ts;
			long sleep = sleepTime - time;
			if (sleep > 0) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
				}
			}
			//	}
			//	while (key[0] == 0) {
			//		try {
			//			Thread.sleep(100);
			//		} catch (InterruptedException e1) {
			//		}
			//	}
		} while (key[0] != ' ');
		panel.remove(ep);
		//env.clear();
		//		try {
		//		Thread.sleep(1000);
		//} catch (InterruptedException e) {
		//}
	}

	public static void setRandomFloor() {
		Random rand = new Random();
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setTypeIndex(i, j, rand.nextDouble() < 0.1 ? 1 : 0);
	}

	public static void setCheckerBoardFloor() {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setTypeIndex(i, j, ((i + j) % 2));
	}

	public static void setQuadrantFloor() {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setTypeIndex(i, j, (i >= 5) && (j <= 4) ? 3 : 0);
	}

	public static void sensor1() {
		Actuator actuator = new ImpulsDrive(0.2, 0.0);
		DiskType diskType1 = new DiskType(DiskMaterial.RUBBER, actuator);
		//Sensor sensor = new DiskDistanceSensor(env, 0, 0.5 * Math.PI, 5);
		Sensor sensor = new RadarSensor(env, 0, 0.2, 5);
		DiskType diskType2 = new DiskType(DiskMaterial.METAL, sensor);
		Sensor sensor2 = ClosestDiskSensor.getPositionAngleSensor(env, 0.25 * Math.PI, 3);
		DiskType diskType3 = new DiskType(DiskMaterial.METAL, sensor2);

		Disk d1 = env.newRootDisk(9, 6, 0.5, Math.toRadians(0), diskType1);
		Disk d2 = env.newRootDisk(4, 5, 1.0, Math.toRadians(180), diskType2);
		d2.attachDisk(Math.toRadians(90), 0.8, diskType3);

		//dc1.applyImpulse(0.02, 0.0, 0.2, 0.45);
		d1.setActivity(0, 0.5);
		d2.applyImpulse(-10, 0.0);
	}
}
