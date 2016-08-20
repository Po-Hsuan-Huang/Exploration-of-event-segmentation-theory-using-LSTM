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

import java.awt.Color;
import java.awt.Graphics2D;

import diskworld.DiskType;
import diskworld.interfaces.Actuator;
import diskworld.interfaces.DiskSymbol;
import diskworld.visualization.AbstractDiskSymbol;
import diskworld.visualization.DrawUtils;
import diskworld.visualization.VisualisationItem;
import diskworld.visualization.VisualizationOption;
import diskworld.visualization.VisualizationOptions;
import diskworld.visualization.VisualizationSettings;

public abstract class ActuatorWithVisualisation implements Actuator {

	private final String name;
	private final ActuatorVisualisationVariant[] variants;
	private final AbstractDiskSymbol diskSymbol;

	public ActuatorWithVisualisation(String name, AbstractDiskSymbol diskSymbol) {
		this.name = name;
		this.diskSymbol = diskSymbol;
		this.variants = getVisualisationVariants();
	}

	public ActuatorWithVisualisation(String name) {
		this(name, null);
	}

	protected abstract ActuatorVisualisationVariant[] getVisualisationVariants();

	@Override
	public int getDim() {
		return 1;
	}

	@Override
	public VisualisationItem getVisualisationItem() {
		return new VisualisationItem() {
			@Override
			public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, double[] measurement, VisualizationSettings settings, DiskType diskType) {
				VisualizationOption option = settings.getOptions().getOption(VisualizationOptions.GROUP_ACTUATORS, name);
				// add actuator to visualisation options, if not yet there
				if (option == null) {
					settings.getOptions().addOption(VisualizationOptions.GROUP_ACTUATORS, name, getVariantNames());
					option = settings.getOptions().getOption(VisualizationOptions.GROUP_ACTUATORS, name);
				} else {
					if ((variants != null) && (!option.hasVariants())) {
						option.setVariants(getVariantNames());
					}
				}
				if (option.isEnabled()) {
					ActuatorVisualisationVariant visualisation;
					if (option.hasVariants()) {
						visualisation = variants[option.chosenVariantIndex()];
					} else {
						visualisation = variants[0];
					}
					visualisation.draw(g, centerx, centery, radius, angle, activity, diskSymbol, settings);
				}
			}
		};
	}

	protected String[] getVariantNames() {
		if (variants.length <= 1)
			return null;
		String res[] = new String[variants.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = variants[i].getVariantName();
		}
		return res;
	}

	public String getName() {
		return name;
	}

	protected final static ActuatorVisualisationVariant NO_VISUALISATION = new ActuatorVisualisationVariant("no visualization") {
		@Override
		public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, DiskSymbol diskSymbol, VisualizationSettings settings) {
		}
	};

	protected final static ActuatorVisualisationVariant ACTIVITY_AS_TEXT = new ActuatorVisualisationVariant("activity as text") {
		@Override
		public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, DiskSymbol diskSymbol, VisualizationSettings settings) {
			int x = settings.mapX(centerx);
			int y = settings.mapY(centery);
			g.setColor(settings.getActuatorValueTextColor());
			int numDigits = 2;
			DrawUtils.drawStringCentered(g, DrawUtils.asString(activity, numDigits), x, y);
		}
	};

	protected static ActuatorVisualisationVariant getDiskSymbolVisualization(final int index) {
		return new ActuatorVisualisationVariant("disk symbol (#" + index + ")") {
			@Override
			public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, DiskSymbol diskSymbol, VisualizationSettings settings) {
				Color color = settings.getActivityColor(activity[index]);
				drawDiskSymbol(g, centerx, centery, radius, angle, color, diskSymbol, settings);
			}
		};
	}

	protected static ActuatorVisualisationVariant getDiskSymbolVisualization() {
		return new ActuatorVisualisationVariant("disk symbol") {
			@Override
			public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, DiskSymbol diskSymbol, VisualizationSettings settings) {
				Color color = settings.getDefaultDiskSymbolColor();
				drawDiskSymbol(g, centerx, centery, radius, angle, color, diskSymbol, settings);
			}
		};
	}

	public static void drawDiskSymbol(Graphics2D g, double centerx, double centery, double radius, double angle, Color color, DiskSymbol diskSymbol, VisualizationSettings settings) {
		if (diskSymbol != null) {
			VisualizationOption option = settings.getOptions().getOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_DISK_SYMBOLS);
			if (option.isEnabled()) {
				g.setColor(color);
				int x = settings.mapX(centerx);
				int y = settings.mapY(centery);
				int x1 = settings.mapX(centerx - radius);
				int y1 = settings.mapY(centery + radius);
				diskSymbol.draw(g, x, y, x - x1, y - y1, angle);
			}
		}
	}

}
