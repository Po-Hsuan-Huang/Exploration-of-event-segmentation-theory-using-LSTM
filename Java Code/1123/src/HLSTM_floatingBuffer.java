import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import org.hamcrest.core.IsEqual;
import org.w3c.dom.stylesheets.LinkStyle;

import de.jannlab.Net;
import de.jannlab.core.BadHack;
import de.jannlab.core.CellArray;
import de.jannlab.core.CellType;
import de.jannlab.core.Link;
import de.jannlab.core.NetBase;
import de.jannlab.core.NetStructure;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleProvider;
import de.jannlab.data.SampleSet;
import de.jannlab.error.MSEAccumulator;
import de.jannlab.generator.GenerateNetworks;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.math.Matrix;
import de.jannlab.tools.DoubleTools;
import de.jannlab.tools.NetTools;
/**
 * This one is for discontinuous function such as jigsaw trajectory.
 * Floating buffer should be faster in prediction 
 * 
 * 
 * 
 * */
public class HLSTM_floatingBuffer  {
	//
	public static final double DEFAULT_ALPHA = 0.9;//0.9;
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
	 * Sample used for testing EarlyStopping
	 */
	static Sample testSet;
	/**
	 * InputBuffer
	 */
	private static double[] inputBuffer = null;
	/**
	 * Buffer Size : before the data sequence reach MaxBuffersize. in length, we has to adjust the rebuffer size to the length of the sequence.
	 * 
	 */
	private static int bufferSize = 0;
	/**
	 * Max Buffer Size :  when bufferSize reach the maxBufferSize, we fix the rebuffer size. 
	 */
	private static int MaxBufferSize = window;
	
	// Net specifications
	static int DimIn;
	static int DimOut;
	final int hid;
	private NetStructure struct;
	private static Net net;
	private static Net netCopy;

	public HLSTM(final int in, final int hid, final int out) {

		HLSTM.DimIn = in;
		HLSTM.DimOut = out;
		this.hid = hid;
		// Specs of LSTM
		LSTMGenerator gen = new LSTMGenerator();
		gen.inputLayer(in);
//		gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.TANH,true); // ( gates, cell-input, cell-output)
		gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.LINEAR, true);
		gen.outputLayer(out, CellType.LINEAR);

		// Generate LSTM
		net = gen.generate();
		netCopy= net.copy();
//		net = GenerateNetworks.generateNet("LSTM-1-tanh18-linear1");

		init();
	
		// Initialize weights	
		Random rnd = new Random();
		weights[0] = 0;
		DoubleTools.fill(weights, 1, weightsnum, rnd, -0.1,0.1);
		bestweights = weights;
		net.writeWeights(weights, 0);
		System.out.println("struct : " + net.getStructure());
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

		// this weightsnum+1 thing  due to one space to store non-connected links weight values 0;
		// we write weights from the second weight by offseting 1 space, i.e., net.writeWeights(weights,1);
		
		HLSTM.activity= new double[DimIn]; 
		HLSTM.lastgradacts = new double[DimIn];
		HLSTM.dactsMomentum = new double[DimIn];
		
		MeanSquare = new double[weights.length];
		for(int i= 0; i<weights.length; i++){
			MeanSquare[i] = 1.0;
		}
		g = new double[weights.length];
		for(int i= 0; i<weights.length; i++){
			g[i] = mu;
		}

		
	}
	/**
	 * LSTM predict() should take in x of length=window to predict the imminent output. 
	 */
	/*
	public double[] predict(double[] x) {
		double[] h = new double[1];
		final double[] y = new double[DimOut];
		net.rebuffer(100);
		net.reset();
		net.setFrameIdx(0);
		for( int t =0; t< window; t++){
		h[0]=x[t];
		net.input(h, 0);
		if( t != window-1) {net.incrFrameIdx();}
		}
		net.compute();
		if(net.getFrameIdx()==window-1){ net.output(y, 0);}; // predicting sensation
		net.rebuffer(1);

		return y;
	}
	*/

	public double[] predict(double[] x) {
		double[] h = new double[1];
		final double[] y = new double[DimOut];
		//net.rebuffer(1);
		net.reset();
		net.setFrameIdx(0);
		for( int t =0; t< window; t++){
			h[0]=x[t];
			net.input(h, 0);
			net.compute();
			if( t != window-1) {net.incrFrameIdx();} // Sebastian said this is wrong.
		}

		//if(net.getFrameIdx()==window-1){ 
		net.output(y, 0);
		
		//}; // predicting sensation
//		net.rebuffer(1);

		return y;
	}
	
	/** Update with momentum method.
	 * 
	 * @param sample A matrix that contains input sequence and target sequence.
	 */
