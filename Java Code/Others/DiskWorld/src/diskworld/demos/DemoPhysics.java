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
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.demos.DemoLauncher.Demo;
import diskworld.environment.AgentMapping;
import diskworld.interfaces.AgentController;
import diskworld.visualization.VisualizationSettings;

/**
 * @author Jan
 *
 * TODO: convert TestPhysics to DemoPhysics
 */
public class DemoPhysics {

	public static void main(String[] args) {
		Demo[] demos = new Demo[] {
				getBounceDemo(),
		};
		DemoLauncher.runDemos("DiskWorld Physics Demonstration", demos);
	}

	private static Demo getBounceDemo() {
		return new Demo() {
			@Override
			public String getTitle() {
				return "Collision1: Elastic Bounce";
			}

			@Override
			public long getMiliSecondsPerTimeStep() {
				return 5;
			}

			@Override
			public Environment getEnvironment() {
				Environment env = new Environment(10, 10);
				DiskType type = new DiskType(DiskMaterial.METAL);

				double x1 = 1;
				double y1 = 1;
				double r1 = 1;
				Disk d1 = env.newRootDisk(x1, y1, r1, type);

				double x2 = 9;
				double y2 = 8;
				double r2 = 0.5;
				Disk d2 = env.newRootDisk(x2, y2, r2, type);

				d1.applyImpulse(4, 4);
				d2.applyImpulse(-1, -1);
				return env;
			}

			@Override
			public AgentMapping[] getAgentMappings() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public AgentController[] getControllers() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean adaptVisualisationSettings(VisualizationSettings settings) {
				return true;
			}
		};
	}

	/*private static void setElasticity(Environment env, double elasticity) {
		env.getPhysicsParameters().setDisk2DiskElasticty(elasticity);
		env.getPhysicsParameters().setDisk2WallElasticty(elasticity);
	}*/

}

