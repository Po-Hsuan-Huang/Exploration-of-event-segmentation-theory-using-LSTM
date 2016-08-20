package EventSegmentationArchitecture;

/*
 * This class is a forward model chooser that allows selection of forward model
 *  */

public class ForwardModelList {
	ForwardModel temp;
int dimIn;
int dimOut;
double firstSensation;
boolean intercept ;
double initialGain ;
double lambda ;
	
	ForwardModelList(Object[] arguments){

		for (int j=0; j<arguments.length; j++){
			switch(j){
			case(0):dimIn = (int) arguments[0];
			case(1):dimOut = (int) arguments[1];
			case(2):firstSensation = (double) arguments[2];
			case(3):intercept = (boolean) arguments[3];
			case(4):initialGain = (double) arguments[4];
			case(5):lambda = (double)arguments[5];
			};
		}			
		
		
	}
		
		
	public ForwardModel	LSTMModel_master(  ){
		 temp = new LSTMModel_master(dimIn, dimOut); 
		return temp ;
	}

		

	
	 public ForwardModel LSTMModel(  ){
		 temp = new HLSTM(dimIn, dimIn+dimOut ,dimOut); 
		return temp ;
	}
	
	
	public ForwardModel RLSModel(){
		temp = new LinearRLSModel(dimIn, firstSensation, intercept, initialGain, lambda); 
		return temp ;
	}
	

}
