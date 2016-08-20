package de.jannlab.examples.pingpong;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class WriteObject {

    /**obj and objName should have the same name*/
	public static void Write(Object obj, String objName) {
		
		
		//String objName  = obj.getClass().getName();
		
		System.out.println("Writing objects...");
		
		String name = objName +".bin";
		

		try {
			FileOutputStream fs = new FileOutputStream(name);

			ObjectOutputStream os = new ObjectOutputStream(fs);

			os.writeObject(obj);

			os.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
