// extends makes Car inherit all methods and variables inside MachineGun.
public class Car extends MachineGun{ 
	// Class Variables that will be used by methods

	protected  float direction;
	protected  float speed;
	
	// setMethods to encapsulate variables
	public void setDirection(float direction){
		this.direction = direction;
	}	
	
	// set direction to encapsulate variables
	public void setSpeed(float speed){
		this.speed = speed;
	}	
	public String drive() {
				
		return String.format(" go %.3f degree with %.3f m/s", direction, speed);
		
	}
	
}
