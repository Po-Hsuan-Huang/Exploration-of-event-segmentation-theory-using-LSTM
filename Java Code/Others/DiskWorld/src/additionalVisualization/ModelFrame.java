package additionalVisualization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Visualization of the motivation module using progress bars
 * @author Christian Gumbsch
 *
 */
public class ModelFrame extends JFrame { 
	
	//Progressbars for the different motivations
	JProgressBar pbHunger;
	JProgressBar pbFear;
	JProgressBar pbContinuity;
	JProgressBar pbCuriosity;
	
	//Labels used
	JPanel labels;
	//Bars used
	JPanel bars;
	
	JLabel[] models;
	JLabel[] errors;
	JLabel[] deviation;

	/**
	 * Constructor
	 * @param startValueHunger, initial value of the hunger reservoir
	 * @param startValueFear, initial value of the fear reservoir
	 * @param startValueContinuity, initial value of the continuity reservoir
	 * @param startValueCuriosity, initial value of the curiosity reservoir
	 * @param hungerPriority, priority of hunger
	 * @param fearPriority, priority of fear
	 * @param continuityPriority, priority of continuity
	 * @param curiosityPriority, priority of curiosity
	 */
    public ModelFrame() { 
    	JFrame frame = new JFrame(); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setSize(350, 400);
        
        frame.setLocationRelativeTo(null); 
        frame.setTitle("  Models    "); 
        frame.setVisible(true);
        
        //Left Side of the window contains labels
        labels = new JPanel(new GridLayout(9,1));
        JLabel titleWest = new JLabel("Models:");
        labels.add(titleWest);
        models = new JLabel[8];
        JLabel label1 = new JLabel("  0 ");
        models[0] = label1;
        labels.add(label1);
        for(int j = 1; j < 8; j++){
        	models[j] = new JLabel("    ");
        	labels.add(models[j]);
        }

       
        
        frame.add(labels, BorderLayout.WEST);
        
        //Right side of the window contains progressbars of reservoirs
        bars = new JPanel(new GridLayout(9,1));
        
        JLabel title = new JLabel("        Mean Errors ");
        bars.add(title);
        errors = new JLabel[8];
        JLabel error1 = new JLabel("        ");
        errors[0] = error1;
        bars.add(error1);
        for(int j = 1; j < 8; j++){
        	errors[j] = new JLabel("        ");
        	bars.add(errors[j]);
        }
        
        frame.add(bars, BorderLayout.CENTER); 
        deviation = new JLabel[8];
        JPanel labels2 = new JPanel(new GridLayout(9,1));
        JLabel label21 = new JLabel("  Mean SD  ");
        labels2.add(label21);
        for(int i = 0; i < 8; i++){
        	deviation[i] = new JLabel(" ");
        	labels2.add(deviation[i]);
        }
        frame.add(labels2, BorderLayout.EAST); 
        
        frame.setLocationRelativeTo(this); 
        frame.setVisible(true);
    } 
    
    /**
     * Update the visualization with the new reservoirs of the motivations
     * @param hungerReservoir, new reservoir of hunger
     * @param fearReservoir, new reservoir of fear
     * @param continuityReservoir, new reservoir of continuity
     * @param curiosityReservoir, new reservoir of curiosity
     */
    public void updateFrame(LinkedList<Double> errors, LinkedList<Double> sd){
    	
    	int active = 0;
    	int whichActive = -1;
    	
    	for(int i = 0; i < 8; i++){
    		if(errors.get(i) == -1){
    			this.errors[i].setText("        ");
    			this.deviation[i].setText("        ");
    		}
    		else if (errors.get(i) == -2){
    			this.errors[i].setText("        ");
    			this.deviation[i].setText("        ");
    		}
    		else{
    			this.errors[i].setText("        " + errors.get(i));
    			this.deviation[i].setText("  " + sd.get(i));
    			active++;
    			whichActive = i;
    		}
    	}
    	
    	if(active == 1){
    		models[whichActive].setText("  " + whichActive + " ");
    	}
    }
} 