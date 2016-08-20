package de.jannlab.examples.pingpong;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.misc.DoubleTools;
import de.jannlab.tools.NetTools;
import de.jannlab.training.GradientDescent;

public class HLSTM implements ForwardModel {

	// sequence length
	static int length = 100;

	// Net specifications
	static int DimIn;
	static int DimOut;
	final int hid;
	private static double[] weights;
	private static Net net;

	public HLSTM(final int in, final int hid, final int out) {

		HLSTM.DimIn = in;
		HLSTM.DimOut = out;
		this.hid = hid;

		//
		LSTMGenerator gen = new LSTMGenerator();
		gen.inputLayer(in);
		gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.TANH,
				true);
		gen.outputLayer(out, CellType.TANH);
		//

		net = gen.generate();
		net.rebuffer(length);
		net.initializeWeights(rnd);

	}

	public double[] predict(double[] x) {
		final double[] y = new double[1];

		net.reset();
		net.input(x, 0);
		net.compute();
		net.output(y, 0); // predicting sensation
		return y;
	}

	//
	// config parameter.
	//
	GradientDescent trainer = new GradientDescent();

	final int epochs = 20;
	final double learningrate = 0.001;
	final double momentum = 0.9;

	private static Random rnd = new Random(0L);

	public void update(double[] data, double[] target) {

		SampleSet trainset = generateSample(data, target);

		if (trainset.size() >= 1) {
			// net.rebuffer(length);
			GradientDescent trainer = new GradientDescent();
			trainer.setOnline(false); // should be false because the injection
										// of sample is one by one.
			trainer.setTrainingSet(trainset);
			trainer.setNet(net);
			trainer.setRnd(rnd);
			trainer.setPermute(true);
			trainer.setLearningRate(learningrate);
			trainer.setMomentum(momentum);
			trainer.setEpochs(epochs);
			trainer.train();
//			System.out.println("FrameSize : " + net.getStructure());
		}
	}

	// private static ArrayList<Double> data = new ArrayList<Double>();
	// private static ArrayList<Double> target = new ArrayList<Double>();

	public static SampleSet set = new SampleSet();

	public static SampleSet generateSample(double[] current_input,	double[] current_target) {
	

		if (set.size() < length) {

			Sample sample = new Sample(current_input, current_target, 1, 1, 1, 1);
			
			set.add(sample);
//			printSet(set);
			System.out.println("buffer size : " + set.size() );
			return set;

		} else if (set.size() >= length) {
			Sample sample = new Sample(current_input, current_target, 1,
					length, 1, 1);
//			set.remove(0);
			set.add(sample);
			printSet(set);
			System.out.println("size : " + set.size());
			
			
				return set;


		} else {
			return null;
		}

		//
		// ArrayList<Double> tempdata = new ArrayList<Double>(1);
		// ArrayList<Double> temptarget = new ArrayList<Double>(1);
		//
		// /** increase buffer sample size before reaching sequence size
		// *
		// *
		// */
		// if (data.size() < length * DimIn && target.size() < length * DimOut)
		// {
		// for (double d : current_input) {
		// data.add(d);
		// }
		//
		// for (double d : current_target){
		// target.add(d);
		// }
		//
		// /** remove the first and add the last space of the sample when
		// reaching the sequence size
		// *
		// * */
		//
		// } else if (data.size() >= length * DimIn
		// && target.size() >= length * DimOut) {
		//
		// tempdata.addAll(0, data.subList(DimIn, length ));
		// for (double d : current_input) { // adding terms until the length
		// equals to data.length.
		// tempdata.add(d);
		// }
		// for (double d : current_target) {// adding terms until the length
		// equals to target.length.
		// temptarget.addAll(0, target.subList(DimOut, length ));
		// temptarget.add(d);
		// }
		//
		//
		//
		// if (tempdata.size() == data.size() && temptarget.size() ==
		// target.size()){
		// data = tempdata;
		// target = temptarget;
		// }else{
		// System.out.println("the gradient update failed due to algorithm error.\n");}
		//
		// } else { };
		//
		// double[] p = new double[data.size()]; // initiates an array all
		// default value 0;
		//
		// double[] q = new double[target.size()];
		//
		// for (int i = 0; i < data.size(); i++) {
		// p[i] = data.get(i);
		// }
		// for (int i = 0; i < target.size(); i++) {
		// q[i] = target.get(i);
		// }
		//
		// Sample sample = new Sample(p, q, 5, length, 1, 1);
		// // double[] sth = sample.getInputLength();
		// // int sth1 = sample.getInputSize();
		// // int sth2 = sample.getInputLength();
		// // System.out.println("input sample : " + Arrays.toString(sth));
		// // System.out.println("input length : " + sth2);
		// // System.out.println("input size : " + sth1 );
		// SampleSet set = new SampleSet();
		// set.add(sample);
		//
		//
		// return set;

	}

	// print the elements of SampleSet
	static void printSet(SampleSet set) {
		for (Sample s : set) {
			System.out.println("sample Idx : " + set.lastIndexOf(s));
			System.out.println(" input ==> target : "
					+ DoubleTools.asString(s.getInput(), 1) + " ==> "
					+ DoubleTools.asString(s.getTarget(), 1));

		}
		System.out.println("\n");
	}

	private LinkedList<Double> errors = new LinkedList<Double>();
	private int denom = errors.size();

	private double meanError = 0.0;
	private double errorVariance = 0.0;

	public void updateError(double e) {
		if (errors.size() < length) {
			errors.add(e);
		} else if (errors.size() == length) {
			errors.removeFirst();
			errors.addLast(e);
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

	public double[] inverseForwardModel(double y) {
		// TODO Auto-generated method stub
		return null;
	}

	/* making a clean hard copy of LSTM model without errors history */
	public HLSTM copy() {
		HLSTM clone = new HLSTM(DimIn, hid, DimOut);
		clone.DimIn = this.DimIn;
		clone.DimOut = this.DimOut;
		clone.weights = this.weights.clone();
		clone.errors = new LinkedList<Double>();
		clone.errorVariance = 0.0;
		clone.meanError = 0.0;
		return clone;

	}

	/* making a hard copy of the LSTM model,along with the errors. */
	public HLSTM copyWithErrors() {
		HLSTM clone = this.copy();
		for (Double e : this.errors) {
			clone.errors.add(e);
		}
		clone.meanError = this.meanError;
		clone.errorVariance = this.errorVariance;
		return clone;

	}

	public String getName() {
		String name = "HLSTM";
		return name;
	}

	public double[] getWeights() {
		return net.getWeights();

	}

}
