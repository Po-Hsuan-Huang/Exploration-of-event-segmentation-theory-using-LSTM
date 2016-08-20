package Hiwi;

import java.awt.Color;

import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actuators.EmptyActuator;
import diskworld.interfaces.Actuator;
import diskworld.visualization.CircleDiskSymbol;
import diskworld.visualization.PolygonDiskSymbol;

public class Burrdock implements InteractableObjects {
	
	private int type = 0;
	private Disk disk;
	private Disk[] burrs;
	private double radius = 3.0;
	private Environment env;
	Arm arm;
	boolean merged = false;

	
	public Burrdock (Environment env, double x, double y, double angle, DiskType handtype, Arm arm){
		this(env, x, y, angle, handtype, arm, true);
	}
	
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
		for(Disk b : burrs){
			((BurrEventHandler) b.getEventHandler()).split();
		}
		env.getDiskComplexesEnsemble().removeDiskComplex(disk.getDiskComplex());
		
	}

	@Override
	public Disk getDisk() {
		return disk;
	}
	
	public void merged(){
		for(Disk b : burrs){
			((BurrEventHandler) b.getEventHandler()).setMerged();
		}
		if(!merged){
			arm.slowDown();
			arm.setTimeForInteraction();
			arm.setHandBurrDistance();
			merged = true;
			arm.predictModels();
		}
		
	}
	
	public boolean isMerged(){
		return merged;
	}
	
	public boolean hasMoved(){
		return false;
	}
}
