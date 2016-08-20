package EventSegmentationScenario;

import diskworld.Disk;

/**
 * Object that the arm can interact with
 * 
 * @author Christian Gumbsch
 *
 */
public interface InteractableObjects {
	/**
	 * Getter for the type of the object
	 * @return Integer in {0, 100, 200} representing the object's type
	 */
	public int getType();
	
	/**
	 * Getter for the disk which is the main part of the object
	 * @return main disk of the object 
	 */
	public Disk getDisk();
	
	/**
	 * Getter for object's position x-wise
	 * @return x-coordinate of object's main disk
	 */
	public double getX();
	
	/**
	 * Getter for object's position y-wise
	 * @return y-coordinate of object's main disk
	 */
	public double getY();
	
	/**
	 * Getter for object's orientation
	 * @return angle describing the orientation of the object's main disk
	 */
	public double getAngle();
	
	/**
	 * Getter for object's size
	 * @return radius of object
	 */
	public double getSize();
	
	/**
	 * Remove this object from the simulation
	 */
	public void remove();
	
	/**
	 * Checks if the object is attached to the arm
	 * @return boolean stating if the object is merged
	 */
	public boolean isMerged();
	
	/**
	 * Checks if the object has moved since it's creation
	 * @return boolean describing if the object has moved
	 */
	public boolean hasMoved();
}