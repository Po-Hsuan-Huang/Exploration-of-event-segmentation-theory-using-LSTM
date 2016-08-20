package de.jannlab.examples.pingpong_offline;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.CubicCurve2D;
import java.util.Arrays;
import java.util.Currency;
import java.util.Random;

import javax.swing.JFrame;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.examples.tools.OnlineDiagram;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.tools.RegressionValidator;
import de.jannlab.training.GradientDescent;

public class Main {

	// time steps of simulation
	final static int timespan = 200;
	// current time step
	private static int step;
	// left boundary of world
	static double WorldLeftBound = 0;
	// right boundary of world
	static double WorldRightBound = 1;

	/** the 1D world is one dimensional */

	public static double[] array(double... x) {
		return x;
	}

	public static Sample generateSample(int timespan, boolean withNoise) {

		double[] currentlocation = new double[timespan];
		double[] nextlocation = new double[timespan];

		// Initialization
		double velocity = 0.05;
		double location = 0;
		Ball ball = new Ball(location, velocity);

		ball.start();
		if (ball.isStart()) {
			for (int t = 0; t < timespan; t++) {
//				System.out.println("\n" + t);

				currentlocation[t] = ball.getLoc();

//				System.out.println("current loc : " + currentlocation[t]);

				if (currentlocation[t] <= WorldLeftBound
						&& ball.getVelocity() < 0) {
					ball.bounce();
				}
				if (currentlocation[t] >= WorldRightBound
						&& ball.getVelocity() > 0) {
					ball.bounce();
				}

				ball.fly();

				nextlocation[t] = ball.getLoc();

			}

			ball.stop();
		}
		if(withNoise){
			
			Random noiseGen  = new Random();
			double[] noise = new double[timespan] ;
			
			for(int i = 0 ; i < timespan; i++){
				noise[i] =  -0.1+ 0.2*noiseGen.nextDouble() + nextlocation[i];
				}

			return new Sample(currentlocation, noise,1,timespan,1,timespan);
		
		}else{
			
			return new Sample(currentlocation, nextlocation,1,timespan,1,timespan);
		}
	}

	// Net specifications
	static int length = 30;
	private static Random rnd = new Random(0L);

	public static Net genLSTM(int in, int hid, int out) {

		LSTMGenerator gen = new LSTMGenerator();
		
		gen.inputLayer(in);
		gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.TANH,
				true);
		gen.outputLayer(out, CellType.TANH);
		//

		Net net = gen.generate();
		net.rebuffer(length);
		net.initializeWeights(rnd);

		return net;
	}
	
	
    

	

	public static void main(String[] args) {
//		
//		Generate samples
		final  int epochs = 200;
		final double learningrate = 0.001;
		final double momentum = 0.9;
		
		Sample sample = generateSample(timespan, true);
		SampleSet trainset = new SampleSet();
		trainset.add(sample);
		System.out.println("trainset : "+ sample.toString());		
		
		Sample test= generateSample((int) (0.2*timespan), false);
		SampleSet testset= new SampleSet();
		testset.add(test);
		System.out.println("testset : "+ test.toString());		
		

		
//		
//		Generate network
//		
		Net net1 = genLSTM(1, 3, 1);
		Net net2 = genLSTM(1, 3, 1);

//		
//		Start Training	(OFFLINE)
//		
		System.out.println("offline :\n ");
		GradientDescent trainer = new GradientDescent();
		trainer.setOnline(false); 
		trainer.setTrainingSet(trainset);
		trainer.setNet(net1);
		trainer.setRnd(rnd);
		trainer.setPermute(true);
		trainer.setLearningRate(learningrate);
		trainer.setMomentum(momentum);
		trainer.setEpochs(epochs);
		trainer.train();
		
		
		System.out.println("-----------------------\n");
//		
//		Start Training	(ONLINE)
//
		System.out.println("online :\n ");

		GradientDescent trainer2 = new GradientDescent();
		trainer2.setOnline(true); 
		trainer2.setTrainingSet(trainset);
		trainer2.setNet(net2);
		trainer2.setRnd(rnd);
		trainer2.setPermute(true);
		trainer2.setLearningRate(learningrate);
		trainer2.setMomentum(momentum);
		trainer2.setEpochs(epochs);
		trainer2.train();
		
//		
//		evaluation
//		
//		offline training
	
		  final double thres = 0.04;
	        
	            RegressionValidator v1 = new RegressionValidator(net1, thres);
	            for (Sample s : testset) {
	                v1.apply(s);
	            }
	            
	            System.out.println("validation set result offline : " + (v1.ratio() * 100) + "%.");
	        
	
//		on-line training.¥	
      
	          RegressionValidator v2 = new RegressionValidator(net2, thres);
	          for (Sample s : testset) {
	              v2.apply(s);
	          }
	          
	          System.out.println("validation set result online : " + (v2.ratio() * 100) + "%.");
	}

}
