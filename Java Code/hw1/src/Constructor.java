class Machine{
	
	/*Constructors differs from methods.constructor has no return type, and the name has to be
	 the same as the class.
	 it prepares objects for use. And permit overloading.
	 That is, there can be several constructors. 
	 
	 */
	private String name;
	private int code;
	// we can have multiple constructors with same names as
// long as the parameters are different types.
	public Machine(){
		this("Arnie",0); // call a constructor inside a
		// constructor that accepts name and code.
		// calling the third constructor.
		System.out.println("Constructor running.");
		name = "Arnie.";
	}
	public Machine(String name){
		this(name,0);// calling the third constructor.
		System.out.println("Second constructor running.");
		this.name  = name;
	}
	public  Machine(String name, int code) {
		this.name = name;
		this.code =code;
		System.out.println("Third constructor running.");

	}
	
}



public class Constructor {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Machine machine1 = new Machine();
		//evoke the machine running, therefore create an object.
		Machine machine2 = new Machine("Bartie");
		// run constructor accepts string
		Machine machine3 = new Machine("Charlie",1);
		// run constructor accepts name and code.

	}

}
