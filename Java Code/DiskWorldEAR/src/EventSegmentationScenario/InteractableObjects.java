package EventSegmentationScenario;

import diskworld.Disk;


public interface InteractableObjects {
	
	public int getType();
	public double getX();
	public double getY();
	public double getAngle();
	public double getSize();
	public void remove();
	//public boolean toBeRemoved();
	public Disk getDisk();
	public boolean isMerged();
	public boolean hasMoved();
	
}