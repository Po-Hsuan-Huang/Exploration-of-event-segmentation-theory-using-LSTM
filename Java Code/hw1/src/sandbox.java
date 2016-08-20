import java.util.ArrayList;
import java.util.Arrays;

public class sandbox {
	public static void main(String[] args) {

		ArrayList<Double> sample = new ArrayList<Double>();
		double[] a = { 1, 2, 4, 5, 6, 7, 8, 8, 9, 0, 1 };

		for (double i : a) {
			sample.add(i);
			
			System.out
					.println("samples : " + Arrays.toString(sample.toArray()));
		}
	}
}
