package EventSegmentationScenario;

import diskworld.Disk;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.CollisionEventHandler;
import diskworld.linalg2D.Point;

/**
 * CollisionEventHandler that states what happens if the fly object has
 * a collision with another disk
 * 
 * @author Christian Gumbsch
 *
 */
public class FlyEventHandler implements CollisionEventHandler {
	
	private Fly fly;
	private Arm arm;
	/**Has the object been touched and moved through arm contact before?*/
	private boolean hasMoved = false;
	
	
	public FlyEventHandler (Fly fly, Arm arm){
		this.fly = fly;
		this.arm = arm;
	}

	@Override
	public void collision(CollidableObject collidableObject,
			Point collisionPoint, double exchangedImpulse) {
		
		if(collidableObject instanceof Disk){
			//If the fly object collides with a disk of the arm
			if(((Disk) collidableObject).belongsTo(arm.getDiskComplex())){
				//The arm is informed about the collision and slowed down to avoid
				// Diskworld-internal problems of overlapping disks
				arm.setHapticFeedback( fly.getDisk().getDiskComplex().getSpeedx(), fly.getDisk().getDiskComplex().getSpeedy());
				arm.slowDown();
				arm.increaseMaxNrOfModels();
				hasMoved = true;
			}
		}
		
		
	}
	
	/**
	 * Has the object been touched and moved through arm contact before?
	 * @return boolean, stating if the object moved
	 */
	public boolean hasMoved(){
		return hasMoved;
	}

}
