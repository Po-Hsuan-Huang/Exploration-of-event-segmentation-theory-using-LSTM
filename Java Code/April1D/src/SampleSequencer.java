import de.jannlab.data.Sample;
import de.jannlab.math.Matrix;


public class SampleSequencer{
	
	
	private Matrix inputTemp;
	private Matrix targetTemp;
	private int dimIn  =1;
	private int dimOut =1;
	public  int window;
	

	/**
	 * Construct inputTemp, targetTemp
	 *  of sequence length 100, size 1.
	 * */
	public SampleSequencer(){
		 inputTemp = new Matrix(100, 1);
		 targetTemp = new Matrix(100, 1);
		 dimIn = 1 ;
		 dimOut = 1;
	}
	/**
	 * Construct Sample sequence of length t, size i;
	 * */
	public SampleSequencer(int timeFrames,int inputDim, int targetDim){
		 inputTemp = new Matrix(timeFrames, inputDim);
		 targetTemp = new Matrix(timeFrames, targetDim);
		 dimIn = inputDim;
		 dimOut = targetDim;
		 window = timeFrames;
	}

	/**
	 * Use copy constructor to create a hard copy ;
	 * */
	public  SampleSequencer(SampleSequencer copy){
		this.inputTemp = copy.inputTemp.copy();
		this.targetTemp = copy.targetTemp.copy();
		this.dimIn = copy.dimIn;
		this.dimOut = copy.dimOut;
		this.window = copy.window;
	
	}
	
	
	public  Sample getSample(double[] input, double[] target) {
		// the input and the desired states are given by constructing tuple.
		// define the length of the windows by the length of the input sequence.
		// input and target sequence has to be of the same length (not long term memory, but simple MLP)
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
		
		
		// define the length of the windows by the length of the input sequence.
		// input and target sequence has to be of the same length (not long term memory, but simple MLP)
		int window = inputTemp.rows; 
		double Null = 99;
		/* update the input matrix. */
		/** Fill the null space of the input Sequence in the last storage space stores null */
		if(inputTemp.get(window-1, 0) == Null ){
			for (int i = 0; i < dimIn; i++) {
				inputTemp.set(window-1, i, input[i]);			
				}
		}
		/**Update the sequence as usual, but instead of new target datum, the latest target is set to null */
		else if (inputTemp.get(window-1, 0)!=Null){
			double[] target = new double[dimOut];
			
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
						for (int i = 0; i < dimIn; i++) {
							inputTemp.set(t, i, input[i]);				
						}
						for (int i = 0; i < dimOut; i++) {
							target[i]= Null;
							targetTemp.set(t, i, target[i]);
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
		// define the length of the windows by the length of the input sequence.
		// input and target sequence has to be of the same length (not long term memory, but simple MLP)
		double Null = 99;
		/* update the input matrix. */

		/** Fill the null space of the target Sequence is the last storage space stores null */
		if(targetTemp.get(window-1, 0)==Null){
			for (int i = 0; i < dimOut; i++) {
				targetTemp.set(window-1, i, target[i]);			
				}
		}
		/**Update the sequence as usual, but instead of new target datum, the latest target is set to null */
		else if (targetTemp.get(window-1, 0)!=Null){
			double[] input = new double[dimIn];
			
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
					for (int i = 0; i < dimIn; i++) {
						input[i]=  Null;
						inputTemp.set(t, i, input[i]);}
					for (int i = 0; i < dimOut; i++) {
						targetTemp.set(t, i, target[i]);}
					} 
			}
		}
		 Sample sample = new Sample(inputTemp, targetTemp);
		return sample;

	}
}
