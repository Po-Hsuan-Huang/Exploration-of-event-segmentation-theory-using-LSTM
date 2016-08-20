package diskworld.demos;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actions.DiskAction;
import diskworld.actions.Joint;
import diskworld.actions.JointActionType;
import diskworld.actuators.EmptyActuator;
import diskworld.actuators.Mouth;
import diskworld.demos.DemoLauncher.Demo;
import diskworld.environment.AgentMapping;
import diskworld.environment.FloorCellType;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.AgentController;
import diskworld.visualization.CircleDiskSymbol;
import diskworld.visualization.PolygonDiskSymbol;
import diskworld.visualization.VisualizationOptions;
import diskworld.visualization.VisualizationSettings;

public class Skorpion implements Demo {
	
	Random r = new Random(0);
	
	Environment env;
	
	private Disk body;
	private Joint[] upperArmJoints;
	private Joint[] forearmJoints;
	private Joint handjoint;
	private Disk mouth;
	private Disk sting;
	private LinkedList<Disk> allDisks;
	
	double forearmRotation = 0;
	double upperarmRotation = 0;
	double handRotation = 0.0;
	
	double lengthOfHand;
	
	private Disk object;
	double foodRadius = 3.5;
	DiskType foodType;
	
	private int numUpperArmLimbs;
	private int numPerUpperArmLimbs;
	private int numForearmLimbs;
	private int numPerForearmLimbs;
	
	@Override
	public String getTitle() {
		return "Scorpion";
	}
	
	@Override
	public long getMiliSecondsPerTimeStep() {
		return 5;
	}
	
