package Hiwi;

import java.util.LinkedList;

public class ProbabilityErrors {
	
	double e0 = 0.0;
	double e00 = 0.0;
	double e01 = 0.0;
	double e10 = 0.0;
	double e11 = 0.0;
	double e1 = 0.0;
	
	LinkedList<Double> error0 = new LinkedList<Double>();
	LinkedList<Double> error1 = new LinkedList<Double>();
	LinkedList<Double> error00 = new LinkedList<Double>();
	LinkedList<Double> error01 = new LinkedList<Double>();
	LinkedList<Double> error10 = new LinkedList<Double>();
	LinkedList<Double> error11 = new LinkedList<Double>();
	
	int n0 = 0;
	int n1 = 0;
	int n00 = 0;
	int n01 = 0;
	int n10 = 0;
	int n11 = 0;
	
	double[] distanceE01 = new double[1000];
	double[] distanceN01 = new double[1000];
	
	public void addError(int from, int to, double error){
		if(from == 0){
			if(to == 0){
				error00.add(error);
				e00 =( e00 * ((double)n00) + error)/((double) n00 + 1);
				n00++;
			}
			else{
				error01.add(error);
				e01 =( e01 * ((double)n01) + error)/((double) n01 + 1);
				n01++;
			}
		}
		else{
			if(to == 0){
				error10.add(error);
				e10 =( e10 * ((double)n10) + error)/((double) n10 + 1);
				n10++;
			}
			else{
				error11.add(error);
				e11 =( e11 * ((double)n11) + error)/((double) n11 + 1);
				n11++;
			}
		}
	}
	
	
	public void addError(int from, double error){
		if(from == 0){
			error0.add(error);
			e0 =( e0 * ((double)n0) + error)/((double) n0 + 1);
			n0++;
		}
		else{
			error1.add(error);
			e1 =( e1 * ((double)n1) + error)/((double) n1 + 1);
			n1++;
		}
	}
	
	public String stringError(int from, int to, long rs){
		String s = "double[] e" + from + to + "RS" + rs + "= {";
		
		LinkedList<Double> xs;
		
		if(from == 0){
			if(to == 0){
				xs = error00;
			}
			else{
				xs = error01;
			}
		}
		else{
			if(to == 0){
				xs = error10;
			}
			else{
				xs = error11;
			}
		}
		for(Double d : xs){
			s = s + d + ", ";
		}
		return s + "};";
	}
	
	public String stringError(int from, long rs){
		String s = "double[] e" + from + "RS" + rs + "= {";
		
		LinkedList<Double> xs;
		
		if(from == 0){
			xs = error0;
		}
		else{
			xs = error1;
		}
		for(Double d : xs){
			s = s + d + ", ";
		}
		return s + "};";
	}
	
	public String stringMeanError(int from){
		String s = "meanE" + from + "= ";
		if(from == 0){
			s += e0;
		}
		else{
			s += e1;
		}
		s += ";";
		
		return s;
	}
	
	public String stringMeanError(int from, int to){
		String s = "meanE" + from + to + "= ";
		
		if(from == 0){
			if(to == 0){
				s += e00;
			}
			else{
				s += e01;
			}
		}
		else{
			if(to == 0){
				s += e10;
			}
			else{
				s += e11;
			}
		}

		return s + ";";
	}
	

}
