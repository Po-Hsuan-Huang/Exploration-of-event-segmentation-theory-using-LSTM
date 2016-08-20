

import java.util.Arrays;

import Jama.Matrix;

/**
 * Primitive Array Operation Package
 * */


public class PAO {
	
	static double[] MeanSquare = null;
    /**
     *  Main function is only for test purpose.
     * */
//	public static void main(String[] args) {
//		
//		double[] a = {1,2,3,4,5,6};
//		MeanSquare= new double[a.length];
//		double[] RMS = RMSquare(a);
//		System.out.println("RMS : " + Arrays.toString(RMS));
//		
//	}
//	private static double[] RMSquare(double[] a){
//		
//		double[] prod = PAO.prod(a, a);
//		double[] scprod = PAO.scalarprod(prod, 0.1);
//		double[] scprod2 = PAO.scalarprod(MeanSquare, 0.9);
//		double[] plus = PAO.sum(scprod,scprod2);
//		MeanSquare = plus;
//		double[] RMS = PAO.sqrt(MeanSquare); 
//		return RMS;
//		
//	} 
	
	
	
	/**
	 *  The function is a imitation of MatLab function linspace(init, end, step)
	 */
	private int[] linspace(int init, int end, int step){
		int len = (end-init)/step + 1;
		int counter = 0;
		int[] line = new int[len];
		for(int i =init; i<=end; i+=step){ 
			line[counter]=init+i*step;
			counter++ ;
		}
		return line;
	}
	
	/**
	 * 
	 * Sum of the 1D array elements
	 * 
	 */
	public static double sum(double[] A){
		double sum  = 0;
		for(int i=0; i<A.length; i++){ sum+=A[i];}
		return sum;
	}
	
	/**
	 * element-wise sum
	 * */
	public static double[] sum( double[] A, double[] B){
		
		double[] Sum =null;
	
		Matrix C = new Matrix(A, 1);
		Matrix D = new Matrix(B,1);
		Matrix S = C.plus(D);
		Sum = S.getColumnPackedCopy();		
		return Sum;
	}

	/**
	 * element-wise product A*B
	 * */
	public static double[] prod(double[] A , double[] B){
		double[] prod =null;
		Matrix C = new Matrix(A, 1);
		Matrix D = new Matrix(B,1);
		Matrix S = C.arrayTimes(D);
		prod = S.getColumnPackedCopy();		
		return prod;
	}
	

	/**
	 * element-wise product s*A
	 * */
	public static double[] scalarprod(double[] A , double s){
		double[] prod =null;
		Matrix C = new Matrix(A, 1);
		Matrix S = C.timesEquals(s);
		prod = S.getColumnPackedCopy();		
		return prod;
	}

	/**
	 * element-wise square root
	 * */
	public static double[] sqrt(double[] A){
		double[] sqrt =new double[A.length];
		for(int i = 0; i< A.length; i++){
			sqrt[i] = Math.sqrt(A[i]);
		}
		return sqrt;
	}
}
