package backup;
//package EventSegmentationArchitecture;

//import Jama.Matrix;
//import Jama.LUDecomposition;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.NetStructure;
import de.jannlab.data.Data;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.error.ErrorMetric;
import de.jannlab.error.MSEAccumulator;
import de.jannlab.io.Serializer;
import de.jannlab.math.Matrix;
import de.jannlab.tools.DoubleTools;
import de.jannlab.tools.NetTools;
import de.jannlab.generator.GenerateNetworks;

public  class LSTMModel implements ForwardModel {
	/**
	 * error statistics
	 * 
	 * @param T
	 *            error memory
	 * @param error
	 *            storing sensory error of the previous 100 steps
	 * @param denom
	 *            size of errors
	 * @param sum
	 *            ; sum of errors;
	 * @param MeanError
	 *            meanError
	 * @param errorVariance
	 * @param square_sum
	 *            sum of squared difference of errors
	 * @param weights
	 *            weights of the LSTM.
	 */
	 private int dimIn;
	 private int dimOut;
	 private String modelType;

	/* the weights stores of 5-d weights for each sensory channel. */
	 private Net net;
	 private double[] weights;
	 private int T = 100; // buffer

	// initialize Matrix input , target for forward model
	 private Matrix input = new Matrix(T, 5);
	 private Matrix target = new Matrix(T, 1);

	// initialize Matrix input target for inverse model
	 private Matrix input_inverse = new Matrix(T, 1);
	 private Matrix target_inverse = new Matrix(T, 5);

	 private double[] prediction;// output for each dimension of sensory prediction
						// should be double. Declare double[] is for
						// generalization.
	 private LinkedList<Double> errors = new LinkedList<Double>();
	 private int denom = errors.size();
	 
	 private double meanError = 0.0;
	 private double errorVariance = 0.0;

	/*
	 * Initialize the configuration and weight of the LSTM model.
	 */
	public LSTMModel(int dimIn, int dimOut) {
		this.dimIn = dimIn; // supposed to be 5. 8 forward models are used to
							// predict sensation.
		this.dimOut = 1;// supposed to be 1.

		prediction = new double[dimOut];

		modelType = "LSTM-" + this.dimIn + "-tanh16-linear" + this.dimOut;
		// specify the configuration of NN
		this.net = GenerateNetworks.generateNet(modelType);
		/* initialize the weights object. */
		/* the weights stores of 5-d weights for each sensory channel. */
		weights = new double[net.getWeightsNum()];

		// fill(obj, offset, size, initial value, value_min,value_max)
		DoubleTools.fill(weights, 0, weights.length, new Random(), -0.1, 0.1);
		net.writeWeights(weights, 0);
		// the input and the desired states are given by constructing tuple.

	}

	/*
	 * Only predict y, not updating.
	 */
	public double[] predict(double[] motorCommand) {

		net.reset();
		net.input(motorCommand, 0);
		net.compute();
		net.output(prediction, 0); // predicting sensation
		return prediction;
	}


	
	

	public void update(double[] motorCommand, double sensation) {

		/*
		 * buffer the network for backpropagation. The arg is the length of
		 * sequence.
		 */
		net.rebuffer(100);// time sequence being propagated.
		// initialize error.
		MSEAccumulator err = new MSEAccumulator();
		// the input and the desired states are given by constructing tuple.
		// Matrix input = new Matrix(100, 5);
		// Matrix target = new Matrix(100, 1);
		Matrix temporaryInput = input.copy();
		Matrix temporaryTarget = target.copy();
		/* update the input matrix. */
		// construct temporaryInput Matrix
		for (int t = 0; t < T; t++) {
			
			if (t == 0) {
				// add the latest sensation x to the beginning of the sequence.
				for (int i = 0; i < dimIn; i++) {temporaryInput.set(t, i, motorCommand[i]);				}
				for (int i = 0; i < dimOut; i++) {temporaryTarget.set(t, i, sensation);
				}
			} else if ( t != T-1 ) {
				// shift the time window by one.
				for (int i = 0; i < dimIn; i++) {
					temporaryInput.set(t+1, i, input.get(t,i));}
				for (int i = 0; i < dimOut; i++) {
					temporaryTarget.set(t, i+1, target.get(t,i));}
			
			} else {	}

		}
		// update the matrix by replace input with temporaryInput;
		input = temporaryInput.copy();
		target = temporaryTarget.copy();

//
//		System.out.println("input matrix columns : " + input.cols);
//		System.out.println("input matrix rows : " + input.rows);
//		System.out.println("\n" + input.toString());
//		System.out.println("target matrix columns : " + target.cols);
//		System.out.println("target matrix rows : " + target.rows);
//		System.out.println("\n" + target.toString());
//		NetStructure k = net.getStructure();
//		System.out.println(k);
		final Sample sample = new Sample(input, target);
		NetTools.performForward(net, err, sample);
		// err.error()
		NetTools.performBackward(net);
		double[] gradweights = new double[net.getWeightsNum()];
		net.readGradWeights(gradweights, 0);
		for (int i = 0; i < gradweights.length; i++) {
			weights[i] += 0.001 * gradweights[i];
		}
		net.writeWeights(weights, 0);

		// online computation (not learning)
		net.rebuffer(1);

		// System.out.println("Gradient weight : " + gradweights);
	};

