package Hiwi;

import java.util.LinkedList;

import javax.xml.transform.ErrorListener;

public class ModelFitting {
	//Flags:

	//states if a suprise that leads to no model change is added as a transition
	boolean allowSelftransitions = false;
	
	//int[] time;// = 0;
	
	//Keeps track of the prediction error for every dimension
	ProbabilityErrors[] pe;
	
	int dimIn;
	int dimOut;
	double initialGain;
	double lambda;
	
	private ModelList[] models;
	private int[] currentModels;
	
	//Saves the prediction after predict() to see how good it was when doTimeStep() is called
	private double[] currentPrediction;
	
	LinearRLSModel[] modelBackup;
	
	ModelList[] temporaryModels;
	
	
	int[] searchingTime;
	static int maxSearchingTime = 10;//20;
	
	GaussModelMatrix[] transitions;
	double[][] surprisingSensations;
	
	double[] lastSensation;
	
	//int maxModels = 2;
	int[] maxNrOfModels;
	
	//GaussianModel specialModel;
	long rs;
	
	LinkedList<Double> errorList = new LinkedList<Double>();
	
	double meanError = 0.0;
	double counterForError = 0;
	
	//int timeToSearchWithProbabilities = 25000;
	
	int[][][] correctPredictedModelTransitions;
	int[][][][] falsePredictedModelTransitions;
	int[] predictedModel;
	
	int[][] rewardingTransitions = {{1, 0}, {2, 0}, {3, 0}};
	
	Arm arm;
	
	Double[] errorsPerModel;
	
	public ModelFitting(int dimIn, double[] firstSensation, double initialGain, double lambda, long rs, Arm arm){
		
		this.arm = arm;
		this.rs = rs;
		this.dimIn = dimIn;
		this.dimOut = firstSensation.length;
		this.initialGain = initialGain;
		this.lambda = lambda;
		
		models = new ModelList[dimOut];
		for(int i = 0; i < dimOut; i++){
			double[] yI = {firstSensation[i]};
			LinearRLSModel temp = new LinearRLSModel(dimIn, yI, false, initialGain, lambda);
			models[i] = new ModelList();
			models[i].add(temp);
		}
		
		this.currentModels = new int[dimOut];
		for(int i = 0; i < dimOut; i++){
			currentModels[i] = 0;
		}
		
		searchingTime = new int[dimOut];
		
		this.temporaryModels = new ModelList[dimOut];
		for(int i = 0; i < dimOut; i++){
			temporaryModels[i] = new ModelList();
		}
		
		modelBackup = new LinearRLSModel[dimOut];
		
		
		this.transitions = new GaussModelMatrix[dimOut];
		this.surprisingSensations = new double[dimOut][dimOut];
		for(int i = 0; i < dimOut; i++){
			transitions[i] = new GaussModelMatrix(i);
			surprisingSensations[i] = null;
		}
		
		pe = new ProbabilityErrors[dimOut];
		for(int i = 0; i < dimOut; i++){
			pe[i] = new ProbabilityErrors();
		}
		
		this.lastSensation = firstSensation;
		
		maxNrOfModels = new int[dimOut];
		for(int i = 0; i < dimOut; i++){
			maxNrOfModels[i] = 2;
		}
		maxNrOfModels[2] = 4;
		maxNrOfModels[3] = 4;
		maxNrOfModels[4] = 3;
		maxNrOfModels[5] = 3;
		
		correctPredictedModelTransitions = new int[dimOut][4][4];
		falsePredictedModelTransitions = new int[dimOut][4][4][4];
		predictedModel = new int[dimOut];
		
		this.errorsPerModel = new Double[dimOut];
	}
	
	public double[] predict(double[] x){
		double[] p = new double[dimOut];
		for(int i = 0; i < dimOut; i++){
			p[i] = models[i].get(currentModels[i]).predict(x)[0];
		}
		
		currentPrediction = p;
		
		return p;
	}
	
	
	public double[] forwardModel(Double[] p){
		double[] x = new double[dimIn];
		double nX = 0;
		//System.out.println("");
		for(int i = 0; i < dimOut; i++){
			if(p[i] != null ){
				double[] pI = {p[i]};
				double[] xI =  models[i].get(currentModels[i]).forwardModel(pI);
				if(xI != null){
					//System.out.print("Dim" + i + " says: ");
					for(int j = 0; j < xI.length; j++){
						x[j] += xI[j];
						//System.out.print(xI[j] + ", ");
					}
					//System.out.println();
					nX++;
				}
				else{
					//System.out.println("x" + i + " says null");
				}
			}
			else{
				//System.out.println("p" + i + " says null");
				
			}
		}
		
		if(nX == 0){
			return null;
		}
		
		//System.out.print("CalcX = [");
		for(int i = 0; i < dimIn; i++){
			x[i] = x[i];///nX;
			//System.out.print(x[i] + ", ");
		}
		//System.out.println("];");
		return x;
	}
	
