package Hiwi;

import java.util.Random;

public class BurrdockLearningScenario implements ScenarioManager{

	int timeToAddHeavyBurrdock = 0;
	int timeToAddBurrdock = 10000;//30000;
	int timeForAll = 15000;//35000;
	
	int BURRDOCK = 0;
	int HEAVYBURRDOCK = 100;
	int FLY = 200;
	
	Random r;
	
	public BurrdockLearningScenario(Random r){
		this.r = r;
	}

	@Override
	public boolean useScriptedMovement(int time) {
		return time < 200;
	}

	@Override
	public boolean removeObject(int time) {
		return time == 1000;
	}

	@Override
	public int newObjectType(int time) {
		if(time < timeToAddHeavyBurrdock){
			return FLY;
		}

		else if (time < timeToAddBurrdock){
			return HEAVYBURRDOCK;
		}
		else if(time < timeForAll){
			return BURRDOCK;
		}
		return r.nextInt(2) * 100;
	}

	@Override
	public Double[] getDesiredSensation(int time, InteractableObjects object) {
		
		if(time % 5000 < 250){
			Double[] goal = {100.0, 100.0, null, null, null, null, null, null, null};
			return goal;
		}
		
		
		if(time > 3000){
			if(object.isMerged()){
				Double[] goal = {null, null, (double) 52.0, (double) 52.0, null, null, null, null, null};
				return goal;
			}
			else if(object instanceof Fly){
				Double[] goal = {null, null , null, null, 0.5, -5.0, null, null, null};
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
	public void effectOnModels(int time, ModelFitting models) {
		/*if(time == timeToAddHeavyBurrdock){
			models.increaseMaxNrOfModels(2);
			models.increaseMaxNrOfModels(3);
			models.increaseMaxNrOfModels(4);
			models.increaseMaxNrOfModels(5);
		}*/
		if(time == timeToAddBurrdock){
			models.increaseMaxNrOfModels(2);
			models.increaseMaxNrOfModels(3);
		}
		
	}

	@Override
	public boolean autonoumosPlanning(int time, int relevantDim, ModelFitting models) {
		if(time % 5000 < 250 )
			return false;
		/*if(time < timeToAddHeavyBurrdock && time > 5000)
			return true;
		if(time > timeToAddHeavyBurrdock + 2000 && time < timeToAddBurrdock )
			return true;
		
		return time > timeToAddBurrdock + 2000;*/ return true;
	}


}
