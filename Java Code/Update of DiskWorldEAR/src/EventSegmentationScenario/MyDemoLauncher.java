package EventSegmentationScenario;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import EventSegmentationArchitecture.ModelFitter;
import diskworld.Environment;
import diskworld.environment.AgentMapping;
import diskworld.interfaces.AgentController;
import diskworld.visualization.EnvironmentPanel;
import diskworld.visualization.VisualizationSettings;

public class MyDemoLauncher {

	public static interface MyDemo {

		/**
		 * Determines the real time factor of the visualisation. Note this has no effect on the
		 * delta_t that is added to the simulation time stamp in every time step!
		 * 
		 * @return how manz miliseconds shall pass (in real time) between executing two time steps
		 */
		long getMiliSecondsPerTimeStep();

		/**
		 * Creates and populates the simulation environment
		 * 
		 * @return environment including the agent's DiskComplexes
		 */
		Environment getEnvironment();

		/**
		 * Provide an array of AgentMapping objects, that provide the connection between the sensor array (used in the AgentController interface) and the
		 * actual disk world sensors, and between the actuator array (used in the AgentController interface) and the actual disk world actions&actuators
		 * Note: Since each agent uses different disks in the disk world, all AgentMapping objects must be different, even
		 * if some agents use the same controller
		 * 
		 * @return array of AgentMapping objects, one for each agent in the simulation
		 */
		AgentMapping[] getAgentMappings();

		/**
		 * Provide the controllers, that get sensor values and produce actuator values.
		 * Note: It is possible that agents share a controller (if the Controller implementation allows that). In that case,
		 * there may be multiple copies of the same AgentController object in the array returned. However, the length of the
		 * array is determined by the number of agents in the simulation (and must hence be identical to the length of
		 * the array returned by {@link #getAgentMappings()}.
		 * 
		 * @return array of AgentMapping objects, one for each agent in the simulation
		 */
		AgentController[] getControllers();

		/**
		 * A descriptive title of the demo
		 * 
		 * @return some human readable brief description
		 */
		String getTitle();

		/**
		 * May be used to change visualization settings.
		 * NOTE: The VisualizationOptions are created on the fly during painting, which means that they are not
		 * yet generated
		 * 
		 * @param settings
		 *            object passed in order to apply changes to the visualization defaults
		 * @return
		 *         true if the settings could be adapted
		 */
		boolean adaptVisualisationSettings(VisualizationSettings settings);
		
		boolean finished();
		
		ModelFitter backUpArchitecture();

	}
	public static MyDemo[] myDemos;
	private static final int FRAME_SIZE_X = 620;
	private static final int FRAME_SIZE_Y = 700;
	private static final int PANEL_SIZE_X = 600;
	private static final int PANEL_SIZE_Y = 600;
	static final double DT_PER_STEP = 0.01;

	/**
	 * Runs a sequence of demos
	 * 
	 * @param title
	 *            title of the window
	 * @param demos
	 *            array of demos to show
	 */
	public static void runDemos(String title, MyDemo demos[]) {
		JFrame frame = new JFrame();
		frame.setTitle(title);
		frame.setVisible(true);
		frame.setSize(new Dimension(FRAME_SIZE_X, FRAME_SIZE_Y));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myDemos = demos;
		for (MyDemo demo : demos) {
			runDemo(frame, demo, DT_PER_STEP, demo.getMiliSecondsPerTimeStep());
		}
		frame.dispose();
	}

	/**
	 * Runs a single demo
	 * 
	 * @param demo
	 *            demo to show
	 */
	public static ModelFitter runDemo(MyDemo demo) {
		JFrame frame = new JFrame();
		frame.setTitle(demo.getTitle());
		frame.setVisible(true);
		frame.setSize(new Dimension(FRAME_SIZE_X, FRAME_SIZE_Y));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ModelFitter mf = runDemo(frame, demo, DT_PER_STEP, demo.getMiliSecondsPerTimeStep());
		frame.dispose();
		return mf;
	}