/*
	public void update(Sample sample) {
		int epoch = 100;
		net.rebuffer(sample.input.rows);
		for (int e = 0; e<epoch; e++){
			net.reset();
			net.setFrameIdx(0);
			for(int t = 0; t< sample.input.rows; t ++){
				// sample.input.get(int t, int offset)
				double[] in = {sample.input.get(t, 0)};
				net.input(in,0);
				net.compute();	
				double[] tar = {sample.target.get(t, 0)};
				net.target(tar, 0);
				net.injectError();
				if(t < (sample.input.rows -1)) { net.incrFrameIdx();}
			}
	
//			double[] tar = {sample.target.get(sample.target.rows - 1, 0)};
//			net.target(tar, 0);
//			net.injectError();
					
	//		for (int t= net.getFramesNum()-1; t >=0 ; t--){
	//			double[] tar = {sample.target.get(t, 0)};
	//			net.target(tar, 0);
	//			net.injectError();
	//			if(t >0){ net.decrFrameIdx();}
	//		}
			//int frameidx = net.getFrameIdx();
	
			
//			weights = net.getWeights(); Never use get weights, otherwise you get  a reference.
			net.readWeights(weights, 0);
			// compute the weight gradient
	//		NetTools.performBackward(net);
			net.computeGradient();
			double[] delta = new double[weightsnum];
			net.readGradWeights(dweights, 0);
	//		accumulateWeightsDiffs(frameidx); // why it give rise to different dweight from readGradWeights?
			for (int i = 0; i < net.getWeights().length; i++) {
				dweightsMomentum[i] = mu*dweights[i]+alpha*dweightslast[i];
				weights[i] += dweightsMomentum[i];
				this.dweightslast[i] = dweightsMomentum[i];
				this.dweights[i] = 0.0;
				
				}
			net.writeWeights(weights, 0);
			
	//		System.out.println("weights : " + Arrays.toString(net.getWeights()));
	//		this.resetWeightDiffs();
	//		this.accumulateWeightsDiffs(frameIdx);
	//		this.computeWeightDiffs();
	//		this.adjustWeights();

		}
		net.rebuffer(1);
	}
	*/
	/** Update rprop with Nesterov momentum method.
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
				double[] in = {sample.input.get(t, 0)};
				net.input(in,0);
				net.compute();	
				double[] tar = {sample.target.get(t, 0)};
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
			double[] RMS = RMSquare(dweights);
			
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
		
		bestweights= EarlyStop(testSet,bestweights,weights);
		
		net.writeWeights(bestweights, 0);
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
	
	public static void getTestSample(Sample testSet){
		
		HLSTM.testSet = testSet; 
		
	}
	
     /**
     * Use the forward model inversely to generate a motor command to receive a 
	 * desired change in sensory information
	 * 
	 * This inverse model doesn't allow consecutive inverse prediction. Its inner states has to be updated by previous target
	 * via forward prediction
	 * @param y
	 * 		the desired sensory change
	 * @return motor command believed to most likely cause the desired change in
	 * in sensory information
	 */
	public double[] inverseForwardModel(double y) {
		double[] tar = {y};
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
//		double[] deltas =net.getGradInputBuffer(0);
//		for(int i= 0; i<deltas.length;i++){
//			deltas[i] =0; 
//				}
		net.target(tar, 0);
		net.injectError(); 
		net.computeGradient();
				
		double[] deltas = net.getGradInputBuffer(0); // a_h
		inputBuffer = BadHack.getInputBuffer(net,0);		

//		System.out.println("deltas : " + Arrays.toString(deltas));
			
			/**
			 *  Array[1] InputGate
			 *  Array[2] ForgetGate
			 *  Array[3] CellGate
			 *  Array[8] OutputGate
			 */ 
		int[] gatesIDs = {1,2,3,8};	
		CellArray[] array = net.getStructure().arrays;

		double[] deltasOnGates = new double[net.getStructure().cellsnum];
		for(int i = 0; i< gatesIDs.length;i++){
			int idx = gatesIDs[i];
			for(int p = array[idx].cellslbd; p<=array[idx].cellsubd; p++){
			deltasOnGates[p]=deltas[p];
			}
		}
			
		double[] dacts = new double[array[0].cellsnum];
		for(int i=array[0].cellslbd ; i<=array[0].cellsubd; i++){
			activity[i]=inputBuffer[i];
			double[] weights = WeightMatrix.getRow(i).data;
			dacts[i]=PAO.sum(PAO.prod(weights, deltasOnGates));
		}
	// for input value cell, its ID is 0, which you can see in arrays[0] cellslbd is the lowerbound
	// of index, cellsubd is the index upperbound. We only have to compute the gradient descend of 
	// cell 0. 
		for(int i =0; i< activity.length;i++){
					
			dactsMomentum[i]=-mu*dacts[i]+alpha*lastgradacts[i];
			activity[i]+= dactsMomentum[i];
			lastgradacts[i]=dactsMomentum[i];
//			System.out.println("activity[t] :" + activity[i]);
			}
	inputBuffer=activity; 
	//net.writeActivity(activity); You don't need this since getOutputBuffer gives you reference.
		
		return activity;
	}


	
	/**
	 * A a matrix of one row.
	 * B a matrix of one row.
	 * 
	 * A,B must have the same number of columns.
	 * */
	private double innerProduct(Matrix A, Matrix B){
		
		double c = 0;
		if(A.cols == B.cols){
			for(int i =0; i<A.cols; i++){
				c += A.get(0, i)*B.get(0, i);
			}
		}else{
			System.out.println("Error : A,B not of the same size !");
			}
		return c;
		
	}
	
	/**
	 *  Calculate the scaling factor of adaptive learning rate.
	 *  MeanSquare= 0.9 *MeanSquareLast +0.1 * innerProduct(dweights, dweights);			 
	 *  @param a double[] dweights.
	 * */
	private double[] RMSquare(double[] a){
		
		double[] prod = PAO.prod(a, a);
		double[] scprod = PAO.scalarprod(prod, 0.1);
		double[] scprod2 = PAO.scalarprod(MeanSquare, 0.9);
		double[] plus = PAO.sum(scprod,scprod2);
		MeanSquare = plus;
		double[] RMS = PAO.sqrt(MeanSquare); 
		return RMS;
		
	} 

	/**
	 * Accumulates the weight differences for the given frame index (time index)
	 * and the current gradient results.
	 * 
	 * @param frameidx
	 *            Frame index (time index).
	 */
	private void accumulateWeightsDiffs(final int frameidx) {
		//
		// collect deltas * activations .
		//
		for (int t = 0; t <= frameidx; t++) {
			//
			final double[] outputs = HLSTM.net.getOutputBuffer(t);
			final double[] deltas = HLSTM.net.getGradOutputBuffer(t);
			
//			double[] deltas = new double[weightsnum] ;
//			HLSTM.net.readGradWeights(deltas, 0);
			//
			/*
			 * // # DEBUG # final double[] deltasin = net.getGradInputBuffer(t);
			 * for (int i = 0; i < deltas.length; i++) { this.deltasdbg[i] +=
			 * Math.abs(deltas[i]); this.deltasindbg[i] +=
			 * Math.abs(deltasin[i]); } // #
			 */
			int off = 0;
			//
			for (int i = 0; i < HLSTM.linksnum; i++) {
				//
				final int src = this.links[off + Link.IDX_SRC];
				final int dst = this.links[off + Link.IDX_DST];
				final int widx = this.links[off + Link.IDX_WEIGHT];
				//
				if (widx > 0) {
					final double dk = deltas[dst];
					final double xj = outputs[src];
					//
					final double dw = (dk * xj);
					this.dweights[widx] += (dw);
				}
				//
				off += Link.LINK_SIZE;
			}
		}
	}

	/**
	 * Finally computes the weight differences based on the accumulated values.
	 */
	private void computeWeightDiffs() {
		//
		// add momentum and multiply learning rate mu.
		//
		for (int i = 1; i <= this.weightsnum; i++) {
			//
			// calculate momentum.
			//
			final double mw = this.dweightslast[i] * this.alpha;
			this.dweights[i] = (this.mu * this.dweights[i]) + mw;
		}
		
		System.out.println("dweights : " +  Arrays.toString(weights));
	}

	/**
	 * Adjusts the weights.
	 */
	private void adjustWeights() {
		//
		for (int i = 1; i <= this.weightsnum; i++) {
			//
			this.weights[i] += this.dweights[i];
			/*
			 * // # DEBUG # this.weightsdbg[i] += Math.abs(this.dweights[i]); //
			 * #
			 */
		}
		
		net.writeWeights(weights, 1);

	}

    /**
     * PARAMETERS OF ERROR CALCULATION 
     */
	private LinkedList<Double> errors = new LinkedList<Double>();
	private int denom ;
	private double meanError = 0.0;
	private double errorVariance = 0.0;

	public void updateError(double e) {
		if (errors.size() < window) {
			errors.add(e);
		} else if (errors.size() == window) {
			errors.removeFirst();
			errors.addLast(e);
		}
		denom  =errors.size();
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
	public void clear(){
		net = null;
		System.out.println("Net Cleared");
	}
	


}
