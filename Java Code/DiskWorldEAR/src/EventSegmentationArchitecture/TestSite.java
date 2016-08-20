package EventSegmentationArchitecture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;


import de.jannlab.Net;
import de.jannlab.core.NetStructure;
import de.jannlab.generator.GenerateNetworks;
import de.jannlab.math.Matrix;
import de.jannlab.tools.DoubleTools;
import de.jannlab.tools.NetTools;

public class TestSite {
	public static Net net;
	public static Object[] arguments;
	public static double[] weights;

	public static void main(String[] args) {
		// Matrix IsNull = new Matrix(1,2);
		// IsNull.set(0, 1, 302);

		// String modelType = "a";
		// int i = 0;
		//
		// while (i < 100){
		//
		// i = i +1;
		//
		// switch(modelType)
		// {
		// case "a": System.out.println("a");
		// case "b": System.out.println("b");
		// case "c": System.out.println("c");
		//
		// }
		//
		// }
		//
		/*
		final HLSTM m = new HLSTM(1, 3, 1);
		final ModelList writeToDisk = new ModelList();
		writeToDisk.add(m);

		writeWeights(writeToDisk);

//		writeToDisk.clear();
		
		// System.out.println("M :" + M);
		final ModelList readFromDisk = readWeights();
		// System.out.println("M :" + M);
		for(ForwardModel f : writeToDisk.list) {
			System.out.println(f);
		}
		for(ForwardModel f : readFromDisk.list) {
			System.out.println(f);
		}
		double[] Q = RandomGen2(0.1,19);
		System.out.println("Rnd  : " + Arrays.toString(Q));
		
		*/
		
		
		
	}
	

	private static int[]  RandomGen(int num, int len){
		Random noise = new Random(10);
		int[] rnd = new int[len];
		for (int i =0; i<len;i++){
		rnd[i]= noise.nextInt(num);}
		return rnd;
		
	}
	private static double[]  RandomGen2(double num, int len){
		Random noise = new Random(10);
		double[] rnd = new double[len];
		for (int i =0; i<len;i++){
			rnd[i]=(double) 2*(noise.nextFloat()-0.5);
		}
		return rnd;
		
	}
	
	public static void writeWeights(ModelList modelList) {

		System.out.println("Writing Weights...");

		try (FileOutputStream fs = new FileOutputStream("testObj.bin")) {
			try (ObjectOutputStream os = new ObjectOutputStream(fs)) {
				os.writeObject(modelList);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ModelList readWeights() {
		System.out.println("Reading Objects...");

		try (FileInputStream fi = new FileInputStream("testObj.bin")) {
			try (ObjectInputStream os = new ObjectInputStream(fi)) {
				return (ModelList) os.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
