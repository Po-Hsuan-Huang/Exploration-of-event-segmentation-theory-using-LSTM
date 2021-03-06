package EventSegmentationArchitecture;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.BadHack;
import de.jannlab.core.CellArray;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.error.MSEAccumulator;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.math.Matrix;
import de.jannlab.tools.DoubleTools;

public class HLSTM_May21 implements ForwardModel, Serializable {
	//
	public static final double DEFAULT_ALPHA = 0.9;//0.9;
	public static final double DEFAULT_MU = .01; // 10^{-5}
	public static final int DEFAULT_VLDINTERVAL = 5;
	public static final boolean DEFAULT_EARLYSTOP = false;
	public static final int DEFAULT_EARLYSTOPCOUNT = 0 ;
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
	 * EarlyStop Boolean.
	 */
	private boolean isEarlyStop = DEFAULT_EARLYSTOP;
	/**
	 * Weights vector of the reference network.
	 */
	private double[] weights = null;
	/**
	 * Best weights during training epochs. It is used in EarlyStopping.
	 */
	private double[] bestweights = null;
	/**
	 * Mean Error of the last 10 updates. 
	 */
	private static double movingMeanErr =1;
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
	 * A list of learning rate for adaptive training of inverse forward model.
	 * */
	private static double[] z = null;
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
	private static int window = 500;
	/**
	 * Sample used for testing EarlyStopping
	 */
	private static Sample testSet;
	/**
	 * The handle of function Sample Sequencer that sequencing the input and target   
	 */
	private SampleSequencer sequencer;
	/**
	 * EarlyStopCounter
	 */
	private static int earlyStopCounter = 0;
	/**
	 * InputBuffer
	 */
	private static double[] inputBuffer = null;
	// Net specifications
	private static int DimIn;
	private static int DimOut;
	private static int hid;
	private static Net net;
	private static Net netCopy;

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

	public HLSTM_May21(final int in, final int hid, final int out) {

		HLSTM_May21.DimIn = in;
		HLSTM_May21.DimOut = out;
		this.hid = hid;
		// Specs of LSTM
		LSTMGenerator gen = new LSTMGenerator();
		gen.inputLayer(in);
		gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.LINEAR,true);
		gen.outputLayer(out, CellType.LINEAR);
		//		net = GenerateNetworks.generateNet("LSTM-1-tanh18-linear1");

		// Generate LSTM
		net = gen.generate();
		netCopy = net.copy();
		init();

