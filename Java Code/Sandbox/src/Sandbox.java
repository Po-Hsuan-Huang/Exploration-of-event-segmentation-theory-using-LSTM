import de.jannlab.math.Matrix;

public class Sandbox {

	public static void main(String[] args) {
		/**
		 * PCoordinate c1 = new PCoordinate(5, 5); PCoordinate c2 = new
		 * PCoordinate(2, 3);
		 * 
		 * c1.multiply(c2); c2 = PCoordinate.multiply(c1, c2);
		 * PCoordinate.increaseByOne(c1, c2);
		 * System.out.println(String.format("%d, %d", c1.getX(), c1.getY()));
		 * System.out.println(String.format("%d, %d", c2.getX(), c2.getY()));
		 */
		int a = 100;
		int b = 5;
		Matrix m = new Matrix(a, b);
		for (int row = 0; row < a; row++) {
			for (int col = 0; col < b; col++) {
				m.set(row, col, 10*row + col);
			}
		}
		Matrix ans = m.getRow(2);
		System.out.println("getRow(1) : "+m.toString());
		System.out.println("getRow(1) : "+ans.toString());


	}

}
