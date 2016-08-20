package de.jannlab.examples.pingpong_online;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.tools.DocumentationTool.Location;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.core.NetStructure;
import de.jannlab.data.Sample;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.tools.NetTools;
import de.jannlab.examples.pingpong_online.HLSTM;

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
		int timespan = 2000;
		 // training sequence length.
		int T = 100;

		double[] currentlocation = new double[T];
		double[] nextlocation = new double[T];
		// Initialization

		double velocity = 0.05;
		double location = 0;

		// Brain params
		
		
		//Initiation of models
		Ball ball  = new Ball(location,velocity);
		HLSTM Brain = new HLSTM(1,3,1); // JannLab doesn't support online learning.
		// Training phase
		Brain.init();
		ball.start();
		
		
		//Write file
		ArrayList<Double> PRED = new ArrayList<Double>();
		ArrayList<Double> TARGET = new ArrayList<Double>();
		ArrayList<Double> ERR = new ArrayList<Double>();
		ArrayList<Double> ERRVAR = new ArrayList<Double>();

		
		for (int t = 0; t < timespan; t++) {
			
			
		if(t< T){
			System.out.println("\n" + t);

			//phyiscs
			currentlocation[t] = ball.getLoc();
			// Make Prediction
			 System.out.println("current loc : " + currentlocation[t]);
			 nextlocation[t]= physicsModel( ball, currentlocation[t]);
			 // nextlocation[t] = ball.getVelocity();
			// Train Brian 
			 
			 TARGET.add(nextlocation[t]);
			 PRED.add(currentlocation[t]);
			 
			 Brain.updateError(0);
			 
		}
		
		else if (t >=T){
			
			if(t == T){
				double[] data = currentlocation;
				double[] target = nextlocation;
				
				Brain.update(data , target);	
			}
			
			
			System.out.println("\n" + t);
			for (int i = 0; i<T-1; i ++){
			currentlocation[i]= currentlocation[i+1];}
		//phyiscs
		currentlocation[T-1] = ball.getLoc();
		// Make Prediction
		 double[] data = currentlocation;
		 double[] la = {data[T-1]};
		 double[] predict = Brain.predict(la);
		 System.out.println("predction : " + predict[0]);
		 
		 // WRITE FILE
		 PRED.add(predict[0]);
		 WriteFiles.Write(PRED, "pred");

		System.out.println("current loc : " + currentlocation[T-1]);
		for (int i = 0; i<T-1; i ++){
			nextlocation[i]= nextlocation[i+1];}
		 nextlocation[T-1]= physicsModel( ball, currentlocation[T-1]);
		 //nextlocation[T-1] = ball.getVelocity();

		// Train Brian 
		 double[] target = nextlocation;
		 System.out.println("data length : "+ data.length);
		 System.out.println("target length : "+ target.length);

		 Brain.update(data , target);
		  
		 
		 //WRITE FILE
		 TARGET.add(target[T-1]);
		 WriteFiles.Write(TARGET, "target");
		 // UPDATE ERROR
		 Brain.updateError(predict[0]-target[T-1]);
		 
		 ERR.add(Brain.getMeanError());
		 ERRVAR.add(Brain.getErrorVariance());
		 System.out.println("Mean error : " + Brain.getMeanError());
		 // WRITE FILE
		 WriteFiles.Write(ERR, "err");
		 WriteFiles.Write(ERRVAR, "errvar");
		 

		 
		}

		 
		}
		
		ball.stop();
		System.out.println("isOnline : " + Brain.isOnline());
	}
}
