class Person2{
	String name;
	int age;
	
	void speak(){
		System.out.println("My name is "+ name);
	}
	
	void calculateYearsToRetirement(){
		int yearsLeft = 65- age;
		System.out.println(yearsLeft);
	}
	int intYearsToRetirement(){
		int yearsLeft = 65- age;
		return yearsLeft	;
	}
	int getAge(){
		return age;
	}
	
	String getName(){
		return name;
	}
}

public class Methods {

	public static void main(String[] args) {
		Person2 person1 =  new Person2(); // create a new objects, ends the object wtih ().
		person1.name = "Joe Adams";
		person1.age = 35;
		// person1.speak();
		person1.calculateYearsToRetirement();
		int retire = person1.intYearsToRetirement();
		System.out.println("Years until retirement: "+retire);
		// encapsulate the values.
		int age = person1.getAge();
		String name = person1.getName();
		System.out.println("My age is "+age);
		System.out.println("My Name is "+name);

	}

}
