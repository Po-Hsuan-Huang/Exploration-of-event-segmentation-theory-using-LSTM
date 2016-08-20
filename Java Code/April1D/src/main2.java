import java.util.ArrayList;
import java.util.Arrays;

import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.math.Matrix;


/**
 *  This Main Class runs the 1-D simulation of a point ball bouncing between [0, 1] with
 *  constant velocity. The Brain that made of LSTM model tries to predict the location
 *  of the ball in the next time step.
 *  
 * */

public class main2 {


	/** parameters of the physical model */

	public static double physicsModel(Ball ball,double input) {
		// left boundary of world
		final double WorldLeftBound = -1.0;
		// right boundary of world
		final double WorldRightBound = 1.0;

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
	// Physics params
			static int timespan = 1000;			
		// training sequence length.
			static int window = 100;
		//Write file
			static ArrayList<Double> INPUT = new ArrayList<Double>();
			static ArrayList<Double> PRED = new ArrayList<Double>();
			static ArrayList<Double> TARGET = new ArrayList<Double>();
			static ArrayList<Double> ERR = new ArrayList<Double>();
			static ArrayList<Double> ERRVAR = new ArrayList<Double>();
			static ArrayList<Double> WEI = new ArrayList<Double>();
			
			static Ball ball;
			static Sample sample;
	public static void main(String[] args) {
	

		// Initialization

		double velocity = 0.2;
		double location = 0.0;
		double currentlocation = location;
		double nextlocation  = location + velocity;
		double[] data = {currentlocation};
		double[] target = {velocity};
		double[] predict ;
		double[] inputvec = new double[window];

		//Initiation of models
		ball  = new Ball(location,velocity);
		SampleSequencer_before sequencer = new SampleSequencer_before(window,1);
		HLSTM Brain = new HLSTM(1,2,1); // JannLab doesn't support online learning.

		Brain.mu= 0.01;
		Brain.alpha = 0;
		HLSTM.window = window;
		// Training phase
		// Brain.init();
		ball.start();
		double tStart = System.nanoTime();
		for (int t = 0; t < timespan; t++) {
			if(t<window){
			//System.out.println("\n" + t);

			//phyiscs
			currentlocation = ball.getLoc();
			data[0] = currentlocation;

			nextlocation= physicsModel( ball, currentlocation);
			target[0] = ball.getVelocity();
//					 	 System.out.println("data length : "+ Arrays.toString(data));
//					 	 System.out.println("target length : "+ Arrays.toString(target));
			sample =sequencer.getSample(data,target);
			System.out.println("[" + (double)Math.round(sample.input.data[window-1]*100)/100 + "] => [" + (double)Math.round(100*sample.target.data[window-1])/100 + "]");

			}
			// train phase
			if( t >= window){
				
				if(t== window){
					
//					Brain.getTestSample(sample);
					Brain.update(sample);
				}
				
				// Make Prediction
				currentlocation = ball.getLoc();
				data[0] = currentlocation;
				INPUT.add(data[0]);
				WriteFiles.Write(INPUT,"input");
				sample = sequencer.addSampleInput(data);
				
				 for(int i =0;i<sample.input.rows; i++){
				 	inputvec[i] = sample.input.get(i, 0);
				 	}
				 
				predict = Brain.predict(inputvec);
				//System.out.println("predction : " + predict[0]);
				
				nextlocation = physicsModel(ball,currentlocation);
				target[0] = ball.getVelocity();
				sample = sequencer.addSampleTarget(target);
				Brain.update(sample);
				
				// UPDATE ERROR
				Brain.updateError(predict[0]-target[0]);
				ERR.add(Brain.getMeanError());
				ERRVAR.add(Brain.getErrorVariance());
				
				
				// WRITE FILE
				PRED.add(predict[0]);
				WriteFiles.Write(PRED, "pred");
				//WRITE FILE
				TARGET.add(target[0]);
				WriteFiles.Write(TARGET, "target");
				// WRITE FILE
				WriteFiles.Write(ERR, "err");
				WriteFiles.Write(ERRVAR, "errvar");


				 System.out.println(" d: " + Math.round(sample.input.data[window-1]*100)/100 + " p: " +
				 Math.round(100*predict[0])/100+ " t: "+ Math.round(sample.target.data[window-1]*100)/100+ " e: " + (Brain.getMeanError()));
			}
			
			
		}
		
		


		ball.stop();
		double tEnd = System.nanoTime();		// Start Simulation
		System.out.println("TimeLapse : " + (tEnd-tStart));
		// test phase

		double[] netIn = new double[window];
		double[] netOut = new double[1];
		 
		 
		for(int t =0; t<20; t++){
			
			for(int i =0;i<sample.input.rows; i++){
			 	netIn[i] = sample.input.get(i, 0);
			 	}
			netOut = Brain.predict(netIn);
		    System.out.println("[" + (double)Math.round(netIn[window-1]*100)/100 + "] => [" + (double)Math.round(1000*netOut[0])/1000 + "]");
		    data[0] = netIn[window-1] + netOut[0];
		    sample=sequencer.addSampleInput(data);

		}
		Brain.clear();
	}
	
}




