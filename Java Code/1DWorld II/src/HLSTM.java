import java.util.Random;

import com.sun.org.apache.bcel.internal.generic.NEW;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.GenerateNetworks;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.tools.DoubleTools;
import de.jannlab.training.GradientDescent;


public class HLSTM implements ForwardModel {

	//sequence length
	static int length = 100;
	
	// Net specifications
	int DimIn = 1;
	int DimOut = 1;
	int hid = 3;
	private static double[] weights;
	private static String modelType;
	private static Net net;

	private static Sample sample;

	HLSTM(){
		modelType = "LSTM-" + this.DimIn + "-tanh3-linear" + this.DimOut;
		
		Net net = GenerateNetworks.generateNet(modelType);

        net.rebuffer(length);
        
        weights = new double[net.getWeightsNum()];

		// fill(obj, offset, size, initial value, value_min,value_max)
		DoubleTools.fill(weights, 0, weights.length, new Random(), -0.1, 0.1);
		net.writeWeights(weights, 0);
		// the input and the desired states are given by constructing tuple.

	}

	@Override
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
    /**
    final double learningrate = 0.001;
    final double momentum     = 0.9;
    private static Random rnd = new Random(0L);
    */
	@Override
	public void update(double[] data, double[] target) {
/**	 GitHub JannLab library
    GradientDescent trainer = new GradientDescent();
    trainer.setNet(net);
    trainer.setRnd(rnd);
    trainer.setPermute(true);
    trainer.setLearningRate(learningrate);
    trainer.setMomentum(momentum);
    */
		
		
		
		
// TODO Auto-generated method stub
	final static double[] data =new double[length];
	
}
    public static Sample generateSample(final double current_input, final double current_target) {
       for (int i =0; i<length; i++){
    		if(i==length-1){ 
    			data[length-1] = current_input ;  }
    		else if(i>0){
    			data[i-1] = data[i]; }
       }
    		
       }
		return new Sample(data, target, 2, length, 1, 1);
    }
    
    public static SampleSet generate(final int n, final int length) {
        SampleSet set = new SampleSet();
        //
        for (int i = 0; i < n; i++) {
            set.add(generateSample(length));
        }
        //
        return set;
    

	@Override
	public void updateError(double e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getMeanError() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getErrorVariance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] inverseForwardModel(double y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ForwardModel copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ForwardModel copyWithErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getWeights() {
		// TODO Auto-generated method stub
		return null;
	}

}
