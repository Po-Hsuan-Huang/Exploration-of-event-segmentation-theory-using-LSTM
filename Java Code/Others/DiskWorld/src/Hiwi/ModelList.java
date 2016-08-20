package Hiwi;

import java.util.LinkedList;

public class ModelList {
	LinkedList<LinearRLSModel> list;
	
	public ModelList(){
		list = new LinkedList<LinearRLSModel>();
	}
	
	public void add(LinearRLSModel m){
		list.add(m);
	}
	
	public LinearRLSModel get(int i){
		return list.get(i);
	}
	
	public void set(int i, LinearRLSModel elem){
		list.set(i, elem);
	}
	
	public void clear(){
		list.clear();
	}

	public int size(){
		return list.size();
	}
}
