

import java.util.Scanner;

public class input_output {
	public static void main(String[] args){
		// Create scanner object
		Scanner input = new Scanner(System.in);
		// output the prompt
		System.out.println("What is your nationality ?");
		
		// Wait for the user to enter something.
		String value = input.next();
		
		System.out.println("what is your age ?");
		int value2 = input.nextInt();
		//Tell them what they entered.
		System.out.println("You are a "+ value2 + " years old "+value+"ese.");
		
	}
}

