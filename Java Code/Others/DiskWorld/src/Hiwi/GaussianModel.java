package Hiwi;
import java.sql.PseudoColumnUsage;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import Jama.*;

public class GaussianModel {
	
	boolean useCovariance = false;
	
	int xSize;
	double means[];
	double C[][];
	int n = 0;
	
	double dimVarianceStart = 1.0;//300.0;
	double nodimVarianceStart = 1.0;//300.0;
	
	
	
	public GaussianModel(int xSize, double[] sensation, int dim){
		this.xSize = xSize;
		this.means = new double[xSize];
		this.C = new double[xSize][xSize];
			
		for(int j = 0; j < xSize; j++){
			if(j == dim){
				C[j][j] = dimVarianceStart;
			}
			else{
				C[j][j] = nodimVarianceStart;
			}
		}
		
		
		this.update(sensation);
		
	}
	
	public void update(double[] sensation){
		
		double[] newMeans = new double[means.length];
		for(int i = 0; i < xSize; i++){
			newMeans[i] = (means[i] * ((double) n) + sensation[i])/((double) n + 1);
		}
		if(n > 1){
			if(useCovariance){
				for(int j = 0; j < xSize; j++){
					for(int h = 0; h < xSize; h++){ //TODO
						C[j][h] += (sensation[j] - newMeans[j]) * (sensation[h] - means[h]);
					}
				}
			}
			else{
				for(int j = 0; j < xSize; j++){
					C[j][j] += (sensation[j] - means[j]) * (sensation[j] - means[j]);
				}
			}
			
		}
		
		for(int i = 0; i < xSize; i++){
			means[i] = (means[i] * ((double) n) + sensation[i])/((double) n + 1);
		}
		n++;
	}
	
	public double getMean(int i){
		return means[i];
	}
	
	public double[] getMean(){
		return means;
	}
	
	
	public double[][] getCovarianceMatrix(){
		double[][] M = new double[xSize][xSize];
		for(int i = 0; i < xSize; i++){
			for(int j = 0; j < xSize; j++){
				M[i][j] = C[i][j]/((double) n);
			}
		}
		return M;
	}
	
	
	public double gauss(double[] sensation){
		double[][] covMatrix = getCovarianceMatrix();
		double[] xMinusMu = vecMinus(sensation, means);
		double[][] covMatrixCopy = covMatrix.clone();
		double[] invCovTimesXMinusMu = matrixTimesVector(invMatrix(covMatrixCopy), xMinusMu);
		double a = vectorTTimesVector(xMinusMu, invCovTimesXMinusMu);
		//double a = vectorTTimesVector(vecMinus(sensation, means), matrixTimesVector(invMatrix(covMatrix), vecMinus(sensation, means)));
		if(Double.isNaN(a)){
			throw new Error("A is nan");
		}
		
		double exponent = Math.exp(-0.5 * a);
		double det = (new Matrix(covMatrix)).det();//det(covMatrix);
		double nenner = Math.sqrt(Math.pow(2 * Math.PI, xSize) * det);
		/*if(nenner == 0){
			throw new Error("Nenner 0");
		}
		if(Double.isNaN(exponent)){
			throw new Error("Exponent is nan, a = " + a);
		}
		if(Double.isNaN(nenner)){
			throw new Error("Nenner is nan, det =" + det);
		}*/
		if(nenner < 0.00000001){
			nenner = 0.00000001;
		}
		return exponent/nenner;
	}
	
	public double[] gaussGradient (double[] x){
		double[] vec = matrixTimesVector(invMatrix(getCovarianceMatrix()), vecMinus(x, means));
		double gauss = gauss(x);
		/*if(gauss < 0.00001){
			gauss = 0.00001;
		}
		//System.out.println("gaussGradient = [");
		for(int i = 0; i < x.length; i++){
			//System.out.print(gauss(x) + " * " + vec[i] + ", ");
			vec[i] = - gauss(x) * vec[i];
		}*/
		for(int i = 0; i < vec.length; i++){
			vec[i] = -1 * vec[i];///norm;
		}
		
		return vec;
	}
	
	public double[] forwardModelGradient(double[] x){
		double[] grad = gaussGradient(x);
		double[][] covMatrix = this.getCovarianceMatrix();
		int meanIndex = secondSmallestVariance(); //TODO
		double meanForward = covMatrix[meanIndex][meanIndex];
		double varianceForward = varianceOfVariancesWithoutSmallestVariance(covMatrix);///17.0; //TODO
		for(int i = 0; i < covMatrix.length; i++){
			grad[i] *= calcGauss(covMatrix[i][i], meanForward, varianceForward);
		}
		return grad;
		
	}
	
