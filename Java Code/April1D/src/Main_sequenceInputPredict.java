
import java.util.ArrayList;
import java.util.Random;

import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.math.Matrix;

/**
 *  This Main Class runs the 1-D simulation of a point ball bouncing between [0, 1] with
 *  constant velocity. The Brain that made of LSTM model tries to predict the location
 *  of the ball in the next time step.
 *  
 * */




public class Main_sequenceInputPredict {





	/** parameters of the physical model */

	public static double physicsModel(Ball ball,double input) {
		
		// left boundary of world
		final double WorldLeftBound = -1;
		// right boundary of world
		final double WorldRightBound = 1;

		if (ball.isStart()) {
			if ((double)Math.round(input*10)/10 == WorldLeftBound  && ball.getVelocity() < 0) {
				ball.bounce();
			}
			if ((double)Math.round(input*10)/10 == WorldRightBound  && ball.getVelocity() > 0) {
				ball.bounce();
			}
			ball.fly();
		}
		return ball.getLoc();
	}


	
	
	/** Parameters for main method */

	public static void main(String[] args) {
		/**
		DAFAULT VALUES:
				int timespan = 2000;
				int T = 100;
				double velocity = 0.05;
				double location = 0;
		*/
		
		// Physics params
		int timespan = 1000;
		int testSection = 100;
		 // training sequence length.
		int window = 100;

		double currentlocation;
		double[] datum = new double[1];

		double nextlocation ;
		double[] data = new double[window];
		double[] predict = new double[1];
		double[] target = new double[1];
		// Initialization

		double velocity = 0.2;
		double location = 0;
		Random noise = new Random(11);
		
		
		//Initiation of models
		Ball ball  = new Ball(location,velocity);
		SampleSequencer_before sequencer = new SampleSequencer_before();
		Sample sample = null ;
		
		HLSTM Brain = new HLSTM(1,3,1); // JannLab doesn't support online learning.
		Brain.mu = 0.01;
		Brain.alpha = 0;
		Brain.window = window;
		// Training phase
		Brain.init();
		ball.start();
		
		
		//Write file
		ArrayList<Double> PRED = new ArrayList<Double>();
		ArrayList<Double> TARGET = new ArrayList<Double>();
		ArrayList<Double> ERR = new ArrayList<Double>();
		ArrayList<Double> ERRVAR = new ArrayList<Double>();

		// Start Simulation
		for (int t = 0; t < timespan; t++) {
			/** Initial Phase
			 * When the length of simulation smaller than the required sequence length,
			 * NN doesn't predict nor update. The sample is accumulated until sufficient to predict.
			 * the Error is assume 0 before reaching sequence length.
			 */
			if(t< window){
				System.out.println("\n" + t);
				//phyiscs
				 currentlocation = ball.getLoc();
				 data[0] = currentlocation;
				// Make Prediction
//				 System.out.println("current loc : " + currentlocation);
				 nextlocation= physicsModel( ball, currentlocation);
			 	 target[0] = nextlocation;
			 	 
				 sample = sequencer.getSample(data, target);
				 TARGET.add(nextlocation);
				 PRED.add(currentlocation);
//				 Brain.updateError(0);
				 
			}
			/** Training Phase
			 * When the sample reaches sequence length, NN starts to predict and update.
			 * Adding noise to prevent improve generalization.
			 */
			else if (t>= window && t < timespan){
	
				if(t == window){
					Brain.getTestSample(sample);
					Brain.update(sample);
					}
				
		
				//phyiscs
				currentlocation = ball.getLoc();
				// add Uniform noise
//				datum[0] = currentlocation +(0.5*velocity)* (2*(noise.nextFloat()-0.5));
				// add Gaussian noise.
				double sdd = 0.5*velocity;
				datum[0] = currentlocation ;//+ (sdd*noise.nextGaussian());

				// Make Prediction
				 sample = sequencer.addSampleInput(datum);
				 data = sample.input.getColumn(0).data;
				 predict[0] = Brain.predict(data)[0];
		 
				 // WRITE FILE
				 PRED.add(predict[0]);
				 WriteFiles.Write(PRED, "pred");
								
			 	 nextlocation= physicsModel( ball, currentlocation);
			 	 target[0] = nextlocation;
			 	 
				 //WRITE FILE
				 TARGET.add(target[0]);
				 WriteFiles.Write(TARGET, "target");
				 
				 sample  = sequencer.addSampleTarget(target);
				 Brain.update(sample);
				 
				 Brain.updateError(predict[0]-target[0]);
				 ERR.add(Brain.getMeanError());
				 ERRVAR.add(Brain.getErrorVariance());
				 // WRITE FILE
				 WriteFiles.Write(ERR, "err");
				 WriteFiles.Write(ERRVAR, "errvar");
				 
				 System.out.println("d : " + (double)Math.round(10000*datum[0])/10000+ " p: " + (double)Math.round(10000*predict[0])/10000  +" t : "+ (double)Math.round(10000*target[0])/10000 + " e : " + Brain.getMeanError());

				}
			
				 
			 }
		/**  Testing Phase
		 *  	The model stops updating, but making prediction based on 
		 *   previous prediction. A successful NN should be able to predict the 
		 *   sequential pattern without adjacent location data, base on its long term 
		 *   memory.   
		 * */
		currentlocation = 0;
		 for(int t=0;  t < testSection ; t++){
			 datum[0]=currentlocation;
			 sample = sequencer.addSampleInput(datum);
			 data = sample.input.getColumn(0).data;
			 predict[0] =(double) Math.round(10*Brain.predict(data)[0])/10;
			 currentlocation=predict[0];	 
			 System.out.println("[ " + (double)Math.round(10*datum[0])/10 + "] -> [ " + 
			 (double)Math.round(10*predict[0])/10  + "] ");

			}
		ball.stop();
		Brain.clear();

		}
		
	}