	/*public double vectorNorm(double[] x){
		double res = 0.0;
		for(double d : x){
			res += d * d;
		}
		return Math.sqrt(res);
	}*/
	
	/*public void setMeans(int dim, int from, int to, double[] mean){
		transitions[dim].get(from, to).setMeans(mean);
	}*/
	
	
	public double[] forwardToChangeModel(double[] sensation, int dim){
		
		LinkedList<int[]> firstOrderTransitions = new LinkedList<int[]>();
		LinkedList<int[]> secondOrderTransitions = new LinkedList<int[]>();
		
		for(int i = 0; i < rewardingTransitions.length; i++){
			if(rewardingTransitions[i][0] == currentModels[dim]){
				firstOrderTransitions.add(rewardingTransitions[i]);
			}
			else{
				int[] secondOrderTransition = {currentModels[dim], rewardingTransitions[i][0]};
				secondOrderTransitions.add(secondOrderTransition);
			}
		}
		
		if(!firstOrderTransitions.isEmpty()){
			if(firstOrderTransitions.size() == 1){
				return forwardToChangeModel(sensation, dim, currentModels[dim], firstOrderTransitions.getFirst()[1]);
			}
			else{
				return forwardToChangeModel(sensation, dim, currentModels[dim], findNearestTransition(sensation, dim, firstOrderTransitions));
			}
		}
		
		if(secondOrderTransitions.size() == 1){
			return forwardToChangeModel(sensation, dim, currentModels[dim], secondOrderTransitions.getFirst()[1]);
		}
		
		
		return forwardToChangeModel(sensation, dim, currentModels[dim], findNearestTransition(sensation, dim, secondOrderTransitions));
	}
	
	private double euclidianDistance(double[] x, double[] y){
		double res = 0;
		for(int i = 0; i < x.length; i++){
			res += (x[i] - y[i]) * (x[i] - y[i]);
		}
		return Math.sqrt(res);
	}
	
	private int findNearestTransition(double[] sensation, int dim, LinkedList<int[]> possibleTransitions){
		int bestTo = 0;
		double similarityBestTo = Double.MAX_VALUE;
		for(int i = 0; i < possibleTransitions.size(); i++){
			GaussianModel trans = this.transitions[dim].get(possibleTransitions.get(i)[0], possibleTransitions.get(i)[1]);
			if(trans != null){
				double[] transMean = trans.getMean();
				double similarity = euclidianDistance(sensation, transMean);
				if(similarity < similarityBestTo){
					bestTo = i;
					similarityBestTo = similarity;
				}
			}
		}
		
		return possibleTransitions.get(bestTo)[1];
	}
	
	public double[] transformDeltaSensation(double[] x){
		double maxVal = Double.MIN_VALUE;
		for(int i = 0; i < x.length; i++){
			if(Math.abs(x[i]) > maxVal){
				maxVal = Math.abs(x[i]);
			}
		}
		for(int j = 0; j < x.length; j++){
			x[j] *= 7.0/maxVal;
		}
		return x;
	}
	
	
	public double[] forwardToChangeModel (double[] sensation, int dim, int from, int to){
		
		System.out.println("Transition: " + from + " -> " + to);
		GaussianModel g = transitions[dim].get(from, to);
		
		if(g== null){
			return null;
		}
		
		Double[] newSensation = new Double[dimOut];
		double[] deltaSensation = g.forwardModelGradient(sensation);//g.gaussGradient(sensation); 
		for(int i = 0; i < dimOut; i++){
			if(Double.isNaN(deltaSensation[i])){
				return null;
			}
		}
		deltaSensation = transformDeltaSensation(deltaSensation); //TODO
		//System.out.print("delta = [");
		for(int i = 0; i < dimOut; i++){
			newSensation[i] =  deltaSensation[i];
			//if(newSensation[i] != null)
				//System.out.print(newSensation[i] + ", ");
			//else
				//System.out.print("null, ");
		}
		//System.out.println("]");
		
		return forwardModel(newSensation);
		
	}
	
