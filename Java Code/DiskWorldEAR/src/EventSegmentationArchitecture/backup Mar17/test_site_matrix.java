package EventSegmentationArchitecture;

import java.util.Arrays;

import Jama.LUDecomposition;
import Jama.Matrix;


public class test_site_matrix {

	public static void main(String[] args)
    {
    	
    	  Matrix input = new Matrix(100,1);
    	  input =null;
    	  System.out.println(input.toString());
    	System.out.println(" LUDecomposition \n Ax = b \n  A, b are known, x = ? ");
    	
        double [][] values = {{1, 1, 2}, {2, 4, -3}, {3, 6, -5}};  // each array is a row in the matrix
        double [] rhs = { 9, 1, 0 }; // rhs vector
        double [] answer = { 1, 2, 3 }; // this is the answer that you should get.
        System.out.println("A :");
        Matrix a = new Matrix(values);
        a.print(10, 2);
        LUDecomposition luDecomposition = new LUDecomposition(a);
        System.out.println("Lower triangle L");
        luDecomposition.getL().print(10, 2); // lower matrix
        System.out.println("upper triangle U");
        luDecomposition.getU().print(10, 2); // upper matrix
        System.out.println("Recover the Matrix L*U ");
        Matrix L = luDecomposition.getL();
        Matrix U = luDecomposition.getU();
        Matrix R = L.times(U);
        R.print(10,2);
        
        Matrix b = new Matrix(rhs, rhs.length);
        Matrix x = luDecomposition.solve(b); // solve Ax = b for the unknown vector x
        System.out.println("solution of LUD:");
        x.print(10, 2); // print the solution
        Matrix residual = a.times(x).minus(b); // calculate the residual error
        double rnorm = residual.normInf(); // get the max error (yes, it's very small)
        //System.out.println("residual: " + rnorm);
        
        System.out.println("solution of inverse");
        Matrix inverse_a = a.inverse();
        Matrix d = inverse_a.times(b);
        d.print(10, 2);
  
    }

	/**
	 * print the content of the array instead of hashcode, which is effectively
	 * eqauivalent to id.
	 */
	private void printArray(Object[] array) {

		double[][] k = d.getArrayCopy();
		System.out.println(Arrays.deepToString(k));
		double[] motorCommand = null;
		double[] com = d.getColumnPackedCopy();
		System.out.println(Arrays.toString(com));

	}
}
