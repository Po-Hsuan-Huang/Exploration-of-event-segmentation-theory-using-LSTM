package Hiwi;

import java.util.LinkedList;

public class ExampandableIntMatrix {
	
	LinkedList<LinkedList<Integer>> m = new LinkedList<LinkedList<Integer>>();
	
	public ExampandableIntMatrix(int size){
		for(int i = 0; i < size; i++){
			m.add(new LinkedList<Integer>());
		}
	}
	
	public void add(Integer elem, int dim){
		m.get(dim).add(elem);
	}
	
	public LinkedList<Integer> get(int dim){
		return m.get(dim);
	}
	
	public void clear(){
		for(int i = 0; i < m.size(); i++){
			m.get(i).clear();
		}
	}

}
