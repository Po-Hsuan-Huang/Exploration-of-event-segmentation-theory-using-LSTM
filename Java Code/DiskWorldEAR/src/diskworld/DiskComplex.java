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

import diskworld.actions.DiskModification;
import diskworld.collisions.Collision;
import diskworld.collisions.CollisionTracker;
import diskworld.environment.Epsilons;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.CollisionDetector;
import diskworld.interfaces.DiskChangeListener;
import diskworld.interfaces.FrictionModel;
import diskworld.linalg2D.CoordinateSystem;
import diskworld.linalg2D.OptimalShiftAndSmallRotation;
import diskworld.skeleton.Island;
import diskworld.skeleton.Skeleton;
import diskworld.skeleton.TransientEdge;
import diskworld.skeleton.Vertex;

// Holds a set of disks. The center of mass of the relative positions must be 0,0 at all times.

/**
 * Holds a set of disks. There is a CoordinateSystem associated with a DiskComplex, the origin of which is given by the center of mass of all disks.
 */
public class DiskComplex {

	//code to produce deterministic hash values 
	private static int instanceCount = 0;
	private final int instanceID = ++instanceCount;

	@Override
	public int hashCode() {
		return instanceID;
	}

	private final List<Disk> disks;
	private final DiskComplexEnsemble diskcomplexEnsemble;
	private final CoordinateSystem coordinates;
	private final Skeleton skeleton;
	private final Set<Disk> fixedDisks;

	private double mass, massMomentum;
	private double momentumx, momentumy, angularMomentum;
	private double speedx, speedy, angularSpeed;
	private double minDiskRadius;
	private double minDiskRadiusOverDistanceToOrigin;

	private DiskComplex(DiskComplexEnsemble diskComplexEnsemble, Skeleton skeleton) {
		this.disks = new LinkedList<Disk>();
		this.coordinates = new CoordinateSystem();
		this.diskcomplexEnsemble = diskComplexEnsemble;
		this.skeleton = skeleton;
		this.fixedDisks = new HashSet<Disk>();
		mass = 0.0;
		massMomentum = 0.0;
		momentumx = 0.0;
		momentumy = 0.0;
		angularMomentum = 0.0;
		speedx = 0.0;
		speedy = 0.0;
		angularSpeed = 0.0;
		minDiskRadius = 0.0;
		minDiskRadiusOverDistanceToOrigin = 0.0;
	}

	public DiskComplex(DiskComplexEnsemble diskcomplexEnsemble) {
		this(diskcomplexEnsemble, new Skeleton());
	}

	/**
	 * Convenience method: Creates a disk and adds it at the defined place, finding a suited neighbour
	 */
	public Disk addNewDisk(double x, double y, double radius, double angle, DiskType diskType) {
		Disk disk = new Disk(this, x, y, radius, angle, diskType);
		addNewDisk(disk, null, false);
		return disk;
	}

	/**
	 * Called by Disk.attachDisk()
	 */
	Disk addNewDisk(double x, double y, double radius, double angle, DiskType diskType, Disk connectedTo) {
		Disk disk = new Disk(this, x, y, radius, angle, diskType);
		addNewDisk(disk, connectedTo, false);
		return disk;
	}

	void addNewDisk(Disk newDisk, Disk connectedTo, boolean isControllable) {
		if (connectedTo == null)
			connectedTo = findClosest(newDisk);
		if (disks.isEmpty()) {
			// first disk added, register DiskComplex in DiskComplexEnsemble
			registerInDiskComplexEnsemble();
		}
		disks.add(newDisk);
		Vertex vertex;
		if (connectedTo == null) {
			vertex = skeleton.createFirstVertex(newDisk, isControllable);
		} else {
			vertex = skeleton.createVertex(newDisk, connectedTo, isControllable);
		}
		newDisk.setSkeletonVertex(vertex);
		recalculateAfterStructuralChange();
		diskcomplexEnsemble.diskWasAdded(newDisk);
	}

	private void registerInDiskComplexEnsemble() {
		diskcomplexEnsemble.register(this);
	}

	public double getSpeedx() {
		return speedx;
	}

	public double getSpeedy() {
		return speedy;
	}

	public double getAngularMomentum() {
		return angularMomentum;
	}

	public double getAngularSpeed() {
		return angularSpeed;
	}

	public double getMass() {
		return mass;
	}

	public double getMassMomentum() {
		return massMomentum;
	}

	public double getCenterx() {
		return coordinates.getOriginX();
	}

	public double getCentery() {
		return coordinates.getOriginY();
	}

	public List<Disk> getDisks() {
		return disks;
	}

	public CoordinateSystem getCoordinates() {
		return coordinates;
	}