	@Override
	public Environment getEnvironment(){
		// create environment
		int sizex = 100;
		int sizey = 100;
		env = new Environment(sizex, sizey);
		
		// provide disk types 
		DiskType rootType = new DiskType(DiskMaterial.DOUGH.withColor(Color.ORANGE));
		DiskType upperJointType = new DiskType(DiskMaterial.DOUGH.withColor(Color.RED));
		DiskType lowerJointType = new DiskType(DiskMaterial.DOUGH.withColor(Color.MAGENTA));
		DiskType limbType = new DiskType(DiskMaterial.DOUGH.withColor(Color.BLUE));
		DiskType handJointType = new DiskType(DiskMaterial.DOUGH.withColor(Color.GREEN));
		DiskType handType = new DiskType(DiskMaterial.DOUGH.withColor(Color.YELLOW));
		Actuator mouthActuator = new Mouth(false, 0.0, 0.0, 0.0, 0.0, 0.0);
		DiskType mouthType = new DiskType(DiskMaterial.DOUGH.withColor(Color.PINK), mouthActuator);
		Actuator stingActuator =  new Mouth(false, 0.0, 0.0, 0.0, 0.0, 0.0);
		DiskType stingType = new DiskType(DiskMaterial.DOUGH.withColor(Color.GRAY), stingActuator);
		
		//provide disk sizes
		double bodySize = 5;
		double jointSize = 1;
		double handSize = 0.375;
		double limbSize = 0.2;
		double mouthSize = 4;
		
		//construct body
		body = env.newFixedRoot(sizex/8, sizey/8, bodySize, 0, rootType);
		Disk d = body;
		allDisks = new LinkedList<Disk>();
		allDisks.add(d);
		
		// construct upper arm
		numUpperArmLimbs = 6;
		numPerUpperArmLimbs = 20;
		upperArmJoints = new Joint[numUpperArmLimbs + 1];
		upperArmJoints[0] = body.attachJoint(0, jointSize, 0, upperJointType);
		upperArmJoints[0].setRange(JointActionType.ActionType.SPIN, Math.toRadians(-80), 0);
		d = upperArmJoints[0];
		allDisks.add(d);
		
		for (int i = 0; i < numUpperArmLimbs; i++) {
			if (i > 0) {
				upperArmJoints[i] = d.attachJoint(0, jointSize, 0, upperJointType);
				// limit all joints 
				if(i % 2 == 0){
					upperArmJoints[i].setRange(JointActionType.ActionType.SPIN, Math.toRadians(-160), Math.toRadians(0));	
				}
				else{
					upperArmJoints[i].setRange(JointActionType.ActionType.SPIN, Math.toRadians(0), Math.toRadians(160));		
				}
				d = upperArmJoints[i];
				allDisks.add(d);
			}
			for (int j = 0; j < numPerUpperArmLimbs; j++) {
				d = d.attachDisk(0, limbSize, limbType);
				allDisks.add(d);
			}
		}
		upperArmJoints[numUpperArmLimbs] = d.attachJoint(0, jointSize, 0, upperJointType);
		if(numUpperArmLimbs % 2 == 0){
			upperArmJoints[numUpperArmLimbs].setRange(JointActionType.ActionType.SPIN, Math.toRadians(-80), Math.toRadians(0));	
		}
		else{
			upperArmJoints[numUpperArmLimbs].setRange(JointActionType.ActionType.SPIN, Math.toRadians(0), Math.toRadians(80));		
		}
		d = upperArmJoints[numUpperArmLimbs];
		allDisks.add(d);
		
		
		// construct forearm
		numForearmLimbs = 6;
		numPerForearmLimbs = 20;
		forearmJoints = new Joint[numForearmLimbs + 1];
		forearmJoints[0] = d.attachJoint(0, jointSize, Math.toRadians(90), lowerJointType);
		forearmJoints[0].setRange(JointActionType.ActionType.SPIN, Math.toRadians(10), Math.toRadians(90));	
		d = forearmJoints[0];
		allDisks.add(d);
				
		for (int i = 0; i < numForearmLimbs; i++) {
			if (i > 0) {
				forearmJoints[i] = d.attachJoint(0, jointSize, 0, lowerJointType);
				// limit all joints 
				if(i % 2 == 0){
					forearmJoints[i].setRange(JointActionType.ActionType.SPIN, Math.toRadians(-160), Math.toRadians(0));	
				}
				else{
					forearmJoints[i].setRange(JointActionType.ActionType.SPIN, Math.toRadians(0), Math.toRadians(160));		
				}
				d = forearmJoints[i];
				allDisks.add(d);
			}
			for (int j = 0; j < numPerForearmLimbs; j++) {
					d = d.attachDisk(0, limbSize, limbType);
					allDisks.add(d);
			}
		}
		
		forearmJoints[numForearmLimbs] = d.attachJoint(0, jointSize, 0, lowerJointType);
		if(numForearmLimbs % 2 == 0){
			forearmJoints[numForearmLimbs].setRange(JointActionType.ActionType.SPIN, Math.toRadians(-80), Math.toRadians(0));	
		}
		else{
			forearmJoints[numForearmLimbs].setRange(JointActionType.ActionType.SPIN, Math.toRadians(0), Math.toRadians(80));		
		}
		d = forearmJoints[numForearmLimbs];
		allDisks.add(d);
		
		//create the hand
		handjoint = d.attachJoint(0, jointSize, Math.PI/2, handJointType);
		handjoint.setRange(JointActionType.ActionType.SPIN, Math.toRadians(-90), Math.toRadians(90));
		allDisks.add(handjoint);
		Disk firstHand = handjoint.attachDisk(0, handSize, handType);
		d = firstHand;
		allDisks.add(d);
		int handLength = 12;
		lengthOfHand = handLength * handSize * 2;
		for(int i = 0; i < handLength; i++){
			d = d.attachDisk(0, handSize, handType);
			allDisks.add(d);
		}
		d = firstHand.attachDisk(Math.PI/2, handSize, handType);
		for(int i = 1; i < handLength; i++){
			d = d.attachDisk(0, handSize, handType);
			allDisks.add(d);
		}
		
		//create mouth
		mouth = body.attachDisk(Math.PI/4, mouthSize, mouthType);
		allDisks.add(mouth);
		
		//create sting
		sting = env.newFixedRoot(85, 85, mouthSize, 0, stingType);
		allDisks.add(sting);
		
		Actuator emptyActuator = new EmptyActuator("Food", new PolygonDiskSymbol(null).getTriangleSymbol(0.5));
		foodType = new DiskType(DiskMaterial.HEAVYDOUGH.withColor(Color.GREEN), emptyActuator);//new DiskType(new DiskMaterial(1.0, 0, 50.0, 1.0, Color.GREEN.brighter()), emptyActuator);
		object = env.newRootDisk(70, 60, foodRadius, foodType);
		//object.attachDisk(0, 0.5, foodType);
		//d2.applyImpulse(0, -800); 
		
		

		this.setQuadrantFloor(env, FloorCellType.FULL, FloorCellType.FULL);//new FloorCellType(0.0, Color.DARK_GRAY.darker().darker()), FloorCellType.EMPTY);
		
		return env;
	}
	
