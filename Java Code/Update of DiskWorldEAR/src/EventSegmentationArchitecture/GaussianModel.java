package EventSegmentationArchitecture;

import Jama.*;

/**
 * Multinormial Gaussian distribution
 * 
 * @author Christian Gumbsch
 *
 */
public class GaussianModel {
	
	/** states whether the covariances are updated (= true) or only variances (=false)*/
	private boolean useCovariance = false;
	/** dimension of the multidimensional Gaussian*/
	int n;
	/** mean of the Gaussian*/
	double means[];
	/** matrix used to compute the covariance matrix*/
	double C[][];
	/** number of times the Gaussian was updated*/
	int nrUpdates = 0;
	/** initial values of variances*/
	double varianceStart = 1.0;
	
	
	/**
	 * Constructor
	 * @param sensation, first vector to initialize the distribution
	 */
	public GaussianModel(double[] sensation){
		this.n = sensation.length;
		this.means = new double[n];
		this.C = new double[n][n];
			
		for(int j = 0; j < n; j++){
			C[j][j] = varianceStart;
		}
		this.update(sensation);
	}
	
	/**
	 * Update the mean and covariance matrix of the Gaussian
	 * @param sensation, vector used to update the distribution
	 */
	public void update(double[] sensation){
		
		//Compute new mean before updating
		double[] newMeans = new double[means.length];
		for(int i = 0; i < n; i++){
			newMeans[i] = (means[i] * ((double) nrUpdates) + sensation[i])/((double) nrUpdates + 1);
		}
		
		//Update covariance matrix:
		if(nrUpdates > 1){
			if(useCovariance){
				for(int j = 0; j < n; j++){
					for(int h = 0; h < n; h++){ 
						C[j][h] += (sensation[j] - newMeans[j]) * (sensation[h] - means[h]);
					}
				}
			}
			else{
				for(int j = 0; j < n; j++){
					C[j][j] += (sensation[j] - means[j]) * (sensation[j] - means[j]);
				}
			}
		}
		
		//Update mean:
		for(int i = 0; i < n; i++){
			means[i] = (means[i] * ((double) nrUpdates) + sensation[i])/((double) nrUpdates + 1);
		}
		nrUpdates++;
	}
	

	/**
	 * Getter for mean
	 * @return mean of the Gaussian
	 */
	public double[] getMean(){
		return means;
	}
	