	/**
	 * x component of speed of a point that moves and rotates with this DiskComplex
	 * 
	 * @param x
	 *            absolute x coordinate of point
	 * @param y
	 *            absolute y coordinate of point
	 * @return x component of speed of point
	 */
	public double getSpeedx(double x, double y) {
		return getSpeedx() - (y - getCentery()) * getAngularSpeed();
	}

	/**
	 * y component of speed of a point that moves and rotates with this DiskComplex
	 * 
	 * @param x
	 *            absolute x coordinate of point
	 * @param y
	 *            absolute y coordinate of point
	 * @return y component of speed of point
	 */
	public double getSpeedy(double x, double y) {
		return getSpeedy() + (x - getCenterx()) * getAngularSpeed();
	}

	/**
	 * x component of speed of a point that moves and rotates with this DiskComplex
	 * 
	 * @param ry
	 *            relative y coordinate of point = y - getCentery()
	 * @return x component of speed of point
	 */
	public double getSpeedxRelative(double ry) {
		return getSpeedx() - ry * getAngularSpeed();
	}

	/**
	 * y component of speed of a point that moves and rotates with this DiskComplex
	 * 
	 * @param rx
	 *            relative y coordinate of point = x - getCenterx() 
	 * @return y component of speed of point
	 */
	public double getSpeedyRelative(double rx) {
		return getSpeedy() + rx * getAngularSpeed();
	}

	
	/**
	 * x component of speed of disk that moves and rotates with this DiskComplex
	 * 
	 * @param disk
	 *            disk
	 * @return x component of speed of point
	 */
	public double getSpeedx(Disk disk) {
		return getSpeedx(disk.getX(), disk.getY());
	}

	/**
	 * y component of speed of disk that moves and rotates with this DiskComplex
	 * 
	 * @param disk
	 *            disk
	 * @return y component of speed of point
	 */
	public double getSpeedy(Disk disk) {
		return getSpeedy(disk.getX(), disk.getY());
	}

	/**
	 * Perform a time step
	 * 
	 * @param dt
	 *            the delta that time shall advance
	 * @param frictionModel
	 *            Model for Friction coefficients
	 * @param collisionTracker
	 * @param collisionDetector
	 * @param changeListeners
	 */
	public void doTimeStep(double dt,
			FrictionModel frictionModel,
			CollisionDetector collisionDetector,
			CollisionTracker collisionTracker,
			DiskChangeListener collisionDetectorChangeListener,
			Collection<DiskChangeListener> changeListeners) {
		// TODO remove friction from here
		if (frictionModel != null) {
			if (isMoving()) {
				applyFriction(dt, frictionModel);
			}
		}
		if (isMoving()) {
			//System.out.print(coordinates.getOriginX()+"+"+getSpeedx()+"*"+dt+" = ");
			//System.out.print(coordinates.getAngle()+"+"+getAngularSpeed()+"*"+dt+" = ");
			double oldx = coordinates.getOriginX();
			double oldy = coordinates.getOriginY();
			double oldangle = coordinates.getAngle();

			double newx = oldx + getSpeedx() * dt;
			double newy = oldy + getSpeedy() * dt;
			double newangle = oldangle + getAngularSpeed() * dt;
			coordinates.setOrigin(newx, newy);
			coordinates.setAngle(newangle);

			// call collision detector related change listener
			if (collisionDetectorChangeListener != null) {
				for (Disk d : getDisks()) {
					collisionDetectorChangeListener.diskHasMoved(d);
				}
			}
			LinkedList<Collision> collisions = collisionDetector.getCollisions(this);
			if (collisions.isEmpty()) {
				// call other change listeners
				for (DiskChangeListener changeListener : changeListeners) {
					if (changeListener != collisionDetectorChangeListener) {
						for (Disk d : getDisks()) {
							changeListener.diskHasMoved(d);
						}
					}
				}
			} else {
				// undo movement, call change listener of collision detector again
				collisionTracker.addAll(collisions, false);
				coordinates.setOrigin(oldx, oldy);
				coordinates.setAngle(oldangle);
				if (collisionDetectorChangeListener != null) {
					for (Disk d : getDisks()) {
						collisionDetectorChangeListener.diskHasMoved(d);
					}
				}
			}

			//System.out.println(coordinates.getAngle());

		}
	}

	public void teleport(double shiftx, double shifty, double rotation) {
		getCoordinates().setOrigin(getCenterx() + shiftx, getCentery() + shifty);
		getCoordinates().setAngle(getCoordinates().getAngle() + rotation);
		for (Disk d : getDisks()) {
			diskcomplexEnsemble.callDiskMovedListeners(d);
		}
	}

