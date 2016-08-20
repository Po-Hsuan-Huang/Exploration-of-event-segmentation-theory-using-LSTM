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
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;

public class DrawUtils {
	/**
	 * Draw a string centered at given position
	 * 
	 * @param g
	 *            graphics object
	 * @param string
	 *            text
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 */
	public static void drawStringCentered(Graphics2D g, String string, int x, int y) {
		Rectangle2D r = g.getFontMetrics().getStringBounds(string, null);
		int ascent = g.getFontMetrics().getAscent();
		g.drawString(string, x - (int) r.getWidth() / 2, y + ascent / 2);
	}

	public static Color invertedColor(Color color) {
		ColorSpace cs = color.getColorSpace();
		float cf[] = color.getColorComponents(null);
		for (int i = 0; i < cf.length; i++) {
			cf[i] = cs.getMinValue(i) + (cs.getMaxValue(i) - cf[i]);
		}
		return new Color(cs, cf, color.getAlpha() / 255.0f);
	}

	public static Color interpolatedColor(Color c1, Color c2, float f) {
		float c1f[] = c1.getColorComponents(null);
		float c2f[] = c2.getColorComponents(null);
		for (int i = 0; i < c1f.length; i++) {
			c1f[i] = c1f[i] * (1.0f - f) + c2f[i] * f;
		}
		float alpha = c1.getAlpha() * (1.0f - f) + c2.getAlpha() * f;
		return new Color(c1.getColorSpace(), c1f, Math.round(alpha / 255.0f));
	}

	/**
	 * Create a string of a double array
	 * 
	 * @param array
	 *            the array of sensor values
	 * @param numDigits
	 *            number of digits to be shown
	 * @return formatted string
	 */
	public static String asString(double[] array, int numDigits) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(String.format("%." + numDigits + "f", array[i]));
		}
		return sb.toString();
	}
}