		// Initialize weights
		Random rnd = new Random();
		weights[0] = 0;
		DoubleTools.fill(weights, 1, weightsnum, rnd, -0.1, 0.1);
		bestweights = weights;
		net.writeWeights(weights, 0);
		// System.out.println(" init weights : " +
		// Arrays.toString(net.getWeights()));
	}

	protected void init() {
		// There are N weights for all connections, but the length of the vector must be
		// N+1, as the rest of  non-connected cells has virtual links with weight zero.
		this.weightsnum = net.getWeightsNum(); // because getWeightsNum()+1 == getWeights.length
		this.links = net.getLinks();
		HLSTM_May21.linksnum = net.getLinksNum();
		this.cellsnum = net.getStructure().cellsnum;
		
		this.weights = new double[net.getWeights().length];
		this.dweights = new double[weights.length];
		this.dweightslast = new double[weights.length];
		this.dweightsMomentum = new double[weights.length];
		
		// for sequencing sample
		if (sequencer == null) {
			sequencer = new SampleSequencer(window, DimIn,DimOut);
		}
		// this weightsnum+1 thing  due to one space to store non-connected links weight values 0;
		// we write weights from the second weight by offseting 1 space, i.e., net.writeWeights(weights,1);
		
		HLSTM_May21.activity= new double[DimIn]; 
		HLSTM_May21.lastgradacts = new double[DimIn];
		HLSTM_May21.dactsMomentum = new double[DimIn];
		
		inputBuffer = new double[weights.length];

		MeanSquare = new double[weights.length];
		for(int i= 0; i<weights.length; i++){
			MeanSquare[i] = 1.0;
		}
		g = new double[weights.length];
		for(int i= 0; i<weights.length; i++){
			g[i] = 1;
		}
		z = new double[DimIn];
		for(int i = 0; i<z.length ; i++){
			z[i] = 1; 
		}
	}
	public double[] predict(double[] x) {
		SampleSequencer Copy = new SampleSequencer(sequencer);
		Sample sample = Copy.addSampleInput(x);
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
		Copy.addSampleTarget(y);
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
		earlyStopCounter++;
		if( earlyStopCounter%window==0){
			isEarlyStop=true;
			getTestSample(sample);
		}
	
		
	}
	
	/** Update with momentum method.
	 * 
	 * @param sample A matrix that contains input sequence and target sequence.
	 */
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
		int epoch = 20;
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
		if(DEFAULT_EARLYSTOP){
		bestweights= EarlyStop(testSet,bestweights,weights);
		net.writeWeights(bestweights, 0);
		}

		net.rebuffer(1);
	}
	/**
	 * The function is used for training early-stopping. If the newly trained weights produce larger
	 * MeanSqaureError than the old weights, then terminate the training.
	 * 
	 * @return stopTrain Boolean True if best weights generate smaller error than new weights. 
	 */
	private static double[] EarlyStop( Sample testSet, double[] bestWeights, double[] newWeights){
		
		
		netCopy.writeWeights(bestWeights, 0);
		double bestErr = ComputeError(testSet,netCopy);
		
		

		netCopy.writeWeights(newWeights, 0);
		double newErr = ComputeError(testSet,netCopy);
		movingMeanErr = 0.9*movingMeanErr + 0.1*newErr;
		if(bestErr>movingMeanErr){
		 bestWeights= newWeights;	
		}
		
		return bestWeights;
	}

	public static void getTestSample(Sample testSet){
		
		HLSTM_May21.testSet = testSet; 
		
	}
	
	private static double ComputeError(Sample testSet, Net netCopy){
		
		MSEAccumulator accumulater = new MSEAccumulator();

		double[] value = new double[DimOut];
		double[] target = testSet.target.getRow(window -1).data;
		double[] h = new double[DimIn];
		netCopy.reset();
		netCopy.setFrameIdx(0);
		for( int t =0; t< testSet.input.rows; t++){
			h=testSet.input.getRow(t).data;
			netCopy.input(h, 0);
			netCopy.compute();
			if( t != window-1) {net.incrFrameIdx();} // Sebastian said this is wrong.
		}
		netCopy.output(value, 0);		
		for (int i = 0 ; i<value.length; i++){
			accumulater.accumulate(value[i], target[i]);
		}
		
		return accumulater.error();
		
	}
    /**
    * Use the forward model inversely to generate a motor command to receive a 
	 * desired change in sensory information
	 * 
	 * @param d
	 * 		the desired sensory change
	 * @return motor command believed to most likely cause the desired change in
	 * in sensory information
	 */
	public double[] inverseForwardModel(double d) {
		double[] tar = {d};

		
		inputBuffer = BadHack.getInputBuffer(net, 0);	
		
		// compute the weightMatrix and deltaMatrix
		double[] ReadWeights = new double[weights.length];
		net.readWeights(ReadWeights, 0);
		Matrix WeightMatrix = new Matrix(cellsnum,cellsnum);
		
		for(int i =0; i< linksnum; i++){
			int xIndex = links[3*i+0];
			int yIndex= links[3*i+1];
			int zIndex =links[3*i+2];
			double weightValue = ReadWeights[zIndex];
			WeightMatrix.set(xIndex, yIndex , weightValue);
		}
//		System.out.println("Weights : " +  WeightMatrix.toString());
		
		net.setFrameIdx(window-1);
		double[] deltaOut = net.getGradOutputBuffer(0);
		
		net.target(tar, 0);
		net.injectError(); 
		
		// activity descent loop
		int epoch = 100;
		for (int j = 0; j<epoch; j++){

		net.computeGradient();
		
		
				
		double[] deltas = net.getGradInputBuffer(0); // a_h
			
//		System.out.println("deltas : " + Arrays.toString(deltas));
			
			
//			 *  Array[1] InputGate
//			 *  Array[2] ForgetGate
//			 *  Array[3] CellGate
//			 *  Array[8] OutputGate
			 
		int[] gatesIDs = {1,2,3,8};	
		CellArray[] array = net.getStructure().arrays;

		double[] deltasOnGates = new double[net.getStructure().cellsnum];
		for(int i = 0; i< gatesIDs.length;i++){
			int idx = gatesIDs[i];
			for(int p = array[idx].cellslbd; p<=array[idx].cellsubd; p++){
			deltasOnGates[p]=deltas[p];
			}
		}
			
		double[] dacts = new double[DimIn];
		
		// for input value cell, its ID is 0, which you can see in arrays[0] cellslbd is the lowerbound
	    // of index, cellsubd is the index upperbound. We only have to compute the gradient descend of 
		// cell 0. 
		for(int i=array[0].cellslbd ; i<=array[0].cellsubd; i++){
			
			activity[i]=inputBuffer[i];
			double[] weights = WeightMatrix.getRow(i).data;
			dacts[i]=PAO.sum(PAO.prod(weights, deltasOnGates));
		
		
			// prop matheod (adaptive learning rate)
			double lbd = 0.1; double ubd = 10;// limiting range: [0.1, 10], [0.1,100]
			if (dacts[i]*lastgradacts[i]>0){
				if(z[i]<ubd){
					z[i] = z[i] + 0.05;
				}
			}else if(dacts[i]*lastgradacts[i]<0){
				if(z[i]>lbd){
					z[i] = z[i]*0.95;
				}
			}else{
				//DO NOTHING WHEN dacts*lastgradacts ==0 ;
			}
		
		
	// for input value cell, its ID is 0, which you can see in arrays[0] cellslbd is the lowerbound
	// of index, cellsubd is the index upperbound. We only have to compute the gradient descend of 
	// cell 0. 
			
			// basic method (on- line)
			dactsMomentum[i]= mu*z[i]*dacts[i];
			activity[i]+= dactsMomentum[i];
			lastgradacts[i] = dactsMomentum[i];
			
			// momentum method (full-batch)
//			dactsMomentum[i]=mu*z[i]*dacts[i]+alpha*lastgradacts[i];
//			activity[i]+= dactsMomentum[i];
//			lastgradacts[i]=dactsMomentum[i];
//			System.out.println("activity[t] :" + activity[i]);
			}
	//net.writeActivity(activity); You don't need this since getOutputBuffer gives you reference.
		}
		return activity;
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
	public HLSTM_May21 copy() {
		HLSTM_May21 clone = new HLSTM_May21(DimIn, hid, DimOut);
		clone.sequencer =  new SampleSequencer(sequencer);
		clone.weights = this.weights.clone();
		clone.errors = new LinkedList<Double>();
		clone.errorVariance = 0.0;
		clone.meanError = 0.0;

		return clone;

	}

	/* making a hard copy of the LSTM model,along with the errors. */
	public HLSTM_May21 copyWithErrors() {
		HLSTM_May21 clone = this.copy();
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
