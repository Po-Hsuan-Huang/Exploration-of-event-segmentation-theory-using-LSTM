
public class Ball implements Cloneable{
	// location of the ball
	private static double loc; 
	// velocity of the ball per step.
	private static double vel;
	private static boolean start =  false;

	
	public Ball(double loc, double vel){
		setLoc(loc);
		setVelocity(vel);
	}
	
	
	public void start(){
		setStart(true);
	}
	
	public void stop(){
		setStart(false);
	}
	
	public void fly(){
		loc += vel;
		
	}
	
	public void bounce(){
		vel = -vel;
	}
	public static void setLoc(double location){
		loc=location;
	}
	public double getLoc(){
		return loc;
	}
	
	public double getVelocity(){
		return vel;
	}
	
	public static void setVelocity(double velocity){
		vel = velocity;
	}

	public boolean isStart() {
		return start;
	}

	public static void setStart(boolean start) {
		Ball.start = start;
	}
	

	
	
	
	
}