	/**
	 * Getter for covariance matrix
	 * @return covariance matrix of Gaussian
	 */
	public double[][] getCovarianceMatrix(){
		double[][] M = new double[n][n];
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				M[i][j] = C[i][j]/((double) nrUpdates);
			}
		}
		return M;
	}
	
	/**
	 * Compute the function value f(x) of this Gaussian f at a given point x
	 * @param x
	 * @return f(x), with f being the Gaussian distribution
	 */
	public double gauss(double[] x){
		double[][] covMatrix = getCovarianceMatrix();
		double[] xMinusMu = vecMinus(x, means);
		double[][] covMatrixCopy = covMatrix.clone();
		double[] invCovTimesXMinusMu = matrixTimesVector(invMatrix(covMatrixCopy), xMinusMu);
		double a = vectorTTimesVector(xMinusMu, invCovTimesXMinusMu);
		double exponent = Math.exp(-0.5 * a);
		double det = (new Matrix(covMatrix)).det();
		double denominator = Math.sqrt(Math.pow(2 * Math.PI, n) * det);
		//To avoid errors based on rounding errors  use a threshold for minimal values
		if(denominator < 0.00000001){
			denominator = 0.00000001;
		}
		return exponent/denominator;
	}
	
	/**
	 * Compute a weightened gradient W * f'(x) of the Gaussian distribution f at a given point x.
	 * The weights of W are determined heuristically based on the covariance matrix to increase
	 * the strength of dimensions with small variance and decrease the influence of dimensions 
	 * with big variances
	 * 
	 * @param x
	 * @return W * f'(x)
	 */
	public double[] inverseModelGradient(double[] x){
		double[] grad = gaussGradient(x);
		double[][] covMatrix = this.getCovarianceMatrix();
		int meanIndex = secondSmallestVariance(); //Use the second Smallest Variance to exclude color with variance = 0
		double meanForward = covMatrix[meanIndex][meanIndex];
		double varianceForward = varianceOfVariancesWithoutSmallestVariance(covMatrix);
		for(int i = 0; i < covMatrix.length; i++){
			grad[i] *= calcGauss(covMatrix[i][i], meanForward, varianceForward);
		}
		return grad;
		
	}
	
	/*
	 * ----------------------------------------------------------------------
	 * Helper functions to compute the gradient for the inverse forward model
	 * ----------------------------------------------------------------------
	 */
	
	/**
	 * Compute the gradient f'(x) of the Gaussian distribution f at a given point x
	 * @param x
	 * @return f'(x)
	 */
	private double[] gaussGradient (double[] x){
		double[] vec = matrixTimesVector(invMatrix(getCovarianceMatrix()), vecMinus(x, means));
		for(int i = 0; i < vec.length; i++){
			vec[i] = -1 * vec[i];
		}
		return vec;
	}
	
	/**
	 * Compute the variance of the variances
	 * @param covMatrix,
	 * 		the covariance matrix of the Gaussian
	 * @return the variance of the variances
	 */
	/*private double varianceOfVariances(double[][] covMatrix){
		double var = 0.0;
		double meanVariance = 0.0;
		for(int i = 0; i < covMatrix.length; i++){
			meanVariance += covMatrix[i][i];
		}
		meanVariance = meanVariance/((double)covMatrix.length);
		
		for(int i = 0; i < covMatrix.length; i++){
			var += (covMatrix[i][i] - meanVariance)* (covMatrix[i][i] - meanVariance);
		}
		return var/((double) covMatrix.length);
	}*/
	
	/**
	 * Compute the variance of the variances ignoring the smallest variance,
	 * this method is preferred, since the dimension describing object's color
	 * quickly approximates 0
	 * @param covMatrix
	 * 		the covariance matrix of the Gaussian
	 * @return the variance of the variances
	 */
	private double varianceOfVariancesWithoutSmallestVariance(double[][] covMatrix){
		int smallest = smallestVariance();
		double var = 0.0;
		double meanVariance = 0.0;
		for(int i = 0; i < covMatrix.length; i++){
			if(i != smallest)
				meanVariance += covMatrix[i][i];
		}
		meanVariance = meanVariance/((double)covMatrix.length - 1);
		
		for(int i = 0; i < covMatrix.length; i++){
			if(i != smallest)
				var += (covMatrix[i][i] - meanVariance)* (covMatrix[i][i] - meanVariance);
		}
		return var/((double) covMatrix.length -1);
	}
	
	/**
	 * Find the smallest variance of the covariance matrix
	 * @return index of the smallest variance
	 */
	private int smallestVariance(){
		int minI = 0;
		double minVar = Double.MAX_VALUE;
		for(int i = 0; i < C.length; i++){
			if(C[i][i] < minVar){
				minI = i;
				minVar = C[i][i];
			}
		}
		return minI;
	}
	
	/**
	 * Find the smallest variance of the covariance matrix excluding
	 * one dimension
	 * @param d, dimension to be ignored
	 * @return smallest variance ignoring dimension d
	 */
	private int smallestVarianceWithoutOneDim(int d){
		int minI = 0;
		double minVar = Double.MAX_VALUE;
		for(int i = 0; i < C.length; i++){
			if(C[i][i] < minVar && d != i){
				minI = i;
				minVar = C[i][i];
			}
		}
		return minI;
	}
	
	/**
	 * Find the second smallest variance of the covariance matrix
	 * @return index of the second smallest variance
	 */
	private int secondSmallestVariance(){
		return smallestVarianceWithoutOneDim(smallestVariance());
	}
	
	/**
	 * Compute an one-dimensional Gaussian g(x), given the mean 
	 * and the variance of the Gaussian
	 * @param x
	 * @param mean, mean of Gaussian g
	 * @param var, variance of Gaussian g
	 * @return g(x)
	 */
	private static double calcGauss(double x, double mean, double var){
		double sd = Math.sqrt(var);
		if(sd < 0.000001){
			sd = 0.000001;
		}
		double exp = Math.exp(-0.5 * ((x - mean)/sd) * ((x - mean)/sd));
		return exp / (sd * Math.sqrt(2 * Math.PI));
	}
	
	
	/*
	 * -------------------------------
	 * Matrix and vector calculations
	 * -------------------------------
	 */
	
	/**
	 * Subtraction of vectors
	 * @param x
	 * @param y
	 * @return x - y
	 */
	private static double[] vecMinus (double[] x, double[] y){
		double[] z = new double[x.length];
		for(int i = 0; i < x.length; i++){
			z[i] = x[i] - y[i];
		}
		return z;
	}
	
	/**
	 * Inversion of a Matrix M
	 * @param M
	 * @return M^(-1) if possible, the pseudo inverse of M otherwise
	 */
	private static double[][] invMatrix(double[][] M){
		Matrix m = new Matrix(M);
		m = Matrices.pinv(m);
		return m.getArray();
	}
	
	/**
	 * Multiplication of a Matrix with a vector
	 * @param M, the matrix
	 * @param x, the vector
	 * @return M * x
	 */
	private static double[] matrixTimesVector(double[][] M , double[] x){
		double[] y = new double[x.length];
		for(int i = 0; i < x.length; i++){
			double res = 0.0;
			for(int j = 0; j < x.length; j++){
				res += M[i][j] * x[j];
			}
			y[i] = res;
		}
		return y;
	}
	/**
	 * Multiplication of a transposed vector with another vector
	 * @param x
	 * @param y
	 * @return x^T * y
	 */
	public static double vectorTTimesVector(double[] x, double[] y){
		double res = 0.0;
		for(int i = 0; i < x.length; i++){
			res += x[i] * y[i];
		}
		return res;
		
	}
		
}
