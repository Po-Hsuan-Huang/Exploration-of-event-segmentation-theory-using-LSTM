package EventSegmentationScenario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.w3c.dom.NameList;

/**
 * A simple class to print strings into files. Can be used to save the results
 * of tests in a file.
 * 
 * @author Christian Gumbsch
 *
 */
public class Fileprinter {

	/** Read the .txt file specified by String filename. Store
	 * the .txt file into an arrayList of strings, and overwrite the .txt file with the
	 * updated arrayList of strings.   
	 * @param name, 
	 * 			string used to name the file
	 * @param ss, 
	 * 			array of strings to be written into the file
	 * @param rs, 
	 * 			random seed used in this simulation. This number
	 * 			is concatenated to the name of the file to distinguish
	 * 			between simulation runs with different random seeds
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	static ArrayList<ArrayList<String>> DataList= new ArrayList<ArrayList<String>>();
	static ArrayList<String> NameList = new ArrayList<String>();
	public static void printInFile(String name, String[] ss, long rs){
			
			String ParentPath = "/Users/pohsuanhuang/Desktop/Curriculum Section/Butz Lab/Java Code/Update of DiskWorldEAR/";
			String ChildPath = name + "," + rs + ".txt";
			ArrayList<String> Data = null;
			int currentFileIdx = 0 ;
			boolean inList = false;
			
			for(int i=0; i< NameList.size(); i++){
				if(name == NameList.get(i)){
					Data = DataList.get(i); 
					inList =true;
					currentFileIdx = i;}
				else{ inList=false;}
			}
			
			if(inList==false){
				NameList.add(name);
				Data = new ArrayList<String>();
				DataList.add(Data);
				currentFileIdx = DataList.size()-1;
			}
			
			File file = new File(ChildPath);
			if(file.isFile()){
				
				for (String s :ss) {	Data.add(s);	}
				DataList.add(currentFileIdx, Data); 
				
				try(BufferedWriter writer =  new BufferedWriter(new FileWriter(file,true));){
					for (int i = 0; i < Data.size(); i++) {
//					System.out.println("writing stirngs :" + Data.get(i) );
					writer.write(Data.get(i));
					writer.newLine();
					}
					writer.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(!file.exists()){
				try {
					file.createNewFile();
					Fileprinter.printInFile(name,ss,rs);
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
	}
	public static void printInFile(String name, String[] ss, long rs, boolean ReadFile){
		ArrayList<String> newData = new ArrayList<String>(); 

		if(!ReadFile){
			printInFile( name,  ss,  rs);
			}
		else{
			String ParentPath = "/Users/pohsuanhuang/Desktop/Curriculum Section/Butz Lab/Java Code/DiskWorldEAR/";
			String ChildPath = name + "," + rs + ".txt";
			File file = new File(ChildPath);
			if(file.isFile()){
				
				for (String s :ss) {	newData.add(s);	}
				ArrayList<String> Data = readFile(name,rs);
				Data.addAll(newData);
				
				try(BufferedWriter writer =  new BufferedWriter(new FileWriter(file));){
					for (int i = 0; i < Data.size(); i++) {
//					System.out.println("writing stirngs :" + Data.get(i) );
					writer.write(Data.get(i));
					writer.newLine();
					}
					writer.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(!file.exists()){
				try {
					file.createNewFile();
					Fileprinter.printInFile(name,ss,rs,ReadFile);
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public static ArrayList<String> readFile( String name,long rs ){
		ArrayList<String> Data= new ArrayList<String>();
		String ParentPath = "/Users/pohsuanhuang/Desktop/Curriculum Section/Butz Lab/Java Code/DiskWorldEAR/";
		String ChildPath = name + "," + rs  + ".txt";
		File file = new File(ChildPath);
		if(file.isFile()){
			try(BufferedReader reader =  new BufferedReader(new FileReader(file));){
				String c;
				while((c = reader.readLine())!=null){
					Data.add(c);
				}
				reader.close();
			}
			catch (FileNotFoundException e) {
				 	System.err.format("Cannot find the file '%s'.", name);
				 	e.printStackTrace();
			} 
			catch(IOException e){
				System.err.format("General exception occured trying to reade '%s'.", name);
			 	e.printStackTrace();				}
		}
		else{
			try {
			file.createNewFile();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return Data;
	}

	/** for test purpose only */
	/**
	public static void main(String[] args) {
		String[] content = new String[10];
		String[] reader;
		for (int j =0; j<10 ; j++){
			String string = String.valueOf(j);
			content[j]= string;
		}
		printInFile("SandBox",content,100);
		
		reader = readFile("SandBox",100);
		System.out.println("reader:" + Arrays.toString(reader));
	}
	*/ 
	
}