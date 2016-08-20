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
import java.awt.Polygon;
import java.awt.Shape;

import diskworld.Environment;

public class VisualizationSettings {

	// Color Scheme 
	private ColorScheme colorScheme;

	private double minx, miny, maxx, maxy;
	private double factorx, factory;
	private int width, height;
	private double shadowFactorX, shadowFactorY;

	private VisualizationOptions options;
	private int collisionShape[][], collisionShapeCopy[][];

	public VisualizationSettings() {
		colorScheme = new ColorScheme();
		minx = 0.0;
		miny = 0.0;
		maxx = 1.0;
		maxy = 1.0;
		width = 0;
		height = 0;
		factorx = 0;
		factory = 0;
		options = new VisualizationOptions();
		collisionShape = starShape();
		collisionShapeCopy = new int[2][collisionShape[0].length];
		shadowFactorX = 0.1;
		shadowFactorY = -0.1;
	}

	private int[][] starShape() {
		int num = 10;
		int r1 = 5;
		int r2 = 10;
		int[] x = new int[num];
		int[] y = new int[num];
		for (int i = 0; i < num; i++) {
			double angle = 2 * Math.PI * (double) i / (double) num;
			double r = (i % 2 == 0) ? r1 : r2;
			x[i] = (int) Math.round(Math.cos(angle) * r);
			y[i] = (int) Math.round(Math.sin(angle) * r);
		}
		return new int[][] { x, y };
	}

	public VisualizationOptions getOptions() {
		return options;
	}

	public boolean isOptionEnabled(String group, String key) {
		VisualizationOption option = options.getOption(group, key);
		return option == null ? false : option.isEnabled();
	}

	public boolean isOptionEnabled(String key) {
		VisualizationOption option = options.getOption(VisualizationOptions.GROUP_GENERAL, key);
		return option == null ? false : option.isEnabled();
	}

	public int mapX(double x) {
		return (int) Math.round((x - minx) * factorx);
	}

	public int mapY(double y) {
		return (int) Math.round((maxy - y) * factory);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setViewDimension(int width, int height) {
		this.width = width;
		this.height = height;
		calculateFactors();
	}

	public void setViewedRect(double minx, double miny, double maxx, double maxy) {
		this.minx = minx;
		this.maxx = maxx;
		this.miny = miny;
		this.maxy = maxy;
		calculateFactors();
	}

	public void setFullView(Environment env) {
		setViewedRect(0, 0, env.getMaxX(), env.getMaxY());
	}

	private void calculateFactors() {
		factorx = width / (maxx - minx);
		factory = height / (maxy - miny);
	}

	public ColorScheme getColorScheme() {
		return colorScheme;
	}

	public Color getFloorColor(int type) {
		return colorScheme.getFloorColor(type);
	}

	public Color getWallColor() {
		return colorScheme.wallColor;
	}

	public Color getDefaultDiskSymbolColor() {
		return colorScheme.getDefaultDiskSymbolColor();
	}

	public Color getDefaultDiskSymbolColor(Color col) {
		return colorScheme.getDiskSymbolColor(col);
	}

	public Color getActivityColor(double activity) {
		if (activity >= 0.0)
			return DrawUtils.interpolatedColor(colorScheme.activity_0_color, colorScheme.activity_plus1_color, (float) activity);
		else
			return DrawUtils.interpolatedColor(colorScheme.activity_0_color, colorScheme.activity_minus1_color, (float) -activity);
	}

	public Color getMeasurementColor(double measurementScaled) {
		return DrawUtils.interpolatedColor(colorScheme.measurement_min_border_color, colorScheme.measurement_max_border_color, (float) measurementScaled);
	}

	public void setActivityColors(Color min, Color zero, Color max) {
		colorScheme.activity_0_color = zero;
		colorScheme.activity_plus1_color = max;
		colorScheme.activity_minus1_color = min;
	}

	public Color getSensorShapeBorder() {
		return colorScheme.sensorShapeBorder;
	}

	public Color getSensorShapeFill() {
		return colorScheme.sensorShapeFill;
	}

	public Color getSensorValueTextColor() {
		return colorScheme.sensorValueTextColor;
	}

	public Color getSensorValueGraphicalColor() {
		return colorScheme.sensorValueGraphicalColor;
	}

	public Color getActuatorValueTextColor() {
		return colorScheme.actuatorValueTextColor;
	}

	public Color getImpulsGeneratorBorderColor() {
		return colorScheme.impulseGeneratorBorderColor;
	}

	public Color getImpulsGeneratorColor(double activity) {
		if (activity >= 0.0)
			return colorScheme.impulseGeneratorPlusColor;
		else
			return colorScheme.impulseGeneratorMinusColor;
	}

	public Color getTimeColor() {
		return colorScheme.getTimeColor();
	}

	public Color getPermanentEdgesColor() {
		return colorScheme.permanentEdgesColor;
	}

	public Color getTransientEdgesColor() {
		return colorScheme.transientEdgesColor;
	}

	public Shape getCollisionShape(int x, int y) {
		for (int i = 0; i < collisionShape[0].length; i++) {
			collisionShapeCopy[0][i] = collisionShape[0][i] + x;
			collisionShapeCopy[1][i] = collisionShape[1][i] + y;
		}
		return new Polygon(collisionShapeCopy[0], collisionShapeCopy[1], collisionShapeCopy[0].length);
	}

	public boolean paintShadows() {
		return options.getOrCreateOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_SHADOW).isEnabled();
	}

	public double getShadowOffsetFactorX() {
		return shadowFactorX;
	}

	public double getShadowOffsetFactorY() {
		return shadowFactorY;
	}

}
