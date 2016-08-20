
public class Stairs {
	// location of the ball
	private static double loc; 
	// velocity of the ball per step.
	private static double size = 1;
	private static boolean start =  false;
	private static double floor = 0;
	private static double ceiling = 1;
	
	
	
	public Stairs(double loc, double size, double ceiling, double floor){
		setLoc(loc);
		setStepSize(size);
		setCeiling(ceiling);
		setFloor(floor);
	}
	
	
	public void setFloor(double floor) {
		Stairs.floor = floor;
	}
	
	public double getFloor(){
		return Stairs.floor;
	}

	public void setCeiling(double ceiling) {
		Stairs.ceiling = ceiling;
	}
	
	public double getCeiling(){
		return Stairs.ceiling;
	}
	


	public void start(){
		setStart(true);
	}
	
	public void stop(){
		setStart(false);
	}
	
	private int counter =0;
	public void stepup(){
		 loc += size;
		 
		 
	//  For 0,0,0,0,1 sequence test	 
//		double loc= getLoc();
//		
//		if(counter<4){
//			setLoc(0);
//			counter += 1;
//		}
//		
//		
//		if(counter==4){
//			setLoc(1);
//			counter=0;
//			}
		

	}
	
	public void fall(){
		loc = floor;
		
	}
	public static void setLoc(double location){
		loc=location;
	}
	public double getLoc(){
		return loc;
	}
	
	public double getStepSize(){
		return Stairs.size;
	}
	
	public static void setStepSize(double size){
		Stairs.size = size;
	}

	public boolean isStart() {
		return start;
	}

	public static void setStart(boolean start) {
		Stairs.start = start;
	}
}
