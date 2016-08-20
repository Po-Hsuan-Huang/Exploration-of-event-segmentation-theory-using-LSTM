package EventSegmentationScenario;

import diskworld.Disk;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.CollisionEventHandler;
import diskworld.linalg2D.Point;

/**
 * An EventHandler that states what happens to a Burrdock
 * when colliding with a different disk.
 * 
 * @author Christian Gumbsch
 *
 */
public class BurrEventHandler implements CollisionEventHandler {
	
	/**The disk type to which a burrdock can be attached*/
	private DiskType handtype;
	/**The disk holding this CollisionEventHandler*/
	private Disk owner;
	/** The object to which the owner-disk belongs to*/
	private Burrdock object;
	/**Is the burrdock attached to the hand?*/
	private boolean merged = false;
	/**The disk to which the owner-disk is attached to*/
	private Disk mergedWith;
	/**Diskworld environment and arm*/
	private Environment env;
	private Arm arm;
	
	
	/**
	 * Constructor
	 * @param env, diskworld environment
	 * @param owner, owner-disk of this CollisionEventHandler
	 * @param handtype, type of attachable disks 
	 * @param arm, arm of the simulation
	 * @param object, burrdock the owner-disk belongs to
	 */
	public BurrEventHandler (Environment env, Disk owner, DiskType handtype, Arm arm, Burrdock object){
		this.owner = owner;
		this.handtype = handtype;
		this.env = env;
		this.arm = arm;
		this.object = object;
	}

	@Override
	public void collision(CollidableObject collidableObject,
			Point collisionPoint, double exchangedImpulse) {
		if(collidableObject instanceof Disk){
			//If the object is not yet attached to a disk and the disk it collided with is attachable
			if(!merged && ((Disk) collidableObject).getDiskType().equals(handtype)){
				//The arm is informed about this contact
				arm.setHapticFeedback(owner.getDiskComplex().getSpeedx(), owner.getDiskComplex().getSpeedy());
				arm.increaseMaxNrOfModels();
				env.getDiskComplexesEnsemble().mergeDiskComplexes(owner, (Disk)collidableObject);
				mergedWith = (Disk) collidableObject;
				merged = true;
				//The burrdock is informed about the merging
				object.merged();
			}
		}
	}
	
	/**
	 * If this disk is attached to another disk, split the two disks
	 */
	public void split(){
		if(mergedWith != null){
			mergedWith.getDiskComplex().split(mergedWith, owner);
		}
	}
	
	/**
	 * If another disk of the burrdock is attached to the hand, then this disk is
	 * attached as well
	 */
	public void setMerged(){
		this.merged = true;
	}
}
