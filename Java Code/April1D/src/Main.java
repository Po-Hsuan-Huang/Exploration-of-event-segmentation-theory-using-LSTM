
import java.util.ArrayList;

import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.math.Matrix;

/**
 *  This Main Class runs the 1-D simulation of a point ball bouncing between [0, 1] with
 *  constant velocity. The Brain that made of LSTM model tries to predict the location
 *  of the ball in the next time step.
 *  
 * */




public class Main {

	/** parameters of the physical model */

	public static double physicsModel(Ball ball,double input) {
		
		// left boundary of world
		final double WorldLeftBound = 0;
		// right boundary of world
		final double WorldRightBound = 1;

		if (ball.isStart()) {
			if (input <= WorldLeftBound + Math.pow(10,-6) && ball.getVelocity() < 0) {
				ball.bounce();
			}
			if (input >= WorldRightBound - Math.pow(10,-6) && ball.getVelocity() > 0) {
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
		int timespan = 800;
		int trainSection = 605;
		 // training sequence length.
		int window = 100;

		double currentlocation;
		double nextlocation ;
		double[] data = new double[1];
		double[] predict = new double[1];
		double[] target = new double[1];
		// Initialization

		double velocity = 0.05;
		double location = 0;
		
		
		//Initiation of models
		Ball ball  = new Ball(location,velocity);
		SampleSequencer_before sequencer = new SampleSequencer_before();
		Sample sample = null ;
		HLSTM Brain = new HLSTM(1,3,1); // JannLab doesn't support online learning.
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
				 Brain.updateError(0);
				 
			}
			/** Training Phase
			 * When the sample reaches sequence length, NN starts to predict and update.
			 */
			else if (t>= window && t < trainSection){
			System.out.println("\n" + t);
	
				if(t == window){	Brain.update(sample);}
				
		
				//phyiscs
				currentlocation = ball.getLoc();
//				System.out.println("loc : " + currentlocation);
				// Make Prediction
				 data[0] = currentlocation;
				// predict[0] = Math.pow(10, 5.5)* Brain.predict(data)[0];
				 predict[0] = Brain.predict(data)[0];

				 System.out.println("predction : " +  predict[0]);
		 
				 // WRITE FILE
				 PRED.add(predict[0]);
				 WriteFiles.Write(PRED, "pred");
			
				 // System.out.println("current loc : " + currentlocation);
					
			 	 nextlocation= physicsModel( ball, currentlocation);
			 	 target[0] = nextlocation;
//			 	 System.out.println("data length : "+ data.length);
//			 	 System.out.println("target length : "+ target.length);
//			 	 System.out.println("target : " + target[0]);
			 	 sample  = sequencer.getSample(data,target);
				 Brain.update(sample);
			  
			 
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
			 else if(t>= trainSection && t < timespan){
				 System.out.println(t);
				if(t == trainSection){predict[0]=target[0];}
				
				currentlocation = predict[0];
//				System.out.println("loc : " + currentlocation);
				 data[0] = currentlocation;
				 predict[0] = Brain.predict(data)[0];

				 System.out.println("predction : " +  predict[0]);
				 
				 // WRITE FILE
				 PRED.add(predict[0]);
				 WriteFiles.Write(PRED, "pred");
				 nextlocation = target[0];
				 nextlocation= physicsModel( ball, nextlocation);
			 	 target[0] = nextlocation;			 	 

			 	//WRITE FILE
				 TARGET.add(target[0]);
				 WriteFiles.Write(TARGET, "target");
				 
				 
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
		ball.stop();

		}
		
	}



