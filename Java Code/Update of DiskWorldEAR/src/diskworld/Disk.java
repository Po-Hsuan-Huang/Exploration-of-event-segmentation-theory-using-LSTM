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

import diskworld.actions.Joint;
import diskworld.collisions.Collision;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.CollisionEventHandler;
import diskworld.interfaces.Sensor;
import diskworld.linalg2D.AngleUtils;
import diskworld.linalg2D.Point;
import diskworld.linalg2D.RelativePosition;
import diskworld.skeleton.Vertex;

/**
 * The basic unit of a DiskWorld simulation. 
 * @author Jan
 */
public class Disk implements CollidableObject {
	// TODO: Extract disk elements for sensors and actuators into subclasses

	//code to produce deterministic hash values 
	private static int instanceCount = 0;
	private final int instanceID = ++instanceCount;

	@Override
	public int hashCode() {
		// Note: Disk can be compared wit walls (both are CollidableObjects). We resolve this by iving disks a positive and walls a negative hasCode   
		return instanceID;
	}

	/**
	 * Current position relative to DiskComplex
	 */
	private final RelativePosition position;

	/**
	 * Current activation in case the disk is attached to an actuator, otherwise null
	 */
	private double[] activity;

	/**
	 * Latest measurements of the sensors attached to this disk (null if no sensors present)
	 */
	private final double[][] measurement;

	/**
	 * Current radius, may change during simulation (due to growing, shrinking)!
	 */
	private double radius;

	/**
	 * Current mass, may change during simulation (due to growing, shrinking)!
	 */
	private double mass;

	/**
	 * Current z position (integer), disks have always thickness 1 and are placed at integer height
	 */
	private int zLevel;

	/**
	 * Static properties (that are not supposed to change during the lifetime of a simulation)
	 */
	private final DiskType diskType;

	/**
	 * Link to the DiskComplex the disk belongs to
	 */
	private DiskComplex belongsTo;

	/**
	 * Handler called when collision of this disk occurs, or null
	 */
	private CollisionEventHandler eventHandler;

	/**
	 * corresponding vertex in the skeleton
	 */
	private Vertex skeletonVertex;

	/**
	 * Optional name for disk
	 */
	private String name;

	/**
	 * Holds the result of the last disk action
	 */
	private boolean actionSuccess;

	/**
	 * Holds the energy consumption of last actuator effect
	 */
	private double consumedEnergyByActuator;

	/**
	 * Accumulator for ego motions of the disk
	 */
	private double egomotionx, egomotiony;

	/**
	 * Non-public constructor
	 * 
	 * @param diskComplex
	 *            the DiskComplex to which the new Disk will be added to
	 * @param x
	 *            current absolute x position
	 * @param y
	 *            current absolute y position
	 * @param radius
	 *            radius of the disk
	 * @param angle
	 *            rotation angle of the disk
	 * @param diskType
	 *            type of the disk
	 */

	protected Disk(DiskComplex diskComplex, double x, double y, double radius, double angle, DiskType diskType) {
		belongsTo = diskComplex;
		position = new RelativePosition(belongsTo.getCoordinates());
		position.setAbsPosition(x, y);
		position.setAbsAngle(angle);
		this.diskType = diskType;
		eventHandler = null;
		setRadius(radius); // computes the mass 
		activity = diskType.hasActuator() ? createActivityArray(diskType.getActuator()) : null;
		measurement = createMeasurmentArays(diskType.getSensors());
		name = null;
		actionSuccess = true;
		egomotionx = 0.0;
		egomotiony = 0.0;
		consumedEnergyByActuator = 0.0;
		zLevel = 0;
	}

	private double[] createActivityArray(Actuator actuator) {
		double res[] = new double[actuator.getDim()];
		for (int i = 0; i < res.length; i++) {
			res[i] = 0.0;
		}
		return res;
	}

