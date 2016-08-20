package EventSegmentationScenario;

import diskworld.interfaces.AgentController;

/**
 * New AgentController interface for arm-class.
 * 
 * @author Christian Gumbsch
 *
 */
public interface MyAgentController extends AgentController{
	
	public void doTimeStep(double[] sensorValues, double actuatorValues[]);
	
	/**
	 * Called after every doTimeStep
	 */
	public void doAfterTimeStep();
	
	/**
	 * Called when the simulation is finished to
	 * print possible results into a log-file
	 */
	public void finishPrints();
}