	public void findBestModelWithProbabilities(int dim){
		int bestModel = highestProbability(dim);
		transitions[dim].addTransition(lastSensation, currentModels[dim], bestModel);
		currentModels[dim] = bestModel; 
	}
	
	public int highestProbability(int dim){
		int bestModel = -1;
		double bestProbability = Double.MIN_VALUE;
		for(int j = 0; j < models[dim].size(); j++){
			double probJ = transitions[dim].calcTransitionProbability(lastSensation, currentModels[dim], j);
			if(probJ > bestProbability){
				bestProbability = probJ;
				bestModel = j;
			}
		}
		return bestModel;
		
	}
	
	public void doTimeStepModelForcing(double[] x, double[] y, double[] sensation, int time, int[] modelRecommendations, boolean objectSwitched){
		double sumOfErrors = 0.0;
		for(int i = 0; i < dimOut; i++){
			double[] yI = {y[i]};
			boolean switchedModels = false;
			if(currentModels[i] != modelRecommendations[i]){
				switchedModels = true;
			}
			if(modelRecommendations[i] < models[i].size()){
				currentModels[i] = modelRecommendations[i];
			}
			else{
				models[i].add(new LinearRLSModel(dimIn, yI, false, initialGain, lambda));
				currentModels[i] = modelRecommendations[i];
			}
			LinearRLSModel m = models[i].get(currentModels[i]);
			if(!switchedModels && !objectSwitched){
				m.update(x, yI);
				double error = Math.abs(currentPrediction[i] - y[i]); 
				m.updateError(error);
				errorsPerModel[i] = error;
			}
			else{
				errorsPerModel[i] = null;
			}
			sumOfErrors += Math.abs(currentPrediction[i] - y[i]);
		}
		for(int i = 0; i < dimOut; i++){
			if(searchingTime[i] == 0){
				System.out.print(currentModels[i] + ", ");
			}
			else{
				System.out.print("S, ");
			}
		}
		System.out.println("Errors: " + sumOfErrors);
	}
	
	
	//Main method
	// Motorcommand x, change in sensation y, full sensory information "sensation"
	// time of simulation, boolean stating whether the models are updated,
	// modelRecommendation = if you want to force which models are picked, otherwise modelRecommendation = null
	public void doTimeStep(double[] x, double[] y, double[] sensation, int time, boolean training, int[] modelRecommendations){

		
		double[] errorDim = new double[dimOut];
		boolean[] surprising = new boolean[dimOut];
		
		//If suprise -> build temporary models
		for(int i = 0; i < dimOut; i++){
			LinearRLSModel m = models[i].get(currentModels[i]);
			//if prediction error > 2 sigma in one dimension => Surprise, start searching for new models
			if(time > 800 && searchingTime[i] == 0 
					&& Math.abs(currentPrediction[i] - y[i]) > m.getMeanError() + 2.0 * Math.sqrt(m.getErrorVariance())){
				
					surprising[i] = true;
					searchingTime[i] = maxSearchingTime;
					temporaryModels[i].clear();
					for(int j = 0; j < models[i].size(); j++){
						temporaryModels[i].add(models[i].get(j).copy());
					}
					if(models[i].size() < maxNrOfModels[i] && training){ //only expand models if in training
						double[] yI = {y[i]};
						temporaryModels[i].add(new LinearRLSModel(dimIn, yI, false, initialGain, lambda));
					}
					//Save a backup of the current model to undo changes if surprise leads to a new model
					modelBackup[i] = models[i].get(currentModels[i]).copyWithErrors();
					surprisingSensations[i] = lastSensation;
				
				/*if(time > timeToSearchWithProbabilities){
					predictedModel[i] = highestProbability(i);
				}*/
				
			}
		}
		
		lastSensation = sensation;
		
		double sumOfErrors = 0.0;
		
		for(int i = 0; i < dimOut; i++){
			double[] yI = {y[i]};
			LinearRLSModel m = models[i].get(currentModels[i]);
			
			sumOfErrors += Math.abs(currentPrediction[i] - y[i]);
			
			//Stick with the current model
			if(!surprising[i]){ //TODO
				if(training && searchingTime[i] == 0){ //Update the models only during training
					m.update(x, yI);
					m.updateError(Math.abs(currentPrediction[i] - y[i]));
				}//TODO
				
				if(searchingTime[i] == 0){
					errorDim[i] = Math.abs(currentPrediction[i] - y[i]);
				}			
			
			if(searchingTime[i] > 0){ //Currently searching which model to choose to predict dimension i
		
				//Predict with every temporary model, then update
				for(int j = 0; j < temporaryModels[i].size(); j++){
					LinearRLSModel tm = temporaryModels[i].get(j);
					double prediction = tm.predict(x)[0];
					tm.update(x, yI);
					tm.updateError(Math.abs(prediction - y[i]));
					
				}
				
				searchingTime[i] -= 1;
				
				//If searching is over, choose the model with the smallest error
				if(searchingTime[i] == 0){
					double minError = Double.MAX_VALUE;
					int bestModel = -1;
					for(int j = 0; j < temporaryModels[i].size(); j++){
						double tmError = temporaryModels[i].get(j).getMeanError();
						if (tmError < minError){
							minError = tmError;
							bestModel = j;
						}
					}
					
					if(modelRecommendations != null && modelRecommendations[i] != -1){
						bestModel = modelRecommendations[i];
					}
					
					/*if(time > timeToSearchWithProbabilities){
						if(bestModel == predictedModel[i]){
							correctPredictedModelTransitions[i][currentModels[i]][bestModel]++;
						}
						else{
							falsePredictedModelTransitions[i][currentModels[i]][bestModel][predictedModel[i]]++;
						}
						predictedModel[i] = -1;
					}*/
					
					//No backu needed since model is not updated while searching //TODO
					/*if(bestModel != currentModels[i] || !training){ 
						models[i].set(currentModels[i], modelBackup[i]);
						modelBackup[i] = null;
					}*/
					
					if(bestModel < models[i].size()){ //The model already existed
						
						if(bestModel != currentModels[i] || allowSelftransitions){
							if(training)
								transitions[i].addTransition(surprisingSensations[i], currentModels[i], bestModel);
						}
						currentModels[i] = bestModel;
					}
					else{ //A new model is the best model
						if(training){
							transitions[i].newDimension(models[i].size());
							models[i].add(temporaryModels[i].get(bestModel));
							transitions[i].addTransition(surprisingSensations[i], currentModels[i], bestModel);
							currentModels[i] = bestModel;
						}
					}
					surprisingSensations[i] = null;
				}
			}

			}
		}
		
		
		for(int i = 0; i < dimOut; i++){
			if(surprisingSensations[i] == null && training){
				//TODO vlt doch nur alle 100 schritte
				transitions[i].addNoTransition(lastSensation, currentModels[i]);
			}
		}
		
		for(int i = 0; i < dimOut; i++){
			if(searchingTime[i] == 0){
				System.out.print(currentModels[i] + ", ");
			}
			else{
				System.out.print("S, ");
			}
		}
		System.out.println("Errors: " + sumOfErrors);
		arm.addError(sumOfErrors);
		meanError = sumOfErrors/((double) dimOut);
		counterForError++;
	}
	
	
	
	
	public boolean finished(){
		return false;
	}
	
