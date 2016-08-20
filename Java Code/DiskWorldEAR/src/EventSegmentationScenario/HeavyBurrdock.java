package EventSegmentationScenario;

import diskworld.DiskType;
import diskworld.Environment;

public class HeavyBurrdock extends Burrdock{

	public HeavyBurrdock(Environment env, double x, double y, double angle,
			DiskType handtype, Arm arm) {
		super(env, x, y, angle, handtype, arm, false);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int getType(){
		return 100;
	}

}
