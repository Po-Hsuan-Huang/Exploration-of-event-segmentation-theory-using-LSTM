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
package diskworld.visualization;

import java.awt.Color;
import java.util.Random;

import diskworld.environment.FloorCellType;

public class ColorScheme {
	// arrays have setters and getters
	private Color[] floorColors;

	// other fields are public, set their values directly!
	public Color activity_0_color;
	public Color activity_plus1_color;
	public Color activity_minus1_color;
	public Color wallColor;
	public Color sensorShapeBorder;
	public Color sensorShapeFill;
	public Color sensorValueTextColor;
	public Color sensorValueGraphicalColor;
	public Color actuatorValueTextColor;
	public Color measurement_min_border_color;
	public Color measurement_max_border_color;
	public Color impulseGeneratorBorderColor;
	public Color impulseGeneratorPlusColor;
	public Color impulseGeneratorMinusColor;
	private Color timeColor;
	public Color transientEdgesColor;
	public Color permanentEdgesColor;
	public Color collisionsColor;
	public Color blockingCollisionsColor;
	public Color gridColor;
	public Color gridTextColor;
	private Color defaultDiskSymbolColor;
	private Color shadowColor;

	public ColorScheme() {
		// for random initial colors
		Random random = new Random(0);
		floorColors = new Color[FloorCellType.NUM_FLOOR_TYPES];
		for (int i = 0; i < floorColors.length; i++) {
			if (i < FloorCellType.numFloorCellTypesDefined()) {
				floorColors[i] = FloorCellType.getCellType(i).getDisplayColor();
			} else {
				floorColors[i] = getRandomSaturatedColor(random);
			}
		}
		activity_0_color = Color.BLACK;
		activity_plus1_color = Color.GREEN;
		activity_minus1_color = Color.RED;
		wallColor = Color.DARK_GRAY;
		sensorShapeBorder = Color.YELLOW;
		sensorShapeFill = new Color(1.0f, 1.0f, 1.0f, 0.2f);
		measurement_min_border_color = Color.YELLOW;
		measurement_max_border_color = Color.RED;
		sensorValueTextColor = Color.BLACK;
		sensorValueGraphicalColor = Color.MAGENTA;
		actuatorValueTextColor = Color.RED;
		impulseGeneratorBorderColor = Color.DARK_GRAY;
		impulseGeneratorPlusColor = Color.GREEN;
		impulseGeneratorMinusColor = Color.RED;
		timeColor = Color.YELLOW;
		permanentEdgesColor = Color.WHITE;
		transientEdgesColor = Color.RED;
		collisionsColor = Color.YELLOW;
		blockingCollisionsColor = Color.RED;
		gridColor = Color.LIGHT_GRAY;
		gridTextColor = Color.GRAY;
		defaultDiskSymbolColor = Color.YELLOW;
		shadowColor = new Color(0.3f, 0.3f, 0.3f, 0.9f);
	}

	private Color getRandomSaturatedColor(Random random) {
		float c1 = random.nextFloat();
		float c2 = random.nextFloat();
		switch (random.nextInt(3)) {
		case 0:
			return new Color(1.0f, c1, c2);
		case 1:
			return new Color(c1, 1.0f, c2);
		case 2:
			return new Color(c1, c2, 1.0f);
		}
		return null;
	}

	public Color getFloorColor(int type) {
		if ((type < 0) && (type >= FloorCellType.NUM_FLOOR_TYPES))
			throw new IllegalArgumentException("maximum number of floor types is set to: " + FloorCellType.NUM_FLOOR_TYPES);
		return floorColors[type];
	}

	public void setFloorColor(int type, Color color) {
		if ((type < 0) && (type >= FloorCellType.NUM_FLOOR_TYPES))
			throw new IllegalArgumentException("maximum number of floor types is set to: " + FloorCellType.NUM_FLOOR_TYPES);
		floorColors[type] = color;
	}

	public Color getDiskSymbolColor(Color diskColor) {
		return DrawUtils.invertedColor(diskColor);
	}

	public Color getTimeColor() {
		return timeColor;
	}

	public Color getDefaultDiskSymbolColor() {
		return defaultDiskSymbolColor;
	}

	public Color getShadowColor() {
		return shadowColor;
	}

	public void setShadowColor(Color color) {
		shadowColor = color;
	}

}
