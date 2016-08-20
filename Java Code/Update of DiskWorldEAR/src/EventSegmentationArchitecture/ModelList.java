package EventSegmentationArchitecture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

/**
 * List containing forward models
 * 
 * @author Christian Gumbsch
 *
 */
public class ModelList {
	LinkedList<ForwardModel> list;
	
	public ModelList(){
		list = new LinkedList<ForwardModel>();
	}
	
	/**
	 * Add a new forward model to the list
	 * @param m, forward model to be added
	 */
	public void add(ForwardModel m){
		list.add(m);
	}
	
	/**
	 * Get one forward model of the list based on the forward models index
	 * @param i, index of the forward model to be retrieved
	 * @return forward model at index i
	 */
	public ForwardModel get(int i){
		return list.get(i);
	}
	
	/**
	 * Change the forward model in the list at a given index to another one
	 * @param i, index of the forward model to be changed
	 * @param m, new forward model to be set at index i 
	 */
	public void set(int i, ForwardModel m){
		list.set(i, m);
	}
	
	/**
	 * Clear the list
	 */
	public void clear(){
		list.clear();
	}

	/**
	 * Getter for the list's size
	 * @return size of the list
	 */
	public int size(){
		return list.size();
	}/** The printWeights serialize ModelList[] into "LSTM_weight.bin" */
	
	public void printWeights(ModelList[] ModelList, String name) {

		
		System.out.println("Writing Weights...");
		ModelList[] weights = null;

		try {
			FileOutputStream fs = new FileOutputStream("ModelList.bin");

			ObjectOutputStream os = new ObjectOutputStream(fs);

			weights = ModelList.clone();

			os.writeObject(weights);
			os.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ModelList[] readWeights() {
		System.out.println("Reading Objects...");
		ModelList[] weights = null;
		try {
			FileInputStream fi = new FileInputStream("ModelList.bin");

			ObjectInputStream os = new ObjectInputStream(fi);
			
			weights = (ModelList[]) os.readObject();

			os.close();
			

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return weights;
	}
}