	public boolean isSearching(){
		for(int i = 0; i < searchingTime.length; i++){
			if(searchingTime[i] != 0){
				return true;
			}
		}
		return false;
	}
	
	public boolean isSearching(int dim){
		return searchingTime[dim] != 0;
	}
	
	public void increaseMaxNrOfModels(int dim){
		maxNrOfModels[dim]++;
	}
	
	public void debuggingPrints(double[] x, double distanceX, double distanceY, long rs, boolean zeroToOne){
		GaussianModel g = transitions[2].get(0, 1);
		if(currentModels[2] == 1)
			g = transitions[2].get(1, 0);
		double[] mean = g.means;
		System.out.println("mean = [");
		for(double d: mean){
			System.out.print(d + ", ");
		}
		System.out.println("];");
		/*
		if(zeroToOne){
		if(searchingTime[2] == 0 && currentModels[2] == 0){
			String[] sdx = { distanceX + ", "};
			Fileprinter.printInFile("DistanceXMod0Rs" + rs, sdx, rs);
			String[] sdy = {"" + distanceY + ", "};
			Fileprinter.printInFile("DistanceYMod0Rs" + rs, sdy, rs);
			String[] sp = {"" + transitions[2].calcTransitionProbability(x, 0, 1) + ", "};
			Fileprinter.printInFile("TransprobMod0To1Rs" + rs, sp, rs);
		}
		}
		else{
		if(searchingTime[2] == 0 && currentModels[2] == 1){
			String[] sdx = { distanceX + ", "};
			Fileprinter.printInFile("DistanceXMod1Rs" + rs, sdx, rs);
			String[] sdy = {"" + distanceY + ", "};
			Fileprinter.printInFile("DistanceYMod1Rs" + rs, sdy, rs);
			String[] sp = {"" + transitions[2].calcTransitionProbability(x, 1, 0) + ", "};
			Fileprinter.printInFile("TransprobMod1To0Rs" + rs, sp, rs);
		}
		}*/
	}
	
