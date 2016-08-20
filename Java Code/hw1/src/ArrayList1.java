import java.awt.List;
import java.util.ArrayList;
public class ArrayList1 {
	

	public static void main(String[] args) {
		/**For the first two constructors, an empty array list is created.
			The initial capacity is ten unless explicitly specified by using
			 the second constructor.*/
		// A boolean is stored and then retrieved
		    ArrayList<Boolean> list = new ArrayList<Boolean>();
		    list.add(true);
		    boolean flag = list.get(0);		
	//
		    
		        // ArrayList is a resizable 1D array that implements List interface.
		    // ArrayList belongs to collection class, while array belongs to Arrays
		        ArrayList<String> cent2 =  new ArrayList<String>();
		        cent2.add("A");
		        cent2.add("B");
		        cent2.add("C");
		        System.out.println(cent2);
		        // After creation, the length of an array is fixed.
		        String[] cent3 = {"A","B","C"};
		        System.out.println(cent3);
		        boolean s =cent3.equals(cent2);
		        
		        System.out.println(s);
		        
		      
	}

}
