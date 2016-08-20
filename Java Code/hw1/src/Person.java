import java.io.Serializable;

public class Person implements Serializable {

	
	private static final long serialVersionUID = 5305425788823406005L;
	private int id;
	private String name;
	
	public Person(int id, String name) {
		this.id = id;
		this.name = name;
	}


	@Override
	public String toString() {
		return "Person [id=" + id + ", name = '" + name + "]";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
