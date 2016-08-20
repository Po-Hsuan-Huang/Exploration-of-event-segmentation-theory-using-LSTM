import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class WriteFiles {

	public static void main(String[] args) {
		File file  = new File("text2.txt");
		// surround the FileReader with exception warning
		try (BufferedWriter br = new BufferedWriter(new FileWriter(file));){			

			br.write("line 1");
			br.newLine();
			br.write("line 2");
			// you don't need to close br for Java 7
			
		}catch(IOException e){
			System.out.println("Unable to write file: "+ file.toString());
		}finally{
			// always will be executed. 
		};
	}

}
