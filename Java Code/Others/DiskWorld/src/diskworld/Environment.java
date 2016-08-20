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
package diskworld;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import diskworld.actions.DiskAction;
import diskworld.actions.DiskModification;
import diskworld.actions.Joint;
import diskworld.collisions.CollisionTracker;
import diskworld.collisions.FullSearchCollisionDetector;
import diskworld.environment.AgentMapping;
import diskworld.environment.DefaultPhysicsParameters;
import diskworld.environment.Floor;
import diskworld.environment.FloorCellType;
import diskworld.environment.Wall;
import diskworld.grid.Grid;
import diskworld.grid.GridBasedCollisionDetector;
import diskworld.grid.GridWithDiskMap;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.CollisionDetector;
import diskworld.interfaces.DiskChangeListener;
import diskworld.interfaces.PhysicsParameters;
import diskworld.interfaces.Sensor;
import diskworld.interfaces.TimeStepListener;
import diskworld.linalg2D.Line;
import diskworld.linalg2D.Point;
//import diskworld.storygenerator.Assignment;
import diskworld.visualization.PaintableEnvironmentClone;

/**
 * Class that holds the simulation environment (DiskComplexEnsemble, Floor, Walls). 
 * 
 * @author Jan
 *
 */
public class Environment {

	private static final boolean DEFAULT_USE_GRID_BASED_COLLISION_DETECTION = true;
	private static final int DEFAULT_NUM_GRID_CELLS_X = 30;
	private static final int DEFAULT_NUM_GRID_CELLS_Y = 30;
	private static final double DEFAULT_SCALE_FACTOR = 1.0;
	private static final double DEFAULT_WALL_THICKNESS = 0.2;

	private final Floor floor;
	private final DiskComplexEnsemble diskComplexes;
	private final GridWithDiskMap diskGrid;
	private final Grid floorGrid;
	private final List<TimeStepListener> timeStepListeners;
	private final CollisionDetector collisionDetector;
	private CollisionTracker collisionTracker;
	private double time;
	private PhysicsParameters physicsParameters;

	public Environment(int floorSizex, int floorSizey, double floorScale, double gridCellSizex, double gridCellSizey, boolean gridBasedCollisionDetector) {
		floor = new Floor(floorSizex, floorSizey, floorScale);
		diskGrid = new GridWithDiskMap(floor.getMaxX(), floor.getMaxY(), gridCellSizex, gridCellSizey);
		floorGrid = new Grid(floor.getMaxX(), floor.getMaxY(), floor.getNumX(), floor.getNumY());
		physicsParameters = new DefaultPhysicsParameters(floor, floorGrid);
		diskComplexes = new DiskComplexEnsemble();
		if (gridBasedCollisionDetector) {
			collisionDetector = new GridBasedCollisionDetector(diskGrid);
		} else {
			collisionDetector = new FullSearchCollisionDetector();
		}
		collisionTracker = new CollisionTracker(physicsParameters);
		diskComplexes.addChangeListener(diskGrid);
		if (!gridBasedCollisionDetector) {
			diskComplexes.addChangeListener((FullSearchCollisionDetector) collisionDetector);
		}
		time = 0.0;
		timeStepListeners = new LinkedList<TimeStepListener>();
	}

	public Environment(int floorSizex, int floorSizey, double floorScale, Collection<Wall> wallCollection, double gridCellSizex, double gridCellSizey, boolean gridBasedCollisionDetector) {
		this(floorSizex, floorSizey, floorScale, gridCellSizex, gridCellSizey, gridBasedCollisionDetector);
		if (wallCollection != null) {
			for (Wall wall : wallCollection) {
				if (!canAddWall(wall)) {
					throw new RuntimeException("could not add wall");
				}
			}
		}
	}

	/**
	 * Tries to add a wall to the environment
	 * 
	 * @param wall
	 *            new wall to be added
	 * @return true if the wall was added, false if the wall collided with some disk
	 */
	public boolean canAddWall(Wall wall) {
		return collisionDetector.canAddWall(wall);
	}

	/**
	 * Removes a wall from the environment
	 * 
	 * @param wall
	 *            new wall to be removed
	 */
	public void removeWall(Wall wall) {
		if (!collisionDetector.removeWall(wall)) {
			throw new RuntimeException("tried to remove a non existing wall");
		}
	}

