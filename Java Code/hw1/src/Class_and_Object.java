class Person{
	
	
	// declaring variable here to avoid confusion.
	String name;
	int age;
	// Classes can contain 
	
	// 1.data
	
	//2. Subroutines(methods)
	void speak()	{
		for (int i =0; i<1;i++){
		System.out.println("My name is "+ name);
		System.out.println("I am "+ age +" years old.");
		}
	}
	
	void sayHello(){
		System.out.println("hello!");
	}
	
}



public class Class_and_Object {

	public static void main(String[] args) {
		Person person1 =  new Person(); // create a new objects, ends the object wtih ().
		person1.name = "Joe Adams";
		person1.age = 35;
		person1.speak();
		Person person2 = new Person();
		person2.name = "Daisey Lorenz";
		person2.age = 105;
		person2.speak();
		System.out.println(person1.name);
		
	}

}
