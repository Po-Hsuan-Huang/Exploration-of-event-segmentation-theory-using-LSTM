

class Thing {
	// the variable is attached to instance.
	public String name;
	// static constant is attached to the class
	// and cannot be reassign values.
	public static String description;
	public static int count = 0;

	public void showName() {
		System.out.println(description + ":" + name);
	}

	// static method attached to class, not the instance.
	public static void showInfo() {
		System.out.println(description);
	}

	public int id;

	public Thing() {
		id = count;
		count++;
	}

}

public class Static {

	public static void main(String[] args) {
		Thing.description = "I am a thing.";
		System.out.println(Thing.description);
		System.out.println("Before creating objects: " + Thing.count);
		Thing thing1 = new Thing();
		Thing thing2 = new Thing();
		System.out.println("After creating objects: " + Thing.count);

		thing1.name = "Bob";
		thing2.name = "Sue";

		thing1.showName();
		thing2.showName();

	}
}
