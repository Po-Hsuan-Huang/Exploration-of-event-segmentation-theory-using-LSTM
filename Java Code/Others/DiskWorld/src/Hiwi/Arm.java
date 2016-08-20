package Hiwi;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import additionalVisualization.ModelFrame;
import Hiwi.MyDemoLauncher.MyDemo;
import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.DiskMaterial;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.actions.DiskAction;
import diskworld.actions.Joint;
import diskworld.actions.JointActionType;
import diskworld.actuators.EmptyActuator;
import diskworld.actuators.Mouth;
import diskworld.demos.DemoLauncher;
import diskworld.demos.DemoLauncher.Demo;
import diskworld.demos.Skorpion;
import diskworld.environment.AgentMapping;
import diskworld.environment.FloorCellType;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.AgentController;
import diskworld.interfaces.FrictionModel;
import diskworld.interfaces.PhysicsParameters;
import diskworld.visualization.PolygonDiskSymbol;
import diskworld.visualization.VisualizationOptions;
import diskworld.visualization.VisualizationSettings;

public class Arm implements MyDemo{
	
	//Training/Testing parameters
	boolean alwaysSupervised = false;
	boolean objectSwitched = false;
	
	boolean initialPhase = true;
	boolean training = true;
	double[][] testingPos = {{37.5, 37.5}, {37.5, 67.5}, {67.5, 37.5}, {67.5, 67.5}};
	LinkedList<Integer> testingSet = new LinkedList<Integer>();
	LinkedList<Integer> trainingObjectTypes = new LinkedList<Integer>();
	int objectsTested = 0;
	int objectsTrained = 0;
	int stretchTime = 350;
	boolean stretching = false;
	boolean timeForInteractionStretch = false;
	LinkedList<Integer> timesForFly = new LinkedList<Integer>();
	LinkedList<Integer> timesForBurrdock = new LinkedList<Integer>();
	LinkedList<Integer> timesForHeavyBurrdock = new LinkedList<Integer>();
	//LinkedList<Double> errorsForFly = new LinkedList<Double>();
	//LinkedList<Double> errorsForBurrdock = new LinkedList<Double>();
	//LinkedList<Double> errorsForHeavyBurrdock = new LinkedList<Double>();
	
	ExampandableIntMatrix predictedModelsFly = new ExampandableIntMatrix(8);
	ExampandableIntMatrix predictedModelsBurrdock = new ExampandableIntMatrix(8);
	ExampandableIntMatrix predictedModelsHeavyBurrdock = new ExampandableIntMatrix(8);
	
	
	int maxTrainingSets = 30;
	int nrOfTrainingSets = 0;
	int nrOfTestingSets = 0;
	int numTestPos = 5;
	
	long miliseconds = 1;
	
	int time = 0;
	
	int GRABFLY = 0;
	int PUSHFLY = 1;
	int GRABBURRDOCK = 2;
	int CARRYBURRDOCK = 3;
	int GRABHEAVYBURRDOCK = 4;
	int CARRYHEAVYBURRDOCK = 5;
	
	int[] performedActions = new int[6];
	int currentAction = -1;
	LinkedList<Double> errorsCurrentAction = new LinkedList<Double>();
	

	int timeForInteraction = 500;
	int timeActionPerformed = 0;
	
	boolean useDifferentObjects = false; //false = burrdock, true = fly
	boolean useForwardModelsForCommands = true;
	
	boolean includeRotation = false;
	
	double distanceToPit = 10.0;//20.0; //10
	
	int ignoreCounter = 0;
	
	
	ScenarioManager scenario = new BurrdockScenario();
	
	public Arm(long x){
		rand = new Random(x);
		rs = x;
		scenario = new LearningScenario(rand);
	}
	
	Random rand;
	long rs;
	
	//Interactable Object types
	//int PRICK = 0;

	int BURRDOCK = 0;
	int HEAVYBURRDOCK = 100;
	int FLY = 200;
	
	double minDistanceToObjects = 4.0;
	
	//Grenzen der Positionen die ein Objekt einnehmen kann
	double minPosX;
	double minPosY;
	double maxPosX;
	double maxPosY;
	
	Environment env;
	Arm arm = this;
	
	private Disk body;
	private Joint[] upperArmJoints;
	private Joint[] forearmJoints;
	private Joint handjoint;
	private LinkedList<Disk> allDisks;
	
	private InteractableObjects object;
	
	private int numUpperArmLimbs;
	private int numPerUpperArmLimbs;
	private int numForearmLimbs;
	private int numPerForearmLimbs;
	