	public void finishPrint(long rs){
		
		/*for(int dim = 0; dim < dimOut; dim++){
			String name0 = "CorrectPredictedTransitionsDim" + dim;
			for(int i = 0; i < 4; i++){
				for(int j = 0; j < 4; j++){
					String[] s = new String[1];
					s[0] = "From " + i + " to " + j + " " + correctPredictedModelTransitions[dim][i][j];
					Fileprinter.printInFile(name0, s, rs);
				}
			}
		}
		
		for(int dim = 0; dim < dimOut; dim++){
			String name0 = "FalselyPredictedTransitionsDim" + dim;
			for(int i = 0; i < 4; i++){
				for(int j = 0; j < 4; j++){
					for(int h = 0; h < 4; h++){
						String[] s = new String[1];
						s[0] = "From " + i + " to " + j + " falsely " + h + " " + falsePredictedModelTransitions[dim][i][j][h];
						Fileprinter.printInFile(name0, s, rs);
					}
				}
			}
		}*/
		
		for(int dim = 0; dim < dimOut; dim++){
			for(int i = 0; i < models[dim].size(); i++){
				String[] s0 = new String[3];
				/*GaussianModel gm0 = transitions[dim].get(i);
				if(gm0 != null){
					s0[0] = gm0.getCinString(dim, i);
					s0[1] = gm0.getMeanInString(dim, i);
					s0[2] = gm0.getNinString(dim, i);
					String name0 = "GaussTransition" + dim + "Models" + i;
					Fileprinter.printInFile(name0, s0, rs);
				}*/
			
				for(int j = 0; j < models[dim].size(); j++){
					String[] s1 = new String[3];
					GaussianModel gm1 = transitions[dim].get(i, j);
					if(gm1 != null){
						s1[0] = gm1.getCinString(dim, i, j);
						s1[1] = gm1.getMeanInString(dim, i, j);
						s1[2] = gm1.getNinString(dim, i, j);
						String name1 = "GaussTransition" + dim + "Models" + i + j;
						Fileprinter.printInFile(name1, s1, rs);
					}
					
				}
			}
		}
		
		/*for(int dim = 0; dim < dimOut; dim++){
			for(int i = 0; i < models[dim].size(); i++){
				String[] s2 = {models[dim].get(i).toString(dim, i)};
				String name2 = "RLS" + dim + "Model" + i;
				Fileprinter.printInFile(name2, s2, rs);
			}
		}

		for(int i = 0; i < dimOut; i++){
			String[] s = {pe[i].stringError(0, 0, rs), pe[i].stringError(0, 1, rs), pe[i].stringError(1, 0, rs), pe[i].stringError(1, 1, rs), pe[i].stringError(0, rs), pe[i].stringError(1, rs), pe[i].stringMeanError(0), pe[i].stringMeanError(1), pe[i].stringMeanError(0, 0), pe[i].stringMeanError(0, 1), pe[i].stringMeanError(1, 0), pe[i].stringMeanError(1, 1)};
			Fileprinter.printInFile("ProbError" + i , s, rs);
		}
		
		double sum = 0.0;
		
		for(double X = -30; X < 30; X += 0.1){
			for(double Y = -30; Y < 30; Y += 0.1){
				double[] testSens = {52.0, 52.0, 52.0, 52.0 , X, Y};
				
				double gauss = transitions[2].calcTransitionProbability(testSens, 0, 1, transitionProbabilitiesWithoutSensation[2][0]);
				String[] smosaic = {gauss + ", "};
				Fileprinter.printInFile("TransitionMosaicDim2Mod01", smosaic, rs);
				
				double gauss2 = transitions[2].calcNoTransitionProbability(testSens, 0, transitionProbabilitiesWithoutSensation[2][0]);
				String[] smosaic2 = {gauss2 + ", "};
				Fileprinter.printInFile("NoTransitionMosaicDim2Mod0", smosaic2, rs);
				
				double gauss3 = transitions[2].get(0, 1).gauss(testSens);
				String[] smosaic3 = {gauss3 + ", "};
				Fileprinter.printInFile("TransitionGaussMosaicDim2Mod01", smosaic3, rs);
				
				double gauss4 = transitions[2].get(0).gauss(testSens);
				String[] smosaic4 = {gauss4 + ", "};
				Fileprinter.printInFile("NoTransitionGaussMosaicDim2Mod0", smosaic4, rs);
				
				sum += gauss;
			}
		}
		
		sum = 0.1 * 0.1 * sum;
		String[] sumS = { sum + ", "};
		Fileprinter.printInFile("SumofGauss01", sumS, rs);
		
		sum = 0.0;
		
		for(double X = 30; X < 70; X += 0.1){
			for(double Y = 30; Y < 70; Y += 0.1){
				double[] testSens = {52.0, 52.0, X, Y , 0.0, 0.0};
				
				double gauss = transitions[2].calcTransitionProbability(testSens, 1, 0, transitionProbabilitiesWithoutSensation[2][1]);
				String[] smosaic = {gauss + ", "};
				Fileprinter.printInFile("TransitionMosaicDim2Mod10", smosaic, rs);
				
				double gauss2 = transitions[2].calcNoTransitionProbability(testSens, 1, transitionProbabilitiesWithoutSensation[2][1]);
				String[] smosaic2 = {gauss2 + ", "};
				Fileprinter.printInFile("NoTransitionMosaicDim2Mod1", smosaic2, rs);
				
				double gauss3 = transitions[2].get(1, 0).gauss(testSens);
				String[] smosaic3 = {gauss3 + ", "};
				Fileprinter.printInFile("TransitionGaussMosaicDim2Mod10", smosaic3, rs);
				
				double gauss4 = transitions[2].get(1).gauss(testSens);
				String[] smosaic4 = {gauss4 + ", "};
				Fileprinter.printInFile("NoTransitionGaussMosaicDim2Mod0", smosaic4, rs);
				
				sum += gauss;
			}
		}
		
		sum = 0.1 * 0.1 * sum;
		String[] sumS2 = { sum + ", "};
		Fileprinter.printInFile("SumofGauss10", sumS2, rs);
		
		//TESTS FOR PLANNING
		/*GaussianModel g = transitions[2].get(0, 1);
		if(currentModels[2] == 1)
			g = transitions[2].get(1, 0);
		
		
		double[][] C = g.getCovarianceMatrix();
		for(int i = 0; i < C.length; i++){
			for(int j = 0; j < C.length; j++){
				System.out.print(C[i][j] + ", ");
			}
			System.out.println("");
		}
		
		double meanOfVariances = C[g.smallestVariance()][g.smallestVariance()];
		double varianceOfVariances = g.varianceOfVariances(C)/17.0;
		System.out.print("Gewichtung: ");
		for(int i = 0; i < C.length; i++){
			System.out.print(g.calcGauss(C[i][i], meanOfVariances, varianceOfVariances) + ", ");
		}
		System.out.println("");
		
		double[] m = g.means;
		System.out.print("mean = [");
		for(int i = 0; i < m.length; i++){
			System.out.print(m[i] + ", ");
		}
		System.out.println("]");
		*/
		
		//Prob.Tests
		//double[] testSens1 = {52.0, 52.0, 52.0, 52.0 , 0.0, 0.0, 0.0, 0.0};
		//double[] testSens2 = {52.0, 52.0, 52.0, 52.0 , 0.0, 0.0, 20.0, 20.0};
		//double[] testSens3 = {52.0, 52.0, 52.0, 52.0 , 0.0, 0.0, 36.0, 36.0};
		//double[] testSens4 = {52.0, 52.0, 52.0, 52.0 , 0.0, 0.0, 50.0, 50.0};
		//System.out.println("P(1->0| s1) = " +  transitions[2].calcTransitionProbability(testSens1, 1, 0));
		//System.out.println("P(1->0| s2) = " +  transitions[2].calcTransitionProbability(testSens2, 1, 0));
		//System.out.println("P(1->0| s3) = " +  transitions[2].calcTransitionProbability(testSens3, 1, 0));
		//System.out.println("P(1->0| s4) = " +  transitions[2].calcTransitionProbability(testSens4, 1, 0));
		/*
		GaussianModel g = transitions[2].get(1, 0);
		
		
		double[][] C = g.getCovarianceMatrix();
		for(int i = 0; i < C.length; i++){
			for(int j = 0; j < C.length; j++){
				System.out.print(C[i][j] + ", ");
			}
			System.out.println("");
		}
		
		double[] m = g.means;
		System.out.print("mean = [");
		for(int i = 0; i < m.length; i++){
			System.out.print(m[i] + ", ");
		}
		System.out.println("]");
		
		for(double X = 0; X < 60; X += 0.1){
			for(double Y = 0; Y < 60; Y += 0.1){
				double[] testSens = {52.0, 52.0, 52.0, 52.0 , 0.0, 0.0, X, Y};
				
				//double gauss = transitions[2].calcTransitionProbability(testSens, 1, 0);
				//String[] smosaic = {gauss + ", "};
				//Fileprinter.printInFile("TransitionMosaicDim2Mod10", smosaic, rs);
			}
		}*/
		
		/*System.out.println("P(0| s1) = " +  transitions[2].calcNoTransitionProbability(testSens1, 0));
		System.out.println("P(0| s2) = " +  transitions[2].calcNoTransitionProbability(testSens2, 0));
		System.out.println("P(0| s3) = " +  transitions[2].calcNoTransitionProbability(testSens3, 0));
		System.out.println("P(0| s4) = " +  transitions[2].calcNoTransitionProbability(testSens4, 0));
		
		System.out.println("f_{0->1}(s1) = " +  transitions[2].get(0, 1).gauss(testSens1));
		System.out.println("f_{0}(s1) = " +  transitions[2].get(0).gauss(testSens1));
		
		System.out.println("f_{0->1}(s2) = " +  transitions[2].get(0, 1).gauss(testSens2));
		System.out.println("f_{0}(s2) = " +  transitions[2].get(0).gauss(testSens2));
		
		System.out.println("f_{0->1}(s3) = " +  transitions[2].get(0, 1).gauss(testSens3));
		System.out.println("f_{0}(s3) = " +  transitions[2].get(0).gauss(testSens3));
		
		System.out.println("f_{0->1}(s4) = " +  transitions[2].get(0, 1).gauss(testSens4));
		System.out.println("f_{0}(s4) = " +  transitions[2].get(0).gauss(testSens4));
		
		for(double X = -30; X < 30; X += 0.1){
			for(double Y = -30; Y < 30; Y += 0.1){
				double[] testSens = {52.0, 52.0, 52.0, 52.0 , X, Y};
				
				double gauss = transitions[2].calcTransitionProbability(testSens, 0, 1);
				String[] smosaic = {gauss + ", "};
				Fileprinter.printInFile("TransitionMosaicDim2Mod01", smosaic, rs);
			}
		}
		for(double X = 30; X < 70; X += 0.1){
			for(double Y = 30; Y < 70; Y += 0.1){
				double[] testSens = {52.0, 52.0, X, Y, 5.0, -5.0};
				
				double gauss = transitions[2].calcTransitionProbability(testSens, 1, 0);
				String[] smosaic = {gauss + ", "};
				Fileprinter.printInFile("TransitionMosaicDim2Mod10", smosaic, rs);
			}
		}*/
		
		
	}
	
	public double getMeanError(){
		double res = meanError/counterForError;
		meanError = 0.0;
		counterForError = 0;
		return res;
	}
	
	public Double[] getErrorsPerDim(){
		return this.errorsPerModel;
	}
	
	public int predictNextModel(double[] sensation, int dim){
		return transitions[dim].findBestTransition(currentModels[dim], sensation);
	}

}
