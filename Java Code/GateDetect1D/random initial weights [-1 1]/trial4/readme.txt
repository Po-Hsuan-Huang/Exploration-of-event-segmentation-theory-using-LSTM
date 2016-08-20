date : July 6


This trial generates 1000 samples of initial weights set randomly using LSTM build-in Tool,

DoubleTools.fill, and the range of initialization is 1. 

This is different from trial2, trial3.





public static void InjectRandomWeights(HLSTM net, int seed, double range){		
		
		Random rnd  = new Random(seed);
		
		double rand = rnd.nextLong();   
		
		// discard the first 100 random numbers		
		for(int i = 0; i < 100; i++){
		
			rand = rnd.nextLong();   
		
		}
		// use the 101th random number as the seed
		net.SeedWeights(rnd.nextLong(),range);

	}
	
	public void SeedWeights(long RndSeed, double range){
		
		Random rnd = new Random(RndSeed);
		weights[0] = 1;
		DoubleTools.fill(weights, 1, weightsnum, rnd, -range,range);
		bestweights = weights;
		net.writeWeights(weights, 0);
	}

