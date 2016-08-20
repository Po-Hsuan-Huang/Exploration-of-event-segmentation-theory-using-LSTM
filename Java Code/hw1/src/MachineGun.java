
public class MachineGun {
	// variables
	
	private int MerchantId = 123;// can only be used inside class
	protected String Info = "some_strings."; // can only be access by the class and its children.
	static String id = "MachineGun";
	
	// subroutines
	public String start(){
		return String.format("%s started", id);
	}
	public String stop(){
		return String.format("%s stopped", id);
	}
}