	//@R
	//	public boolean doTimeStep(Assignment assignment, double currentTime, double dt, FrictionModel frictionModel) {
	//		if (frictionModel != null) {
	//			if (isMoving()) {
	//				applyFriction(dt, frictionModel);
	//			}
	//		}
	//		CollisionDetector cd = this.diskcomplexEnsemble.getCollisionDetector();
	//		
	//		double[] values = new double[3];
	//		values = assignment.getTimeStepValues(currentTime, cd.getNonSelfCollisions(this));
	//				
	//		coordinates.setOrigin(values[0], values[1]);
	//		coordinates.setAngle(values[2]);
	//
	//		return true;
	//	}

	private void applyFriction(double dt, FrictionModel frictionModel) {
		double totalEnergy = getTotalEnergy();
		applyAllFrictionImpulses(dt, frictionModel);
		reCalculateSpeeds();
		if (getTotalEnergy() > totalEnergy) {
			momentumx = 0.0;
			momentumy = 0.0;
			angularMomentum = 0.0;
			reCalculateSpeeds();
		}
	}

	public double getTranslationEnergy() {
		return 0.5 * mass * (speedx * speedx + speedy * speedy);
	}

	public double getRotationEnergy() {
		return 0.5 * massMomentum * angularSpeed * angularSpeed;
	}

	public double getTotalEnergy() {
		return getTranslationEnergy() + getRotationEnergy();
	}

	private void applyAllFrictionImpulses(double dt, FrictionModel frictionModel) {
		for (Disk d : disks) {
			applyFrictionImpulse(d, getSpeedx(d) + d.getEgoMotionx(), getSpeedy(d) + d.getEgoMotiony(), dt, frictionModel);
		}
	}

	private void applyFrictionImpulse(Disk d, double speedx, double speedy, double dt, FrictionModel frictionModel) {
		double s2 = speedx * speedx + speedy * speedy;
		if (s2 > Epsilons.EPSILON_FRICTION_SPEED) {
			//double f = dt/Math.sqrt(s2)*d.getFloorContact(physicsParameters.getFrictionModel());
			//double forcex = f*speedx;
			//double forcey = f*speedy;
			double force[] = frictionModel.getFrictionForce(d, speedx, speedy);
			double deltamx = dt * force[0];
			double deltamy = dt * force[1];
			applyImpulse(deltamx, deltamy, d.getX(), d.getY());
		}
	}

	/**
	 * Is the disk complex translating or rotating
	 * 
	 * @return true if one of the speeds is not 0.0
	 */
	public boolean isMoving() {
		return (getSpeedx() != 0.0) || (getSpeedy() != 0.0) || (getAngularSpeed() != 0.0);
	}

	public double getMaxTimeStep() {
		double maxDisplacement = minDiskRadius * Epsilons.MAX_RELATIVE_DISK_TIMESTEP_DISPLACEMENT;
		double maxRotationAngle = minDiskRadiusOverDistanceToOrigin * Epsilons.MAX_RELATIVE_DISK_TIMESTEP_DISPLACEMENT;
		double maxdtx = maxDisplacement / Math.abs(getSpeedx());
		double maxdty = maxDisplacement / Math.abs(getSpeedy());
		double maxdta = maxRotationAngle / Math.abs(getAngularSpeed());
		double max = Double.MAX_VALUE;
		if (!Double.isNaN(maxdtx))
			max = Math.min(max, maxdtx);
		if (!Double.isNaN(maxdty))
			max = Math.min(max, maxdty);
		if (!Double.isNaN(maxdta))
			max = Math.min(max, maxdta);
		return max;
	}

	/**
	 * Add impulse (= force x time = mass x speed) and angular impulse (=angular speed change x mass momentum) times at a given point is applied to the DiskComplex.
	 * 
	 * @param impulseX
	 *            x component of the impulse
	 * @param impulseY
	 *            y component of the impulse
	 * @param angularImpulse
	 *            change of angular momentum
	 */
	public void applyImpulse(double impulseX, double impulseY, double angularImpulse) {
		/*System.out.println("X: " + impulseX + " Y: " + impulseY);
		if(Math.abs(impulseX) > 200){
			impulseX = 200 * impulseX/Math.abs(impulseX);
		}
		if(Math.abs(impulseY) > 200){
			impulseY = 200 * impulseY/Math.abs(impulseY);
		}*/
		momentumx += impulseX;
		momentumy += impulseY;
		angularMomentum += angularImpulse;
		reCalculateSpeeds();
	}

