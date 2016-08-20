package Hiwi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A simple class to print strings into files.
 * Can be used to save the results of tests in a file.
 * @author Christian Gumbsch
 *
 */
public class Fileprinter {
	
	/**
	 * Append a string to the existing text in a "data.txt" file
	 * @param s, the string that should be written in the file
	 */
	public static void printInFile(String name, String[] ss, long rs){
		
		File log = new File(name + "RS" + rs + ".txt");
		try{
	    if(!log.exists()){
	            log.createNewFile();
	    }
	    PrintWriter out = new PrintWriter(new FileWriter(log, true));
	    out.append('\n');
	    for(String s : ss)
	    	out.append(s);
	    out.close();
	    }catch(IOException e){
	    }
	}
}