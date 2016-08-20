package EventSegmentationArchitecture;

import java.util.Arrays;
import java.util.LinkedList;
import Jama.Matrices;
import Jama.Matrix;



/**
 * Linear prediction using recursive least squares updates.
 * 
 * @author Patrick O. Stalph, Martin V. Butz, Christian Gumbsch
 */
public class LinearRLSModel implements ForwardModel {
	/** input dimension */
	private int dimIn;
	/** output dimension */
	private int dimOut;
	/** <tt>m*x+c</tt> or <tt>m*x</tt> only? */
	private boolean intercept;
	/** initial gain for the inverse covariance matrix */
	private double initialGain;
	/** forget rate in (0,1] */
	private double lambda;

	/** [dimOut][dimIn+intercept?] */
	public double[][] weights;
	/** inverted covariance matrix [dimIn+intercept?][dimIn+intercept?] */
	public double[][] gainMatrix;
	/** holds the prediction to avoid mem-alloc and multiple computations */
	public double[] prediction;

	// arrays for temporary storage to avoid mem alloc.
	public double[] extPredInput;
	public double[] gainVector;
	public double[][] tmpMatrix1;
	public double[][] tmpMatrix2;
	
	//error statistics
	public int errorUpdates = 0;
	public double meanError = 0.0;
	public double errorVariance = 0.0;
	public LinkedList<Double> errors = new LinkedList<Double>();
	public int errorsSize = 1000; //1000
	
	int tooBigValuesThreshold = 100;

	/**
	 * Alternative constructor, which is not used for XCSF (included for
	 * backwards compatibility with other experiments).
	 * 
	 * @param dimIn
	 *            number of inputs
	 * @param initialPrediction
	 *            initial prediction
	 * @param intercept
	 *            also learn an offset?
	 * @param initialGain
	 *            the initial gain for updates (higher value means faster
	 *            adaptation)
	 * @param lambda
	 *            forget rate in <tt>(0,1]</tt>, where <tt>1</tt> means no
	 *            forgetting
	 */
	public LinearRLSModel(final int dimIn,
			final double firstSensation, final boolean intercept,
			final double initialGain, final double lambda) {
		this(dimIn, 1, intercept, initialGain, lambda);
		double[] initialPrediction = {firstSensation};
		if (intercept) {
			// initialize weights
			for (int p = 0; p < dimOut; p++) {
				// last weight is the intercept
				weights[p][dimIn] = initialPrediction[p];
			}
		}
		// init gainMatrix
		this.initializeGainMatrix();
	}

	/**
	 * Common private constructor.
	 * 
	 * @param dimIn
	 *            number of inputs
	 * @param dimOut
	 *            number of outputs
	 * @param intercept
	 *            <code>true</code> if an offset weight should be learned;
	 *            <code>false</code> otherwise
	 * @param initialGain
	 *            the initial gain for updates
	 * @param lambda
	 *            the forget rate in <tt>(0,1]</tt>
	 */
	private LinearRLSModel(final int dimIn, final int dimOut,
			final boolean intercept, final double initialGain,
			final double lambda) {
		if (dimIn < 1 || dimOut < 1 || initialGain < 0 || lambda <= 0
				|| lambda > 1) {
			throw new IllegalArgumentException("illegal arguments for RLS");
		}
		this.intercept = intercept;
		this.initialGain = initialGain;
		this.lambda = lambda;
		this.dimIn = dimIn;
		this.dimOut = dimOut;
		int length = dimIn + (intercept ? 1 : 0);
		weights = new double[dimOut][length];
		gainMatrix = new double[length][length];
		prediction = new double[dimOut];
		// temporary arrays
		extPredInput = new double[length];
		if (intercept) {
			extPredInput[dimIn] = 1;
		}
		gainVector = new double[length];
		tmpMatrix1 = new double[length][length];
		tmpMatrix2 = new double[length][length];
	}

