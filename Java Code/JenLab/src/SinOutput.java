import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import javax.swing.JFrame;

import de.jannlab.Net;
import de.jannlab.data.Data;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.examples.tools.OnlineDiagram;
//import de.jannlab.generator.GenerateNetworks;
import de.otte.dcm.net.GenerateNetworks;
import de.jannlab.io.Serializer;
import de.jannlab.tools.DoubleTools;


public class SinOutput {

	final static Random rnd = new Random(100);

	
    public static void showSample(
    	final Sample samples
    ) {
            
        final OnlineDiagram diagram = new OnlineDiagram(
            samples.input.rows, -2, 2, 2
        ); 
        
    	diagram.assignColor(0, Color.GREEN);
    	diagram.assignColor(1, Color.RED);
        //
        diagram.setVisible(0, true);
    	double[] data = new double[2];
    	//
        for (int i = 0; i < samples.input.rows; i++) {
    		data[0] = samples.input.get(i, 0);
    		data[1] = samples.target.get(i, 0);
    		diagram.record(data);
        }
        diagram.repaint();
        //
        JFrame frame = new JFrame("Signal view");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(diagram);
        frame.setSize(800, 400);
        frame.setVisible(true);
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
	                case KeyEvent.VK_ESCAPE:
	                    System.exit(0);
	                    break;
                }
            }
        });
        
    }
	
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		
		int samples = 100;
		int T = 300;
		int deltat = 10;
		
		PrintStream s = new PrintStream(new FileOutputStream(new File("bag.nn")));
		
		for (int i = 0; i < samples; i++) {
			
			double[] input  = new double[T];
			double[] target = new double[T];
			
			double alpha = rnd.nextDouble() * 2.0 * Math.PI;
			double phi = 0.1; //0.05 + (rnd.nextDouble() * 0.25); 
			
			for (int t = 0; t < T; t++) {
				input[t]  = Math.sin((t * phi) + alpha); 
				target[t] = Math.sin(((t + deltat) * phi) + alpha); 
			}
			
			
			s.println(DoubleTools.asString(input, " "));
			s.println(DoubleTools.asString(target, " "));
			
			
		}

		SampleSet sampleset = Data.loadSamples("bag.nn", 1, 1);
		//Sample input = sampleset.get(0);// ArrayList ?
		System.out.println(sampleset.get(0).input);
		System.out.println(sampleset.get(0));

		showSample(sampleset.get(0));// sampleset includes input-> label pairs
		
		
		
		Net net = GenerateNetworks.generateNet("LSTM-1-tanh6-linear1");
		double[] weights = Serializer.read("myweight.weights");
		net.writeWeights(weights, 0);
		
		net.reset();
		
//		
//		int epoch = 20;
//		for (int i = 0; i < epoch; i++) {
//			// for a single timestep		
//			Sample input = sampleset.get(i);
//			
//			String input2 = input.toString();
//			net.input();
//			net.compute();
//			net.output();
//			
//			
//		}
		
		
		
		
		
		
		
		System.out.println(net);
		
		
	}
	
}