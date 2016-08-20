class Frosch {
	
	private int id;
	private String name;
	// constructor
	public Frosch (int id, String name){
		this.id =id;
		this.name = name;
		
	}
	
	public String toString(){
		//solution 1
		return String.format("%4d :%s",id,name)
;		
		
		/* solution 2
		StringBuilder ab = new StringBuilder();
		ab.append("id: ").append(id).append("name:").append(name);
		return ab.toString();
		*/
		/* solution 3 inefficient
		  return "id:"+ id + "name:" + name;
		*/
	}
}
public class toString {

	public static void main(String[] args) {
		Object obj = new Object();
		
		Frosch frog1 = new Frosch(7, "Freddy");
		Frosch frog2 = new Frosch(2, "Denny");
		System.out.println(frog1);
		System.out.println(frog2);
		
		
		
	}

}
