package hw1;

import java.util.Random;

public class RndNumGenerator {

	public static void main(String[] args) {
	
	
		int Size = 13; // size of the array that stores weights.
		
		double[] weight = new double[Size];
		
		Random rnd  = new Random(seed);
		

		
		double rand = rnd.nextLong();   
		
				
		for(int i = 0; i < 100; i++){
		

			
			rand = rnd.nextLong();   
			
			System.out.println("long value : " + rand);
					
		
		}
		
		
		for(int i = 0 ; i < Size; i ++){
			
			
			
			
			weight[i] =  rnd.nextLong()/1E19    * range;
			
			System.out.println( "weight" +i + " : " +  weight[i]);
		}

	

	}
}