	/**
	 * Run the given demo in the frame
	 * 
	 * @param frame
	 *            frame to show the demo
	 * @param demo
	 *            the demo to be run
	 */
	private static ModelFitter runDemo(JFrame frame, MyDemo demo, double dt, long miliSecondsPerStep) {
		Environment env = demo.getEnvironment();
		EnvironmentPanel ep = new EnvironmentPanel();
		ep.setEnvironment(env);
		ep.getSettings().setFullView(env);
		JPanel panel = new JPanel();
		frame.add(panel);
		JLabel label = new JLabel(demo.getTitle());
		label.setFont(new Font(Font.SERIF, Font.BOLD, 36));
		panel.add(label);
		ep.setPreferredSize(new Dimension(PANEL_SIZE_X, PANEL_SIZE_Y));
		panel.add(ep);
		frame.add(panel);
		frame.validate();

		AtomicBoolean running = new AtomicBoolean(true);
		AtomicBoolean fast = new AtomicBoolean(true);
		frame.addKeyListener(getKeyListener(running, fast));
		AgentController[] controllers = demo.getControllers();
		AgentMapping[] mappings = demo.getAgentMappings();
		if (controllers == null)
			controllers = new AgentController[0];
		if (mappings == null)
			mappings = new AgentMapping[0];
		if (controllers.length != mappings.length)
			throw new IllegalArgumentException("length of arrays returned by getControllers() and getAgentMappings() do not agree!");
		showNames(demo.getTitle(), mappings);
		boolean settingsAdapted = false;
		int t = 0;
		boolean stillRunning = true;
		// TODO Here runs the timeSteps
		while (running.get() && /*t < 50000*/ !demo.finished() && stillRunning) {
			if(fast.get()){
				miliSecondsPerStep = 1;
			}
			else{
				miliSecondsPerStep = 1000;
			}
			if (!settingsAdapted) {
				settingsAdapted = demo.adaptVisualisationSettings(ep.getSettings());
			}
			long ts = System.currentTimeMillis();
			env.doTimeStep(dt, mappings);
			for (int i = 0; i < controllers.length; i++) {
				if(controllers[i] instanceof MyAgentController){
					((MyAgentController)controllers[i]).doAfterTimeStep();
					//stillRunning = !((MyAgentController)controllers[i]).readyToStop();
				}
				controllers[i].doTimeStep(mappings[i].getSensorValues(), mappings[i].getActuatorValues());
			}
			long time = System.currentTimeMillis() - ts;
			long sleep = miliSecondsPerStep - time;
			if (sleep > 0) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
				}
			}
			t++;
		}
		panel.remove(ep);
		if(controllers.length > 0 && controllers[0] instanceof MyAgentController)
			((MyAgentController)controllers[0]).finishPrints();
		return demo.backUpArchitecture();
	}

	private static void showNames(String title, AgentMapping[] mappings) {
		System.out.println(title);
		System.out.println();
		for (int i = 0; i < mappings.length; i++) {
			System.out.print("Agent " + i + " sensors:");
			for (String s : mappings[i].getSensorNames()) {
				System.out.print(" " + s);
			}
			System.out.println();
			System.out.print("Agent " + i + " actuators:");
			for (String s : mappings[i].getActuatorNames()) {
				System.out.print(" " + s);
			}
			System.out.println();
		}
		System.out.println();
	}

	private static KeyListener getKeyListener(final AtomicBoolean running, final AtomicBoolean fast) {
		return new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				char key = e.getKeyChar();
				if (key == ' '){
					running.set(false);
				}
				else if(key == 's'){
					System.out.println("Slow simulation down");
					fast.set(false);
				}
				else if (key == 'f'){
					System.out.println("Speed up simulation");
					fast.set(true);
				}
					
					
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		};
	}

}