	/**
	 * Provide an EventHandler (disables an already existing EventHandler!)
	 * 
	 * @param handler
	 *            new CollisionEventHandler
	 */
	public void setEventHandler(CollisionEventHandler handler) {
		eventHandler = handler;
	}

	/**
	 * Get the currently installed EventHandler
	 * 
	 * @return current EventHandler or null
	 */
	public CollisionEventHandler getEventHandler() {
		return eventHandler;
	}

	/**
	 * Calls the handler for collision events
	 * 
	 * @param obj2
	 * @param px
	 * @param py
	 * @param exchangedImpulse
	 */
	public void callEventHandler(CollidableObject obj2, double px, double py, double exchangedImpulse) {
		if (eventHandler != null)
			eventHandler.collision(obj2, new Point(px, py), exchangedImpulse);
	}

	/**
	 * Convenience method: Creates a new disk neighbouring to this and adds it to the same diskComplex
	 * 
	 * @param relativeAngle
	 *            the angle relative to the this disk's orientation at which the new disk is placed
	 * @param radius
	 *            the radius of the new disk
	 * @param orientationOffset
	 *            the offset for the orientation, if 0 the new disk is oriented in placement direction (radially away from this disk)
	 * @param diskType
	 *            the DiskType of the new disk
	 * @return the newly created disk
	 */
	public Disk attachDisk(double relativeAngle, double radius, double orientationOffset, DiskType diskType) {
		double direction = getAngle() + relativeAngle;
		double sr = getRadius() + radius;
		return belongsTo.addNewDisk(getX() + sr * Math.cos(direction), getY() + sr * Math.sin(direction), radius, direction + orientationOffset, diskType, this);
	}

	/**
	 * Convenience method: Creates a new disk neighbouring to this and adds it to the same diskComplex, assuming 0 orientation offset
	 * 
	 * @param relativeAngle
	 *            the angle relative to the this disk's orientation at which the new disk is placed
	 * @param radius
	 *            the radius of the new disk
	 * @param diskType
	 *            the DiskType of the new disk
	 * @return the newly created disk
	 */
	public Disk attachDisk(double relativeAngle, double radius, DiskType diskType) {
		return attachDisk(relativeAngle, radius, 0.0, diskType);
	}

	/**
	 * Creates a new disk that acts as a joint
	 * 
	 * @param relativeAngle
	 *            the angle relative to the this disk's orientation at which the new disk is placed
	 * @param radius
	 *            the radius of the new disk
	 * @param orientationOffset
	 *            the offset for the orientation, if 0 the new disk is oriented in placement direction (radially away from this disk)
	 * @param diskType
	 *            the DiskType of the new disk
	 * @return the newly created disk
	 */
	public Joint attachJoint(double relativeAngle, double radius, double orientationOffset, DiskType diskType) {
		double direction = getAngle() + relativeAngle;
		double sr = getRadius() + radius;
		Joint joint = new Joint(this, getX() + sr * Math.cos(direction), getY() + sr * Math.sin(direction), radius, direction + orientationOffset, diskType);
		belongsTo.addNewDisk(joint, this, true);
		return joint;
	}

	/**
	 * Current absolute x position
	 * 
	 * @return current absolute x
	 */
	public double getX() {
		return position.getAbsX();
	}

	/**
	 * Current absolute y position
	 * 
	 * @return current absolute y
	 */
	public double getY() {
		return position.getAbsY();
	}

	/**
	 * Current z level (an integer)
	 * 
	 * @return current z level value
	 */
	public int getZLevel() {
		return zLevel;
	}

	/**
	 * The type of this disk (may be shared with other Disk objects)
	 * 
	 * @return static properties of this disk
	 */
	public DiskType getDiskType() {
		return diskType;
	}