	/**
	 * An impulse (= force x time) at a given point is applied to the DiskComplex.
	 * 
	 * @param impulseX
	 *            x component of the impulse
	 * @param impulseY
	 *            y component of the impulse
	 * @param pointX
	 *            x coordinate (absolute) of the point at which the impulse is applied
	 * @param pointY
	 *            y coordinate (absolute) of the point at which the impulse is applied
	 */
	public void applyImpulse(double impulseX, double impulseY, double pointX, double pointY) {
		applyImpulse(impulseX, impulseY, angularMomentum(pointX, pointY, impulseX, impulseY));
	}

	//	public void applyFrictionImpulses(Map<Disk, double[]> newPositions, double dt, FrictionModel frictionModel) {
	//		for (Entry<Disk, double[]> e : newPositions.entrySet()) {
	//			Disk d = e.getKey();
	//			double[] pos = e.getValue();
	//			double f = 1.0 / dt;
	//			double sx = (pos[0] - d.getX()) * f;
	//			double sy = (pos[1] - d.getY()) * f;
	//			applyFrictionImpulse(d, sx, sy, dt, frictionModel);
	//		}
	//		reCalculateSpeeds();
	//	}

	/**
	 * Get dot product of speed at a point with the given direction vector
	 * 
	 * @param pointX
	 * @param pointY
	 * @param directionX
	 * @param directionY
	 * @return the speed component projected on the given direction
	 */
	public double getProjectedSpeed(double pointX, double pointY, double directionX, double directionY) {
		return getSpeedx(pointX, pointY) * directionX + getSpeedy(pointX, pointY) * directionY;
	}

	/**
	 * How much will the value getProjectedSpeed() change as an effect of the applyImpulse() method (where the impulse is equal to the direction vector)
	 * 
	 * @param pointX
	 * @param pointY
	 * @param dirx
	 * @param diry
	 * @return change of speed
	 */
	public double getProjectedSpeedChangeByImpulse(double pointX, double pointY, double dirx, double diry) {
		// How much does (mx/m - (y-cy)*am/mm)*dx + (my/m + (x-cx)*am/mm)*dy change when
		// mx += dx;
		// my += dy;
		// am += rx*dy-ry*dx;
		// where rx := x-cx; ry := y-cy
		// 
		// Answer: 
		// (dx/m - ry*(rx*dy-ry*dx)/mm)*dx + (dy/m + rx*(rx*dy-ry*dx)/mm)*dy
		// (dx^2 + dy^2)/m + (-ry*(rx*dy-ry*dx)*dx + rx*(rx*dy-ry*dx)*dy)/mm
		// (dx^2 + dy^2)/m + (dy*rx-dx*ry)^2/mm
		double rx = pointX - getCenterx();
		double ry = pointY - getCentery();
		return getProjectedSpeedChangeByImpulseRelative(rx, ry, dirx, diry);
	}
	
	/**
	 * How much will the value getProjectedSpeed() change as an effect of the applyImpulse() method (where the impulse is equal to the direction vector)
	 * 
	 * @param rx
	 * @param ry
	 * @param dirx
	 * @param diry
	 * @return change of projected speed 
	 */
	public double getProjectedSpeedChangeByImpulseRelative(double rx, double ry, double dirx, double diry) {
		double dyrx_dxry = diry * rx - dirx * ry;
		double dx2_dy2 = dirx * dirx + diry * diry;
		return dx2_dy2 / mass + dyrx_dxry * dyrx_dxry / massMomentum;		
	}
		

	/**
	 * Angular momentum (p-c) x m of a linear momentum m that acts at a point p, respective to centre c
	 * 
	 * @param px
	 *            x coordinate of point at which the momentum acts
	 * @param py
	 *            y coordinate of point at which the momentum acts
	 * @param mx
	 *            x component of linear momentum
	 * @param my
	 *            y component of linear momentum
	 * @param cx
	 *            x coordinate of rotation center
	 * @param cy
	 *            y coordinate of rotation center
	 * @return angular momentum (z component of cross product r x m)
	 */
	public static double angularMomentum(double px, double py, double mx, double my, double cx, double cy) {
		return angularMomentumRelative(px - cx, py - cy, mx, my);
	}

	/**
	 * Angular momentum (p-c) x m of a linear momentum m that acts at a point p, respective to DiskComplex center
	 * 
	 * @param px
	 *            x coordinate of point at which the momentum acts
	 * @param py
	 *            y coordinate of point at which the momentum acts
	 * @param mx
	 *            x component of linear momentum
	 * @param my
	 *            y component of linear momentum
	 * @return angular momentum (z component of cross product r x m)
	 */
	public double angularMomentum(double px, double py, double mx, double my) {
		return angularMomentum(px, py, mx, my, coordinates.getOriginX(), coordinates.getOriginY());
	}