	public void updateError(double error) {
		if (errors.size() < T) {
			errors.add(error);
		} else if (errors.size() == T) {
			errors.removeFirst();
			errors.addLast(error);
		}

	}

	public double getMeanError() {
			double sum = 0.0;
			double square_sum = 0.0;
			
		for (int i = 0; i < denom; i++) {
			sum += errors.get(i);
			square_sum += Math.pow((errors.get(i) - meanError), 2);
		}
	     meanError = sum / denom;
		 errorVariance = square_sum / denom;

		return this.meanError;
	}

	public double getErrorVariance() {
		return this.errorVariance;
	}

	public double[] inverseForwardModel(double intention) {
		
		// compute the deltas
		NetTools.performBackward(net);
		double[] deltas = net.getGradOutputBuffer(0);
		// compute the weight of each link
		int[] Links = net.getLinks();   // index of links and corresponding weights.
		double[][] WeightMatrix = null;
		double[] ReadWeights = net.getWeights();
		
		for(int i =0; i< Links.length/3; i++){
			int xIndex = Links[3*i];
			int yIndex= Links[3*i+1];
			int zIndex =Links[3*i+2];
			double zValue = ReadWeights[zIndex];
			WeightMatrix[xIndex][yIndex] = zValue;			
		} 
		// Matrix WeightM = new Matrix(WeightMatrix);
		
		/*** compute the input derivatives x = inv(W) * a 
			x: predicted input (input derivative in Sebastian's word)
			a: states of the last hidden layer.
			W: weight matrix from x to a.
		*/
		// invert the matrix
		LUDecom b = new LUDecom();
		double[] predictedCommand = b.decomposition(WeightMatrix,deltas);
//		LUDecomposition luDecomposition = new LUDecomposition(WeightM);
//		luDecomposition.getL();//lower matrix;
//		luDecomposition.getU(); // upper matrix;
//		
//		Matrix b = new Matrix(deltas, deltas.length );
//		Matrix x = luDecomposition.solve(b); // solve Ax = b for unknow vector x;
//		
//		Matrix residual = WeightM.times(x).minus(b); // calculate the residual error
//      double rnorm = residual.normInf(); // get the max error (yes, it's very small)
//      // System.out.println("residual: " + rnorm);
//      
//      double[] predictedCommand = x.getColumnPackedCopy();
      return predictedCommand;
      
  }
		
		


	/* making a clean hard copy of LSTM model without errors history */
	public LSTMModel copy() {
		LSTMModel clone = new LSTMModel(dimIn, dimOut);
		clone.dimIn = this.dimIn;
		clone.dimOut = this.dimOut;
		clone.weights = this.weights.clone();
		clone.errors = new LinkedList<Double>();
		clone.errorVariance = 0.0;
		clone.meanError = 0.0;
		return clone;

	}

	/* making a hard copy of the LSTM model,along with the errors. */
	public LSTMModel copyWithErrors() {
		LSTMModel clone = this.copy();
		for (Double e : this.errors) {
			clone.errors.add(e);
		}
		clone.meanError = this.meanError;
		clone.errorVariance = this.errorVariance;
		return clone;

	}
	
	public String getName(){
		String name = "LSTMModel";
		return name;
	}
	public double[] getWeights(){
		return  net.getWeights();
		
	}

}
