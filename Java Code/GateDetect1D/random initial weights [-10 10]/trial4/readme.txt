date : Jne 29

This trial genrates weights randomly using LSTM build-in Tool,

however, the seeds were chosen by Brain.SeeWeights(). 

This is different from trial2, trial3, since 

the rnd.nextLong() doens't really generate 

very uniform random numbers.

This take further investigation.

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
	