	/**
	 * Convenience Constructor, setting floorScale to 1
	 * 
	 * @param floorSizex
	 * @param floorSizey
	 * @param floorScale
	 * @param wallCollection
	 */
	public Environment(int floorSizex, int floorSizey, double floorScale, Collection<Wall> wallCollection) {
		this(floorSizex, floorSizey, floorScale, wallCollection, DEFAULT_NUM_GRID_CELLS_X, DEFAULT_NUM_GRID_CELLS_Y, DEFAULT_USE_GRID_BASED_COLLISION_DETECTION);
	}

	public Environment(int floorSizex, int floorSizey, Collection<Wall> wallCollection) {
		this(floorSizex, floorSizey, DEFAULT_SCALE_FACTOR, wallCollection, DEFAULT_NUM_GRID_CELLS_X, DEFAULT_NUM_GRID_CELLS_Y, DEFAULT_USE_GRID_BASED_COLLISION_DETECTION);
	}

	/**
	 * Convenience Constructor, setting floorScale to 1 and using and bounding walls
	 * 
	 * @param floorSizex
	 * @param floorSizey
	 */
	public Environment(int floorSizex, int floorSizey) {
		this(floorSizex, floorSizey, getBoundingWalls(floorSizex * DEFAULT_SCALE_FACTOR, floorSizey * DEFAULT_SCALE_FACTOR));
	}

	/**
	 * //@R
	 * Convenience Constructor, setting floorScale to 1 and using and bounding walls
	 * 
	 * @param floorSizex
	 * @param floorSizey
	 */
	public Environment(int floorSizex, int floorSizey, double floorScale) {
		this(floorSizex, floorSizey, floorScale, getBoundingWalls(floorSizex * floorScale, floorSizey * floorScale));
	}

	private static Collection<Wall> getBoundingWalls(double maxx, double maxy) {
		LinkedList<Wall> res = new LinkedList<Wall>();
		res.add(new Wall(new Line(new Point(0, 0), new Point(maxx, 0)), DEFAULT_WALL_THICKNESS)); // these 4 walls are the outside walls of the frame
		res.add(new Wall(new Line(new Point(maxx, 0), new Point(maxx, maxy)), DEFAULT_WALL_THICKNESS));
		res.add(new Wall(new Line(new Point(maxx, maxy), new Point(0, maxy)), DEFAULT_WALL_THICKNESS));
		res.add(new Wall(new Line(new Point(0, maxy), new Point(0, 0)), DEFAULT_WALL_THICKNESS));
		return res;
	}

	public ObjectConstructor createObjectConstructor() {
		return new ObjectConstructor(this);
	}

	/**
	 * Creates a new disk complex consisting of a new disk that acts as a joint.
	 * Note: If the disk is fixed, only the SPIN and RESIZE actions are possible,
	 * if the disk is not fixed, only the RESIZE action is possible.
	 * 
	 * @param x
	 *            x coordinate of disk center
	 * @param y
	 *            y coordinate of disk center
	 * @param radius
	 *            radius of the disk
	 * @param angle
	 *            orientation of the disk
	 * @param diskType
	 *            type of the disk
	 * @param fixed
	 *            indicates if the disk shall be marked as fixed
	 * @return the new disk
	 */
	public Joint newRootJoint(double x, double y, double radius, double angle, DiskType diskType, boolean fixed) {
		DiskComplex dc = diskComplexes.createNewDiskComplex();
		Joint joint = new Joint(dc, x, y, radius, angle, diskType);
		dc.addNewDisk(joint, null, true);
		if (fixed)
			dc.setDiskFixed(joint);
		return joint;
	}

	/**
	 * Convenience Method: Creates a new disk complex consisting of a new disk
	 * 
	 * @param x
	 *            x coordinate of disk center
	 * @param y
	 *            y coordinate of disk center
	 * @param radius
	 *            radius of the disk
	 * @param angle
	 *            orientation of the disk
	 * @param diskType
	 *            type of the disk
	 * @param fixed
	 *            if true, the disk will be marked as fixed
	 * @return the new disk
	 */
	public Disk newRootDisk(double x, double y, double radius, double angle, DiskType diskType, boolean fixed) {
		DiskComplex dc = diskComplexes.createNewDiskComplex();
		Disk disk = dc.addNewDisk(x, y, radius, angle, diskType);
		if (fixed)
			dc.setDiskFixed(disk);
		return disk;
	}

