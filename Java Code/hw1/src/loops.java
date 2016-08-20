
// loop tutorial
public class loops {
	public static void main(String[ ] args){
		int value;
		value = 1;
		
		while(value < 10)
		{
			value += 1;
			System.out.println("dude"+value);
		}

		for( int i=0; i< 20; i=i+2 ){
			
			System.out.println("Hello"+ i);// terminate by starting a new line
			System.out.printf("The value is: %d \n ", i);// not break line

		}

	}
	
}
