package EventSegmentationArchitecture;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.core.Link;
import de.jannlab.core.NetBase;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.math.Matrix;
import de.jannlab.tools.DoubleTools;
import de.jannlab.tools.NetTools;

public class HLSTM implements ForwardModel, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//
	public static final double DEFAULT_ALPHA = 0.9;// 0.9;
	public static final double DEFAULT_MU = 0.0001; // 10^{-5}
	public static final int DEFAULT_VLDINTERVAL = 5;
	public static final boolean DEFAULT_EARLYSTOP = false;
	public static final int DEFAULT_EARLYSTOPCOUNT = 10;
	public static final int DEFAULT_EPOCHS = 100;
	public static final double DEFAULT_TARGETERROR = 0.0;
	public static final boolean DEFAULT_ONLINE = true;
	public static final boolean DEFAULT_PERMUTE = true;

	/**
	 * Learning rate.
	 */
	private double mu = DEFAULT_MU;
	/**
	 * Mommentum factor.
	 */
	private double alpha = DEFAULT_ALPHA;
	/**
	 * Weights vector of the reference network.
	 */
	private double[] weights = null;
	/**
	 * Number of weights of the reference network.
	 */
	private int weightsnum = 0;
	/**
	 * number of cells in one hidden node
	 */
	private int cellsnum = 0;
	/**
	 * The last weight differences.
	 */
	private double[] dweightslast = null;
	/**
	 * The current weight differences.
	 */
	private double[] dweights = null;
	/**
	 * The gradient of weights modulated by a momentum term.
	 */
	private static double[] dweightsMomentum = null;
	/**
	 * The links of the reference network.
	 */
	private int[] links = null;
	/**
	 * The number of links.
	 */
	private static int linksnum = 0;
	/**
	 * An array of learning rate of adaptive learning.
	 * */
	private static double[] g = null;
	/**
	 * The Mean Square of weights used to scale the learning rates.
	 */
	private static double[] MeanSquare = null;
	/**
	 * The output of each activation function in a LSTM compute cell
	 */
	private static double[] activity = null;
	/**
	 * The last gradient of activations. It is used for momentum method of gradient descent.
	 */
	private static double[] lastgradacts = null;
	/**
	 * The gradient of weights modulated by a momentum term.
	 */
	private static double[] dactsMomentum = null;
	/**
	 * Length of the seuquence that is used for memorizing sequential patterns.
	 */
	static int window = 100;
	/**
	 * The handle of function Sample Sequencer that sequencing the input and target   
	 */
	SampleSequencer sequencer;

	// Net specifications
	static int DimIn;
	static int DimOut;
	final int hid;
	private static Net net;

	@Override
	public String toString() {
		return new StringBuilder() //
				.append(net + ", ") //
				.append(hid + ", ") //
				.append(DimIn + ", ") //
				.append(DimOut + ", ") //
				.append(window + ", ") //
				.append(linksnum + ", ") //
				.toString();
	}

	public HLSTM(final int in, final int hid, final int out) {

		HLSTM.DimIn = in;
		HLSTM.DimOut = out;
		this.hid = hid;
		// Specs of LSTM
		LSTMGenerator gen = new LSTMGenerator();
		gen.inputLayer(in);
		gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.LINEAR,true);
		gen.outputLayer(out, CellType.LINEAR);
		//		net = GenerateNetworks.generateNet("LSTM-1-tanh18-linear1");

		// Generate LSTM
		net = gen.generate();
		
		init();

		// Initialize weights
		Random rnd = new Random();
		weights[0] = 0;
		DoubleTools.fill(weights, 1, weightsnum, rnd, -0.1, 0.1);
		net.writeWeights(weights, 0);
		// System.out.println(" init weights : " +
		// Arrays.toString(net.getWeights()));
	}

	protected void init() {
		// There are N weights for all connections, but the length of the vector must be
		// N+1, as the rest of  non-connected cells has virtual links with weight zero.
		this.weightsnum = net.getWeightsNum(); // because getWeightsNum()+1 == getWeights.length
		this.links = net.getLinks();
		HLSTM.linksnum = net.getLinksNum();
		this.cellsnum = net.getStructure().cellsnum;
		
		this.weights = new double[net.getWeights().length];
		this.dweights = new double[weights.length];
		this.dweightslast = new double[weights.length];
		HLSTM.dweightsMomentum = new double[weights.length];
		
		// for sequencing sample
		if (sequencer == null) {
			sequencer = new SampleSequencer(window, DimIn);
		}
		// this weightsnum+1 thing  due to one space to store non-connected links weight values 0;
		// we write weights from the second weight by offseting 1 space, i.e., net.writeWeights(weights,1);
		
		HLSTM.activity= new double[cellsnum+1]; 
		HLSTM.lastgradacts = new double[cellsnum+1];
		HLSTM.dactsMomentum = new double[cellsnum+1];
		
		MeanSquare = new double[weights.length];
		for(int i= 0; i<weights.length; i++){
			MeanSquare[i] = 1.0;
		}
		g = new double[weights.length];
		for(int i= 0; i<weights.length; i++){
			g[i] = mu;
		}
	}
	public double[] predict(double[] x) {
		Sample sample = sequencer.addSampleInput(x);
		double[] h = new double[DimIn];
		
		final double[] y = new double[DimOut];
		//net.rebuffer(1);
		net.reset();
		net.setFrameIdx(0);
		for( int t =0; t< window; t++){
			h= sample.input.getRow(t).data;
			net.input(h, 0);
			net.compute();
			if( t != window-1) {net.incrFrameIdx();} // Sebastian said this is wrong.
		}
		net.output(y, 0);
		// using prediction as quasi-target, otherwise, target space in sequence will remain Null during forward modeling.
		sequencer.addSampleTarget(y);
		return y;
	}
	
	/**  Sequence the new sample sequence,and feed in real update routine.
	 * 
	 * @param data double[] contains the latest input.
	 * @param target double[] contain the latest target 
	 */
	public void update(double[] data, double target) {
		
		double[] tar = {target};
		Sample sample = sequencer.getSample(data, tar);
		update(sample);
	}
	
	/** Update with momentum method.
	 * 
	 * @param sample A matrix that contains input sequence and target sequence.
	 */
	// parameters
	SampleSet tset = new SampleSet();
	int buffersize = 0;
