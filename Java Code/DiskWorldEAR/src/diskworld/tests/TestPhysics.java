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
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.environment.FloorCellType;
import diskworld.environment.Wall;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.CollisionEventHandler;
import diskworld.linalg2D.Line;
import diskworld.linalg2D.Point;
import diskworld.visualization.EnvironmentPanel;

public class TestPhysics {

	static JFrame frame = new JFrame();
	static Environment env;
	static List<Disk[]> mergeList = new LinkedList<Disk[]>();

	private final static double defaultElasticity = 0.01;
	private final static double defaultFrictionCoefficient = 0.9;

	// necessary to avoid creating similar DiskTypes for each Disk -> this would throw error in ColorScheme if more than 256 disks created
	private static DiskType currentDiskType;
	private static boolean changeDiskMaterialIsNecessary = true;

	public static void main(String[] args) {

		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor(FloorCellType.EMPTY);
		bounceSimple();
		test("Collision1: Elastic Bounce", 600, 5);

		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor(FloorCellType.EMPTY);
		bounceSimple2();
		test("Collision2: Fully Inelastic Bounce", 600, 5);

		env = new Environment(10, 10, 0.1, getWalls(true));
		setUniformFloor(FloorCellType.EMPTY);
		bounceWall();
		test("Collision3: Walls", 1200, 5);

		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor(FloorCellType.EMPTY);
		bounceComplex();
		test("Collision4: Complex objects", 800, 5);

		env = new Environment(10, 10, 0.1, getWalls(true));
		setQuadrantFloor(FloorCellType.GRASS, FloorCellType.EMPTY);
		bounceComplexWall();
		test("Collision5: Complex objects and walls", 800, 5);

		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor(FloorCellType.GRASS);
		bounceComplex2(0.03);
		test("Friction1: Uniform friction", 1500, 5);

		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		movingObject();
		setQuadrantFloor(FloorCellType.GRASS, FloorCellType.ICE);
		test("Friction2: Floor-dependend friction", 2000, 5);

		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor(FloorCellType.EMPTY);
		collidingObjects();
		test("ObjectChanges1: Merging", 1000, 5);

		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor(FloorCellType.EMPTY);
		collidingObjects2();
		test("ObjectChanges2: Merging many", 800, 15);

		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor(FloorCellType.EMPTY);
		splitObjects();
		test("ObjectChanges3: Splitting", 1000, 5);

		env = new Environment(10, 10, 0.1, getWalls(false));
		setUniformFloor(FloorCellType.EMPTY);
		managerToy();
		test("Manager toy", 1200, 15);

		env = new Environment(10, 10, 0.1, getWalls(false));
		setUniformFloor(FloorCellType.EMPTY);
		gasObjects();
		test("Gas simulation", 1200, 15);

		frame.dispose();
		System.exit(0);
	}

	public static LinkedList<Wall> getWalls(boolean insidewall) {
		LinkedList<Wall> walls = new LinkedList<Wall>();
		walls.add(new Wall(new Line(new Point(0, 0), new Point(1, 0)), 0.01)); // these 4 walls are the outside walls of the frame
		walls.add(new Wall(new Line(new Point(1, 0), new Point(1, 1)), 0.01));
		walls.add(new Wall(new Line(new Point(1, 1), new Point(0, 1)), 0.01));
		walls.add(new Wall(new Line(new Point(0, 1), new Point(0, 0)), 0.01));
		if (insidewall)
			walls.add(new Wall(new Line(new Point(0.3, 0.6), new Point(0.6, 0.2)), 0.05)); // actual wall the disk is interacting with
		return walls;
	}

