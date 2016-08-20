package hw1;

public class Polymorphism {
/*
 *polymorphism is one of 3 pillar of the object-oriented language,
 *including inheritance, encapsualtion, and polymorphism.
 *it is saying the funciton of a method can vary depends on the 
 *type of the object that enforce it, or the object that it imposes.
 *Polymorphism means you can always use child class anywhere
 *where you normally use parent class
 */
	public static void main(String[] args) {
		Plants plant1 = new Plants();
		Tree tree = new Tree();
		
		
		Plants plant2 =plant1;// refer plant2 to plant1;
		// polymorphism
		Plants plant3 = tree;
//		plant1.grow();
//		plant2.grow();
		plant3.grow();// it is using method in tree now.
		
		tree.shedLeaves();
		plant3.shedLeaves();
		// plant3 doesn't have shedLeaves method becuase it is a variable of 
		// class plant. But is was referred to object tree().
	}

}
