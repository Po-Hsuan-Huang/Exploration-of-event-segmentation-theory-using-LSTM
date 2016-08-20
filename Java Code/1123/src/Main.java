

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.hamcrest.Condition.Step;

import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.math.Matrix;

/**
 *  This Main Class runs the 1-D simulation of a point ball bouncing between [0, 1] with
 *  constant velocity. The Stairs that made of LSTM model tries to predict the location
 *  of the ball in the next time step.
 *  
 * */




public class Main{





	/** parameters of the physical model */

	public static double physicsModel(Stairs stairs,double input) {
		
		if (stairs.isStart()) {
			stairs.stepup();
			if(stairs.getLoc()>stairs.getCeiling()){ stairs.fall();}
		}
		return stairs.getLoc();
	}


	
	
	/** Parameters for main method */
	static double currentlocation;
	final static int window = 100;

	static double[] datum = new double[1];

	static double nextlocation ;
	static double desiredloc;
	static double[] data = new double[window];
	static double[] predict = new double[1];
	static double[] target = new double[1];
	// Initialization
	public static void main(String[] args) {

		
		// Physics params
		int InversepredictSection = 920;
		int predictSection = 910;
		int trainSection = 910;

		// Initialization

		double size = 0.2;
		double location = 0;
		Random noise = new Random(11);
		
		
		//Initiation of models
		Stairs stairs  = new Stairs(location,size,1,0);
		SampleSequencer sequencer = new SampleSequencer();
		Sample sample = null ;
		
		HLSTM Brain = new HLSTM(1,2,1); // JannLab doesn't support online learning.
		// Training phase
		Brain.init();
		stairs.start();
		
		
		//Write file
		ArrayList<Double> INPUT = new ArrayList<Double>();
		ArrayList<Double> PRED = new ArrayList<Double>();
		ArrayList<Double> TARGET = new ArrayList<Double>();
		ArrayList<Double> ERR = new ArrayList<Double>();
		ArrayList<Double> ERRVAR = new ArrayList<Double>();

		// Start Simulation
		for (int t = 0; t < InversepredictSection; t++) {
			/** Initial Phase
			 * When the length of simulation smaller than the required sequence length,
			 * NN doesn't predict nor update. The sample is accumulated until sufficient to predict.
			 * the Error is assume 0 before reaching sequence length.
			 */
			if(t< window){
				System.out.println("\n" + t);
				//phyiscs
				 currentlocation = stairs.getLoc();
				 data[0] = currentlocation;
				// Make Prediction
//				 System.out.println("current loc : " + currentlocation);
				 nextlocation= physicsModel( stairs, currentlocation);
			 	 target[0] = nextlocation;
			 	 
				 sample = sequencer.getSample(data, target);
				 INPUT.add(currentlocation);
				 PRED.add(currentlocation);
				 TARGET.add(nextlocation);
				 Brain.updateError(0);
				 
			}
			/** Training Phase
			 * When the sample reaches sequence length, NN starts to predict and update.
			 * Adding noise to prevent improve generalization.
			 */
			else if (t>= window && t < trainSection){
			System.out.println("\n" + t);
	
				if(t == window){	
					Brain.getTestSample(sample); // for Early stopping.
					Brain.update(sample);
					}
				
		
				//phyiscs
				currentlocation = stairs.getLoc();
				// add Uniform noise
				datum[0] = currentlocation +(0.5*size)* (2*(noise.nextFloat()-0.5));
				// add Gaussian noise.
				double sdd = 0.1*size;
				datum[0] = currentlocation + (sdd*noise.nextGaussian());
				INPUT.add(datum[0]);
				// Make Prediction
				 sample = sequencer.addSampleInput(datum);
				 data = sample.input.getColumn(0).data;
				 predict[0] = Brain.predict(data)[0];

				 System.out.println("predction : " +  predict[0]);
		 
				 // WRITE FILE
				 PRED.add(predict[0]);
				 WriteFiles.Write(PRED, "pred");
			
				  System.out.println("current loc : " + currentlocation);
					
			 	 nextlocation= physicsModel( stairs, currentlocation);
			 	 target[0] = nextlocation;
			 	 
//			 	 System.out.println("data length : "+ data.length);
//			 	 System.out.println("target length : "+ target.length);
//			 	 System.out.println("target : " + target[0]);
			 	 sample  = sequencer.addSampleTarget(target);
				 Brain.update(sample);
				 
//				 bestweights= EarlyStopping
				 
				 //WRITE FILE
				 TARGET.add(target[0]);
				 WriteFiles.Write(TARGET, "target");
				}
			/**  Testing Phase
			 *  	The model stops updating, but making prediction based on 
			 *   previous prediction. A successful NN should be able to predict the 
			 *   sequential pattern without adjacent location data, base on its long term 
			 *   memory.   
			 * */
			 else if(t>= trainSection && t < predictSection){
				 System.out.println(t);
				if(t == trainSection){predict[0]=target[0];}
				
				currentlocation = predict[0];
				System.out.println("loc : " + currentlocation);
				 datum[0] = currentlocation;
				 sample = sequencer.addSampleInput(datum);
				 data = sample.input.getColumn(0).data;
				 predict[0] = Brain.predict(data)[0];

				 System.out.println("predction : " +  predict[0]);
				 
				 // WRITE FILE			
				 INPUT.add(datum[0]);
				 WriteFiles.Write(INPUT, "input");
				 PRED.add(predict[0]);
				 WriteFiles.Write(PRED, "pred");
				 nextlocation = target[0];
				 nextlocation= physicsModel( stairs, nextlocation);
			 	 target[0] = nextlocation;			 	 
			 	 sequencer.addSampleTarget(target);
			 	//WRITE FILE
				 TARGET.add(target[0]);
				 WriteFiles.Write(TARGET, "target");
				 
				 
			 }
			/**
			 * Inverse predict section
			 */
			 else if(t>= predictSection && t < InversepredictSection){
				 System.out.println("\n "+ t);
				 
				 if(t==predictSection){
					 desiredloc = currentlocation;
					 TARGET.add(desiredloc);
					 WriteFiles.Write(TARGET, "target");
					 currentlocation =nextlocation;
   					 INPUT.add(currentlocation);
					 WriteFiles.Write(INPUT, "input");
				 }else {
				 currentlocation = nextlocation;
				 INPUT.add(nextlocation);
				 WriteFiles.Write(INPUT, "input");
				 }
				 datum[0]=desiredloc;
				 sample =sequencer.addSampleInput(datum);
				 data = sample.input.getColumn(0).data;
				 Brain.predict(data);
				 double[] quote = new double[10];
				 for(int i =0; i<10 ; i++){
					 quote[i]= data[99-i];
				 }
				 
				 System.out.println("last 10 input : " + Arrays.toString(quote));
				 predict = Brain.inverseForwardModel(desiredloc,currentlocation);
				 PRED.add(predict[0]);	 
				 WriteFiles.Write(PRED, "pred");
				 System.out.println("t-y : " + (desiredloc-currentlocation));
				 System.out.println("loc : " + currentlocation);
				 System.out.println("inverse prediction : " + predict[0]);
				 nextlocation=physicsModel(stairs,currentlocation);
				 desiredloc= currentlocation;
				 TARGET.add(desiredloc);
				 if(t != InversepredictSection-1){WriteFiles.Write(TARGET, "target");}

			 }
			 
			 // UPDATE ERROR
			 Brain.updateError(predict[0]-target[0]);
			 
			 ERR.add(Brain.getMeanError());
			 ERRVAR.add(Brain.getErrorVariance());
//			 System.out.println("Mean error : " + Brain.getMeanError());
			 // WRITE FILE
			 WriteFiles.Write(ERR, "err");
			 WriteFiles.Write(ERRVAR, "errvar");
			}
			
		
		
		
			
		
		stairs.stop();
		Brain.clear();

		}
		
	}



