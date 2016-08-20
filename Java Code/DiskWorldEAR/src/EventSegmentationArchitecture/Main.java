package EventSegmentationArchitecture;

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
			if (input <= WorldLeftBound && ball.getVelocity() < 0) {
				ball.bounce();
			}
			if (input >= WorldRightBound && ball.getVelocity() > 0) {
				ball.bounce();
			}
			ball.fly();
		}
		return ball.getLoc();
	}

	/** Parameters for main method */

	public static void main(String[] args) {
		// Physics params
		int timespan = 200;
		double[] currentlocation = new double[timespan];
		double[] nextlocation = new double[timespan];
		// Initialization

		double velocity = 0.05;
		double location = 0;

		// Brain params
		
		
		//Initiation of models
		Ball ball  = new Ball(location,velocity);
		HLSTM Brain = new HLSTM(1,3,1);
		
		// Training phase
		Brain.init();
		ball.start();
		
		for (int t = 0; t < timespan; t++) {
			System.out.println("\n" + t);

		//phyiscs
		currentlocation[t] = ball.getLoc();
		// Make Prediction
		 double[] data = {currentlocation[t]};
		 double[] predict = Brain.predict(data);
		 System.out.println("predction : " + predict[0]);
		System.out.println("current loc : " + currentlocation[t]);
		 nextlocation[t]= physicsModel( ball, currentlocation[t]);
		// Train Brian 
		 double[] target = {nextlocation[t]};
		 Brain.update(data , target);


		 
		}
		
		ball.stop();
		
	}
}