	/**
	 * Angular momentum r x m of a linear momentum m that acts at a point p, respective to center c, where r = p-c
	 * 
	 * @param rx
	 *            x coordinate of vector from center to point at which the momentum acts
	 * @param ry
	 *            y coordinate of vector from center to point at which the momentum acts
	 * @param mx
	 *            x component of linear momentum
	 * @param my
	 *            y component of linear momentum
	 * @return angular momentum (z component of cross product r x m)
	 */
	public static double angularMomentumRelative(double rx, double ry, double mx, double my) {
		return rx * my - ry * mx;
	}
	
	/**
	 * This method must be called whenever the structure has changed, i.e. when - disks have been added - disks have been removed - disk positions have been changed - disk sizes/masses have changed
	 * 
	 * Calculates the new center of mass and shifts origin of the coordinate system there. Calculates the new mass and massMomentum, uses old momenta values to update speeds
	 * 
	 */
	public void recalculateAfterStructuralChange() {
		recaluclateCenterOfMass();
		recalculateMassMomentum();
		recalculateMinRadius();
		reCalculateSpeeds();
	}

	private void recalculateMinRadius() {
		// calculate new minimal disk radius and minimal quotient radius/distance to origin
		minDiskRadius = Double.POSITIVE_INFINITY; //Limits.MAX_DISK_DIAMETER/2.0;
		minDiskRadiusOverDistanceToOrigin = Double.POSITIVE_INFINITY;
		for (Disk d : disks) {
			minDiskRadius = Math.min(minDiskRadius, d.getRadius());
			double dist = d.getDistanceToOrigin();
			if (dist > 0.0) {
				minDiskRadiusOverDistanceToOrigin = Math.min(minDiskRadiusOverDistanceToOrigin, d.getRadius() / dist);
			}
		}
	}

	private void recalculateMassMomentum() {
		// new mass and massMomentum
		massMomentum = 0.0;
		for (Disk d : disks) {
			double dist = d.getDistanceToOrigin();
			double r = d.getRadius();
			massMomentum += d.getMass() * (0.5 * r * r + dist * dist); // massMomentum of disk plus shift by Steiner
		}
	}

	private void recaluclateCenterOfMass() {
		// calculate new center of mass
		double sumx = 0.0;
		double sumy = 0.0;
		double summ = 0.0;
		for (Disk d : disks) {
			double m = d.getMass();
			sumx += m * d.getX();
			sumy += m * d.getY();
			summ += m;
		}
		mass = summ;
		double comx = sumx / summ;
		double comy = sumy / summ;
		// set new coordinate origin
		double deltax = comx - coordinates.getOriginX();
		double deltay = comy - coordinates.getOriginY();
		coordinates.setOrigin(comx, comy);
		// this has moved all disks absolute positions by deltax, deltay; have to shift all back 
		for (Disk d : disks) {
			d.shiftPosition(-deltax, -deltay);
		}
	}

	public void mergePermanently(DiskComplex other, Disk thisDisk, Disk otherDisk) {
		if (other == this)
			throw new IllegalArgumentException("cannot merge with itself");
		skeleton.mergePermanently(thisDisk, otherDisk, other.skeleton);
		performMerge(other);
	}

	private void performMerge(DiskComplex other) {
		for (Disk disk : other.disks) {
			disk.changeOwner(this);
		}
		disks.addAll(other.disks);
		fixedDisks.addAll(other.fixedDisks);
		if (isFixed()) {
			momentumx = 0;
			momentumy = 0;
			angularMomentum = 0;
		} else {
			angularMomentum = jointAngularMomentum(this, other);
			momentumx = momentumx + other.momentumx;
			momentumy = momentumy + other.momentumy;
		}
		recalculateAfterStructuralChange();
		other.disks.clear();
		other.fixedDisks.clear();
	}

	public void mergeTransiently(DiskComplex other, Disk thisDisk1, Disk thisDisk2, Disk otherDisk1, Disk otherDisk2) {
		if (other == this)
			throw new IllegalArgumentException("cannot merge with itself");
		skeleton.mergeTransiently(thisDisk1, otherDisk1, thisDisk2, otherDisk2, other.skeleton);
		performMerge(other);
	}

	private static double jointAngularMomentum(DiskComplex dc1, DiskComplex dc2) {
		// calculate joint center of mass
		double x1 = dc1.getCenterx();
		double y1 = dc1.getCentery();
		double m1 = dc1.getMass();
		double x2 = dc2.getCenterx();
		double y2 = dc2.getCentery();
		double m2 = dc2.getMass();
		double f = 1.0 / (m1 + m2);
		double comx = (x1 * m1 + x2 * m2) * f;
		double comy = (y1 * m1 + y2 * m2) * f;
		// joint angular momentum: sum of individual angular momenta (around individual centers of mass) plus orbit angular momenta (around joint center of mass)
		return dc1.angularMomentum + dc2.angularMomentum + dc1.orbitAngularMomentum(comx, comy) + dc2.orbitAngularMomentum(comx, comy);
	}

