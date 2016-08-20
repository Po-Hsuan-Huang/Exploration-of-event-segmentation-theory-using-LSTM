package diskworld.actuators;

import diskworld.Disk;
import diskworld.Environment;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.DiskSymbol;
import diskworld.visualization.AbstractDiskSymbol;
import diskworld.visualization.VisualisationItem;

public class EmptyActuator extends ActuatorWithVisualisation{
	
	public EmptyActuator (final String name, AbstractDiskSymbol symbol){
		super(name, symbol);
	}

	@Override
	public double evaluateEffect(Disk disk, Environment environment, double[] activity, double partial_dt, double total_dt, boolean firstSlice) {
		// TODO
		return 0;
	}

	@Override
	protected ActuatorVisualisationVariant[] getVisualisationVariants() {
		return new ActuatorVisualisationVariant[] {
				getDiskSymbolVisualization(),
				ACTIVITY_AS_TEXT,
				NO_VISUALISATION,
		};
	}

	@Override
	public boolean isAlwaysNonNegative(int index) {
		return true;
	}

	@Override
	public boolean isBoolean(int index) {
		return true;
	}

	@Override
	public double getActivityFromRealWorldData(double realWorldValue, int index) {
		return realWorldValue;
	}

	@Override
	public String getRealWorldMeaning(int index) {
		return "No Effect, only visual";
	}

}
