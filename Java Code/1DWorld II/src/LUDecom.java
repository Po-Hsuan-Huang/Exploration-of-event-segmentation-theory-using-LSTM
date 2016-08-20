
import Jama.Matrix;
import Jama.LUDecomposition;
public class LUDecom {
	 double[] decomposition(double[][] A, double[] b){
		
		Matrix A_ = new Matrix(A);
		LUDecomposition luDecomposition = new LUDecomposition(A_);
		luDecomposition.getL();//lower matrix;
		luDecomposition.getU(); // upper matrix;
		
		Matrix b_ = new Matrix(b, b.length );
		Matrix x_ = luDecomposition.solve(b_); // solve Ax = b for unknow vector x;
		
		Matrix residual = A_.times(x_).minus(b_); // calculate the residual error
        @SuppressWarnings("unused")
		double rnorm = residual.normInf(); // get the max error (yes, it's very small)
        
        
        double[] predictedCommand = x_.getColumnPackedCopy();
        
		return predictedCommand;
		
	}
}