	/**
	 * The angular momentum of all masses concentrated in the center of gravity around reference point
	 */
	private double orbitAngularMomentum(double refPointx, double refPointy) {
		return angularMomentum(getCenterx(), getCentery(), momentumx, momentumy, refPointx, refPointy);
	}

	private Disk findClosest(Disk newDisk) {
		Disk closest = null;
		double min = Double.MAX_VALUE;
		for (Disk d : disks) {
			double dx = d.getX() - newDisk.getX();
			double dy = d.getY() - newDisk.getY();
			double sr = d.getRadius() + newDisk.getRadius();
			double distRelSqr = (dx * dx + dy * dy) / (sr * sr);
			if (distRelSqr < min) {
				min = distRelSqr;
				closest = d;
			}
		}
		return closest;
	}

	/**
	 * Calulcates the momenta of this DiskComplex, under the assumption that this had been merged to mergedTo
	 */
	private void reCaluculateMomenta(DiskComplex reference) {
		// calculate center of mass of all disks 
		double sumx = 0.0;
		double sumy = 0.0;
		double summ = 0.0;
		for (Disk disk : disks) {
			double m = disk.getMass();
			sumx += disk.getX() * m;
			sumy += disk.getY() * m;
			summ += m;
		}
		double comx = sumx / summ;
		double comy = sumy / summ;

		// calculate momentum of the split off DiskComplex using momenta from reference disk complex 
		double sumMomentumx = 0.0;
		double sumMomentumy = 0.0;
		double sumAngularMomentum = 0.0;
		for (Disk disk : disks) {
			double mx = reference.momentumX(disk);
			double my = reference.momentumY(disk);
			sumMomentumx += mx;
			sumMomentumy += my;
			sumAngularMomentum += angularMomentum(disk.getX(), disk.getY(), mx, my, comx, comy) + reference.spinAngularMomentum(disk);
		}
		momentumx = sumMomentumx;
		momentumy = sumMomentumy;
		angularMomentum = sumAngularMomentum;
	}

	/**
	 * The x component of the momentum of a disk
	 */
	private double momentumX(Disk disk) {
		return getSpeedx(disk) * disk.getMass();
	}

	/**
	 * The x component of the momentum of a disk
	 */
	private double momentumY(Disk disk) {
		return getSpeedy(disk) * disk.getMass();
	}

	/**
	 * The spin angular momentum of a rotating disk
	 */
	private double spinAngularMomentum(Disk disk) {
		double r = disk.getRadius();
		return 0.5 * disk.getMass() * r * r * getAngularSpeed();
	}

	private void reCalculateSpeeds() {
		//System.out.println("X: " + speedx + " Y: " + speedy);
		speedx = momentumx / mass;
		/*if(Math.abs(speedx) > 30){
			speedx = 30 * speedx/Math.abs(speedx);
		}*/
		speedy = momentumy / mass;
		/*if(Math.abs(speedy) > 30){
			speedy = 30 * speedy/Math.abs(speedy);
		}*/
		angularSpeed = angularMomentum / massMomentum;
		assert (!Double.isNaN(angularSpeed));
	}

	public void setDiskFixed(Disk disk) {
		fixedDisks.add(disk);
	}

	public void setDiskNotFixed(Disk disk) {
		fixedDisks.remove(disk);
	}

	public boolean isFixed() {
		return !fixedDisks.isEmpty();
	}

	public boolean isControllable(Disk disk) {
		return skeleton.isControllable(disk);
	}

	public Map<Disk, Set<Disk>> getMovingSets(Disk disk) {
		return skeleton.getMovingSets(disk);
	}

