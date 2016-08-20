package Hiwi;

import java.util.LinkedList;

public class GaussModelMatrix {
	
	LinkedList<LinkedList<GaussianModel>> matrix;
	LinkedList<GaussianModel> noTransitions;
	int dim;
	
	public GaussModelMatrix(int dim){
		matrix = new LinkedList<LinkedList<GaussianModel>>();
		matrix.add(new LinkedList<GaussianModel>());
		matrix.get(0).add(null);
		this.dim = dim;
		noTransitions = new LinkedList<GaussianModel>();
		noTransitions.add(null);
	}
	
	public void newDimension(int s){
		
		if(s < matrix.size()){
			return;
		}
		
		for(int i = 0; i < matrix.size(); i++){
			matrix.get(i).add(null);
		}
		matrix.add(new LinkedList<GaussianModel>());
		for(int i = 0; i < matrix.size(); i++){
			matrix.get(matrix.size() -1).add(null);
		}
		noTransitions.add(null);
	}
	
	public void addTransition(double[] sensation, int from, int to){
		if(from == to){
			addNoTransition(sensation, from);
		}
		if(matrix.get(from).get(to) == null){
			//System.out.println("NEW PROBABILITY " + dim);
			matrix.get(from).set(to, new GaussianModel(sensation.length, sensation, dim));
		}
		else{
			matrix.get(from).get(to).update(sensation);
		}
	}
	
	public void addNoTransition(double[] sensation, int model){
		if(noTransitions.get(model) == null){
			noTransitions.set(model, new GaussianModel(sensation.length, sensation, dim));
		}
		else{
			noTransitions.get(model).update(sensation);
		}
	}
	
	public double calcTransitionProbability(double[] sensation, int from, int to){
		if(from == to){
			return calcNoTransitionProbability(sensation, from);
		}
		if(from >= matrix.size() || to >= matrix.size()){
			return 0.0;
		}
		if(matrix.get(from).get(to) == null){
			return 0.0;
		}
		double nenner = 0.0;
		for(int i = 0; i < matrix.size(); i++){
			if(matrix.get(from).get(i) != null)
				nenner += matrix.get(from).get(i).gauss(sensation);
		}
		nenner += noTransitions.get(from).gauss(sensation);
		if(nenner == 0){
			return 1.0;
			//throw new Error("NaN Probability because of nenner = 0 and " + noTransitions.get(from).gauss(sensation));
		}

		return matrix.get(from).get(to).gauss(sensation)/ nenner;
	}
	
	public double calcNoTransitionProbability(double[] sensation, int from){
		if(from >= noTransitions.size() || noTransitions.get(from) == null){
			return 0.0;
		}
		double nenner = 0.0;
		for(int i = 0; i < matrix.size(); i++){
			if(matrix.get(from).get(i) != null)
				nenner += matrix.get(from).get(i).gauss(sensation);
		}
		nenner += noTransitions.get(from).gauss(sensation);
		if(nenner == 0){
			return 1.0;
			//throw new Error("NaN Probability because of nenner = 0 and " + noTransitions.get(from).gauss(sensation));
		}
		return noTransitions.get(from).gauss(sensation)/ nenner;
	}
	
	public GaussianModel get(int from, int to){
		if(from >= this.matrix.size() || to >= this.matrix.get(from).size()){
			return null;
		}
		return this.matrix.get(from).get(to);
	}
	
	public GaussianModel get(int from){
		return this.noTransitions.get(from);
	}
	
	public void set(int from, int to, GaussianModel g){
		this.matrix.get(from).set(to, g);
	}
	
	public void set(int from, GaussianModel g){
		this.noTransitions.set(from, g);
	}
	
	public int findBestTransition(int from, double[] s){
		double maxProb = 0.0;
		int bestI = 0;
		for(int i = 0; i < matrix.get(from).size(); i++){
			double prob = calcTransitionProbability(s, from, i);
			if(prob > maxProb){
				bestI = i;
				maxProb = prob;
			}
		}
		return bestI;
	}
}
