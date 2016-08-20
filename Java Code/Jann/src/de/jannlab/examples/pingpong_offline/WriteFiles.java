package de.jannlab.examples.pingpong;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class WriteFiles {
	public static void Write( ArrayList<Double> file , String fileName) {
		String str  = fileName + ".txt";
		File file1  = new File(str);
		try (BufferedWriter br = new BufferedWriter(new FileWriter(file1));){			
			for (double sth : file){
				
			br.write(Double.toString(sth));
			br.newLine();
			}
			// you don't need to close br for Java 7
			
		}catch(IOException e){
			System.out.println("Unable to write file: "+ file1.toString());
		}finally{
			// always will be executed. 
		};
	}

}
