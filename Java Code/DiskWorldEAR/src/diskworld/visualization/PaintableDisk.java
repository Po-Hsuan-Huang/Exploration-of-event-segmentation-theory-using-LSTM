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
import java.awt.Graphics2D;

import diskworld.Disk;
import diskworld.DiskType;

class PaintableDisk {

	private final double x, y, r, angle, activity[];
	private final double[][] measurement;
	private final int zLevel;
	private final DiskType type;

	public PaintableDisk(Disk d) {
		x = d.getX();
		y = d.getY();
		r = d.getRadius();
		angle = d.getAngle();
		activity = d.getActivity() == null ? null : d.getActivity().clone();
		measurement = deepClone(d.getSensorMeasurements());
		type = d.getDiskType();
		zLevel = d.getZLevel();
	}

	private double[][] deepClone(double[][] arr) {
		if (arr == null)
			return null;
		double res[][] = new double[arr.length][];
		for (int i = 0; i < res.length; i++) {
			res[i] = arr[i].clone();
		}
		return res;
	}

	public void paint(Graphics2D g, VisualizationSettings settings) {
		paintCircle(g, type.getMaterial().getDisplayColor(), x, y, r, 0, settings);
	}

	public void paintShadow(Graphics2D g, VisualizationSettings settings) {
		paintCircle(g, settings.getColorScheme().getShadowColor(), x, y, r, zLevel, settings);
	}

	public void paintAdditionalItems(Graphics2D g, VisualizationSettings settings) {
		int count = 0;
		for (VisualisationItem item : type.getAdditionalVisualisationItems()) {
			// sensor visualisation items are first, can assign measurement by count...
			double values[] = (measurement != null) && (count < measurement.length) ? measurement[count] : null;
			item.draw(g, x, y, r, angle, activity, values, settings, type);
			count++;
		}
	}

	public static void paintCircle(Graphics2D g, Color col, double cx, double cy, double r, int zLevel, VisualizationSettings settings) {
		cx += zLevel * settings.getShadowOffsetFactorX();
		cy += zLevel * settings.getShadowOffsetFactorY();
		int x1 = settings.mapX(cx - r);
		int y1 = settings.mapY(cy + r);
		int x = settings.mapX(cx);
		int y = settings.mapY(cy);
		int rx = x - x1;
		int ry = y - y1;
		g.setColor(col);
		g.fillArc(x - rx, y - ry, 2 * rx + 1, 2 * ry + 1, 0, 360);
	}

}
