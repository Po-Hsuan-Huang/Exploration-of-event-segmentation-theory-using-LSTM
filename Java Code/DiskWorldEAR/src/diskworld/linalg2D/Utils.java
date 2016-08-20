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
package diskworld.linalg2D;

public class Utils {

	/**
	 * Clips a value to the interval [-1,1]
	 * 
	 * @param x
	 *            value to be clipped
	 * @return -1 if x < -1, 1 if x > 1, x else
	 */
	public static double clip_pm1(double x) {
		return Math.max(-1.0, Math.min(1.0, x));
	}

	/**
	 * Clips a value to the interval [-1,1]
	 * 
	 * @param x
	 *            value to be clipped
	 * @return 0 if x < 0, 1 if x > 1, x else
	 */
	public static double clip_01(double x) {
		return Math.max(0, Math.min(1.0, x));
	}
}
