package EventSegmentationScenario;

import java.awt.Color;

import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actuators.EmptyActuator;
import diskworld.interfaces.Actuator;
import diskworld.visualization.PolygonDiskSymbol;

public class Fly implements InteractableObjects {
	
	private int type = 200;
	private Disk disk;
	private double radius = 3.5;
	private Environment env;
	private FlyEventHandler fev;
	
	public Fly(Environment env, double x, double y, Arm arm){
		this.env = env;
		Actuator emptyActuator = new EmptyActuator("Fly", new PolygonDiskSymbol(null).getSquareSymbol(0.5));
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
	
	public boolean hasMoved(){
		return fev.hasMoved();
	}
	

}