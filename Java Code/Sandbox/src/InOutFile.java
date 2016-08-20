import java.io.File;


public class InOutFile {
  /**
   * File Read Write Practice with package Java.io.File 
   * */
	public static void main(String[] args) {
		
	/**
	 * Create a new File instance atha	
	 */
	
	// File( URI uri)
	
	// File(String pathname)
	String pathname	="/Users/pohsuanhuang/Desktop/Curriculum Section/Butz Lab/Java Code/Sandbox/Constructor1.txt";
	File File1 = new File(pathname);
	
	// FIle(File parent, String child)
	File File2 = new File(File1,"Constructorr2.txt");
	
	// File(String parent, String child)
	String ParentPath = "/Users/pohsuanhuang/Desktop/Curriculum Section/Butz Lab/Java Code/Sandbox";
	String ChildPath = "/Constructor3.txt";
	File File3 = new File(ParentPath,ChildPath);
	
	}
	
	
}
