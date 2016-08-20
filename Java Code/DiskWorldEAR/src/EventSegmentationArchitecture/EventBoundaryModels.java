package EventSegmentationArchitecture;

import java.util.LinkedList;

/**
 * Implementation of the Event Boundary Models-component of the Event Segmentation Architecture
 * EventBoundaryModels holds a matrix of Gaussian distributions describing the conditional probability
 * of executing the transition from one forward model to another forward model (an event boundary).
 * This matrix of event boundaries describes the model transitions of only one sensory dimension
 * (If the sensory information is N-dimensional, N EventBoundaryModels are necessary)
 * 
 * @author Christian Gumbsch
 */
public class EventBoundaryModels {
	
	/** Matrix containing the Gaussians describing the transition from one forward model to another (event boundary) */
	private LinkedList<LinkedList<GaussianModel>> matrix;
	/** Gaussians describing that no forward model transition occurs*/
	private LinkedList<GaussianModel> noTransitions;
	
	/**
	 * Constructor
	 * @param dim,
	 * 		dimension of the sensory information that is ought to be represented by the 
	 * 		event boundaries
	 */
	public EventBoundaryModels(){
		matrix = new LinkedList<LinkedList<GaussianModel>>();
		matrix.add(new LinkedList<GaussianModel>());
		matrix.get(0).add(null);
		noTransitions = new LinkedList<GaussianModel>();
		noTransitions.add(null);
	}
	
	/**
	 * Add a dimension to the matrix of event boundaries.
	 * This method is called, when a new forward model is generated for the 
	 * sensory dimension dim
	 * @param d, number of the new forward model
	 */
	public void newDimension(int d){
		if(d < matrix.size()){
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
	
	/**
	 * A sensory surprise occurred and a model transition (event boundary) was detected. 
	 * The probability of performing this model transition is then updated
	 * 
	 * @param sensation,
	 * 			the sensory information when the transition was detected
	 * @param from,
	 * 			number of the forward model active before the transition 
	 * 			occurred.
	 * @param to,
	 * 			number of the forward model that was active, after the
	 * 			execution of the model transition.
	 */
	public void addTransition(double[] sensation, int from, int to){
		if(from == to){
			//Then the forward model is not changed
			addNoTransition(sensation, from);
		}
		if(matrix.get(from).get(to) == null){
			matrix.get(from).set(to, new GaussianModel(sensation));
		}
		else{
			matrix.get(from).get(to).update(sensation);
		}
	}
	
	/**
	 * A sensory surprise occurred but no model change followed. The probability of not
	 * changing the model, given a surprise and a sensation is now updated
	 * @param sensation,
	 * 			the sensory information when the surprise occurred
	 * @param model,
	 * 			the model that was active, when the surprise occurred.
	 */
	public void addNoTransition(double[] sensation, int model){
		if(noTransitions.get(model) == null){
			noTransitions.set(model, new GaussianModel(sensation));
		}
		else{
			noTransitions.get(model).update(sensation);
		}
	}
	
	/**
	 * Compute the conditional probability of switching from one model to another
	 * based on the current sensory information and given a surprise was detected.
	 * @param sensation,
	 * 			current sensory information
	 * @param from,
	 * 			number of the forward model from which the transition should occurr
	 * @param to,
	 * 			number of the forward model to which the transition should lead
	 * @return P(M_from -> M_to | sensation, surprise occurrence)
	 */
	public double calcTransitionProbability(double[] sensation, int from, int to){
		if(from == to){
			//Then we do not want to switch models but need the probability of staying in
			// the same forward model
			return calcNoTransitionProbability(sensation, from);
		}
		
		//The following cases should not occur:
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
			//This may happen when the probabilities are extremely small and rounded to zero
			return 1.0;
		}
		return matrix.get(from).get(to).gauss(sensation)/ nenner;
	}
	
	/**
	 * Compute the conditional probability of not switching a forward model based on
	 * the sensory information and given a surprise was detected 
	 * @param sensation,
	 * 			sensory information when the surprise was detected
	 * @param from,
	 * 			number of the forward model, for which the probability is computed
	 * @return P(Model_from -> Model_from | sensation, surprise occurrence)
	 */
	public double calcNoTransitionProbability(double[] sensation, int from){
		
		//The following case should not occur:
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
			//This may happen when the probabilities are extremely small and rounded to zero
			return 1.0;
		}
		return noTransitions.get(from).gauss(sensation)/ nenner;
	}
	
	/**
	 * Getter for one event boundary model
	 * @param from,
	 * 			number of the model active before the model transition (event boundary) happened
	 * @param to,
	 * 			number of the model active after the model transition
	 * @return Gaussian distribution describing the event boundary
	 */
	public GaussianModel get(int from, int to){
		if(from >= this.matrix.size() || to >= this.matrix.get(from).size()){
			return null;
		}
		return this.matrix.get(from).get(to);
	}
	
	/**
	 * Getter for one Gaussian distribution describing the probability for 
	 * staying in the same model
	 * @param from,
	 * 			number of the model
	 * @return Gaussian distribution describing the no-transition
	 */
	public GaussianModel get(int from){
		return this.noTransitions.get(from);
	}
	
	/**
	 * Set a new event boundary model
	 * @param from, 
	 * 			number of the model before the forward model transition occured
	 * @param to,
	 * 			number of the model active after the forward model transition
	 * @param g,
	 * 			Gaussian distribution describing the event boundary
	 */
	public void set(int from, int to, GaussianModel g){
		this.matrix.get(from).set(to, g);
	}
	
	/**
	 * Set a new Gaussian distribution describing the probability that one forward model
	 * is not switched during a surprise
	 * 
	 * @param from,
	 * 			number of the forward model
	 * @param g,
	 * 			Gaussian distribution describing the no-transition
	 */
	public void set(int from, GaussianModel g){
		this.noTransitions.set(from, g);
	}
	
	/**
	 * Find the forward model most likely to be active in the next time step 
	 * @param from,
	 * 			number of the model active in the current time step
	 * @param sensation,
	 * 			current sensory information
	 * @return Number of the forward model with the highest transition probability
	 */
	public int findLikeliestForwardModel(int from, double[] sensation){
		double maxProb = 0.0;
		int bestI = 0;
		for(int i = 0; i < matrix.get(from).size(); i++){
			double prob = calcTransitionProbability(sensation, from, i);
			if(prob > maxProb){
				bestI = i;
				maxProb = prob;
			}
		}
		return bestI;
	}

}
