package Hiwi;

public interface ScenarioManager {
	
	public boolean useScriptedMovement(int time);
	public boolean removeObject(int time);
	public int newObjectType(int time);
	public Double[] getDesiredSensation(int time, InteractableObjects object);
	public void effectOnModels(int time, ModelFitting models);
	public boolean autonoumosPlanning(int time, int relevantDim, ModelFitting models);

}
