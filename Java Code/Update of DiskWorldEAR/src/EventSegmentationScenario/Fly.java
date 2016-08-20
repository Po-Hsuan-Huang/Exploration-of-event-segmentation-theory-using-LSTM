package EventSegmentationScenario;

import java.awt.Color;

import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actuators.EmptyActuator;
import diskworld.interfaces.Actuator;
import diskworld.visualization.PolygonDiskSymbol;
/**
 * Implementation of a fly object that slides away
 * when pushed by another disk.
 * Referred to as foe-object in the paper
 * 
 * @author Christian Gumbsch
 *
 */
public class Fly implements InteractableObjects {
	
	/** The type of a fly object is 200*/
	private int type = 200;
	/** The main disk of this object*/
	private Disk disk;
	/** Size of the object*/
	private double radius = 3.5;
	/** Diskworld environment this object appears in*/
	private Environment env;
	/** EventHandler stating the effect of a collision with another disk*/
	private FlyEventHandler fev;
	
	public Fly(Environment env, double x, double y, Arm arm){
		this.env = env;
		new PolygonDiskSymbol(null);
		Actuator emptyActuator = new EmptyActuator("Fly", PolygonDiskSymbol.getSquareSymbol(0.5));
		DiskType flyType = new DiskType(DiskMaterial.ORGANIC.withColor(Color.GRAY), emptyActuator);//new DiskType(new DiskMaterial(1.0, 0, 50.0, 1.0, Color.GREEN.brighter()), emptyActuator);
		this.disk = env.newRootDisk(x, y, radius, flyType);
		this.disk.setZLevel(1);
		fev = new FlyEventHandler(this, arm);
		this.disk.setEventHandler(fev);
		
	}

	@Override
	public int getType() {
		return type;
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
		return disk.getAngle();
	}

	@Override
	public double getSize() {
		return radius;
	}

	@Override
	public void remove() {
		env.getDiskComplexesEnsemble().removeDiskComplex(disk.getDiskComplex());
		
	}

	@Override
	public Disk getDisk() {
		return disk;
	}

	@Override
	public boolean isMerged() {
		return false;
	}
	
	@Override
	public boolean hasMoved(){
		return fev.hasMoved();
	}

}