	//provide disk sizes
	double bodySize = 5;
	double jointSize = 1;
	double handSize = 0.375;
	double limbSize = 0.2;
	double mouthSize = 4;
	
	// provide disk types 
	DiskType rootType = new DiskType(DiskMaterial.DOUGH.withColor(Color.ORANGE));
	DiskType upperJointType = new DiskType(DiskMaterial.DOUGH.withColor(Color.RED));
	DiskType lowerJointType = new DiskType(DiskMaterial.DOUGH.withColor(Color.MAGENTA));
	DiskType limbType = new DiskType(DiskMaterial.DOUGH.withColor(Color.BLUE));
	DiskType handJointType = new DiskType(DiskMaterial.DOUGH.withColor(Color.GREEN));
	DiskType handType = new DiskType(DiskMaterial.DOUGH.withColor(Color.YELLOW));
	DiskType mouthType = new DiskType(DiskMaterial.DOUGH.withColor(Color.PINK));
	
	
	//for pseudo movements
	int timeMovePerformed = 0;
	double[] lastCommand = null;
	double[] lastHandPos = new double[2]; //to switch commands when hand stays still
	
	//Remove burrdocks when they enter this area:
	double burrdockKillerMinX = 50.0;
	double burrdockKillerMaxX = 55.0;
	double burrdockKillerMinY = 50.0;
	double burrdockKillerMaxY = 55.0;
	
	//Remove flys when they exit this area:
	double flyAreaMinX = 27.5;
	double flyAreaMaxX = 67.5;
	double flyAreaMinY = 27.5;
	double flyAreaMaxY = 67.5;
	
	ModelFitting models;
	
	int xSize = 2; 
	int ySize = 8;
	
	int slowDown = 0;
	
	double hapticX;
	double hapticY;
	
	double distancHandBurrX = 0.0;
	double distanceHandBurrY = 0.0;
	
	@Override
	public String getTitle() {
		return "Arm interacting with objects";
	}
	
	@Override
	public long getMiliSecondsPerTimeStep() {
		return miliseconds;
	}
	
	
	/*
	 * Construct and initialize the environment, the robot and the first object
	 * (non-Javadoc)
	 * @see Hiwi.MyDemoLauncher.MyDemo#getEnvironment()
	 */
	