	/**
	 * Convenience Method: Creates a new disk complex consisting of a new, not-fixed disk
	 * 
	 * @param x
	 *            x coordinate of disk center
	 * @param y
	 *            y coordinate of disk center
	 * @param radius
	 *            radius of the disk
	 * @param angle
	 *            orientation of the disk
	 * @param diskType
	 *            type of the disk
	 * @return the new disk
	 */
	public Disk newRootDisk(double x, double y, double radius, double angle, DiskType diskType) {
		return newRootDisk(x, y, radius, angle, diskType, false);
	}

	/**
	 * Convenience Method: Creates a new disk complex consisting of a new non-fixed disk with orientation 0
	 * 
	 * @param x
	 *            x coordinate of disk center
	 * @param y
	 *            y coordinate of disk center
	 * @param radius
	 *            radius of the disk
	 * @param diskType
	 *            type of the disk
	 * @return the new disk
	 */
	public Disk newRootDisk(double x, double y, double radius, DiskType diskType) {
		return newRootDisk(x, y, radius, 0.0, diskType, false);
	}

	/**
	 * Convenience Method: Creates a new disk complex consisting of a new fixed disk
	 * 
	 * @param x
	 *            x coordinate of disk center
	 * @param y
	 *            y coordinate of disk center
	 * @param radius
	 *            radius of the disk
	 * @param angle
	 *            orientation of the disk
	 * @param diskType
	 *            type of the disk
	 * @return the new disk
	 */
	public Disk newFixedRoot(double x, double y, double radius, double angle, DiskType diskType) {
		return newRootDisk(x, y, radius, angle, diskType, true);
	}

	public boolean withdrawDueToCollisions(DiskComplex diskComplex) {
		if (collisionDetector.getCollisions(diskComplex).isEmpty()) {
			return false;
		} else {
			diskComplexes.removeDiskComplex(diskComplex);
			return true;
		}
	}

	public void addTimeStepListener(TimeStepListener listener) {
		timeStepListeners.add(listener);
	}

	public void removeTimeStepListener(TimeStepListener listener) {
		timeStepListeners.remove(listener);
	}

	public Floor getFloor() {
		return floor;
	}

	public DiskComplexEnsemble getDiskComplexesEnsemble() {
		return diskComplexes;
	}

	public boolean canTeleport(Disk d, double newcx, double newcy, double newOrientation) {
		double shiftx = newcx - d.getX();
		double shifty = newcy - d.getY();
		double rotation = newOrientation - d.getAngle();
		DiskComplex dc = d.getDiskComplex();
		dc.teleport(shiftx, shifty, rotation);
		if (collisionDetector.getCollisions(dc).isEmpty()) {
			return true;
		} else {
			dc.teleport(-shiftx, -shifty, -rotation);
			return false;
		}
	}

	public boolean canTeleportCenterOfMass(DiskComplex dc, double newcomx, double newcomy, double newOrientation) {
		double shiftx = newcomx - dc.getCenterx();
		double shifty = newcomy - dc.getCentery();
		double rotation = newOrientation - dc.getCoordinates().getAngle();
		dc.teleport(shiftx, shifty, rotation);
		if (collisionDetector.getCollisions(dc).isEmpty()) {
			return true;
		} else {
			dc.teleport(-shiftx, -shifty, -rotation);
			return false;
		}
	}

	public synchronized PaintableEnvironmentClone getPaintableClone() {
		return new PaintableEnvironmentClone(this);
	}

	public synchronized void doTimeStep(double dt, AgentMapping[] mappings) {
		boolean firstSlice = true;
		while (dt > 0.0) {
			// determine maximal chunk of time that the simulation can process in one go
			double partial_dt = Math.min(dt, diskComplexes.getMaxTimeStep());
			// first perform disk actions (ego motion & setting of activities), if any
			Set<Disk> disksWithEgoMotion = new HashSet<Disk>();
			collisionTracker.clear();
			if (mappings != null) {
				performActions(mappings, partial_dt, disksWithEgoMotion, collisionTracker);
			}
			// next apply friction

			// then effects of actuators
			performActuatorEffects(partial_dt, dt, firstSlice);
			firstSlice = false;
			// finally time step in the disk complexes itself
			DiskChangeListener collisionDetectorChangeListener = collisionDetector instanceof GridBasedCollisionDetector ? diskGrid : null;
			diskComplexes.doTimeStep(partial_dt, physicsParameters.getFrictionModel(), collisionDetector, collisionTracker, collisionDetectorChangeListener);

			// apply impulse transfers by collisions
			collisionTracker.exchangeImpulses(physicsParameters);
			collisionTracker.checkMerging();
			// reset ego motion accumulators
			resetEgoMotionAccus(disksWithEgoMotion);
			time += partial_dt;
			dt -= partial_dt;
		}
		// after the specified chunk of time has progressed, measure sensor values
		performSensorMeasurements();
		// and inject them into AgentMapping objects (if any)
		if (mappings != null) {
			for (int i = 0; i < mappings.length; i++) {
				mappings[i].readSensorValues();
			}
		}
		// last, not least: call time step listeners
		for (TimeStepListener listener : timeStepListeners) {
			listener.timeStepDone();
		}
	}

