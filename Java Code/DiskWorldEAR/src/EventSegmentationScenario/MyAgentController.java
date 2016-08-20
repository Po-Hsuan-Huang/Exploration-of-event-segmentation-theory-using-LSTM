package EventSegmentationScenario;

import diskworld.interfaces.AgentController;

public interface MyAgentController extends AgentController{
	public void doTimeStep(double[] sensorValues, double actuatorValues[]);
	public void doAfterTimeStep();
	public void finishPrints();
	public boolean readyToStop();
}
