package Hiwi;

import diskworld.Disk;
import diskworld.environment.Wall;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.CollisionEventHandler;
import diskworld.linalg2D.Point;

public class FlyEventHandler implements CollisionEventHandler {
	
	private Fly fly;
	private Arm arm;
	private boolean hasMoved = false;
	public FlyEventHandler (Fly fly, Arm arm){
		this.fly = fly;
		this.arm = arm;
	}

	@Override
	public void collision(CollidableObject collidableObject,
			Point collisionPoint, double exchangedImpulse) {
		
		if(collidableObject instanceof Disk){
			if(((Disk) collidableObject).belongsTo(arm.getDiskComplex())){
				arm.setHapticFeedback( fly.getDisk().getDiskComplex().getSpeedx(), fly.getDisk().getDiskComplex().getSpeedy());
				arm.slowDown();
				if(!hasMoved){
					arm.setTimeForInteraction();
					arm.predictModels();
				}
				hasMoved = true;
				//arm.freezeArm();
			}
		}
		
		
	}
	
	public boolean hasMoved(){
		return hasMoved;
	}

}
