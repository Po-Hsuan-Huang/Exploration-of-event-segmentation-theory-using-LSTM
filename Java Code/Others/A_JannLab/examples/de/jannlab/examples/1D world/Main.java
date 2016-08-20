import java.util.ArrayList;
import java.util.Arrays;

public class Main {
	// time steps of simulation
	final static int timespan = 100000;
	// current time step
	private static int step;
	// left boundary of world
	static double WorldLeftBound = 0;
	// right boundary of world
	static double WorldRightBound = 1;
	static LSTMModel Predictor ;
	
	
	/**the 1D world is one dimensional*/
	private static double[] currentlocation = new double[1];
	private static double[] nextlocation = new double[1];
	static WriteObject writer;
	ReadObject reader;
	static ArrayList<Double> errors = new ArrayList<Double>(1);
	

	public static void main(String[] args) {
		// Initialization
		double velocity = 0.1;
		double location =0;
		Ball ball = new Ball(location,velocity);
		Predictor = new LSTMModel(1,1);
		
		ball.start();
		if (ball.isStart()) {
			for (int t = 0; t < timespan; t++) {
				
				currentlocation[0] = ball.getLoc();
				System.out.println("location of the ball : " + Arrays.toString(currentlocation));
				
				if (currentlocation[0] <= WorldLeftBound && ball.getVelocity() < 0	) {
					ball.bounce();
				}
				if( currentlocation[0] >= WorldRightBound && ball.getVelocity() > 0){
					ball.bounce();
				}
				
				ball.fly();
				
				nextlocation[0] = ball.getLoc();
				double error= Math.abs(nextlocation[0] - Predictor.predict(currentlocation)[0]);
				System.out.println("prediction error : " + error);
				errors.add(error); 

				Predictor.update(currentlocation,nextlocation);

			}
		}

		ball.stop();
		WriteObject.Write(errors, "errors");


	}

}