	/**
	 * Initializes the gain matrix for this RLS prediction.
	 */
	private void initializeGainMatrix() {
		final int length = intercept ? dimIn + 1 : dimIn;
		for (int row = 0; row < length; row++) {
			for (int col = 0; col < length; col++) {
				gainMatrix[row][col] = (row != col) ? 0 : initialGain;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xcsf.classifier.Prediction#predict(double[])
	 */
	public double[] predict(final double[] x) {
		for (int row = 0; row < dimOut; row++) {
			// intercept enabled? (last column)
			if (intercept) {
				prediction[row] = weights[row][dimIn];
			} else {
				prediction[row] = 0;
			}
			// multiply with weights
			for (int col = 0; col < dimIn; col++) {
				prediction[row] += weights[row][col] * x[col];
			}
		}
		return prediction;
	}
	
	public double[] inverseForwardModel(double P){
		double[] p = {P};
		Matrix m = new Matrix(weights);
		m = Matrices.pinv(m);
		if(m == null){ //When rank of matrix is < 1
			return null;
		}
		double[][] weightsInv = m.getArray();
		double[] x = new double[weightsInv.length];
		for(int i = 0; i < weightsInv.length; i++){
			for(int j = 0; j < weightsInv[i].length; j++){
				x[i] += weightsInv[i][j] * p[j];
			}
			
			if(Math.abs(x[i]) > tooBigValuesThreshold){ //Catch computational errors of inverted zero models
				return null;
			}
		}
		return x;
	}

	/**
	 * Updates the linear coefficients using the given prediction input and the
	 * actual function value.
	 * <p>
	 * PRECONDITION: {@link #predict(double[])} was called before to assure that
	 * the current prediction is up to date.
	 * <p>
	 * Given a new sample input <tt>x</tt> (extended by 1, i.e.
	 * <tt>[x_1,...,x_n,1]</tt>, if the intercept is learned, too) and the
	 * corresponding error, the recursive least squares technique is a
	 * three-step procedure. First, the kalman gain factor <tt>k</tt> is
	 * computed from the inverted covariance matrix <tt>P</tt>:
	 * 
	 * <pre>
	 * (1)   k = P x / (lambda + x' P x),
	 * </pre>
	 * 
	 * where <tt>lambda</tt> is the forget rate and <tt>x'</tt> denotes the
	 * transpose of <tt>x</tt>. Next, the linear weights <tt>w</tt> are updated
	 * using the computed gain <tt>k</tt> and the error
	 * <tt>e=f(x)-prediction</tt>:
	 * 
	 * <pre>
	 * (2)   w = w + k e
	 * </pre>
	 * 
	 * Finally, the inverse covariance matrix is updated.
	 * 
	 * <pre>
	 * (3)   P = P - k x^T P 
	 *       P = P/lambda
	 * </pre>
	 * 
	 * 
	 * @param x
	 *            the prediction input
	 * @param y
	 *            the actual function value at the given input
	 */
	public void update(final double[] x, final double s) {
		double[] y = {s};
		System.arraycopy(x, 0, extPredInput, 0, dimIn);
		int length = intercept ? dimIn + 1 : dimIn;
		// 1. determine Kalman gain vector: k = (P x / divisor),
		// where divisor = (lambda + x^T P x)
		matrixMultiplication(gainMatrix, extPredInput, gainVector, length);
		double divisor = lambda;
		for (int i = 0; i < length; i++) {
			divisor += extPredInput[i] * gainVector[i];
		}
		for (int i = 0; i < length; i++) {
			gainVector[i] /= divisor;
		}

		// 2. update coefficients w = (w + k*error),
		// where error = (functionValue - prediction)
		// Note, that "this.prediction" must be up to date at the moment!
		for (int k = 0; k < dimOut; k++) {
			double error = y[k] - prediction[k];
			for (int i = 0; i < length; i++) {
				weights[k][i] += gainVector[i] * error;
			}
		}

		// 3. update gainMatrix:
		// gainMatrix = (I - rank1(gainVector, extInput)) * gainMatrix
		for (int row = 0; row < length; row++) {
			for (int col = 0; col < length; col++) {
				double tmp = gainVector[row] * extPredInput[col];
				if (row == col) {
					tmpMatrix1[row][col] = 1.0 - tmp;
				} else {
					tmpMatrix1[row][col] = -tmp;
				}
			}
		}
		matrixMultiplication(tmpMatrix1, gainMatrix, tmpMatrix2, length);
		// divide gainMatrix entries by lambda
		for (int row = 0; row < length; row++) {
			for (int col = 0; col < length; col++) {
				gainMatrix[row][col] = tmpMatrix2[row][col] / lambda;
			}
		}
	}
	
	private static void matrixMultiplication (double[][] a, double[][] b, double[][] c, int n){
		for(int h = 0; h < n; h++){
			for(int i = 0; i< n; i++){
				double value = 0;
				for(int j = 0; j < n; j++){
					value += a[h][j] * b[j][i];
				}
				c[h][i] = value;
			}
		}		
	}
	
	private static void matrixMultiplication(double[][] a, double[] b, double[] c, int n){
		for(int i = 0; i < n; i++){
			double value = 0;
			for(int j = 0; j < n; j++){
				value += a[i][j] * b[j];
			}
			c[i] = value;
		}
	}
	
	public void updateError(double error){
		if(errors.size() >= errorsSize){
			errors.removeFirst();
		}

		errors.add(error);
		double tempMean = 0.0;
		double tempVariance = 0.0;
		for(int i = 0; i < errors.size(); i++){
			tempMean += errors.get(i);
		}
		this.meanError = tempMean / ((double)errors.size());
		for(int i = 0; i < errors.size(); i++){
			tempVariance += (errors.get(i) - meanError) *(errors.get(i) - meanError);
		}
		this.errorVariance = tempVariance/((double) errors.size());		
	}
	
	
	public double getMeanError(){
		return this.meanError;
	}
	
	public double getErrorVariance(){
		return this.errorVariance;
	}
	
	public LinearRLSModel copy(){
		LinearRLSModel clone = new LinearRLSModel(dimIn, prediction[0], intercept, initialGain, lambda);
		clone.dimIn = this.dimIn;
		clone.dimOut = this.dimOut;
		clone.lambda = this.lambda;
		clone.initialGain = this.initialGain;
		clone.intercept = this.intercept;
		

		int length = dimIn + (intercept ? 1 : 0);
		clone.weights = new double[dimOut][length];
		for(int i = 0; i < weights.length; i++){
			clone.weights[i] = this.weights[i].clone();
		}
		clone.gainMatrix = new double[length][length];
		for(int i = 0; i < gainMatrix.length; i++){
			clone.gainMatrix[i] = this.gainMatrix[i].clone();
		}
		clone.prediction = this.prediction.clone();
		
		clone.gainVector = this.gainVector.clone();
		
		clone.tmpMatrix1 = new double[length][length];
		clone.tmpMatrix2 = new double[length][length];
		for(int i = 0; i < tmpMatrix1.length; i++){
			clone.tmpMatrix1[i] = this.tmpMatrix1[i].clone();
			clone.tmpMatrix2[i] = this.tmpMatrix2[i].clone();
		}
		clone.extPredInput = this.extPredInput.clone();
		return clone;
	}
	
	
	
	public LinearRLSModel copyWithErrors(){
		LinearRLSModel clone = this.copy();
		clone.errorUpdates = this.errorUpdates;
		clone.errorVariance = this.errorVariance;
		clone.meanError = this.meanError;
		clone.errors = new LinkedList<Double>();
		for(Double e : this.errors){
			clone.errors.add(e);
		}
		clone.errorsSize = this.errorsSize;
		
		return clone;
	}	

	/*
	 * ----------------------------------------------------------------
	 * Methods not used by the Event Segmentation Architecture but XCSF
	 * ----------------------------------------------------------------
	 */
	
	
	/**
	 * Returns <code>true</code> if the prediction also adapts an intercept
	 * weight and <code>false</code> otherwise.
	 * 
	 * @return the intercept
	 */
	public boolean isIntercept() {
		return intercept;
	}

	/**
	 * Resets the gain matrix by adding the initial gain to the diagonal
	 * elements.
	 */
	public void resetGainMatrix() {
		final int length = intercept ? dimIn + 1 : dimIn;
		for (int i = 0; i < length; i++) {
			gainMatrix[i][i] += initialGain;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see xcsf.classifier.Prediction#getDimension()
	 */
	public int getDimension() {
		return dimOut;
	}

	/**
	 * Returns the weights of this linear RLS prediction. The first index (row)
	 * of the returned array corresponds to the output dimension, the second
	 * index (column) corresponds to the input dimension. If the intercept is
	 * enabled, it is found in the last column of the matrix (index equals input
	 * dimensionality).
	 * 
	 * @return a reference to the linear weights of this prediction, including
	 *         the intercept (if enabled)
	 */
	public double[][] getModel() {
		return weights;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = "prediction{in=" + (dimIn) + ",out=" + dimOut + " weights=";
		for (double[] coeff : weights) {
			s += Arrays.toString(coeff);
		}
		s += ", gain=";
		for (double[] row : gainMatrix) {
			s += Arrays.toString(row);
		}
		return s + "}";
	}
	
	public String toString(int dim, int from) {
		String s = "double[][] weights" + dim + "Models" + from + "= {";
		for (double[] coeff : weights) {
			s += Arrays.toString(coeff) + ", ";
		}
		s += "}, double[][] gain" + dim + "Models" + from + "={";
		for (double[] row : gainMatrix) {
			s += Arrays.toString(row) + ", ";
		}
		return s + "}";
	}
	
}