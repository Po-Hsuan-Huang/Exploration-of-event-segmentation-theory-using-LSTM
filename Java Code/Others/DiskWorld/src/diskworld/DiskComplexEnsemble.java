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
import java.util.Set;

import diskworld.collisions.CollisionTracker;
import diskworld.interfaces.CollisionDetector;
import diskworld.interfaces.DiskChangeListener;
import diskworld.interfaces.FrictionModel;

/**
 * Class that holds and manages multiple DiskComplexes
 * 
 * @author Jan
 * 
 */
public class DiskComplexEnsemble {

	private final Set<DiskComplex> diskComplexes;
	private final Set<DiskChangeListener> changeListeners;
	//private final CollisionDetector collisionDetector;
	//private final Map<Disk, Collision> collisions;
	private final Set<Disk> sensorDisks;
	private final Set<Disk> actuatorDisks;

	public DiskComplexEnsemble() {
		diskComplexes = new HashSet<DiskComplex>();
		//this.collisionDetector = collisionDetector;
		//collisions = new HashMap<Disk, Collision>();
		changeListeners = new HashSet<DiskChangeListener>();
		sensorDisks = new HashSet<Disk>();
		actuatorDisks = new HashSet<Disk>();
	}

	public void addChangeListener(DiskChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	public void removeChangeListener(DiskChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}

	/*	public void clear() {
			for (DiskComplex dc : diskComplexes) {
				if (!dc.isMerged())
					for (DiskChangeListener changeListener : changeListeners) {
						for (Disk d : dc.getDisks()) {
							changeListener.diskWasRemoved(d);
						}
					}
			}
			collisionDetector.setWalls(null);
			diskComplexes.clear();
		}*/

	public Set<DiskComplex> getDiskComplexes() {
		return diskComplexes;
	}

	/**
	 * Provides all disks that have sensors
	 * 
	 * @return collection of sensor disks
	 */
	public Collection<Disk> getSensorDisks() {
		return sensorDisks;
	}

	/**
	 * Provides all disks that have an actuator
	 * 
	 * @return collection of actuator disks
	 */
	public Collection<Disk> getActuatorDisks() {
		return actuatorDisks;
	}

	public DiskComplex createNewDiskComplex() {
		DiskComplex res = new DiskComplex(this);
		// is not yet added to the diskComplexes set, will happen later when it obtains its first disk by calling the method register()
		return res;
	}

	public void diskWasAdded(Disk disk) {
		for (DiskChangeListener changeListener : changeListeners) {
			changeListener.diskWasAdded(disk);
		}
		if (disk.getDiskType().hasSensors()) {
			sensorDisks.add(disk);
		}
		if (disk.getDiskType().hasActuator()) {
			actuatorDisks.add(disk);
		}
	}

	public void removeDiskComplex(DiskComplex dc) {
		if (diskComplexes.remove(dc)) {
			for (Disk d : dc.getDisks()) {
				for (DiskChangeListener changeListener : changeListeners) {
					changeListener.diskWasRemoved(d);
				}
				DiskType diskType = d.getDiskType();
				if (diskType.hasSensors()) {
					sensorDisks.remove(d);
				}
				if (diskType.hasActuator()) {
					actuatorDisks.remove(d);
				}
			}
		}
	}

	/**
	 * Perform a time step
	 * 
	 * @param dt
	 *            the delta that time shall advances
	 * @param collisionTracker
	 * @param collisionDetector
	 * @param collisionDetectorChangeListener
	 */
	public void doTimeStep(double dt, FrictionModel frictionModel, CollisionDetector collisionDetector, CollisionTracker collisionTracker, DiskChangeListener collisionDetectorChangeListener) {
		for (DiskComplex dc : diskComplexes) {
			dc.doTimeStep(dt, frictionModel, collisionDetector, collisionTracker, collisionDetectorChangeListener, changeListeners);
		}
	}

	public double getMaxTimeStep() {
		double min = Double.MAX_VALUE;
		for (DiskComplex dc : diskComplexes) {
			min = Math.min(min, dc.getMaxTimeStep());
		}
		return min;
	}

	public void callDiskMovedListeners(Disk d) {
		for (DiskChangeListener changeListener : changeListeners) {
			changeListener.diskHasMoved(d);
		}
	}

	public void callRadiusChangedListeners(Disk d) {
		for (DiskChangeListener changeListener : changeListeners) {
			changeListener.diskHasChangedRadius(d);
		}
	}

	public void mergeDiskComplexes(Disk d1, Disk d2) {
		DiskComplex dc1 = d1.getDiskComplex();
		DiskComplex dc2 = d2.getDiskComplex();
		if (dc1 != dc2) {
			dc1.mergePermanently(dc2, d1, d2);
			diskComplexes.remove(dc2);
		}
	}

	public void diskComplexWasSplit(DiskComplex diskComplex, DiskComplex[] parts) {
		diskComplexes.remove(diskComplex);
		for (DiskComplex part : parts) {
			diskComplexes.add(part);
		}
	}

	public void register(DiskComplex diskComplex) {
		diskComplexes.add(diskComplex);
	}

	//public Map<Disk, Collision> getCollisions() {
	//return collisions;
	//}

}