	public static void test(String title, int numsteps, int sleepTime) {
		System.out.println(title);
		env.setTime(0);
		EnvironmentPanel ep = new EnvironmentPanel();
		ep.setEnvironment(env);
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
			env.setTime(env.getTime() + dt);
			synchronized (env) {
				for (Disk merge[] : mergeList) {
					env.getDiskComplexesEnsemble().mergeDiskComplexes(merge[0], merge[1]);
				}
			}
			mergeList.clear();
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

	public static void setUniformFloor(FloorCellType type1) {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setType(i, j, type1);
	}

	public static void setCheckerBoardFloor(FloorCellType type1, FloorCellType type2) {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setType(i, j, ((i + j) % 2 == 0) ? type1 : type2);
	}

	public static void setQuadrantFloor(FloorCellType type1, FloorCellType type2) {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setType(i, j, (i >= 5) && (j <= 4) ? type1 : type2);
	}

	/**
	 * the standard method to create disks in TestPhysics.
	 * 
	 * @param env
	 * @param x
	 * @param y
	 * @param radius
	 * @param mass
	 * @param elasticity
	 * @param frictionCoefficient
	 * @return the newly created Disk
	 */
	private static Disk createDisk(Environment env, double x, double y,
			double radius, double mass, double elasticity, double frictionCoefficient) {
		if (changeDiskMaterialIsNecessary) {
			currentDiskType = new DiskType(getNewDiskMaterial(radius, mass, elasticity, frictionCoefficient));
			changeDiskMaterialIsNecessary = false;
		}
		return env.newRootDisk(x, y, radius, 0, currentDiskType);
	}

	private static DiskMaterial getNewDiskMaterial(double radius, double mass, double elasticity, double frictionCoefficient) {
		int r = (int) (Math.random() * 100000000.0);
		Color col = getRandomSaturatedColor(new Random(r));
		return new DiskMaterial(mass / (Math.PI * radius * radius), elasticity, frictionCoefficient, 1.0, col);
	}

	private static void changeDiskMaterial() {
		changeDiskMaterialIsNecessary = true;
	}

	public static void bounceSimple() {

		double frictionCoefficient = defaultFrictionCoefficient;
		double elasticity = 1.0;
		double height = 0.46;

		Disk d1 = createDisk(env, 0.2, height, 0.05, 1.0, elasticity, frictionCoefficient);
		changeDiskMaterial();
		Disk d2 = createDisk(env, 0.8, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);

		d1.applyImpulse(0.2, 0.0);
		d2.applyImpulse(-0.2, 0.0);
	}

	public static void bounceSimple2() {

		double frictionCoefficient = defaultFrictionCoefficient;
		double elasticity = 0.0;
		double height = 0.46;

		changeDiskMaterial();
		Disk d1 = createDisk(env, 0.2, height, 0.05, 1.0, elasticity, frictionCoefficient);
		changeDiskMaterial();
		Disk d2 = createDisk(env, 0.8, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);

		d1.applyImpulse(0.2, 0.0);
		d2.applyImpulse(-0.2, 0.0);
	}

	public static void bounceWall() {

		double frictionCoefficient = defaultFrictionCoefficient;
		double elasticity = 1.0;
		double height = 0.46;

		changeDiskMaterial();
		Disk d1 = createDisk(env, 0.2, height, 0.05, 1.0, elasticity, frictionCoefficient);
		d1.applyImpulse(0.5, 0.0);
	}

	public static void bounceComplex() {

		double frictionCoefficient = defaultFrictionCoefficient;
		double elasticity = 1.0;
		double height = 0.42;

		changeDiskMaterial();
		Disk d1 = createDisk(env, 0.15, height, 0.05, 1.0, elasticity, frictionCoefficient);
		d1.attachDisk(Math.toRadians(0), 0.05, currentDiskType);
		d1.getDiskComplex().applyImpulse(0.2, 0.0, 0.2, height);

		changeDiskMaterial();
		Disk d2 = createDisk(env, 0.8, 0.45, 0.05, 1.0, elasticity, frictionCoefficient);
		d2.attachDisk(Math.toRadians(90), 0.05, currentDiskType);
		d2.getDiskComplex().applyImpulse(-0.2, 0.0, 0.8, 0.5);
	}

	public static void bounceComplexWall() {

		double frictionCoefficient = 0.0;
		double elasticity = 1.0;
		double height = 0.4;

		changeDiskMaterial();
		Disk d1 = createDisk(env, 0.35, height, 0.05, 1.0, elasticity, frictionCoefficient);
		d1.attachDisk(Math.toRadians(180), 0.05, currentDiskType);
		d1.attachDisk(Math.toRadians(270), 0.05, currentDiskType);
		d1.getDiskComplex().addNewDisk(0.15, height, 0.05, Math.toRadians(0), currentDiskType);
		d1.getDiskComplex().applyImpulse(0.2, 0.0, 0.2, height);

		changeDiskMaterialIsNecessary = true;
		createDisk(env, 0.65, height, 0.05, 1.0, elasticity, frictionCoefficient);
	}

	public static void bounceComplex2(double frictionCoefficient) {

		double elasticity = 1.0;
		double height = 0.42;

		changeDiskMaterial();
		Disk d1 = createDisk(env, 0.15, height, 0.05, 1.0, elasticity, frictionCoefficient);
		d1.attachDisk(Math.toRadians(0), 0.05, currentDiskType);
		d1.getDiskComplex().applyImpulse(0.2, 0.0, 0.2, height);

		changeDiskMaterial();
		Disk d2 = createDisk(env, 0.8, 0.45, 0.05, 1.0, elasticity, frictionCoefficient);
		d2.attachDisk(Math.toRadians(90), 0.05, currentDiskType);
		d2.getDiskComplex().applyImpulse(-0.2, 0.0, 0.8, 0.5);
	}

	public static void movingObject() {

		double frictionCoefficient = 0.2;
		double elasticity = 1.0;
		double offs = 0.07;

		changeDiskMaterial();
		Disk d1 = createDisk(env, 0.2, 0.5 + offs, 0.05, 1.0, elasticity, frictionCoefficient);
		d1.attachDisk(Math.toRadians(180), 0.05, currentDiskType);
		d1.attachDisk(Math.toRadians(270), 0.05, currentDiskType);
		d1.getDiskComplex().applyImpulse(0.1, 0.0, 0.2, 0.5 + offs);
		d1.getDiskComplex().applyImpulse(0.1, 0.0, 0.1, 0.5 + offs);
		d1.getDiskComplex().applyImpulse(0.1, 0.0, 0.2, 0.4 + offs);

		changeDiskMaterial();
		Disk d2 = createDisk(env, 0.2, 0.1, 0.05, 2.0, elasticity, frictionCoefficient);
		d2.applyImpulse(0.2, 0.0);
		Disk d3 = createDisk(env, 0.2, 0.2, 0.025, 0.5, elasticity, frictionCoefficient);
		d3.applyImpulse(0.05, 0.0);
	}

	public static void collidingObjects() {

		double frictionCoefficient = 0.005;
		double elasticity = defaultElasticity;

		changeDiskMaterial();
		Disk d1 = createDisk(env, 0.1, 0.4, 0.05, 1.0, elasticity, frictionCoefficient);
		d1.attachDisk(Math.toRadians(90), 0.05, currentDiskType);
		d1.getDiskComplex().applyImpulse(0.2, 0.0, 0.1, 0.5);

		Disk d2 = createDisk(env, 0.9, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);
		d2.attachDisk(Math.toRadians(90), 0.05, currentDiskType);
		d2.getDiskComplex().applyImpulse(-0.2, 0.0, 0.9, 0.6);

		for (final Disk d : d2.getDiskComplex().getDisks()) {
			d.setEventHandler(new CollisionEventHandler() {
				@Override
				public void collision(CollidableObject otherDisk, Point collisionPoint, double exchangedImpulse) {
					if (otherDisk instanceof Disk) {
						mergeList.add(new Disk[] { d, (Disk) otherDisk });
					}
				}
			});
		}
	}

	public static void collidingObjects2() {

		double frictionCoefficient = 0.005;
		double elasticity = defaultElasticity;

		changeDiskMaterial();

		Random rand = new Random(2);
		for (int i = 0; i < 500; i++) {
			Disk d;
			double x;
			double y;
			do {
				x = rand.nextDouble();
				y = rand.nextDouble();
				d = createDisk(env, x, y, 0.007, 1.0, elasticity, frictionCoefficient);
			} while (env.withdrawDueToCollisions(d.getDiskComplex()));
			final Disk dfinal = d;
			dfinal.setEventHandler(new CollisionEventHandler() {
				@Override
				public void collision(final CollidableObject otherDisk, Point collisionPoint, double exchangedImpulse) {
					if (otherDisk instanceof Disk) {
						if (otherDisk instanceof Disk)
							mergeList.add(new Disk[] { dfinal, (Disk) otherDisk });
					}
				}
			});
			double dx = 0.5 - x;
			double dy = 0.5 - y;
			double dist = Math.sqrt(dx * dx + dy * dy);
			double f = 0.2 / dist;
			d.getDiskComplex().applyImpulse((1.0 + rand.nextGaussian()) * dx * f, (1.0 + rand.nextGaussian()) * dy * f, x, y);
		}
	}

	public static void splitObjects() {

		double frictionCoefficient = defaultFrictionCoefficient;
		double elasticity = defaultElasticity;

		changeDiskMaterial();
		final Disk d1 = createDisk(env, 0.35, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);
		final Disk d2 = d1.attachDisk(Math.toRadians(0), 0.05, currentDiskType);
		final Disk d3 = d1.getDiskComplex().addNewDisk(0.55, 0.5, 0.05, Math.toRadians(0), currentDiskType);
		d1.getDiskComplex().addNewDisk(0.65, 0.5, 0.05, Math.toRadians(0), currentDiskType);

		d1.getDiskComplex().applyImpulse(0, 0.2, 0.35, 0.5);
		d1.getDiskComplex().applyImpulse(0, -0.2, 0.65, 0.5);

		changeDiskMaterial();
		Disk splitDisk = createDisk(env, 0.1, 0.1, 0.02, 0.5, elasticity, frictionCoefficient);
		splitDisk.setEventHandler(new CollisionEventHandler() {
			@Override
			public void collision(CollidableObject otherDisk, Point collisionPoint, double exchangedImpulse) {
				if (d1.getDiskComplex().getDisks().size() == 4)
					d1.getDiskComplex().split(d2, d3);
			}
		});
		splitDisk.applyImpulse(0.05, 0.05);
		splitDisk.getDiskComplex().getDisks();
	}

	public static void managerToy() {

		double frictionCoefficient = 0.0;
		double elasticity = 1.0;

		changeDiskMaterial();
		Disk d1 = createDisk(env, 0.1, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);
		d1.applyImpulse(0.4, 0.0);
		createDisk(env, 0.4, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);
		createDisk(env, 0.5, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);
		createDisk(env, 0.6, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);
		createDisk(env, 0.7, 0.5, 0.05, 1.0, elasticity, frictionCoefficient);

		changeDiskMaterial();
		Disk d2 = createDisk(env, 0.5, 0.9, 0.01, 0.0001, elasticity, frictionCoefficient);
		d2.applyImpulse(0.0, -0.00001);
	}

	public static void gasObjects() {

		double frictionCoefficient = 0.0;
		double elasticity = 1.0;

		changeDiskMaterial();
		Random rand = new Random(1);
		Disk d;
		for (int i = 0; i < 100; i++) {
			do {
				double x = rand.nextDouble() * 0.9 + 0.05;
				double y = rand.nextDouble() * 0.45 + 0.525;
				double r = 0.007;
				double f = 0.2;
				d = createDisk(env, x, y, r, 1.0, elasticity, frictionCoefficient);
				d.applyImpulse(rand.nextGaussian() * f, rand.nextGaussian() * f);
				createDisk(env, x + 2 * r, y, r, 1.0, elasticity, frictionCoefficient);
			} while (env.withdrawDueToCollisions(d.getDiskComplex()));
		}

		changeDiskMaterial();
		for (int i = 0; i < 50; i++) {
			do {
				double x = rand.nextDouble() * 0.9 + 0.05;
				double y = rand.nextDouble() * 0.45 + 0.025;
				double f = 1;
				d = createDisk(env, x, y, 0.015, 3.0, elasticity, frictionCoefficient);
				d.applyImpulse(rand.nextGaussian() * f, rand.nextGaussian() * f);
			} while (env.withdrawDueToCollisions(d.getDiskComplex()));
		}
	}

	private static Color getRandomSaturatedColor(Random random) {
		float c1 = random.nextFloat();
		float c2 = random.nextFloat();
		switch (random.nextInt(3)) {
		case 0:
			return new Color(1.0f, c1, c2);
		case 1:
			return new Color(c1, 1.0f, c2);
		case 2:
			return new Color(c1, c2, 1.0f);
		}
		return null;
	}
}