	/**
	 * Current absolute angle, counting full rotations. Can be negative or bigger than 2*Pi.
	 * Sometimes it may be important to get continuous values (that change only little from one time step to the next), in that case
	 * this method is preferable.
	 * Also when the angle is needed to compute trigonometric functions (like Math.sin() or Math.cos()),
	 * this method should be used.
	 * The value increases mathematically positively (counter-clockwise), multiples of 2*Pi correspond to the positive x direction (east).
	 * 
	 * @return current absolute rotation angle + 2*Math.PI*#fullRotations
	 */
	public double getAngle() {
		return position.getAbsAngle();
	}

	/**
	 * Current absolute angle, not counting full rotations. Note: this has a discontinuity at 2*Pi, beyond which the value
	 * drops suddenly to 0.
	 * 
	 * @return current absolute rotation angle as a value in [0,2*Pi], (east = 0, mathematically positive)
	 */
	public double getOrientation() {
		return AngleUtils.mod2PIpositive(position.getAbsAngle());
	}

	/**
	 * Current current radius
	 * 
	 * @return current radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Current current mass
	 * 
	 * @return current mass
	 */
	public double getMass() {
		return mass;
	}

	/**
	 * Current "activation" of the Disk, only relevant if an Actuator is attached to this Disk's DiskType
	 * 
	 * @return current activation value
	 */
	public double[] getActivity() {
		return activity;
	}

	/**
	 * Set "activation" of the Disk, only relevant if an Actuator is attached to this Disk's DiskType
	 * 
	 * @param activity
	 *            new activity value
	 */
	public void setActivity(int index, double activity) {
		this.activity[index] = activity;
	}

	/**
	 * Get the array sensor values
	 * 
	 * @return double 2D array that the sensor measurements are stored in
	 */
	public double[][] getSensorMeasurements() {
		return measurement;
	}

	/**
	 * Get the vertex in the skeleton that corresponds to this disk
	 * 
	 * @return vertex of skeleton of the disk complex
	 */
	public Vertex getSkeletonVertex() {
		return skeletonVertex;
	}

	/**
	 * Set reference to the vertex in the skeleton that corresponds to this disk
	 * 
	 * @param vertex
	 *            vertex of skeleton of the disk complex that corresponds to this disk
	 */
	void setSkeletonVertex(Vertex vertex) {
		this.skeletonVertex = vertex;
	}

	void shiftPosition(double deltax, double deltay) {
		position.setAbsPosition(position.getAbsX() + deltax, position.getAbsY() + deltay);
	}

	double getDistanceToOrigin() {
		return position.getDistanceToOrigin();
	}

	void changeOwner(DiskComplex newOwner) {
		belongsTo = newOwner;
		position.changeCoordinates(newOwner.getCoordinates());
	}

	/**
	 * Does the disk overlap with another disk
	 * 
	 * @param other
	 *            the other disk
	 * @return true if the two overlap
	 */
	public boolean overlaps(Disk other) {
		return overlapsDisk(other.getX(), other.getY(), other.getRadius());
	}

	private boolean overlapsDisk(double x2, double y2, double r2) {
		return Collision.intersecting(getX(), getY(), radius, x2, y2, r2);
	}

	/**
	 * Does another disk belong to the same DiskComplex or another?
	 * 
	 * @param other
	 *            the other Disk
	 * @return true if other disk belongs to the same as this
	 */
	public boolean belongsToSame(Disk other) {
		return belongsTo == other.belongsTo;
	}

	/**
	 * Does this disk belong (currently) to the given DiskComplex?
	 * 
	 * @param diskComplex
	 *            the diskComplex to test
	 * @return true if this belongs to the given DiskComplex
	 */
	public boolean belongsTo(DiskComplex diskComplex) {
		return belongsTo == diskComplex;
	}

	/**
	 * The DiskComplex to which this disk belongs to (currently)
	 * 
	 * @return current DiskComplex
	 */
	public DiskComplex getDiskComplex() {
		return belongsTo;
	}

	//  Modifications:

