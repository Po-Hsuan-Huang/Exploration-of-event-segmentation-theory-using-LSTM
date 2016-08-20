import java.util.Arrays;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.generator.RNNGenerator;
import de.jannlab.math.Matrix;
import de.jannlab.tools.DoubleTools;
import de.jannlab.tools.NetTools;


public class main3 {
	
	/**
	 * create samples with all zeros.
	 * Use MLP to prediction next datapoint, which should be zero as well.
	 */
	
public static void main(String args[]) {
	// TODO Auto-generated constructor stub
	Matrix data = new Matrix(100,1);
	Matrix target = new Matrix(100,1);

	Sample sample = new Sample(data, target);
	for(int i = 0; i<100;i++){
		//sample.input.set(i,0, Math.sin(0.1*Math.PI*i + Math.PI));
		//sample.target.set(i, 0, Math.sin(0.1*Math.PI*i));
		Random rnd0 = new Random() ;
		sample.input.set(i, 0, rnd0.nextDouble());
		sample.target.set(i,0 ,1);
	}
	
	// Generate MLP

	int DimIn = 1;
	int DimOut = 1;
	int hid = 2;
	// Specs of LSTM
	LSTMGenerator gen = new LSTMGenerator();
	gen.inputLayer(DimIn);
	gen.hiddenLayer(hid, CellType.SIGMOID,CellType.TANH,CellType.TANH, true);
	gen.outputLayer(DimOut, CellType.LINEAR);

	// Generate LSTM
	Net net = gen.generate();
	// initialize weights
	int weightsnum = net.getWeightsNum();
	Random rnd = new Random();
	double[] weights = new double[weightsnum];
	DoubleTools.fill(weights, 0, weightsnum, rnd, -0.1,0.1);
	net.writeWeights(weights, 0);
	
	// Print weights
	System.out.println("initial weights : " + Arrays.toString(weights));
	
	// Training session
	
	
	// Permute the seamples	
	double[] dweightslast = new double[net.getWeightsNum()];
	for(int j =0; j< 4000; j ++){
	//System.out.println("sample : " + sample.toString());
	sample = permute(sample);		
		
	int epoch = 100;
	for (int e = 0;e < epoch;e++){
	
		
	net.rebuffer(100);
	net.reset();
	net.setFrameIdx(0);
	
	for(int t = 0; t< net.getFramesNum(); t ++){
		// sample.input.get(int t, int offset)
		double[] in = {sample.input.get(t , 0)};
		net.input(in,0);
		if(t < 100 -1){ net.incrFrameIdx();}
		}	
		net.compute();

	net.setFrameIdx(net.getFramesNum()-1);
		double[] tar = {sample.target.get(net.getFramesNum()-1, 0)};
		net.target(tar, 0);
		net.injectError();
	
	
//	int frameIdx = net.getFrameIdx();
	
	// compute the weight gradient
	NetTools.performBackward(net);		
	double[] gradweights = new double[net.getWeightsNum()];
	net.readGradWeights(gradweights, 0);
	for (int i = 0; i < gradweights.length; i++) {
		weights[i] += 0.0005*gradweights[i]+0.9*dweightslast[i];
		dweightslast[i] = 0.0005*gradweights[i];
	}
	net.writeWeights(weights, 0);
	net.rebuffer(1);
	
	}
	System.out.println("weights : " + Arrays.toString(weights));
	//	if (error <0.1){}
}
	// Test Set
	double meanError = 0.0;
	meanError=Test(net,sample);
	System.out.println("MeanError : "+ meanError );

	
}
	public static Sample permute(Sample sample){
		Sample sampleTemp = sample.copy();
		int num = sample.input.rows;
//		int rndIdx = new Random().nextInt(100);
		int rndIdx = 1;
		for (int i =0 ; i<num; i++){
			if (i< num-rndIdx){
				sampleTemp.input.set(i, 0, sample.input.get(i+rndIdx, 0));
				sampleTemp.target.set(i, 0, sample.target.get(i+rndIdx, 0));
			}
			else if (i >=num-rndIdx){
				sampleTemp.input.set(i,0,sample.input.get(i+rndIdx-num, 0));
				sampleTemp.target.set(i, 0, sample.target.get(i+rndIdx-num, 0));

			}
		}
		return sampleTemp;
		
	}
	
	public static double Test(Net net,Sample sample){
		double[] x1 = new double[1];
		double[] y1 = new double[1];
		double error = 0.0;
		double meanError;
		int samplesnum = sample.input.rows;
		sample = permute(sample);
		for(int i = 0; i<samplesnum; i++){
			x1[0]= sample.input.get(i,0);
			net.reset();
			net.input(x1, 0);
			net.compute();
			net.output(y1,0);
			System.out.println("x => y : " +x1[0]+ "=>"+ y1[0]);
			error += Math.abs(1-y1[0]); 
		}
			meanError = error/samplesnum;
		return meanError;
	}
	
}

