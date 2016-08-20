package de.jannlab.examples.pingpong_online;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.core.Link;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.misc.DoubleTools;
import de.jannlab.tools.NetTools;
import de.jannlab.training.GradientDescent;

public class HLSTM implements ForwardModel {
	  //
    public static final double  DEFAULT_ALPHA           = 0.9;
    public static final double  DEFAULT_MU              = 0.0001;  // 10^{-5}
    public static final int     DEFAULT_VLDINTERVAL     = 5;
    public static final boolean DEFAULT_EARLYSTOP       = false;
    public static final int     DEFAULT_EARLYSTOPCOUNT  = 10;
    public static final int     DEFAULT_EPOCHS          = 100;
    public static final double  DEFAULT_TARGETERROR     = 0.0;
    public static final boolean DEFAULT_ONLINE          = true;
    public static final boolean DEFAULT_PERMUTE         = true;
 
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
     * The last weight differences.
     */
    private double[] dweightslast = null;
    /**
     *  The current weight differences.
     */
    private double[] dweights = null;
    /**
     * The links of the reference network.
     */
    private int[] links = null;
    /**
     * The number of links.
     */
    private int  linksnum = 0;
    

	// sequence length
	static int length  = 100;

	// Net specifications
	static int DimIn;
	static int DimOut;
	final int hid;
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
		gen.outputLayer(out, CellType.LINEAR);
		//

		net = gen.generate();
		net.rebuffer(length);
		net.initializeWeights( ); // initialize weights with ranmdom seed 
		// it doesn't matter if the net is online or offline because the input length is
		// 1 in our case. However, this is a bad hack, too. Should try to implement by yourself.
	}
	 protected void init() {
	        //
	        
	        this.weights      = this.net.getWeights();
	        this.dweights     = new double[this.weights.length]; 
	        this.dweightslast = new double[this.weights.length];
	        //
	        this.weightsnum = net.getWeightsNum();
	        this.links      = net.getLinks(); 
	        this.linksnum   = net.getLinksNum();
	
	    }
	public double[] predict(double[] x) {
		
		final double[] y = new double[DimOut];
		net.reset();
//		int text = net.getFrameIdx();
//		System.out.println("Frameidx : " + text);
//		net.setFrameIdx(buffersize);
//		text = net.getFrameIdx();
//		System.out.println("SetFrameIdx : " + text);
		net.input(x, 0);
		net.compute();    
		net.output(y, 0); // predicting sensation
		return y;
	}
	
	// parameters
	SampleSet tset = new SampleSet();
	int buffersize = 0;
	
	public void update( double[] input, double[] target){
		
		
		// reset activities and weights to zero
//		net.reset();
		// Create Samples
		buffersize=net.getFrameWidth();
		int idx = net.getFrameIdx();
		System.out.println("buffersize : " + buffersize +" FrameIdx : " + idx);

		net.rebuffer(100);
		buffersize=net.getFrameWidth();
		 idx = net.getFrameIdx();		
		System.out.println("After rebuffer "+"buffersize : " + buffersize +" FrameIdx : " + idx);

		if(buffersize < 100){
			System.out.println("should not be here so far");
		Sample sample = new Sample(input, target,1,1, 1, 1);
		tset.add(sample);
		buffersize++;
		double error = NetTools.performForward(net, sample);
		} 
		else if( buffersize==100){
		Sample sample = new Sample(input, target,1,100, 1, 100);
		//tset.remove(0); 
		tset.add(sample);
		double error = NetTools.performForward(net, sample);

		}
		int frameidx = net.getFrameIdx();
		System.out.println("FW frame index : " +  frameidx);
		
		//compute the weight gradience
		NetTools.performBackward(net);  
		System.out.println("BW frame index : " +  frameidx);

		this.resetWeightDiffs();
		this.accumulateWeightsDiffs(frameidx);
		this.computeWeightDiffs();
        this.adjustWeights();
        net.rebuffer(1);
		
		
	}
            /**
             * Resets and current weight differences and stores them
             * in history memory.
             */
            private void resetWeightDiffs() {
                //
                // initialize weight diffs.
                //
                for (int i = 1; i <= this.weightsnum; i++) {
                    this.dweightslast[i] = this.dweights[i];
                    this.dweights[i]     = 0.0;
                }
            }
            
            /**
             * Accumulates the weight differences for the given frame index (time index) 
             * and the current gradient results.
             * @param frameidx Frame index (time index).
             */
            private void accumulateWeightsDiffs(final int frameidx) {
                //
                // collect deltas * activations .
                //
                for (int t = 0; t <= frameidx; t++) {
                    //
                    final double[] outputs = this.net.getOutputBuffer(t);
                    final double[] deltas  = this.net.getGradOutputBuffer(t);
                    //
                    /*
                    // # DEBUG #
                    final double[] deltasin = net.getGradInputBuffer(t);
                    for (int i = 0; i < deltas.length; i++) {
                        this.deltasdbg[i] += Math.abs(deltas[i]);
                        this.deltasindbg[i] += Math.abs(deltasin[i]);
                    }
                    // #
                    */
                    int off = 0;
                    //
                    for (int i = 0; i < this.linksnum; i++) {
                        //
                        final int src  = this.links[off + Link.IDX_SRC];
                        final int dst  = this.links[off + Link.IDX_DST];
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
             * Finally computes the weight differences based on the accumulated
             * values.
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
                    // # DEBUG #
                    this.weightsdbg[i] += Math.abs(this.dweights[i]);
                    // #
                    */
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


		} else {	return null;}

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
	private int denom = 100;

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
	
	public boolean isOnline() {
		return net.isOnline();
		
	}

}
