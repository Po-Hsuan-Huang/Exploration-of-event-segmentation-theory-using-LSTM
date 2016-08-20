import java.util.Random;


public class Random2 {
	static Random rnd = new Random() ;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double b = 12;
		double c = 2*rnd.nextGaussian()-1;
		double d = b*c;
		System.out.println(d);

	}

}
