package de.jannlab.examples.pingpong;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
// use CMD+CTL+O to auto import packages.

public class ReadFiles {

	public static void main(String[] args) {
		File file  = new File("text2.txt");
		BufferedReader br = null;
		// surround the FileReader with exception warning
		try {
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			String line ;
			
			while( (line = br.readLine()) != null){// read one line from the file.
			System.out.println(line);
			}
		} catch (FileNotFoundException e) {
			System.out.println("file not found: " + file.toString());
		} catch(IOException e){
			System.out.println("Unable to read file: "+ file.toString());
		} finally{
			// always will be executed. 
		}
		
		try {
			br.close();// close the bufferedReader to avoid memory leak in the file is read too many times.

		} catch (IOException e) {
			System.out.println("Unable to close a file:"+ file.toString());
		}catch(NullPointerException ex){
			// file probably never opened.
		}
		
	}

}
