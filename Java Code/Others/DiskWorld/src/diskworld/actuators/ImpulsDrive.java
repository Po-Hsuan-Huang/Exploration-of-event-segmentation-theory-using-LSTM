/*******************************************************************************
 *     DiskWorld - a simple 2D physics simulation environment, 
 *     Copyright (C) 2014  Jan Kneissler
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program in the file "License.txt".  
 *     If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package diskworld.actuators;

import java.awt.Graphics2D;

import diskworld.Disk;
import diskworld.Environment;
import diskworld.interfaces.DiskSymbol;
import diskworld.linalg2D.Utils;
import diskworld.shape.RingSegmentShape;
import diskworld.visualization.VisualizationSettings;

public class ImpulsDrive extends ActuatorWithVisualisation {

	protected static final String ACTUATOR_NAME = "Impulse drive";
	protected static final double VIS_ANGLE = 15.0 * Math.PI / 180;
	protected static final double VIS_MINR = 1;
	protected static final double VIS_MAXR = 3;

	private final double maxImpulseFlow;
	private final double energyCostFactor;

	public ImpulsDrive(double maxImpulseFlow, double energyCostFactor) {
		super(ACTUATOR_NAME);
		this.maxImpulseFlow = maxImpulseFlow;
		this.energyCostFactor = energyCostFactor;
	}

	@Override
	public double evaluateEffect(Disk disk, Environment environment, double[] activity, double partial_dt, double total_dt, boolean firstSlice) {
		double angle = disk.getAngle();
		double impulse = activity[0] * maxImpulseFlow * partial_dt;
		disk.getDiskComplex().applyImpulse(-impulse * Math.cos(angle), -impulse * Math.sin(angle), disk.getX(), disk.getY());
		return Math.abs(impulse) * energyCostFactor;
	}

	@Override
	protected ActuatorVisualisationVariant[] getVisualisationVariants() {
		return new ActuatorVisualisationVariant[] {
				GRAPHICAL,
				ACTIVITY_AS_TEXT,
				NO_VISUALISATION
		};
	}

	private static final ActuatorVisualisationVariant GRAPHICAL = new ActuatorVisualisationVariant("graphical") {
		@Override
		public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double activity[], DiskSymbol diskSymbol, VisualizationSettings settings) {
			double minr = radius * VIS_MINR;
			double maxr = radius * VIS_MAXR;
			double r = radius * (VIS_MINR + (VIS_MAXR - VIS_MINR) * Math.abs(activity[0]));
			RingSegmentShape shape1 = new RingSegmentShape(centerx, centery, minr, r, angle - VIS_ANGLE * 0.5, VIS_ANGLE);
			g.setColor(settings.getImpulsGeneratorColor(activity[0]));
			shape1.fill(g, settings.getImpulsGeneratorColor(activity[0]), settings);
			RingSegmentShape shape2 = new RingSegmentShape(centerx, centery, minr, maxr, angle - VIS_ANGLE * 0.5, VIS_ANGLE);
			g.setColor(settings.getImpulsGeneratorBorderColor());
			shape2.drawBorder(g, settings.getImpulsGeneratorBorderColor(), settings);
		}
	};

	@Override
	public boolean isAlwaysNonNegative(int index) {
		return false;
	}

	@Override
	public boolean isBoolean(int index) {
		return false;
	}

	@Override
	public double getActivityFromRealWorldData(double realWorldValue, int index) {
		return Utils.clip_pm1(realWorldValue / maxImpulseFlow);
	}

	@Override
	public String getRealWorldMeaning(int index) {
		return "impulse per time unit";
	}
}
