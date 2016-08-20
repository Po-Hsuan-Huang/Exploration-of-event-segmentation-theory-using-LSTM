package EventSegmentationScenario;

import java.awt.Color;
import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actuators.EmptyActuator;
import diskworld.interfaces.Actuator;
import diskworld.visualization.CircleDiskSymbol;

/**
 * Implementation of the burrdock objects.
 * These objects stick to the hand after contact.
 * Referred to as a food-object in the paper.
 * 
 * @author Christian Gumbsch
 *
 */
public class Burrdock implements InteractableObjects {
	
	/** The type of a light burrdock is 0*/
	private int type = 0;
	/** Main disk of the object*/
	private Disk disk;
	/**Small sticky disks around the main disk*/
	private Disk[] burrs;
	/**Size of the main disk*/
	private double radius = 3.0;
	/**Simulation environment and the arm*/
	private Environment env;
	private Arm arm;
	/**Is the burrdock attached to the arm?*/
	private boolean merged = false;

	
	public Burrdock (Environment env, double x, double y, double angle, DiskType handtype, Arm arm){
		this(env, x, y, angle, handtype, arm, true);
	}
	
	/**
	 * Constructor
	 * @param env,
	 * 			Diskworld environment in which the burrdock is generated
	 * @param x,
	 * 			position x-wise where the burrdock should be generated
	 * @param y,
	 * 			position y-wise where the burrdock should be generated
	 * @param angle,
	 * 			orientation of the burrdock
	 * @param handtype,
	 * 			type of disk to which the burrdock sticks after contact
	 * @param arm,
	 * 			the arm class
	 * @param red,
	 * 			boolean describing the color of the object (true = red, false = pink)
	 */
	protected Burrdock (Environment env, double x, double y, double angle, DiskType handtype, Arm arm, boolean red){
		this.env = env;
		Actuator emptyActuator = new EmptyActuator("Burrdock", new CircleDiskSymbol(0.3));
		DiskType innerType;
		if(red){
			innerType = new DiskType(DiskMaterial.DOUGH.withColor(Color.RED), emptyActuator);//new DiskType(new DiskMaterial(1.0, 0, 50.0, 1.0, Color.GREEN.brighter()), emptyActuator);
		}
		else{
			innerType = new DiskType(DiskMaterial.DOUGH.withColor(Color.MAGENTA), emptyActuator);
		}
		this.disk = env.newRootDisk(x, y, radius, angle, innerType);//env.newRootDisk(x, y, radius, innerType);
		this.disk.setZLevel(1);
		DiskType burrType;
		if(red){
			burrType = new DiskType(DiskMaterial.DOUGH.withColor(Color.RED.brighter()));//new DiskType(new DiskMaterial(1.0, 0, 50.0, 1.0, Color.GREEN.brighter()), emptyActuator);
		}
		else{
			burrType = new DiskType(DiskMaterial.DOUGH.withColor(Color.MAGENTA.brighter()));
		}
		burrs = new Disk[16];
		for(int i = 0; i < 16; i++){
			Disk b = this.disk.attachDisk(Math.PI/8.0 * i, radius/8, burrType);
			b.setZLevel(1);
			b.setEventHandler(new BurrEventHandler(env, b, handtype, arm, this));
			burrs[i] = b;
		}
		this.arm = arm;
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
		return radius + radius/4;
	}

	@Override
	public void remove() {
		//If the object is removed, every disk attached to the hand is split from the hand
		for(Disk b : burrs){
			((BurrEventHandler) b.getEventHandler()).split();
		}
		env.getDiskComplexesEnsemble().removeDiskComplex(disk.getDiskComplex());
	}

	@Override
	public Disk getDisk() {
		return disk;
	}
	
	/**
	 * If this method is called, every disk of the burrdock is marked as
	 * merged. Additionally the arm is informed about the hand-burrdock-
	 * contact and slowed down to avoid Diskworld-problems of overlapping
	 * disks
	 */
	public void merged(){
		for(Disk b : burrs){
			((BurrEventHandler) b.getEventHandler()).setMerged();
		}
		if(!merged){
			arm.slowDown();
			arm.setHandBurrDistance();
			merged = true;
		}
	}
	
	@Override
	public boolean isMerged(){
		return merged;
	}
	
	@Override
	public boolean hasMoved(){
		//Since the burrdock does not move on its own, this method always returns false 
		return false;
	}
}