	public boolean tryPerformDiskModifications(Collection<DiskModification> diskModifications, CollisionDetector collisionDetector, double timeStep, CollisionTracker collisionTracker,
			Set<Disk> disksWithEgoMotion) {

		// transform the modifications to respect fixed disks or to minimize (friction weighted) offset of disks
		//Collection<DiskModification> diskModifications = getShiftedAndRotatedDiskModifications(diskModificationMap, environment.getPhysicsParameters().getFrictionModel(), doNotCorrectAngle);
		//CollisionDetector collisionDetector = environment.getCollisionDetector();

		// perform all modifications
		for (DiskModification dm : diskModifications) {
			performModification(dm);
		}
		LinkedList<Collision> collisions = collisionDetector.getCollisions(this);

		if (collisions.isEmpty()) {
			// collision free: resolve changes to the skeleton due to the modifications
			if (skeleton.diskModificationsSplitSkeleton(diskModifications)) {
				splitIntoSubComplexes();
			} else {
				// no dissociation, recalculate center of mass and other 
				recalculateAfterStructuralChange();
			}
			for (DiskModification dm : diskModifications) {
				Disk d = dm.getDisk();
				d.increaseEgoMotion(dm.getDeltaX() / timeStep, dm.getDeltaY() / timeStep);
				disksWithEgoMotion.add(d);
			}
			return true;
		} else {
			// check if the movement was blocked due to fixedness 
			boolean blocked = false;
			for (Collision c : collisions) {
				DiskComplex dc1 = c.getObj1().getDiskComplex();
				CollidableObject co2 = c.getObj2();
				if (co2 instanceof Disk) {
					DiskComplex dc2 = ((Disk) co2).getDiskComplex();
					if (dc1 == dc2) {
						// Self collision: blocks
						blocked = true;
					}
					if (dc1.isFixed() && dc2.isFixed()) {
						// Collision between two fixed: blocked
						blocked = true;
					}
				} else {
					if (dc1.isFixed()) {
						// Collision between fixed and wall: blocked
						blocked = true;
					}
				}
			}

			// undo all modifications
			for (DiskModification dm : diskModifications) {
				undoModification(dm);
			}

			collisionTracker.addAll(collisions, blocked);
			// if not blocked, prepare impulse transfer by storing the motion vectors  
			if (!blocked) {
				for (DiskModification dm : diskModifications) {
					Disk d = dm.getDisk();
					d.increaseEgoMotion(dm.getDeltaX() / timeStep, dm.getDeltaY() / timeStep);
					disksWithEgoMotion.add(d);
				}
			}
			return false;
		}
	}

	/**
	 * This performs a disk modification. Is not public and should be only called from the
	 * DiskComplexEnsemble to which the changed disk belongs.
	 * Note: No DiskChangeListeners are called by this method. This must still be done by the calling method afterwards!
	 */
	public void performModification(DiskModification dm) {
		Disk d = dm.getDisk();
		if (dm.changesPosition()) {
			d.setPosition(dm.getNewX(), dm.getNewY());
			diskcomplexEnsemble.callDiskMovedListeners(d);
		}
		if (dm.changesAngle()) {
			d.setAngle(dm.getNewAngle());
		}
		if (dm.changesRadius()) {
			d.setRadius(dm.getNewRadius());
			diskcomplexEnsemble.callRadiusChangedListeners(dm.getDisk());
		}
	}

	/**
	 * This undoes the modification. Is not public and should be only called from the
	 * DiskComplexEnsemble to which the changed disk belongs.
	 */
	public void undoModification(DiskModification dm) {
		Disk d = dm.getDisk();
		if (dm.changesPosition()) {
			d.setPosition(dm.getOldX(), dm.getOldY());
			diskcomplexEnsemble.callDiskMovedListeners(d);
		}
		if (dm.changesAngle()) {
			d.setAngle(dm.getOldAngle());
		}
		if (dm.changesRadius()) {
			d.setRadius(dm.getOldRadius());
			diskcomplexEnsemble.callRadiusChangedListeners(dm.getDisk());
		}
	}

