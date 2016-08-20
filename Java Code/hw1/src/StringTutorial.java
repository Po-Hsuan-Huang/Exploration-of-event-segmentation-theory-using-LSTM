

public class StringTutorial { 
	/* Class name can not be called String 
	Otherwise the machine would not know which string are you 
	referring to in the following context.
	*/

	public static void main(String[] args) {
		// inefficient
		String info = " ";// create an empty string
		/*
		 Creating new strings behind scenes, not concatanating as it appears to be.
		 the built strings can never be changed, e.g. they are immutable.
		 */
		info = "Using += to append string";
		info = " is ";
		info = "very inefficient and waste memory";
		
		System.out.println(info);
		////////////   String Builder       ///////////
		StringBuilder ab = new StringBuilder(); // initiate a new object
		
		ab.append("Using StringBuilder method.");
		ab.append(" is ");
		ab.append(" more efficient");
		
		System.out.println(ab.toString());
		
		StringBuilder s = new StringBuilder();
		
		s.append(" .append");
		s.append(" is ");
		s.append(" supreme.");
		
		System.out.println(s.toString());
		
		//////////// Formatting////////////
		
		System.out.println("Here is some text.\t Here is a tab.\n Here is a new line.   ");
	
		System.out.printf("Totoal cost %-10d; quantity is %d\n", 5,120);
		for(int i =0; i<20; i++){
		System.out.printf("%-2d : %s\n", i ,"here is some text");
		}
		// the minus sign of %-2d designate the allignment of the text.

		//formating float point value
		System.out.printf("Total value: %.2f\n" , 5.7892);
		System.out.printf("Total value: %6.1f\n", 23232323.22);
		// the integer part of %6.1f designate the total length of the characters.
	
	}
}
