import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;

import de.jannlab.data.Sample;
import de.jannlab.math.Matrix;


public class SampleSequencer{
	
	
	private Matrix inputTemp;
	private Matrix targetTemp;
	

	/**
	 * Construct inputTemp, targetTemp
	 *  of sequence length 100, size 1.
	 * */
	public SampleSequencer(){
		 inputTemp = new Matrix(100, 1);
		 targetTemp = new Matrix(100, 1);
	}
	/**
	 * Construct Sample sequence of length t, size i;
	 * */
	public SampleSequencer(int t,int i){
		 inputTemp = new Matrix(t, i);
		 targetTemp = new Matrix(t, i);
	}

	
	
	public  Sample getSample(double[] input, double[] target) {
		// the input and the desired states are given by constructing tuple.
	
		int dimIn = 1;
		int dimOut = 1;
		// define the length of the windows by the length of the input sequence.
		// input and target sequence has to be of the same length (not long term memory, but simple MLP)
		int window = inputTemp.rows; 
		/* update the input matrix. */
		// construct temporaryInput Matrix
		for (int t = 0; t < window; t++) {
				// shift the time window by one.
				if (t < window -1){
				for (int i = 0; i < dimIn; i++) {
					inputTemp.set(t, 0, inputTemp.get(t+1,i));}
				for (int i = 0; i < dimOut; i++) {
					targetTemp.set(t, 0, targetTemp.get(t+1,i));}
				}
				
				if (t == window-1) {
				// add the latest sensation x to the beginning of the sequence.
				for (int i = 0; i < dimIn; i++) {inputTemp.set(t, i, input[i]);				}
				for (int i = 0; i < dimOut; i++) {targetTemp.set(t, i, target[i]);
				}
				} 
		}
		
		 Sample sample = new Sample(inputTemp, targetTemp);
		 return sample;
	}
	
	
	/**
	 * Update the input sequence of sample. If the last input datum is not null,
	 * the target sequence is shifted by one space,
	 * leading to null value in targetTemp[window-1]. 
	 * 
	 * input double[]  The present input.
	 * @param <T>
	 */
	public  Sample  addSampleInput(double[] input) {

		// the input and the desired states are given by constructing tuple.
		
		int dimIn = 1;
		int dimOut = 1;
		// define the length of the windows by the length of the input sequence.
		// input and target sequence has to be of the same length (not long term memory, but simple MLP)
		int window = inputTemp.rows; 
		double Null = 99;
		/* update the input matrix. */
		/** Fill the null space of the input Sequence is the last storage space stores null */
		if(inputTemp.get(window-1, 0) == Null ){
			for (int i = 0; i < dimIn; i++) {
				inputTemp.set(window-1, i, input[i]);			
				}
		}
		/**Update the sequence as usual, but instead of new target datum, the latest target is set to null */
		else if (inputTemp.get(window-1, 0)!=Null){
			double[] target = new double[1];
			target[0]=  Null;
			
			for (int t = 0; t < window; t++) {
					// shift the time window by one.
					if (t < window -1){
					for (int i = 0; i < dimIn; i++) {
						inputTemp.set(t, 0, inputTemp.get(t+1,i));}
					for (int i = 0; i < dimOut; i++) {
						targetTemp.set(t, 0, targetTemp.get(t+1,i));}
					}
					
					if (t == window-1) {
					// add the latest sensation x to the beginning of the sequence.
					for (int i = 0; i < dimIn; i++) {inputTemp.set(t, i, input[i]);				}
					for (int i = 0; i < dimOut; i++) {targetTemp.set(t, i, target[i]);
					}
					} 
			}
		}
		 Sample sample = new Sample(inputTemp, targetTemp);
		return sample;
		 
	}
	/**
	 * Update the target sequence of sample. The input sequence is shifted by
	 * one space, leading to null value in inputTemp[window-1]. 
	 * 
	 * target double[]  The present input.
	 */
	public  Sample  addSampleTarget(double[] target) {

		// the input and the desired states are given by constructing tuple.
		
		int dimIn = 1;
		int dimOut = 1;
		// define the length of the windows by the length of the input sequence.
		// input and target sequence has to be of the same length (not long term memory, but simple MLP)
		int window = inputTemp.rows;
		double Null = 99;
		/* update the input matrix. */

		/** Fill the null space of the target Sequence is the last storage space stores null */
		if(targetTemp.get(window-1, 0)==Null){
			for (int i = 0; i < dimIn; i++) {
				targetTemp.set(window-1, i, target[i]);			
				}
		}
		/**Update the sequence as usual, but instead of new target datum, the latest target is set to null */
		else if (inputTemp.get(window-1, 0)!=Null){
			double[] input = new double[1];
			input[0]=  Null;
			System.out.println("input : " + input[0]);
			
			for (int t = 0; t < window; t++) {
					// shift the time window by one.
					if (t < window -1){
					for (int i = 0; i < dimIn; i++) {
						inputTemp.set(t, 0, inputTemp.get(t+1,i));}
					for (int i = 0; i < dimOut; i++) {
						targetTemp.set(t, 0, targetTemp.get(t+1,i));}
					}
					
					if (t == window-1) {
					// add the latest sensation x to the beginning of the sequence.
					for (int i = 0; i < dimIn; i++) {inputTemp.set(t, i, input[i]);				}
					for (int i = 0; i < dimOut; i++) {targetTemp.set(t, i, target[i]);			}
					} 
			}
		}
		 Sample sample = new Sample(inputTemp, targetTemp);
		return sample;

	}
}
