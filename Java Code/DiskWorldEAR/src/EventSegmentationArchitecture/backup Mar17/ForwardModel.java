package EventSegmentationArchitecture;



/**
 * Forward model implementation for the Event Segmentation Architecture.
 * The forward model predicts sensory changes based on motor commands and are
 * updated afterwards. A forward model also keeps track of the prediction error 
 * over the last T-time steps (e.g. T = 100) to estimate the accuracy of the 
 * prediction.
 * 
 * @author Christian Gumbsch
 *
 */

public  interface ForwardModel {
	
	
	/**
	 * Predict the sensory change when performing a given motor command
	 * 
	 * @param x
	 * 		motor command vector used to compute the prediction
	 * 
	 * @return prediction of the sensory change y based on x
	 */
	public double[] predict(double[] x );
	
	/**
	 * Update the forward model
	 * 
	 * @param x
	 * 		motor command executed
	 * @param y
	 * 		sensory change measured after executing x
	 */

	public void update(double[] x, double y);
	
	
	/**
	 * ForwardModel keeps track of the prediction error over the last T-time steps 
	 * (T=1000 in LinearRLSModel).
	 * 
	 * @param e
	 * 	the error of the last prediction
	 */
	public void updateError(double e);
	
	/**
	 * Getter for the mean prediction error
	 * 
	 * @return mean error of prediction of the last T-time steps this model was active
	 */
	public double getMeanError();
	
	
	/**
	 * Getter for the variance of prediction error
	 * 
	 * @return variance of the prediction error over the last T-time steps this model was active
	 */
	public double getErrorVariance();
	
	
	/**
	 * Use the forward model inversely to generate a motor command to receive a 
	 * desired change in sensory information
	 * 
	 * @param y
	 * 		the desired sensory change
	 * @return motor command believed to most likely cause the desired change in
	 * in sensory information
	 */
	public double[] inverseForwardModel(double y);
	
	
	/**
	 * Create a hard copy of this forward model. Only difference is, that the prediction
	 * mean prediction error and the variance of the prediction error are reset in the copy.
	 * 
	 * @return hard copy of the forward model
	 */
	public ForwardModel copy();
	
	/**
	 * Create a hard copy of this forward model, also containing the stored prediction erros of
	 * the last T-time steps.
	 * 
	 * @return hard copy of the forward model
	 */
	public ForwardModel copyWithErrors();
	
	/**get model name*/
	public String getName();
	/**get Weights*/
	public double[] getWeights();
	
	
	
	

}
