
import java.util.*;
public class do_while {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

// Solution 1		
		System.out.println("enter a number:");
		int value = scanner.nextInt();
		while(value !=5){
			System.out.println("Enter a number:");
			value= scanner.nextInt();
	
		}
		System.out.println("Got 5!");
		
// Solution 2
		int value = 0;
		while(value !=5){
			System.out.println("Enter a number:");
			value= scanner.nextInt();
		}
		System.out.println("Got 5!");
		
// Solution 3
		
		do{
			System.out.println("Enter a number:");
			value= scanner.nextInt();
		}
		while(value !=5);
		// Do while loop evaluate the condition at the end,
		// which is different from while, which evaluate at the begining.

		
		System.out.println("Got 5 ! ");
}
}
