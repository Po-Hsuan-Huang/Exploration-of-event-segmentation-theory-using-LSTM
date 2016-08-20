import java.util.ArrayList;
import java.util.Arrays;

public class ArrayConversion {

	static int len;
	private static int[] a1;

	public static void main(String[] args) {

		int[][] a2 = { { 1, 2, 4 }, { 6, 7, 9 }, { 0, 1, 2 } };
		System.out.println(Arrays.deepToString(a2));
		len = a2.length;
		a1 = new int[len*a2[0].length];
		System.out.println(len);
		
		for (int row = 0; row < len; row++) {
			for (int col = 0; col < a2[row].length; col++) {
			 a1[row*len+col] = a2[row][col];
			}
		}
		System.out.println(Arrays.toString(a1));
	}
}