	/**
	 * Composes the provided DiskModifications with a rotation and a translation such that
	 * either: fixed disks stay fixed (same place and orientation)
	 * or: sum of squares of offset (possibly weighted by friction) is minimized
	 * 
	 * @param diskModificationMap
	 *            a map from disks to disk modifications
	 * 
	 * @return a new collection holding the DiskModifications that result from the composition
	 */
	public double minimalResistanceShiftedAndRotatedDiskModifications(Map<Disk, DiskModification> diskModificationMap, FrictionModel frictionModel, boolean doNotCorrectAngle,
			List<DiskModification> shiftedModifications) {
		// determine rotation center cx,cy and rotation angle and offset dx,dy in  
		double phi, cx, cy, shiftx, shifty;
		if (fixedDisks.isEmpty()) {
			// no fixed disks, make the weighted offset minimal
			cx = 0;
			cy = 0;
			OptimalShiftAndSmallRotation osr = new OptimalShiftAndSmallRotation();
			for (Disk d : disks) {
				double x = d.getX();
				double y = d.getY();
				DiskModification dm = diskModificationMap.get(d);
				double weight = frictionModel.getDisplacementResistance(d);
				if (dm == null) {
					osr.addWeightedSample(weight, x, y, x, y);
				} else {
					osr.addWeightedSample(weight, dm.getNewX(), dm.getNewY(), dm.getOldX(), dm.getOldY());
				}
			}
			shiftx = osr.getOffsetx();
			assert (!Double.isNaN(shiftx));
			shifty = osr.getOffsety();
			assert (!Double.isNaN(shifty));
			phi = doNotCorrectAngle ? 0 : osr.getPhi();
			assert (!Double.isNaN(phi));

		} else {
			// there are fixed disks, it does not matter which one to choose, just take the first one 
			Disk fixed = fixedDisks.iterator().next();
			cx = fixed.getX();
			cy = fixed.getY();
			DiskModification mod = diskModificationMap.get(fixed);
			if (mod == null) {
				// fixed disk is not modified, can return disk modifications as they are
				shiftedModifications.addAll(diskModificationMap.values());
				return resistance(shiftedModifications, frictionModel);
			} else {
				// rotate around the new position, then shift back to the old position
				cx = mod.getNewX();
				cy = mod.getNewY();
				shiftx = mod.getOldX() - mod.getNewX();
				shifty = mod.getOldY() - mod.getNewY();
				phi = doNotCorrectAngle ? 0 : mod.getOldAngle() - mod.getNewAngle();
			}
		}
		double sin = Math.sin(phi);
		double cos = Math.cos(phi);
		for (Disk d : getDisks()) {
			DiskModification dm = diskModificationMap.get(d);
			double x, y, angle, radius;
			if (dm == null) {
				x = d.getX();
				y = d.getY();
				angle = d.getAngle();
				radius = d.getRadius();
			} else {
				x = dm.getNewX();
				y = dm.getNewY();
				angle = dm.getNewAngle();
				radius = dm.getNewRadius();
			}
			double dx = x - cx;
			double dy = y - cy;
			double newx = cx + dx * cos - dy * sin + shiftx;
			double newy = cy + dx * sin + dy * cos + shifty;
			shiftedModifications.add(new DiskModification(d, newx, newy, angle + phi, radius));
		}
		return resistance(shiftedModifications, frictionModel);
	}

	private double resistance(List<DiskModification> diskModifications, FrictionModel frictionModel) {
		double sum = 0.0;
		for (DiskModification dm : diskModifications) {
			sum += frictionModel.getDisplacementResistance(dm.getDisk()) * dm.getDisplacement();
		}
		return sum;
	}

	private void splitIntoSubComplexes() {
		Skeleton[] partsSkeletons = skeleton.getSplittedSkeletons();
		DiskComplex[] parts = new DiskComplex[partsSkeletons.length];
		for (int i = 0; i < parts.length; i++) {
			parts[i] = new DiskComplex(diskcomplexEnsemble, partsSkeletons[i]);
			for (Island island : partsSkeletons[i].getIslands()) {
				for (Vertex vertex : island.getVertices()) {
					Disk disk = vertex.getDisk();
					disk.changeOwner(parts[i]);
					parts[i].disks.add(disk);
					if (fixedDisks.contains(disk)) {
						parts[i].fixedDisks.add(disk);
					}
				}
			}
			// calculate the momentum that is assigned to the part, based on speeds taken from this DiskComplex 
			parts[i].reCaluculateMomenta(this);
			parts[i].recalculateAfterStructuralChange();
		}
		diskcomplexEnsemble.diskComplexWasSplit(this, parts);
		disks.clear();
		fixedDisks.clear();
	}

	public Set<Disk> getFixedDisks() {
		return fixedDisks;
	}

	/**
	 * Remove skeleton edge between two disks. If skeleton decomposes into two parts, split DiskComplex into two.
	 * 
	 * @param disk1
	 *            first disk
	 * @param disk2
	 *            second disk
	 */
	public void split(Disk disk1, Disk disk2) {
		if (skeleton.removalOfPermanentEdgeSplitsSkeleton(disk1, disk2)) {
			splitIntoSubComplexes();
		}
	}

	/**
	 * Provide the set of transient edges
	 * 
	 * @return collection of transient edges of the corresponding skeleton
	 */
	public Collection<TransientEdge> getTransientEdges() {
		return skeleton.getTransientEdges();
	}

	/**
	 * Set the angular speed of the DiskComplex to a defined value
	 * 
	 * @param newAngularSpeed
	 */
	public void setAngularSpeed(double newAngularSpeed) {
		angularSpeed = newAngularSpeed;
		angularMomentum = angularSpeed * massMomentum;
	}

	/**
	 * Set the velocity of the DiskComplex to a defined value
	 * 
	 * @param vx
	 *            x component of new velocity vector
	 * @param vy
	 *            y component of new velocity vector
	 */
	public void setVelocity(double vx, double vy) {
		speedx = vx;
		speedy = vy;
		momentumx = mass * vx;
		momentumy = mass * vy;
	}

}