/*
	protected void update(Sample sample) {
		int epoch = 100;
		net.rebuffer(sample.input.rows);
		for (int e = 0; e<epoch; e++){
			net.reset();
			net.setFrameIdx(0);
			for(int t = 0; t< sample.input.rows; t ++){
				// sample.input.get(int t, int offset)
				double[] in =sample.input.getRow(t).data;
				net.input(in,0);
				net.compute();	
				double[] tar = sample.target.getRow(t).data;
				net.target(tar, 0);
				net.injectError();
				if(t < (sample.input.rows -1)) { net.incrFrameIdx();}
			}		

//			weights = net.getWeights(); Never use get weights, otherwise you get  a reference.
			net.readWeights(weights, 0);
			// compute the weight gradient
			net.computeGradient();
			double[] delta = new double[weightsnum];
			net.readGradWeights(dweights, 0);
			for (int i = 0; i < net.getWeights().length; i++) {
				dweightsMomentum[i] = mu*dweights[i]+alpha*dweightslast[i];
				weights[i] += dweightsMomentum[i];
				this.dweightslast[i] = dweightsMomentum[i];
				this.dweights[i] = 0.0;
				
				}
			net.writeWeights(weights, 0);

		}
		net.rebuffer(1);
	}
	*/
	/** Update rmsrprop with Nesterov momentum method.
	 * 
	 * @param sample A matrix that contains input sequence and target sequence.
	 */

	public void update(Sample sample) {
		int epoch = 100;
		net.rebuffer(sample.input.rows);
		for (int e = 0; e<epoch; e++){
			net.reset();
			net.setFrameIdx(0);
			for(int t = 0; t< sample.input.rows; t ++){
				// sample.input.get(int t, int offset)
				double[] in = sample.input.getRow(t).data;
				net.input(in,0);
				net.compute();	
				double[] tar = sample.target.getRow(t).data;
				net.target(tar, 0);
				net.injectError();
				if(t < (sample.input.rows -1)) { net.incrFrameIdx();}
			}

			net.readWeights(weights, 0);
			
			double[] kappa =PAO.scalarprod(dweightslast, alpha);
			
//			System.out.println("kappa : " + Arrays.toString(kappa));
			double[] sum = PAO.sum(weights,kappa);
			net.writeWeights(sum, 0);
			// compute the weight gradient
			net.computeGradient();
			net.readGradWeights(dweights, 0);			
			for (int i = 0; i < net.getWeights().length; i++) {
				
				// adaptive learning rate
				double lbd = 0.1; double ubd = 10;// limiting range: [0.1, 10], [0.1,100]
				if (dweights[i]*dweightslast[i]>0){
					if(g[i]<ubd){
						g[i] = g[i] + 0.05;
					}
				}else{
					if(g[i]>lbd){
						g[i] = g[i]*0.95;
					}
				}
				
				dweightsMomentum[i] = mu*g[i]*dweights[i] + alpha*dweightslast[i]; 
				weights[i] += dweightsMomentum[i];
				this.dweightslast[i] = dweightsMomentum[i];
				this.dweights[i] = 0.0;
				}
			net.writeWeights(weights, 0);
			
		}
		net.rebuffer(1);
	}
	
	public double[] inverseForwardModel(double y) {

		// compute the deltas
		NetTools.performBackward(net);
		double[] deltas = net.getGradOutputBuffer(1);
		// compute the weight of each link
		int[] Links = net.getLinks(); // index of links and corresponding
										// weights.
		double[][] WeightMatrix = null;
		double[] ReadWeights = net.getWeights();

		for (int i = 0; i < Links.length / 3; i++) {
			int xIndex = Links[3 * i];
			int yIndex = Links[3 * i + 1];
			int zIndex = Links[3 * i + 2];
			double zValue = ReadWeights[zIndex];
			WeightMatrix[xIndex][yIndex] = zValue;
		}
		net.readGradWeights(dweights, 0);
		for (int i = 0; i < dweights.length; i++) {
			weights[i] += 0.0005 * dweights[i] + 0.9 * dweightslast[i];
			dweightslast[i] = 0.0005 * dweights[i];
			dweights[i] = 0.0;
		}
		net.writeWeights(weights, 0);

		net.rebuffer(1);
		return net.getWeights();
	}
	
	/**
	 * PARAMETERS OF ERROR CALCULATION
	 */
	private LinkedList<Double> errors = new LinkedList<Double>();
	private int denom;
	private double meanError = 0.0;
	private double errorVariance = 0.0;

	public void updateError(double e) {
		if (errors.size() < window) {
			errors.add(e);
		} else if (errors.size() == window) {
			errors.removeFirst();
			errors.addLast(e);
		}
		denom = errors.size();
	}

	public double getMeanError() {
		double sum = 0.0;
		double square_sum = 0.0;

		for (int i = 0; i < denom; i++) {
			sum += Math.abs(errors.get(i));
			square_sum += Math.pow((errors.get(i) - meanError), 2);
		}
		meanError = sum / denom;
		errorVariance = square_sum / denom;

		return this.meanError;
	}

	public double getErrorVariance() {
		return this.errorVariance;

	}

	

	/* making a clean deep copy of LSTM model without errors history */
	public HLSTM copy() {
		HLSTM clone = new HLSTM(DimIn, hid, DimOut);
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
