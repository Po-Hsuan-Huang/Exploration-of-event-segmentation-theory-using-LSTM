package EventSegmentationScenario;

import java.awt.Color;
import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;

/**
 * Implementation of a null-object.
 * This object is not visible and is not touchable by the hand. Using a filler
 * object is equivalent to not having any object present at this time.
 * They are used (instead of not using an object) to guarantee that an object is
 * present at all times.
 * 
 * @author Christian Gumbsch
 *
 */
public class FillerObject implements InteractableObjects {

	Environment env;
	Disk disk;
	
	public FillerObject(Environment env, double x, double y){
		this.env = env;
		this.disk = env.newRootDisk(x, y, 3.0, new DiskType(DiskMaterial.DOUGH.withColor(Color.BLACK)));
	}
	
	@Override
	public int getType() {
		return 300;
	}

	@Override
	public Disk getDisk() {
		return disk;
	}

	@Override
	public double getX() {
		return disk.getX();
	}

	@Override
	public double getY() {
		return disk.getY();
	}

	@Override
	public double getAngle() {
		return 0;
	}

	@Override
	public double getSize() {
		return disk.getRadius();
	}

	@Override
	public void remove() {
		env.getDiskComplexesEnsemble().removeDiskComplex(disk.getDiskComplex());
	}

	@Override
	public boolean isMerged() {
		return false;
	}

	@Override
	public boolean hasMoved() {
		return false;
	}

}
