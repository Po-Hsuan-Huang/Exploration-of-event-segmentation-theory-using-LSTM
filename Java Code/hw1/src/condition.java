

public class condition {
	public static void main(String[] args)	{
		int i;
		i =1;
		
		while(i<=10){
		 
			if( i != 5  || i != 10 /*or operator*/){
			 System.out.println("true i is not equal to 5");
			 System.out.println(i);
			}
			
			else if(i== 10) {
				System.out.println("i equals"+i);
				break;
			}
			else{
				System.out.println("false, i equal to 5");
			}
			
			i++;
		}
	}
}