	public AgentMapping[] getAgentMappings() {
		// create possible ego-motion actions
		List<DiskAction> actions = new LinkedList<DiskAction>();
		// set max angular speed of joint rotations
		double maxAngularChange = 5.0;//1.0;//0.5;
		// first joint is controlled by target angle
		actions.add(upperArmJoints[0].createJointAction("upperarmJoint#0", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE)); //TARGET
		// other joints are controlled by angle change
		for (int i = 1; i < upperArmJoints.length; i++) {
			actions.add(upperArmJoints[i].createJointAction("upperarmJoint#" + i, maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
		}
		for (int i = 0; i < forearmJoints.length; i++) {
			actions.add(forearmJoints[i].createJointAction("forearmJoint#" + i, maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
		}
		actions.add(handjoint.createJointAction("hand", maxAngularChange, JointActionType.ActionType.SPIN, JointActionType.ControlType.CHANGE));
		
		return new AgentMapping[] { new AgentMapping(actions) };
	}
	
	public void removeObject(){
		if(object != null){
			env.getDiskComplexesEnsemble().removeDiskComplex(object.getDiskComplex());
			object = null;
		}
	}
	
	public void generateNewObject(){
		if(object == null){
			double[] pos = this.generateNewObjectPosition(foodRadius);
			Actuator emptyActuator = new EmptyActuator("Food", new PolygonDiskSymbol(null).getTriangleSymbol(0.5));
			//DiskType foodType = new DiskType(new DiskMaterial(1.0, 0.05, 20.0, 1.0, Color.GREEN.brighter()), emptyActuator);
			object = env.newRootDisk(pos[0], pos[1], foodRadius, foodType);
			//System.out.println("Rel Pos " + object.getX() + " " + object.getY());
			
		}
	}

	@Override
	public AgentController[] getControllers() {
		// the controller of the agent
		AgentController controller = new AgentController() {
			int time = 0;
			@Override
			public void doTimeStep(double[] sensorValues, double[] actuatorValues) {
				
				time++;
				if(time > 200){
					removeObject();
					generateNewObject();
					time = 0;
				}
				//System.out.println("X: " + object.getDiskComplex().getSpeedx() + " Y: " + object.getDiskComplex().getSpeedy());
				double[] commands = foodToMouth(object.getX(), object.getY(), handjoint.getX(), handjoint.getY(), foodRadius, lengthOfHand);
				
				for(int i = 0; i < upperArmJoints.length; i++){
					
					upperarmRotation = upperarmRotation + 0.01 * commands[0];
					
					if(i == 0 || i == numUpperArmLimbs){
						//actuatorValues[i] = 0.5 * upperarmRotation;//
						actuatorValues[i] = 0.25 * commands[0];
					}
					else{
						if(i%2 == 1){
							//actuatorValues[i] = -1 * upperarmRotation;//
							actuatorValues[i] = -0.5 * commands[0];
						}
						else{
							//actuatorValues[i] = upperarmRotation;// 
							actuatorValues[i] = 0.5 * commands[0];
						}
					}
					
				}
				
				
				forearmRotation = forearmRotation + 0.01 * commands[1];
				System.out.println("Desired Rotation: " + forearmRotation);
				System.out.println("Current Rotation: " + upperArmJoints[0].getAngle());
				for(int i = upperArmJoints.length; i < upperArmJoints.length + forearmJoints.length; i++){
					if(i == upperArmJoints.length || i == upperArmJoints.length + numForearmLimbs){
						//actuatorValues[i] = 0.5 * forearmRotation;// 
						actuatorValues[i] = 0.25 * commands[1];
					}
					else{
						if(i%2 == 0){
							//actuatorValues[i] = -1 * forearmRotation;//
							actuatorValues[i] = -0.5 * commands[1];
						}
						else{
							//actuatorValues[i] = forearmRotation;//
							actuatorValues[i] = 0.5 * commands[1];
						}
					}
					
				}
				
				actuatorValues[upperArmJoints.length + forearmJoints.length] = 0.5 * commands[2];
				

			}
		};
		return new AgentController[] { controller };
	}
	
	
	/*
	 * Current Sensation of the animat
	 * returns [BxH, ByH, BdeltaH, HxO, HyO, HdeltaO]
	 */
	public double[] getSensation(){
		
		double[] sensation = new double[6];
		
		//Position of hand in body frame
		double BxH = this.handjoint.getX() - this.body.getX();
		double ByH = this.handjoint.getY() - this.body.getY();
		double BdeltaH = -this.handjoint.getAngle() + Math.PI;
		if(BdeltaH > Math.PI){
			BdeltaH = Math.PI;
		}
		if(BdeltaH < 0){
			BdeltaH = 0;
		}
		sensation[0] = BxH;
		sensation[1] = ByH;
		sensation[2] = BdeltaH;
		
		double[] HposO = this.transformToHandFrame(this.object.getX(), this.object.getY(), BdeltaH);
		
		sensation[3] = HposO[0];
		sensation[4] = HposO[1];
		
		
		//TODO
		//Winkel von Obejct zu Hand:
		double HdeltaO = object.getAngle() - Math.PI - BdeltaH;
		sensation[5] = HdeltaO;
		return sensation;
	}
	
	private double[] transformToHandFrame (double x, double y, double BdeltaH){
		x = x - handjoint.getX();
		y = y - handjoint.getY();
		double temp = x;
		double rotationAngle = Math.PI - BdeltaH;
		x = calculateRotationX(x, y, rotationAngle);
		y = calculateRotationY(temp, y, rotationAngle);
		double[] pos = new double[2];
		pos[0] = x;
		pos[1] = y;
		return pos;
	}
	
	private double calculateRotationX (double x, double y, double alpha){
		return Math.cos(alpha)*x - Math.sin(alpha) * y;
	}
	
	private double calculateRotationY (double x, double y, double alpha){
		return Math.sin(alpha)*x + Math.cos(alpha)*y;
	}
	
	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		settings.getOptions().getOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_GRID).setEnabled(false);
		return true;
	}
	
	public void setQuadrantFloor(Environment env, FloorCellType type1, FloorCellType type2) {
		for (int i = 0; i < env.getFloor().getNumX(); i++)
			for (int j = 0; j < env.getFloor().getNumY(); j++)
				env.getFloor().setType(i, j, (i >= this.mouth.getX() + this.mouth.getRadius() && i <= this.sting.getX() - 2* this.sting.getRadius()) && (j >= this.mouth.getY() + this.mouth.getRadius() && j <= this.sting.getY() - 2*this.sting.getRadius()) ? type1 : type2);
	}
	
	public double[] generateNewObjectPosition(double objectRadius){
		double[] pos = new double[2];
		boolean notFinished = true;
		while(notFinished){
		
		pos[0] = r.nextDouble() * ((this.sting.getX() - 2* this.sting.getRadius()) - (this.mouth.getX() + this.mouth.getRadius())) + (this.mouth.getX() + this.mouth.getRadius());
		pos[1] = r.nextDouble() * ((this.sting.getY() - 2* this.sting.getRadius()) - (this.mouth.getY() + this.mouth.getRadius())) + (this.mouth.getY() + this.mouth.getRadius());
		//System.out.println("New Pos " + pos[0] + " " + pos[1]);
		notFinished = false;
		for(Disk d: allDisks){
			if(euclidianDistance(pos[0], pos[1], d.getX(), d.getY()) < objectRadius + d.getRadius()){
				notFinished = true;
				//System.out.println("Got here");
			}
		}
		}
		return pos;
	}
	
	private double euclidianDistance(double x1, double y1, double x2, double y2){
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}
	
	public double[] foodToMouth (double x, double y, double xH, double yH, double objectRadius, double handlength){
		//commands describes angular changes to be performed
		//commands[0] = upper arm change
		//commands[1] = forearm change
		//commands[2] = hand rotation		
		double[] commands = new double[3];
		
		if(x + objectRadius < xH 
				&& xH - handlength < x - objectRadius){
			//Das Objekt befindet sich x-weise in der Hand
			if(y + objectRadius < yH 
					&& yH - handlength < y - objectRadius){
				//Das Objekt befindet sich y-weise in der Hand
				// Runter, links
				commands[0] = -0.75;
				commands[1] = -0.75;
				//System.out.println("Perfect");
			}
			else if(y > yH){
				//Das Objekt befindet sich oberhalb der Hand
				//Rechts
				commands[0] = 1.0;
				//System.out.println("In Hand, abobve");
			}
			else{
				//Das Objekt befindet sich unterhalb der Hand
				//Runter
				commands[1] = -1.0;
				//System.out.println("In Hand, below");
			}
		}
		else if(y + objectRadius < yH 
				&& yH - handlength < y - objectRadius){
			if(x > xH){
				//Das Objekt befindet sich rechts von der Hand
				//Runter (unten durch)
				commands[1] = -1.0;
				//System.out.println("In Hand, right");
			}
			else{
				//Das Objekt befindet sich links von der Hand
				//Links
				commands[0] = -1.0;
				//System.out.println("In Hand, left");
			}
		}
		else if(x > xH){
			//Das Objekt ist rechts von der Hand
			if(y - 2 > yH + objectRadius){
				//Das Objekt befindet sich rechts "uber der Hand
				//Rechts
				commands[0] = 1.0;
				//System.out.println("right, abobve");
			}
			else{
				//Das Objekt befindet sich rechts unter der hand
				//Runter
				commands[1] = -1.0;
				//System.out.println("right, below");
			}
		}
		else{
			//Das Objekt ist links von der Hand
			if(y + 2 < yH - objectRadius){
				//Das Objekt ist links unter der Hand
				//links runter
				commands[0] = -1.0;
				commands[1] = -1.0;
				//System.out.println("left, below");
			}
			else if(x + objectRadius + 2.0 > xH - handlength ){
				//Das Objekt ist links "uber der Hand aber zu nah an der Hand
				//rechts
				//System.out.println("left, abobve special");
				commands[0] = 1.0;
				
			}
			else{
				//Das Objekt ist links  oberhalb der Hand
				//hoch
				//System.out.println("left, abobve");
				commands[1] = 1.0;
			}
		}
	
		return commands;
	}
	

	public static void main(String[] args) {
		DemoLauncher.runDemo(new Skorpion());
	}

}