	private void resetEgoMotionAccus(Set<Disk> disksWithEgoMotion) {
		for (Disk d : disksWithEgoMotion) {
			d.resetEgoMotion();
		}
	}

	private void performActions(AgentMapping[] mappings, double timestep, Set<Disk> disksWithEgoMotion, CollisionTracker collisionTracker) {
		for (AgentMapping am : mappings) {
			int num = am.getNumActions();
			for (int i = 0; i < num; i++) {
				DiskAction action = am.getAction(i);
				double actionValue = am.getClippedTranslatedActionValue(i);
				Map<Disk, DiskModification> modifications = action.translateIntoDiskModifications(actionValue, timestep);
				Disk disk = action.targetDisk();
				if (modifications == null) {
					// clear result flag
					action.targetDisk().setActionResult(false);
					action.setEnergyConsumedByAction(0.0);
				} else {
					if (modifications.isEmpty()) {
						action.setEnergyConsumedByAction(0.0);
						// do not change the result flag if there were no modifications
					} else {
						// transform the modifications to respect fixed disks or to minimize (friction weighted) offset of disks
						List<DiskModification> diskModifications = new LinkedList<DiskModification>();
						double shiftResistance = disk.getDiskComplex().minimalResistanceShiftedAndRotatedDiskModifications(modifications, getPhysicsParameters().getFrictionModel(),
								!action.correctAngle(), diskModifications);
						boolean moved = disk.getDiskComplex().tryPerformDiskModifications(diskModifications, getCollisionDetector(), timestep, collisionTracker, disksWithEgoMotion);
						action.setEnergyConsumedByAction(shiftResistance * physicsParameters.getShiftResistanceEnergyConsumptionFactor());
						// set result flag
						action.targetDisk().setActionResult(moved);
					}
				}
			}
		}
	}

	/**
	 * Convencience method: time step in case there are no agents present
	 * 
	 * @param dt
	 */
	public void doTimeStep(double dt) {
		doTimeStep(dt, null);
	}

	public Set<DiskComplex> getDiskComplexes() {
		return diskComplexes.getDiskComplexes();
	}

	public PhysicsParameters getPhysicsParameters() {
		return physicsParameters;
	}

	public void setPhysicsParameters(PhysicsParameters physicsParameters) {
		this.physicsParameters = physicsParameters;
		this.collisionTracker = new CollisionTracker(physicsParameters);
	}

	public Collection<Wall> getWalls() {
		return collisionDetector.getWalls();
	}

	/*
	 * public void clear() { diskComplexes.clear(); floor.clear(); }
	 */

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getMaxX() {
		return floor.getMaxX();
	}

	public double getMaxY() {
		return floor.getMaxY();
	}

	public Grid getFloorGrid() {
		return floorGrid;
	}

	public GridWithDiskMap getDiskGrid() {
		return diskGrid;
	}

	public FloorCellType getFloorCellTypeAtPosition(double x, double y) {
		int indexx = floorGrid.getCellxIndex(x);
		int indexy = floorGrid.getCellyIndex(y);
		return floor.getType(indexx, indexy);
	}

	private void performActuatorEffects(double partial_dt, double dt, boolean firstSlice) {
		for (Disk d : diskComplexes.getActuatorDisks()) {
			Actuator actuator = d.getDiskType().getActuator();
			double energyConsumed = actuator.evaluateEffect(d, this, d.getActivity(), partial_dt, dt, firstSlice);
			d.setEnergyConsumedByActuator(energyConsumed);
		}
	}

	private void performSensorMeasurements() {
		for (Disk d : diskComplexes.getSensorDisks()) {
			Sensor[] sensors = d.getDiskType().getSensors();
			for (int i = 0; i < sensors.length; i++) {
				sensors[i].doMeasurement(d, d.getSensorMeasurements()[i]);
			}
		}
	}

	public List<Point> getCollisionPoints(boolean blocked) {
		return collisionTracker.getCollisionPoints(blocked);
	}

	public CollisionDetector getCollisionDetector() {
		return collisionDetector;
	}

}
