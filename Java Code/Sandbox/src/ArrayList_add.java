import java.util.ArrayList;
import java.util.Arrays;

public class ArrayList_add {
	public static void main(String[] args) {
		ArrayList<String> adday = new ArrayList<String>(10);
		for (int i = 0; i < 100; i++) {
			String Str = String.format("The first string is %d \n", i);
			adday.add(Str);
		}
		System.out.println(adday.toString());
	
	
	
	final double[] data =new double[10];
	String s = data.getClass().getSimpleName();
	System.out.println(s);
	}
}
