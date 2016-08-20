

class Frog {
	
	// it is called encapsulation cause we 
	// hide the variables inside the class.
	
	private String name; // variable only visible inside Frog
	private int age;// that's what private does.
	public void setName(String name){
		this.name = name;	
		// this refers to the variable in the current class.
		// this disambiguate the variables of different classes. 
	}
	public void setAge(int age){
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public int getAge(){
		return age;
	}
	
	
	
}





public class Setter {

	
	public static void main(String[] args) {

		Frog frog1 = new Frog();
		// What we used to do:
		//frog1.name= "Bard"
		//frog1.age = 24;
		
		frog1.setName("Bard");
		frog1.setAge(24);
		
		System.out.println("My name is " + frog1.getName() + ",my age is "+frog1.getAge());
		
				
	}

}
