package Hiwi;

public class FlyScenario implements ScenarioManager {


	@Override
	public boolean removeObject(int time) {
		return time == 1000;
	}

	@Override
	public int newObjectType(int time) {
		return 200;
	}

	@Override
	public Double[] getDesiredSensation(int time, InteractableObjects object) {
		if(time > 3000){
			if(object.isMerged()){
				Double[] goal = {null, null, (double) 52.0, (double) 52.0, null, null, null, null, null};
				return goal;
			}
			else{
				Double[] goal = {null, null , null, null, 0.5, -5.0, null, null, null};
				return goal;
			}
		}
		else if(time > 2000 && !object.isMerged()){
			Double[] goal = {null, null, null, null, 0.5, -5.0, null, null, null};
			return goal;
		}
		Double[] nulls = {null, null, null, null, null, null, null, null, null};
		return nulls;
	}

	@Override
	public boolean useScriptedMovement(int time) {
		return time < 200;
	}

	@Override
	public void effectOnModels(int time, ModelFitting models) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean autonoumosPlanning(int time, int relevantDim, ModelFitting models) {
		return time > 5000;
	}

}
