
import	java.util.*;
public class Switch {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		String text;
	do{
		System.out.println("enter a command");
		text = input.nextLine();
		
		switch (text) {
			case "start":
				System.out.println("Machine started");
				break;
			case "stop":
				System.out.println("Machine stopped.");
				break;
			default :
				System.out.println("command not recognized.");
		}
	} while( ! text.equals("stop")); 
	
		
		
	}

}
