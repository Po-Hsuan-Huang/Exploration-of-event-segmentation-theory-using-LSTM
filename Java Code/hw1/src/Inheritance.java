
public class Inheritance {

	public static void main(String[] args) {
		MachineGun MG1 = new MachineGun();
		Car Car1 = new Car();
		MG1.start();
		MG1.stop();
		System.out.println(MG1.start() + ":" + MG1.stop());
		System.out.println(Car1.start() + ":" + Car1.stop());
		Car1.setDirection((float) 34.1);
		Car1.setSpeed((float)23.13);
		System.out.printf(Car1.drive());
	}

}
