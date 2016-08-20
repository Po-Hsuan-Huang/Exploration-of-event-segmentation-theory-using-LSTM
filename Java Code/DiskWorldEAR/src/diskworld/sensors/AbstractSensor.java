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
package diskworld.sensors;

import java.awt.Graphics2D;
import java.util.ArrayList;

import diskworld.DiskType;
import diskworld.Environment;
import diskworld.environment.Floor;
import diskworld.interfaces.Sensor;
import diskworld.visualization.DrawUtils;
import diskworld.visualization.VisualisationItem;
import diskworld.visualization.VisualizationOption;
import diskworld.visualization.VisualizationOptions;
import diskworld.visualization.VisualizationSettings;

public abstract class AbstractSensor implements Sensor {

	protected final Environment environment;
	protected final String sensorName;
	protected final ArrayList<VisualisationVariant> variants;

	public AbstractSensor(Environment environment, String name) {
		this.environment = environment;
		this.sensorName = name;
		this.variants = new ArrayList<VisualisationVariant>();
		variants.add(TEXT_VISUALISATION_IN_DISK);
		variants.add(NO_VISUALISATION);
	}

	/**
	 * Provides access to the floor object
	 * 
	 * @return floor of the environment
	 */
	protected Floor getFloor() {
		return environment.getFloor();
	}

	protected static final VisualisationVariant NO_VISUALISATION = new VisualisationVariant("no visualization") {
		@Override
		public void visualisation(Graphics2D g,
				double[] measurement,
				Object visualisationObject,
				double cx,
				double cy,
				double r,
				double angle,
				VisualizationSettings settings) {
		}
	};

	protected static final VisualisationVariant TEXT_VISUALISATION_IN_DISK = new VisualisationVariant("values in disk") {
		@Override
		public void visualisation(Graphics2D g,
				double[] measurement,
				Object visualisationObject,
				double cx,
				double cy,
				double r,
				double angle,
				VisualizationSettings settings) {
			int x = settings.mapX(cx);
			int y = settings.mapY(cy);
			g.setColor(settings.getSensorValueTextColor());
			int numDigits = 2;
			drawMeasurement(g, x, y, measurement, numDigits);
		}
	};

	/**
	 * Print sensor measurements on screen
	 * 
	 * @param g
	 *            graphics object
	 * @param x
	 *            x coordinate (center of text)
	 * @param y
	 *            y coordinate (center of text)
	 * @param measurement
	 *            the array of sensor values
	 * @param numDigits
	 *            number of digits to be shown
	 */
	protected static void drawMeasurement(Graphics2D g, int x, int y, double[] measurement, int numDigits) {
		DrawUtils.drawStringCentered(g, DrawUtils.asString(measurement, numDigits), x, y);
	}

	protected String[] getVariantNames() {
		if (variants.size() <= 1)
			return null;
		String res[] = new String[variants.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = variants.get(i).getVariantName();
		}
		return res;
	}

	/**
	 * Return a visualisation item, to be attached to DiskTypes that use the sensor
	 * 
	 * @return
	 *         a new visualisation item
	 */
	public VisualisationItem getVisualisationItem() {
		return new VisualisationItem() {
			@Override
			public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, double measurement[], VisualizationSettings settings, DiskType diskType) {
				VisualisationVariant visualisation = getChosenVisualisationVariant(settings);
				if (visualisation != null) {
					visualisation.visualisation(g, measurement, null, centerx, centery, radius, angle, settings);
				}
			}
		};
	}

	protected VisualisationVariant getChosenVisualisationVariant(VisualizationSettings settings) {
		VisualizationOption option = settings.getOptions().getOption(VisualizationOptions.GROUP_SENSORS, sensorName);
		// add sensor to visualisation options, if not yet there
		if (option == null) {
			settings.getOptions().addOption(VisualizationOptions.GROUP_SENSORS, sensorName, getVariantNames());
			option = settings.getOptions().getOption(VisualizationOptions.GROUP_SENSORS, sensorName);
		}
		VisualisationVariant visualisation = null;
		if (option.isEnabled()) {
			if (option.hasVariants()) {
				visualisation = variants.get(option.chosenVariantIndex());
			} else {
				visualisation = variants.get(0);
			}
		}
		return visualisation;
	}

}