	/**
	 * Set absolute position. This method is not public, it should never be called directly! Use DiskModification to change a disks position!
	 * 
	 * @param absPosx
	 *            new absolute x coordinate
	 * @param absPosy
	 *            new absolute y coordinate
	 */
	void setPosition(double absPosx, double absPosy) {
		position.setAbsPosition(absPosx, absPosy);
	}

	/**
	 * Set absolute angle. This method is not public, it should never be called directly! Use DiskModification to change a disks angle!
	 * 
	 * @param absAngle
	 *            new absolute angle coordinate
	 */
	public void setAngle(double absAngle) {
		position.setAbsAngle(absAngle);
	}

	public void setZLevel(int newZLevel) {
		zLevel = newZLevel;
	}

	/**
	 * Set radius. Beware:This method changes the mass! This method is not public, it should never be called directly! Use DiskModification to change a disks radius!
	 * 
	 * @param radius
	 *            new value for radius (must be positive)
	 */
	void setRadius(double radius) {
		if (radius <= 0.0)
			throw new IllegalArgumentException("Radius must be positive");
		this.radius = radius;
		mass = Math.PI * radius * radius * diskType.getMaterial().getDensity();
	}

	private double[][] createMeasurmentArays(Sensor[] sensors) {
		if (sensors == null)
			return null;
		double[][] res = new double[sensors.length][];
		for (int i = 0; i < sensors.length; i++) {
			res[i] = new double[sensors[i].getDimension()];
		}
		return res;
	}

	/**
	 * Apply an impulse at the disk center
	 * 
	 * @param impulseX
	 *            x component of impulse vector
	 * @param impulseY
	 *            y component of impulse vector
	 */
	public void applyImpulse(double impulseX, double impulseY) {
		System.out.println("X: " + impulseX + " Y: " + impulseY);
		belongsTo.applyImpulse(impulseX, impulseY, getX(), getY());
	}

	/**
	 * The optional name for the disk
	 * 
	 * @return name of the disk or null, if not set
	 */
	public String getName() {
		return name;
	}

	/**
	 * Reset the egomotion accumulator
	 */
	public void resetEgoMotion() {
		egomotionx = 0.0;
		egomotiony = 0.0;
	}

	/**
	 * Increment the egomotion accumulators
	 */
	public void increaseEgoMotion(double deltax, double deltay) {
		egomotionx += deltax;
		egomotiony += deltay;
	}

	/**
	 * Returns the x component of accumulated ego motions
	 * 
	 * @return sum of ego motion deltax
	 */
	public double getEgoMotionx() {
		return egomotionx;
	}

	/**
	 * Returns the y component of accumulated ego motions
	 * 
	 * @return sum of ego motion deltay
	 */
	public double getEgoMotiony() {
		return egomotiony;
	}

	/**
	 * Set the name of the disk. This is optional (no errors should occur if disks stay unnamed)
	 * 
	 * @param name
	 *            any string, but convention is to encode the disk position somehow
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Indicates if this disk has been declared as fixed disk. Note that even when this method returns false,
	 * it can well be that the disk is "practically" fixed (it is not declared fixed, but is attached to
	 * a disk declared fixed and there is no joint in the connection path).
	 * 
	 * @return true if this disk is has been marked as fixed
	 */
	public boolean isMarkedFixed() {
		return belongsTo.getFixedDisks().contains(this);
	}

	public void setActionResult(boolean success) {
		actionSuccess = success;
	}

	public boolean getActionResult() {
		return actionSuccess;
	}

	/**
	 * Get the energy consumed by an actuator action
	 * 
	 * @return latest stored energy consumption
	 */
	public double getEnergyConsumedByActuator() {
		return consumedEnergyByActuator;
	}

	/**
	 * Set the energy consumed by an actuator action
	 * 
	 * @param energyConsumed
	 *            set energy consumption by actuator in current time step
	 */
	public void setEnergyConsumedByActuator(double energyConsumed) {
		consumedEnergyByActuator = energyConsumed;
	}
	
}
