package EventSegmentationScenario;

import diskworld.DiskType;
import diskworld.Environment;

/**
 * Implementation of the heavy burrdock.
 * Implemented as a burrdock with pink color and a different type (100)
 * Referred to as a heavy food-object in the paper.
 * 
 * @author Christian Gumbsch
 *
 */
public class HeavyBurrdock extends Burrdock{

	public HeavyBurrdock(Environment env, double x, double y, double angle,
			DiskType handtype, Arm arm) {
		super(env, x, y, angle, handtype, arm, false);
	}
	
	@Override
	public int getType(){
		return 100;
	}

}