/*	static JFrame frame = new JFrame();
	static Environment env;
	static List<Disk[]> mergeList = new LinkedList<Disk[]>();

	public static void main(String[] args) {
		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor();
		setElasticity(1.0);
		bounceSimple();
		test("Collision1: Elastic Bounce", 600, 5);
		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor();
		setElasticity(0.0);
		bounceSimple();
		test("Collision2: Fully Inelastic Bounce", 600, 5);
		env = new Environment(10, 10, 0.1, getWalls(true));
		setUniformFloor();
		setElasticity(1.0);
		bounceWall();
		test("Collision3: Walls", 1200, 5);
		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor();
		setElasticity(1.0);
		bounceComplex();
		test("Collision4: Complex objects", 800, 5);
		env = new Environment(10, 10, 0.1, getWalls(true));
		setUniformFloor();
		setElasticity(1.0);
		setFriction(0.0);
		bounceComplexWall();
		test("Collision5: Complex objects and walls", 800, 5);
		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setElasticity(1.0);
		setUniformFloor();
		setFriction(0.01);
		bounceComplex();
		test("Friction1: Uniform friction", 1500, 5);
		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setLocationDependentFriction(env.getFloor());
		setElasticity(1.0);
		movingObject();
		setQuadrantFloor();
		test("Friction2: Floor-dependend friction", 2000, 5);
		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor();
		setFriction(0.005);
		collidingObjects();
		test("ObjectChanges1: Merging", 1000, 5);
		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setFriction(0.005);
		setUniformFloor();
		collidingObjects2();
		test("ObjectChanges2: Merging many", 800, 15);
		env = new Environment(10, 10, 0.1, new LinkedList<Wall>());
		setUniformFloor();
		splitObjects();
		test("ObjectChanges3: Splitting", 1000, 5);
		env = new Environment(10, 10, 0.1, getWalls(false));
		setUniformFloor();
		setFriction(0.0);
		setElasticity(1.0);
		managerToy();
		test("Manager toy", 1200, 15);
		env = new Environment(10, 10, 0.1, getWalls(false));
		setUniformFloor();
		setFriction(0.0);
		setElasticity(1.0);
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

	public static void setUniformFloor() {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setType(i, j, 0);
	}

	public static void setCheckerBoardFloor() {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setType(i, j, ((i + j) % 2));
	}

	public static void setQuadrantFloor() {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setType(i, j, (i >= 5) && (j <= 4) ? 3 : 0);
	}

	private static Disk createDisk(DiskComplexEnsemble dce, DiskComplex dc, double x, double y, double radius, double mass, int color) {
		DiskType diskType = new DiskType(new DiskMaterial(mass / (Math.PI * radius * radius), color));
		return dce.addNewDisk(dc, x, y, radius, 0, diskType);
	}

	public static void bounceSimple() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		DiskComplex dc1 = dce.createNewDiskComplex();
		double height = 0.46;
		createDisk(dce, dc1, 0.2, height, 0.05, 1.0, 6);
		DiskComplex dc2 = dce.createNewDiskComplex();
		createDisk(dce, dc2, 0.8, 0.5, 0.05, 1.0, 7);
		dc1.applyImpulse(0.2, 0.0, 0.2, height);
		dc2.applyImpulse(-0.2, 0.0, 0.8, 0.5);
	}

	public static void bounceWall() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		DiskComplex dc1 = dce.createNewDiskComplex();
		double height = 0.46;
		createDisk(dce, dc1, 0.2, height, 0.05, 1.0, 6);
		dc1.applyImpulse(0.5, 0.0, 0.2, height);
	}

	public static void bounceComplex() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		DiskComplex dc1 = dce.createNewDiskComplex();
		double height = 0.42;
		createDisk(dce, dc1, 0.15, height, 0.05, 1.0, 6);
		createDisk(dce, dc1, 0.25, height, 0.05, 1.0, 6);
		DiskComplex dc2 = dce.createNewDiskComplex();
		createDisk(dce, dc2, 0.8, 0.45, 0.05, 1.0, 7);
		createDisk(dce, dc2, 0.8, 0.55, 0.05, 1.0, 7);
		dc1.applyImpulse(0.2, 0.0, 0.2, height);
		dc2.applyImpulse(-0.2, 0.0, 0.8, 0.5);
	}

	public static void bounceComplexWall() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		DiskComplex dc1 = dce.createNewDiskComplex();
		double height = 0.4;
		createDisk(dce, dc1, 0.15, height, 0.05, 1.0, 6);
		createDisk(dce, dc1, 0.25, height, 0.05, 1.0, 6);
		createDisk(dce, dc1, 0.35, height, 0.05, 1.0, 6);
		createDisk(dce, dc1, 0.35, height - 0.1, 0.05, 1.0, 6);
		dc1.applyImpulse(0.2, 0.0, 0.2, height);
	}

	public static void movingObject() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		DiskComplex dc1 = dce.createNewDiskComplex();
		double offs = 0.07;
		createDisk(dce, dc1, 0.2, 0.5 + offs, 0.05, 1.0, 6);
		createDisk(dce, dc1, 0.1, 0.5 + offs, 0.05, 1.0, 7);
		createDisk(dce, dc1, 0.2, 0.4 + offs, 0.05, 1.0, 7);
		dc1.applyImpulse(0.1, 0.0, 0.2, 0.5 + offs);
		dc1.applyImpulse(0.1, 0.0, 0.1, 0.5 + offs);
		dc1.applyImpulse(0.1, 0.0, 0.2, 0.4 + offs);

		DiskComplex dc2 = dce.createNewDiskComplex();
		createDisk(dce, dc2, 0.2, 0.1, 0.05, 2.0, 6);
		dc2.applyImpulse(0.2, 0.0, 0.2, 0.1);
		DiskComplex dc3 = dce.createNewDiskComplex();
		createDisk(dce, dc3, 0.2, 0.2, 0.025, 0.5, 6);
		dc3.applyImpulse(0.05, 0.0, 0.2, 0.2);
	}

	public static void collidingObjects() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		final DiskComplex dc1 = dce.createNewDiskComplex();
		createDisk(dce, dc1, 0.1, 0.4, 0.05, 1.0, 6);
		createDisk(dce, dc1, 0.1, 0.5, 0.05, 1.0, 6);
		dc1.applyImpulse(0.2, 0.0, 0.1, 0.5);
		DiskComplex dc2 = dce.createNewDiskComplex();
		createDisk(dce, dc2, 0.9, 0.5, 0.05, 1.0, 7);
		createDisk(dce, dc2, 0.9, 0.6, 0.05, 1.0, 7);
		dc2.applyImpulse(-0.2, 0.0, 0.9, 0.6);

		for (final Disk d : dc1.getDisks()) {
			d.setEventHandler(new CollisionEventHandler() {
				@Override
				public void collision(CollidableObject otherDisk, Point collisionPoint) {
					if (otherDisk instanceof Disk) {
						mergeList.add(new Disk[] { d, (Disk) otherDisk });
					}
				}
			});
		}
	}

	public static void collidingObjects2() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		//DiskComplex dc1 = dce.createNewDiskComplex();
		//dce.addNewDisk(dc1, 0.1, 0.5, 0, 0.05, 1.0, 6);
		//dce.addNewDisk(dc1, 0.1, 0.4, 0, 0.05, 1.0, 6);
		//dc1.applyImpulse(0.1, 0.0, 0.2, 0.5);
		Random rand = new Random(2);
		for (int i = 0; i < 500; i++) {
			final DiskComplex dc = dce.createNewDiskComplex();
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			final Disk d = createDisk(dce, dc, x, y, 0.007, 1, 7);
			d.setEventHandler(new CollisionEventHandler() {
				@Override
				public void collision(final CollidableObject otherDisk, Point collisionPoint) {
					if (otherDisk instanceof Disk) {
						if (otherDisk instanceof Disk)
							mergeList.add(new Disk[] { d, (Disk) otherDisk });
					}
				}
			});
			double dx = 0.5 - x;
			double dy = 0.5 - y;
			double dist = Math.sqrt(dx * dx + dy * dy);
			double f = 0.2 / dist;
			dc.applyImpulse((1 + rand.nextGaussian()) * dx * f, (1 + rand.nextGaussian()) * dy * f, x, y);
		}
	}

	public static void managerToy() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		final DiskComplex dc1 = dce.createNewDiskComplex();
		createDisk(dce, dc1, 0.1, 0.5, 0.05, 1.0, 6);
		dc1.applyImpulse(0.4, 0.0, 0.1, 0.5);
		DiskComplex dc2 = dce.createNewDiskComplex();
		createDisk(dce, dc2, 0.4, 0.5, 0.05, 1.0, 1);
		DiskComplex dc3 = dce.createNewDiskComplex();
		createDisk(dce, dc3, 0.5, 0.5, 0.05, 1.0, 1);
		DiskComplex dc4 = dce.createNewDiskComplex();
		createDisk(dce, dc4, 0.6, 0.5, 0.05, 1.0, 1);
		DiskComplex dc5 = dce.createNewDiskComplex();
		createDisk(dce, dc5, 0.7, 0.5, 0.05, 1.0, 1);
		DiskComplex dc6 = dce.createNewDiskComplex();
		createDisk(dce, dc6, 0.5, 0.9, 0.01, 0.0001, 8);
		dc6.applyImpulse(0.0, -0.00001, 0.5, 0.9);
	}

	public static void gasObjects() {
		DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		Random rand = new Random(1);
		for (int i = 0; i < 100; i++) {
			final DiskComplex dc = dce.createNewDiskComplex();
			double x = rand.nextDouble() * 0.9 + 0.05;
			double y = rand.nextDouble() * 0.45 + 0.525;
			double r = 0.007;
			createDisk(dce, dc, x, y, r, 1, 7);
			createDisk(dce, dc, x + 2 * r, y, r, 1, 7);
			double f = 0.2;
			dc.applyImpulse(rand.nextGaussian() * f, rand.nextGaussian() * f, x, y);
		}
		for (int i = 0; i < 50; i++) {
			final DiskComplex dc = dce.createNewDiskComplex();
			double x = rand.nextDouble() * 0.9 + 0.05;
			double y = rand.nextDouble() * 0.45 + 0.025;
			createDisk(dce, dc, x, y, 0.015, 3, 6);
			double f = 1;
			dc.applyImpulse(rand.nextGaussian() * f, rand.nextGaussian() * f, x, y);
		}
		for (int i = 0; i < 100; i++) {
			env.doTimeStep(0.001);
		}
	}

	public static void splitObjects() {
		final DiskComplexEnsemble dce = env.getDiskComplexesEnsemble();
		final DiskComplex dc1 = dce.createNewDiskComplex();
		createDisk(dce, dc1, 0.35, 0.5, 0.05, 1.0, 6);
		final Disk d2 = createDisk(dce, dc1, 0.45, 0.5, 0.05, 1.0, 6);
		final Disk d3 = createDisk(dce, dc1, 0.55, 0.5, 0.05, 1.0, 7);
		createDisk(dce, dc1, 0.65, 0.5, 0.05, 1.0, 7);
		dc1.applyImpulse(0, 0.2, 0.35, 0.5);
		dc1.applyImpulse(0, -0.2, 0.65, 0.5);
		final DiskComplex dc2 = dce.createNewDiskComplex();
		createDisk(dce, dc2, 0.1, 0.1, 0.02, 0.5, 7).setEventHandler(new CollisionEventHandler() {
			@Override
			public void collision(CollidableObject otherDisk, Point collisionPoint) {
				if (dc1.getDisks().size() == 4)
					dc1.split(d2, d3);
			}
		});
		dc2.applyImpulse(0.05, 0.05, 0.1, 0.1);
	}

	private static void setElasticity(double elasticity) {
		env.getPhysicsParameters().setDisk2DiskElasticty(elasticity);
		env.getPhysicsParameters().setDisk2WallElasticty(elasticity);
	}

	private static void setFriction(final double frictionConstant) {
		env.getPhysicsParameters().setFrictionModel(new GlobalFrictionModel(frictionConstant));
	}

	private static void setLocationDependentFriction(final Floor floor) {
		env.getPhysicsParameters().setFrictionModel(new LocalFrictionModel() {
			@Override
			protected double getFloorContactCoefficient(Disk d) {
				int floorType = floor.getType(d.getX(), d.getY());
				return floorType == 0 ? 0.001 : 0.05;
			}
		});
	}

}
 */