	@Override
	public Environment getEnvironment(){
		
		//if(useDifferentObjects)
			xSize = 5; //8??
		
		// create environment
		int sizex = 100;
		int sizey = 100;
		env = new Environment(sizex, sizey);
	
		
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
		handjoint = d.attachJoint(0, jointSize * 3.5, Math.PI/2, handJointType);
		handjoint.setRange(JointActionType.ActionType.SPIN, Math.toRadians(-90), Math.toRadians(90));
		allDisks.add(handjoint);
		handjoint.setZLevel(1);
		// Wrist joint
		/*Disk firstHand = handjoint.attachDisk(0, handSize, handType);
		d = firstHand;
		allDisks.add(d);
		int handLength = 12;
		for(int i = 0; i < handLength; i++){
			d = d.attachDisk(0, handSize, handType);
			d.setZLevel(1);
			allDisks.add(d);
		}
		d = firstHand.attachDisk(Math.PI/2, handSize, handType);
		for(int i = 1; i < handLength; i++){
			d = d.attachDisk(0, handSize, handType);
			allDisks.add(d);
			d.setZLevel(1);
		}*/
		
		
		//determine min and max Handposition
		minPosX = body.getX() + bodySize + numUpperArmLimbs * jointSize + minDistanceToObjects;
		minPosY = body.getY() + bodySize + numForearmLimbs * jointSize + minDistanceToObjects;
		maxPosX = handjoint.getX() - minDistanceToObjects;
		maxPosY = handjoint.getY() - minDistanceToObjects;
		
		

		this.setFloor(env, FloorCellType.FULL, FloorCellType.FULL, FloorCellType.BURRKILLER);
		

		this.object = new HeavyBurrdock(env, 45, 93, 0, handJointType, this);// Burrdock(env, 35, 56, handType, this);
		models = new ModelFitting(xSize, getSensation(), 1000, 0.99, rs, this); 
		
		this.hapticX = rand.nextDouble();
		this.hapticY = rand.nextDouble();
		
		//Initialize the testing set
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 3; j++){
				testingSet.add((Integer) (j * 100 + i));
			}
		}
		
		
		//initialize testing objects
		for(int i = 2; i >= 0; i--){
			for(int j = 0; j < numTestPos; j++){
				trainingObjectTypes.add((Integer) i * 100);
			}
		}
		//Collections.shuffle(trainingObjectTypes, rand);
		
		return env;
	}
	
	public AgentMapping[] getAgentMappings() {
		// create possible ego-motion actions
		List<DiskAction> actions = new LinkedList<DiskAction>();
		// set max angular speed of joint rotations
		double maxAngularChange = 2.0;//0.5;
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
	
	/*
	 * 
	 * Adding or removing objects
	 * 
	 */
	
	public boolean objectToBeRemoved(){
		
		if(object.getType() == FLY){
			if(object.getX() > 96 || object.getY() > 96 || object.getX() < 4 || object.getY() < 4){
				return true;
			}
		}
		
		if(object.getType() == FLY){
			return euclidianDistance(object.getX(), object.getY(), 52.5, 52.5) > 40;
		}
		
		return euclidianDistance(object.getX(), object.getY(), 52.5, 52.5) < 2.0;//(object.getY() >= burrdockKillerMinY && object.getY() <= burrdockKillerMaxY && object.getX() >= burrdockKillerMinX && object.getX() <= burrdockKillerMaxX);
	}
	
	public void removeObject(){
		if(object != null){
			object.remove();
			object = null;
		}
	}
	
	public double[] generateNewObjectPosition(int objectType){
		if(!training){
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("ObjTested " + objectsTested + ", In TestingSet " + ((int) testingSet.get(objectsTested)) + ", pos " + ((int) testingSet.get(objectsTested))%100);
			return testingPos[((int) testingSet.get(objectsTested))%100];
		}
		/*if(objectType == FLY){
			return generateNewObjectPositionFly();
		}*/ //TODO
		
		return generateNewObjectPositionBurrdock();
	}
	
	private double[] generateNewObjectPositionBurrdock(){
		double objectRadius = 3.75;
		double[] pos = new double[2];
		boolean notFinished = true;
		while(notFinished){
		
		pos[0] = rand.nextDouble() * (this.maxPosX - this.minPosX) + minPosX;
		pos[1] = rand.nextDouble() * (this.maxPosY - this.minPosY) + minPosY;
		notFinished = false;
		for(Disk d: allDisks){
			if(euclidianDistance(pos[0], pos[1], d.getX(), d.getY()) < objectRadius + d.getRadius()){
				notFinished = true;
			}
		}

		if(pos[0] <= burrdockKillerMaxX + distanceToPit && pos[0] >= burrdockKillerMinX - distanceToPit && pos[1] >= burrdockKillerMinY - distanceToPit && pos[1] <= burrdockKillerMaxY + distanceToPit){
			notFinished = true;
		}
		
		
		}
		return pos;
	}
	
	private double[] generateNewObjectPositionFly(){
		double objectRadius = 3.0;
		double[] pos = new double[2];
		boolean notFinished = true;
		while(notFinished){
		
		pos[0] = rand.nextDouble() * (this.flyAreaMaxX - this.flyAreaMinX) + flyAreaMinX;//   - (this.mouth.getX() + this.mouth.getRadius())) + (this.mouth.getX() + this.mouth.getRadius());
		pos[1] = rand.nextDouble() * (this.flyAreaMaxY - this.flyAreaMinY) + flyAreaMinY;// * ((this.sting.getY() - 2* this.sting.getRadius()) - (this.mouth.getY() + this.mouth.getRadius())) + (this.mouth.getY() + this.mouth.getRadius());
		//System.out.println("New Pos " + pos[0] + " " + pos[1]);
		notFinished = false;
		for(Disk d: allDisks){
			if(euclidianDistance(pos[0], pos[1], d.getX(), d.getY()) < objectRadius * 1.5 + d.getRadius()){
				notFinished = true;
			}
		}
		
		}
		return pos;
	}
	
	
	/*
	 * 
	 * Movements of the arm
	 * 
	 * 
	 */
	
	
	public double[] pseudoRandomMovement(int time){
		double[] commands = new double[xSize];
		if(Math.abs(lastHandPos[0] - handjoint.getX()) < 0.00001 ||/*&&*/ Math.abs(lastHandPos[1] - handjoint.getY()) < 0.00001){
			timeMovePerformed = 0;
		}
		if(timeMovePerformed == 0){
			commands[0] = rand.nextDouble() * 2 -1;
			commands[1] = rand.nextDouble() * 2 -1;
			if(includeRotation)
				commands[2] = (rand.nextDouble()  - 0.5) * 2;
			timeMovePerformed = rand.nextInt(200);
			lastCommand = commands;
		}
		else{
			commands[0] = lastCommand[0];
			commands[1] = lastCommand[1];
			if(includeRotation)
				commands[2] = lastCommand[2];
			timeMovePerformed--;
		}
		
		commands[0] += ( rand.nextDouble() * 0.02 - 0.01);
		commands[1] += ( rand.nextDouble() * 0.02 - 0.01);
		
		return commands;
	}
	
	public double[] scriptedMovements(int timeReal){
		int time = timeReal -100;
		
		double[] commands = new double[xSize]; 
		if(time < -50){
			commands[0] = -0.7;
			commands[1] = -0.5;
			if(includeRotation)
				commands[2] = -0.1;
			return commands;
		}
		else if(time < 0){
			commands[0] = 0.5;
			commands[1] = 0.7;
			if(includeRotation)
				commands[2] = 0.1;
			return commands;
		}
		else if(time < 200){
			if(time < 100){
				commands[0] = -1.0;
			}
			else{
				commands[1] = -1.0;
				if(includeRotation)
					commands[2] = -0.1;
			}
			
			return commands;
		}
		
		int t = (time - 200) % 800;
		if(t < 150){
			commands[0] = (-1.0);
		}
		else if(t < 300){
			commands[1] =  (-1.0);
		}
		else if(t < 450){
			commands[0] = 1.0;
		}
		else if(t < 600){
			commands[1] = 1.0;
		}
		else if (t < 700){
			commands[0] = -0.5;
			commands[1] = -0.5;
		}
		else if (t < 800){
			commands[0] = 0.5;
			commands[1] = 0.5;
		}
		commands[0] *= 1.0; commands[0]  += (rand.nextDouble() * 0.02) - 0.01;
		commands[1] *= 1.0; commands[1] +=  (rand.nextDouble() * 0.02) - 0.01;
		
		return commands;
	}
	
	double[] supervisedControll(){

		double[] motor = new double[2];
		if(stretching && stretchTime > 150){
			motor[0] = 1.0;
			motor[1] = 1.0;
			return motor;
		}
		else if (stretching && stretchTime <= 150){
			motor[0] = 52.5 - handjoint.getX();
			motor[1] = 52.5 - handjoint.getY();
			return motor;
		}
		
		if(object.isMerged()){
			motor[0] = 52.5 - object.getX();
			motor[1] = 52.5 - object.getY();
		}
		else{
			motor[0] = object.getX() - handjoint.getX();
			motor[1] = object.getY() - handjoint.getY();
		}
		return motor;
	}

	/*
	 * 
	 * The controller
	 * (non-Javadoc)
	 * @see Hiwi.MyDemoLauncher.MyDemo#getControllers()
	 */
	@Override
	public MyAgentController[] getControllers() {
		// the controller of the agent
		MyAgentController controller = new MyAgentController() {
			
			
			double[] lastSensation = getSensation();
			
			@Override
			public void doTimeStep(double[] sensorValues, double[] actuatorValues) {
				objectSwitched = false;
				time++;
				timeForInteraction--;
				if(stretching){
					stretchTime--;
				}
				if(timeForInteraction < -300 && !initialPhase && !stretching){
					stretching = true;
					stretchTime = 350;
					timeForInteractionStretch = true;
				}
				System.out.print("t = " + timeForInteraction + "    ");
				
				//scenario.effectOnModels(time, models);
				
				for(int i = 0; i < actuatorValues.length; i++)
					actuatorValues[i] = 0.0;
				
				if(objectToBeRemoved() || scenario.removeObject(time) 
						|| (stretching && stretchTime <= 0 && !timeForInteractionStretch) ||
						(!training && timeForInteraction <= 0)){
					/*if(!training){
						printMeanError(-1, stretching);
					}*/
					predictModels();
					if(initialPhase){
						initialPhase = false;
					}
					else if(stretching){
						stretching = false;
					}
					else{
						if(training){
							objectsTrained++;
						}
						else{
							stretching = true;
							stretchTime = 350;
							objectsTested++;
							if(object.getType() == FLY){
								timesForFly.add(500 - timeForInteraction);
							}
							else if (object.getType() == BURRDOCK){
								timesForBurrdock.add(500 - timeForInteraction);
							}
							else{
								timesForHeavyBurrdock.add(500 - timeForInteraction);
							}
						}
					}
					
					
					if(objectsTrained == numTestPos * 3){
						//nrOfTrainingSets++;
						nrOfTestingSets++;
						training = false;
						objectsTrained = 0;
						Collections.shuffle(trainingObjectTypes, rand);
						stretchTime = 350;
						stretching = true;
					}
					
					if(objectsTested == 12){
						printAfterAction();
						nrOfTrainingSets++;
						training = true;
						objectsTested = 0;
						Collections.shuffle(testingSet, rand);
						stretchTime = 350;
						stretching = true;
					}
					
					int newType;
					removeObject();
					double[] pos;
					if(stretching){
						newType = HEAVYBURRDOCK;
						double[] posStretching = {50.0, 95.0};
						pos = posStretching;
					}
					else if (training){
						newType = (int) trainingObjectTypes.get(objectsTrained);
						pos = generateNewObjectPosition(newType);
					}
					else{
						int testingStimuli = (int) testingSet.get(objectsTested);
						if(testingStimuli < 100){
							newType = 0;
						}
						else if (testingStimuli < 200){
							newType = 100;
						}
						else{
							newType = 200;
						}
						pos = generateNewObjectPosition(newType);
					}
					int newAction = -1;
					if(newType == HEAVYBURRDOCK){
						object = new HeavyBurrdock(env, pos[0], pos[1], 0,  handJointType, arm);
						newAction = GRABHEAVYBURRDOCK;
					}
					else if (newType == BURRDOCK){
						object = new Burrdock(env, pos[0], pos[1], 0,  handJointType, arm);
						newAction = GRABBURRDOCK;
					}
					else if(newType == FLY){
						object = new Fly(env, pos[0], pos[1], arm);
						newAction = GRABFLY;
					}
					
					
					slowDown = 10;
					timeForInteraction = 500;
					currentAction = newAction;
					objectSwitched = true;
					
				}
				
				/*if(!training){
					double meanError = models.getMeanError();
					printMeanError(meanError, stretching);
					
				}*/
				
				if(stretching == true && stretchTime <= 0 && timeForInteractionStretch){
					stretching = false;
					timeForInteractionStretch = false;
					timeForInteraction = 0;
				}
				
				System.out.println("Training " + training + ", Stretching " + stretching + ", Stretch Time " + stretchTime + ", InteractionTime " + timeForInteraction);
				
				double[] forwardX; 
				if(stretching || alwaysSupervised){
					forwardX = supervisedControll();
				}
				else{
					if(timeForInteraction > 0 || !training)
						forwardX = models.forwardToChangeModel(getSensation(), 2);
					else
						forwardX = supervisedControll();
				}
				/*else{
					Double[] goalSensation = scenario.getDesiredSensation(time, object);
					Double[] deltaGoal = getDesiredSensationalChange(getSensation(), goalSensation);
					forwardX = models.forwardModel(deltaGoal);
				}*/
				double[] commands = new double[3];
				
				if(forwardX != null){
					
					int lim = 2;
					for(int i = 0; i < lim; i++){ //UNSCHOEN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						if(forwardX[i] > 1.0){
							commands[i] = 1.0;
						}
						else if(forwardX[i] < -1.0){
							commands[i] = -1.0;
						}
						else{
							commands[i] = forwardX[i];
						}
						commands[i] += rand.nextGaussian() * 0.1; //TODO
					}
				}
				else{
					if(scenario.useScriptedMovement(time)){
						commands = scriptedMovements(time);
					}
					else{
						commands = pseudoRandomMovement(time);
					}
				}
				
				lastHandPos[0] = handjoint.getX();
				lastHandPos[1] = handjoint.getY();
				

				if(slowDown > 0){
					for(int i = 0; i < commands.length; i++)
						commands[i] = 0.1 * commands[i];
					
					slowDown--;
				}
				else if( object.getType() == HEAVYBURRDOCK && body.getDiskComplex().equals(object.getDisk().getDiskComplex())){
					for(int i = 0; i < commands.length; i++)
						commands[i] = 0.5 * commands[i];
				}
				
				//Move upper arm according to command
				for(int i = 0; i < upperArmJoints.length; i++){
					if(i == 0 || i == numUpperArmLimbs){
						actuatorValues[i] = 0.5 * commands[0];
					}
					else{
						if(i%2 == 1){
							actuatorValues[i] = -1 * commands[0];
						}
						else{
							actuatorValues[i] = commands[0];
						}
					}
				}
				
				//Move forearm according to command
				for(int i = upperArmJoints.length; i < upperArmJoints.length + forearmJoints.length; i++){
					if(i == upperArmJoints.length || i == upperArmJoints.length + numForearmLimbs){
						actuatorValues[i] = 0.5 * commands[1];
					}
					else{
						if(i%2 == 0){
							actuatorValues[i] = -1 * commands[1];
						}
						else{
							actuatorValues[i] = 1 * commands[1];
						}
					}
				}
				
				//Rotate hand according to command
				if(includeRotation)
					actuatorValues[upperArmJoints.length + forearmJoints.length] = commands[2];	
			}
			
			@Override
			public void doAfterTimeStep() {

				double[] newSensation = getSensation();
				
				double[] x = new double[xSize];
				x[0] = newSensation[0] - lastSensation[0];
				x[1] = newSensation[1] - lastSensation[1];
				System.out.println("CHANGE: " + x[0] + ", " + x[1]);
				
				//if(useDifferentObjects){ //TODO
					x[2] = hapticX;
					//System.out.println("Hap X: " + hapticX);
					x[3] = hapticY;
					//System.out.println("Hap y: " + hapticY);
				//}	
					/*x[4] = 0;
					if(touchedObject){
						x[4] = 1;
					}*/
				x[4] = newSensation[6] - lastSensation[6];
				//x[5] = newSensation[7] - lastSensation[7];
				//x[2] = newSensation[6] - lastSensation[6];
				
				if( object.getType() == HEAVYBURRDOCK && body.getDiskComplex().equals(object.getDisk().getDiskComplex())){
					for(int i = 0; i < 2/*x.length*/; i++) //UNSCHOEN!!!!!!!!!!!!
						x[i] = 2 * x[i];
				}
				
				double[] prediction = models.predict(x);
				
				double[] y = new double[ySize];
				for(int i = 0; i < ySize; i++){
					y[i] =  newSensation[i] - lastSensation[i];
				}
				int[] modelRecommendations = null;
				//Dont use modelRecommendations if you want the model to learn by itself
				// use only for debugging purposes or if you want to compare the exact models used
				if(true){
					if(object.getType() == FLY){
						if(object.hasMoved()){
							int[] mR = {0, 0, 1, 1, 1, 1, 0, 0};
							modelRecommendations = mR;
						}
						else{
							int[] mR = {0, 0, 0, 0, 0, 0, 0, 0};
							modelRecommendations = mR;
						}
					}
					else if(object.getType() == BURRDOCK){
						if(object.isMerged()){
							int[] mR = {0, 0, 3, 3, 2, 2, 0, 0};
							modelRecommendations = mR;
						}
						else{
							int[] mR = {0, 0, 0, 0, 0, 0, 0, 0};
							modelRecommendations = mR;
						}
					}
					else{
						if(object.isMerged()){
							int[] mR = {1, 1, 2, 2, 2, 2, 0, 0};
							modelRecommendations = mR;
						}
						else{
							int[] mR = {0, 0, 0, 0, 0, 0, 0, 0};
							modelRecommendations = mR;
						}
					}
				}
				if(nrOfTrainingSets != 0){
					for(int i = 0; i < modelRecommendations.length; i++){
						if(i!= 2){
							modelRecommendations[i] = -1;
						}
					}
				}
				
				//models.doTimeStepModelForcing(x, y, newSensation, time, modelRecommendations, objectSwitched);
				//printErrors(models.getErrorsPerDim(), modelRecommendations);
				models.doTimeStep(x, y, newSensation, time, (training && !stretching), modelRecommendations);
				lastSensation = newSensation;
			}
			@Override
			public void finishPrints() {
				models.finishPrint(rs);
				
			}
			
			@Override
			public boolean readyToStop(){
				return models.finished();
			}
			
			
		};
		return new MyAgentController[] { controller };
	}
	
	/*
	 * 
	 * Computing sensations and sensational change
	 * 
	 * 
	 */
	public double[] getSensation(){
		//if(useDifferentObjects){
			return getSensationMultpileObjects();
		//}
		//return getSensationOneObject();
	}
	
	//Sensation if only burrdock is used
	private double[] getSensationOneObject(){
		
		double[] sensation = new double[ySize];

		sensation[0] = this.handjoint.getX();
		sensation[1] = this.handjoint.getY();				
		sensation[2] = this.object.getX();
		sensation[3] = this.object.getY();
		sensation[4] = this.object.getX() - handjoint.getX();
		sensation[5] = this.object.getY() - handjoint.getY();
		sensation[6] = this.object.getType();
		return sensation;
	}
	
	//Sensation if fly is used
	private double[] getSensationMultpileObjects(){
		double[] sensation = new double[ySize];

		sensation[0] = this.handjoint.getX();
		sensation[1] = this.handjoint.getY();				
		sensation[2] = this.object.getX();
		sensation[3] = this.object.getY();
		sensation[4] = this.object.getX() - handjoint.getX();
		sensation[5] = this.object.getY() - handjoint.getY();
		
		if(object.isMerged()){
			sensation[4] = distancHandBurrX;
			sensation[5] = distanceHandBurrY;
		}
		
		
		//For Fly:
		sensation[6] = euclidianDistance(object.getX(), object.getY(), 52.5, 52.5); //TODO
		//sensation[7] = Math.abs(52.0 - object.getY());
		//if(touchedObject){
			//sensation[6] = time - contactTime;
		//}
		
		sensation[7] = (double) this.object.getType();
		//System.out.println("Angle: " + sensation[7]);
		return sensation;
		
	}
	
	
	public Double[] getDesiredSensationalChange (double[] currentSensation, Double[] goalSensation){
		Double[] delta = new Double[currentSensation.length];
		for(int i = 0; i < currentSensation.length; i++){
			if(goalSensation[i] == null){
				delta[i] = null;
			}
			else{
				delta[i] = goalSensation[i] - currentSensation[i];
			}
		}
		return delta;
	}
	
	/*
	 * 
	 * For object event handlers
	 * 
	 */
	
	public DiskComplex getDiskComplex(){
		return this.body.getDiskComplex();
	}
	
	public void slowDown(){
		slowDown = 30; 
		//printMeanError(-2, false);
	}
	
	public void setHapticFeedback(double haptX, double haptY){
		this.hapticX = haptX;
		this.hapticY = haptY;
	}
	
	public void setHandBurrDistance(){
		this.distancHandBurrX = this.object.getX() - handjoint.getX();
		this.distanceHandBurrY = this.object.getY() - handjoint.getY();
	}
	
	public void setTimeForInteraction(){
		/*this.printAfterAction(timeActionPerformed);
		timeForInteraction = 350;
		timeActionPerformed = 0;
		if(object.getType() == FLY){
			currentAction = PUSHFLY;
		}
		else if (object.getType() == BURRDOCK){
			currentAction = CARRYBURRDOCK;
		}
		else{
			currentAction = CARRYHEAVYBURRDOCK;
		}*/
	}
	
	/*
	 * 
	 * Helper
	 * 
	 */
	private double euclidianDistance(double x1, double y1, double x2, double y2){
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}
	
	private double euclidianDistance(double[] x, double[] y, int from, int to){
		double res = 0;
		for(int i = from; i < to; i++){
			res += (x[i] - y[i]) * (x[i] - y[i]);
		}
		return Math.sqrt(res);
	}
	
	/*
	 * 
	 * Rest
	 * (non-Javadoc)
	 * @see Hiwi.MyDemoLauncher.MyDemo#adaptVisualisationSettings(diskworld.visualization.VisualizationSettings)
	 */
	
	@Override
	public boolean adaptVisualisationSettings(VisualizationSettings settings) {
		settings.getOptions().getOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_GRID).setEnabled(false);
		return true;
	}
	
	public void setFloor(Environment env, FloorCellType type1, FloorCellType type2, FloorCellType burrKiller) {
		for (int i = 0; i < env.getFloor().getNumX(); i++){
			for (int j = 0; j < env.getFloor().getNumY(); j++){
				if(i >= burrdockKillerMinX && i <= burrdockKillerMaxX && j >= burrdockKillerMinY && j <= burrdockKillerMaxY){
					env.getFloor().setType(i, j, burrKiller);
				}
				else{
					env.getFloor().setType(i, j, (i >= minPosX && i <= maxPosX) && (j >= minPosY && j <= maxPosY) ? type1 : type2);
				}
				
			}
		}
	}
	
	public static void main(String[] args) {
//       MyDemoLauncher.runDemo(new Arm((long)args[0].charAt(0)));
		MyDemoLauncher.runDemo(new Arm(1000L));
	}
	
	public void addError(double error){
		this.errorsCurrentAction.add(error);
	}
	
	public void printAfterAction(){

		String[] ssFly = {"["};
		//String[] seFly = {"["};
		for(int i = 0; i < timesForFly.size(); i++){
			ssFly[0] = ssFly[0] + ((int)timesForFly.get(i));
			//seFly[0] = seFly[0] + ((double)errorsForFly.get(i));
			if(i != timesForFly.size() - 1){
				ssFly[0] = ssFly[0] + ", ";
			//	seFly[0] = seFly[0] + ", ";
			}
		}
		ssFly[0] = ssFly[0] + "];";
		//seFly[0] = seFly[0] + "];";
		timesForFly.clear();
		//errorsForFly.clear();
		
		String[] ssBurrdock = {"["};
		//String[] seBurrdock = {"["};
		for(int i = 0; i < timesForBurrdock.size(); i++){
			ssBurrdock[0] = ssBurrdock[0] + ((int)timesForBurrdock.get(i));
			//seBurrdock[0] = seBurrdock[0] + ((double)errorsForBurrdock.get(i));
			if(i != timesForBurrdock.size() - 1){
				ssBurrdock[0] = ssBurrdock[0] + ", ";
				//seBurrdock[0] = seBurrdock[0] + ", ";
			}
		}
		ssBurrdock[0] = ssBurrdock[0] + "];";
		//seBurrdock[0] = seBurrdock[0] + "];";
		timesForBurrdock.clear();
		//errorsForBurrdock.clear();
		
		String[] ssHeavyBurrdock = {"["};
		//String[] seHeavyBurrdock = {"["};
		for(int i = 0; i < timesForHeavyBurrdock.size(); i++){
			ssHeavyBurrdock[0] = ssHeavyBurrdock[0] + ((int)timesForHeavyBurrdock.get(i));
			//seHeavyBurrdock[0] = seHeavyBurrdock[0] + ((double)errorsForHeavyBurrdock.get(i));
			if(i != timesForHeavyBurrdock.size() - 1){
				ssHeavyBurrdock[0] = ssHeavyBurrdock[0] + ", ";
				//seHeavyBurrdock[0] = seHeavyBurrdock[0] + ", ";
			}
		}
		ssHeavyBurrdock[0] = ssHeavyBurrdock[0] + "];";
		//seHeavyBurrdock[0] = seHeavyBurrdock[0] + "];";
		timesForHeavyBurrdock.clear();
		//errorsForHeavyBurrdock.clear();
		
		Fileprinter.printInFile("FlyTime", ssFly, rs);
		Fileprinter.printInFile("BurrdockTime", ssBurrdock, rs);
		Fileprinter.printInFile("HeavyBurrdockTime", ssHeavyBurrdock, rs);
		
		/*Fileprinter.printInFile("FlyErrors", seFly, rs);
		Fileprinter.printInFile("BurrdockErrors", seBurrdock, rs);
		Fileprinter.printInFile("HeavyBurrdockErrors", seHeavyBurrdock, rs);*/
		
		printPredictedModels();
	}
	
	public boolean finished(){

		return nrOfTrainingSets == maxTrainingSets;
	}
	
	public void predictModels(){
		if(training || stretching){
			return;
		}
		ExampandableIntMatrix m = this.predictedModelsFly;
		if(object.getType() == BURRDOCK){
			m = this.predictedModelsBurrdock;
		}
		else if (object.getType() == HEAVYBURRDOCK){
			m = this.predictedModelsHeavyBurrdock;
		}
		
		for(int i = 0; i < ySize; i++){
			int model = models.predictNextModel(getSensation(), i);
			m.add(model, i);
		}
	}
	
	public void printPredictedModels(){
		for(int i = 0; i < ySize; i++){
			String[] ss = {""};
			LinkedList<Integer> ls = predictedModelsFly.get(i);
			for(int j = 0; j < ls.size(); j++){
				String s = ", ";
				if(j == ls.size() - 1){
					s = "; ";
				}
				ss[0] = ss[0] + ls.get(j) + s;
			}
			Fileprinter.printInFile("PredictedModelFlyDim" + i, ss, rs);
		}
		predictedModelsFly.clear();
		
		for(int i = 0; i < ySize; i++){
			String[] ss = {""};
			LinkedList<Integer> ls = predictedModelsBurrdock.get(i);
			for(int j = 0; j < ls.size(); j++){
				String s = ", ";
				if(j == ls.size() - 1){
					s = "; ";
				}
				ss[0] = ss[0] + ls.get(j) + s;
			}
			Fileprinter.printInFile("PredictedModelBurrdockDim" + i, ss, rs);
		}
		predictedModelsBurrdock.clear();
		
		for(int i = 0; i < ySize; i++){
			String[] ss = {""};
			LinkedList<Integer> ls = predictedModelsHeavyBurrdock.get(i);
			for(int j = 0; j < ls.size(); j++){
				String s = ", ";
				if(j == ls.size() - 1){
					s = "; ";
				}
				ss[0] = ss[0] + ls.get(j) + s;
			}
			Fileprinter.printInFile("PredictedModelHeavyBurrdockDim" + i, ss, rs);
		}
		predictedModelsHeavyBurrdock.clear();
		
	}
	
	/*public void printErrors(Double[] errors, int[] models){
		for(int i = 0; i < errors.length; i++){
			Double d = errors[i];
			if(d!= null){
				String[] ss = { d + ", "};
				Fileprinter.printInFile("ErrorsM_" + i + "," + models[i], ss, rs);
			}
		}
	}
	
	public void printMeanError(double meanError, boolean stretching){
		String[] ss = {meanError + ", "};
		if(meanError == -1){
			String[] sn = {"------------------------------------------"};
			ss = sn;
		}
		if(meanError == -2){
			String[] sn = {"!" + object.getType()};
			ss = sn;
		}
		Fileprinter.printInFile("ErrorsForAllObjects", ss, rs);
	}*/
	

}
