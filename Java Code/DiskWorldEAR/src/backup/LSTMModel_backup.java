package EventSegmentationArchitecture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Random;

import de.jannlab.Net;
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

public class LSTMModel_backup implements ForwardModel {
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
	int dimIn;
	int dimOut;
	String modelType;
	double[] weights;;// the weights stores of 5-d weights for each sensory
						// channel.
	public Net net;

	double[] y;// output
	private int T = 100;
	private LinkedList<Double> errors = new LinkedList<Double>();
	int denom = errors.size();
	double sum;
	double square_sum;
	double meanError;
	double errorVariance;
	private LinkedList<Double> errors_copy = new LinkedList<Double>();

	/*
	 * Initialize the config and weight of the LSTM model.
	 */
	public LSTMModel(int dimIn, int dimOut) {
		this.dimIn = dimIn; // supposed to be 5. 8 forward models are used to
							// predict sensation.
		this.dimOut = dimOut;// supposed to be 1.
		modelType = "LSTM-" + dimIn + "-tanh16-linear" + dimOut;
		// specify the configuration of NN
		this.net = GenerateNetworks.generateNet(modelType);
		// construct initial weights.

	}

	/*
	 * Only predict y, not updating.
	 */
	public double[] predict(double[] x) {

		try {
			weights = Serializer.read("myweight_model.weights");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// net.writeWeights(weights, 0);
		// initialize the weights object.
		double[] weights = new double[net.getWeightsNum()];
		// fill(obj, offset, size, initial value, value_min,value_max)
		DoubleTools.fill(weights, 0, weights.length, new Random(), -0.1, 0.1);
		net.writeWeights(weights, 0);
		// buffer the network for backpropagation. The arg is the length of
		// sequence.
		net.rebuffer(100);
		// initialize error.
		MSEAccumulator err = new MSEAccumulator();
		// the input and the desired states are given by constructing tuple.
		Matrix input = new Matrix(100, 5);
		Matrix target = new Matrix(100, 1);
		System.out.println(input);
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
		net.reset();
		net.input(x, 0);
		net.output(y, 0); // predicting sensation

		// online computation (not learning)
		net.rebuffer(1);
		return y;
	}

	/*
	 * 
	 * */
	public void update(double[] x, double y) {
		// PrintStream s = new PrintStream(new FileOutputStream(new File(
		// "samples.nn")));
		// double[] input = x; // new double[T];
		// double[] target = { y }; // new double[T];
		//
		// s.println(DoubleTools.asString(input, " "));
		// s.println(DoubleTools.asString(target, " "));
		//
		// SampleSet sampleset = Data.loadSamples("samples.nn", dimIn, dimOut);
		try {
			weights = Serializer.read("myweight.weights");
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		net.writeWeights(weights, 0);
		net.reset();

		net.input(x, 1);
		net.compute(); // updating weight.
		net.readWeights(weights, 0);
		try {
			Serializer.write(weights, "myweight.weights");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	};

	public void updateError(double e) {
		if (errors.size() < T) {
			errors.add(e);
		} else if (errors.size() == T) {
			errors.removeFirst();
			errors.addLast(e);
		}

	}

	public double getMeanError() {

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

	public double[] inverseForwardModel(double y) {
		// It is usually difficult to find the inverse function for NN.
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

}
