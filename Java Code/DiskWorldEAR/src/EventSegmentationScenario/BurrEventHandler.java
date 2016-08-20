package EventSegmentationScenario;

import diskworld.Disk;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.interfaces.CollidableObject;
import diskworld.interfaces.CollisionEventHandler;
import diskworld.linalg2D.Point;

public class BurrEventHandler implements CollisionEventHandler {
	
	DiskType handtype;
	Disk owner;
	Disk mergedWith;
	Environment env;
	Arm arm;
	boolean merged = false;
	Burrdock object;
	
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

			if(!merged && ((Disk) collidableObject).getDiskType().equals(handtype)){
				//System.out.println("MERGE!");

				arm.setHapticFeedback(owner.getDiskComplex().getSpeedx(), owner.getDiskComplex().getSpeedy());
				env.getDiskComplexesEnsemble().mergeDiskComplexes(owner, (Disk)collidableObject);
				mergedWith = (Disk) collidableObject;
				merged = true;
				object.merged();
			}
		}
	}
	
	public void split(){
		if(mergedWith != null){
			mergedWith.getDiskComplex().split(mergedWith, owner);
		}
	}
	
	public void setMerged(){
		this.merged = true;
	}
}
