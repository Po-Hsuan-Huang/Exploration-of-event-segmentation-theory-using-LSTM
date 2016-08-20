import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;



public class ReadObject {
	static Object object;
	
	public static Object Read( String objName) {
		System.out.println("Reading Objects...");
		String name = objName + ".bin";
		try {
			FileInputStream fi = new FileInputStream(name);
			
			ObjectInputStream os = new ObjectInputStream(fi);
			
			object = os.readObject();
			
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
		return object;
	}
}