	public double varianceOfVariances(double[][] covMatrix){
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
		
	}
	
	
	public double varianceOfVariancesWithoutSmallestVariance(double[][] covMatrix){
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
	
	public int smallestVariance(){
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
	
	public int smallestVarianceWithoutOne(int one){
		int minI = 0;
		double minVar = Double.MAX_VALUE;
		for(int i = 0; i < C.length; i++){
			if(C[i][i] < minVar && one != i){
				minI = i;
				minVar = C[i][i];
			}
		}
		return minI;
	}
	
	
	public int secondSmallestVariance(){
		return smallestVarianceWithoutOne(smallestVariance());
	}
	
	public double vectorNorm(double[] x){
		double res = 0.0;
		for(double d : x){
			res += d * d;
		}
		return Math.sqrt(res);
	}
	
	/*
	 * MATRIX & VECTOR CALCULATION
	 */
	

	
	public static double[] vecMinus (double[] x, double[] y){
		double[] z = new double[x.length];
		for(int i = 0; i < x.length; i++){
			z[i] = x[i] - y[i];
		}
		return z;
	}
	
	public static double[][] invMatrix(double[][] M){
		Matrix m = new Matrix(M);
		m = Matrices.pinv(m);//m.inverse();
		return m.getArray();
	}
	
	public static double[] matrixTimesVector(double[][] M , double[] x){
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
	
	public static double vectorTTimesVector(double[] x, double[] y){
		double res = 0.0;
		for(int i = 0; i < x.length; i++){
			res += x[i] * y[i];
		}
		return res;
		
	}
	
	public String getCinString(int dim, int from, int to){
		String s = "double[] C" + dim + "Models" + from + to + "= {";
		for(int i = 0; i < C.length; i++){
			/*if(i != 0){
				s = s + " ,";
			}
			s = s + "{";
			for(int j = 0; j < C.length; j++){*/
				if(i != 0){
					s = s + ", ";
				}
				s = s + C[i][i]/((double) n);
			}
			s = s + "};";
		return s;
	}
	
	public String getCinString(int dim, int from){
		return getCinString(dim, from, from);
		/*String s = "double[][] C" + dim + "Models" + from + "= {";
		for(int i = 0; i < C.length; i++){
			if(i != 0){
				s = s + " ,";
			}
			s = s + "{";
			for(int j = 0; j < C.length; j++){
				if(j != 0){
					s = s + ", ";
				}
				s = s + C[i][j];
			}
			s = s + "}";
		}
		s = s + "};";
		return s;*/
	}
	
	public String getMeanInString(int dim, int from, int to){
		String s = "double[] mean" + dim + "Models" + from + to + "= {";
		for(int i = 0; i < means.length; i++){
			if(i != 0){
				s = s + ", ";
			}
			s = s + means[i];
		}
		s = s + "};";
		return s;
	}
	
	public String getMeanInString(int dim, int from){
		String s = "double[] mean" + dim + "Models" + from + "= {";
		for(int i = 0; i < means.length; i++){
			if(i != 0){
				s = s + ", ";
			}
			s = s + means[i];
		}
		s = s + "};";
		return s;
	}
	
	public String getNinString(int dim, int from, int to){
		return "int n" + dim + "Models" + from + to + "= " + n + ";";
	}
	
	public String getNinString(int dim, int from){
		return "int n" + dim + "Models" + from + "= " + n + ";";
	}
	
	public void setN(int n){
		this.n = n;
	}
	
	public void setC(double[][] C){
		this.C = C;
	}
	public void setMeans(double[] means){
		this.means = means;
	}
	
	public static double calcStandGauss(double x){
		double exp = Math.exp(-0.5 * x*x);
		return exp / (Math.sqrt(2 * Math.PI));
	}
	
	public static double calcGauss(double x, double mean, double var){
		double sd = Math.sqrt(var);
		if(sd < 0.000001){
			sd = 0.000001;
		}
		double exp = Math.exp(-0.5 * ((x - mean)/sd) * ((x - mean)/sd));
		return exp / (sd * Math.sqrt(2 * Math.PI));
	}
	
	
	public static void main(String[] args) {
		double[] sensation = {0, 0, 0, 0, 0, 0};
		GaussianModel g = new GaussianModel(6, sensation, 0);
		Random r = new Random(9);
		for(int i = 0; i < 100000; i++){
			double x = r.nextGaussian();
			double[] c = {x/2.0, r.nextGaussian() * 0.5 + x/4.0, r.nextGaussian() * 100, r.nextGaussian() * 100, r.nextGaussian() * 100, r.nextGaussian() * 100};//, 100.0 * r.nextGaussian(), 100.0 * r.nextGaussian(), 100.0 * r.nextGaussian(), 100.0 * r.nextGaussian()};
			g.update(c);
		}
		
		double[][] C = g.getCovarianceMatrix();
		for(int i = 0; i < C.length; i++){
			for(int j = 0; j < C.length; j++){
				System.out.print(C[i][j] + ", ");
			}
			System.out.println("");
		}
		double[] m = g.means;
		for(int i = 0; i < m.length; i++){
			System.out.print(m[i] + ", ");
		}
		
		double sum = 0.0;
		double delta = 0.1;
		
		
		/*for(double X = -2; X < 2; X += 0.1){
			for(double Y = -2; Y < 2; Y += 0.1){
				double[] testSens = {X, Y, 0, 0, 0, 0};
				double gauss = g.gauss(testSens);
				String[] smosaic = {gauss + ", "};
				Fileprinter.printInFile("Babedi3", smosaic, 9);
				sum += gauss;
			}
		}
		sum = Math.pow(delta, 2) * sum;
		double[] test1 = {-1.0, -1.0};
		double[] test2 = {1.0, 1.0};
		System.out.println("a: " + g.gauss(test1) + " b:" + g.gauss(test2) + "sum: " + sum);*/
		System.out.println("");
		double[] current = {0.4, -0.2 , 2, 1, 0, 15};
		double[] gradient = g.gaussGradient(current);
		System.out.println(g.gauss(current));
		System.out.print("grad = [");
		for(double d : gradient){
			System.out.print(d + ", ");
		}
		System.out.println("];");
		
		
		//double[][] m = {{200, 0, 0}, {4, 9000, 1} , {20, 88, 10}};
		//System.out.println(Math.pow(2 * Math.PI, 6));

	}
		
}
