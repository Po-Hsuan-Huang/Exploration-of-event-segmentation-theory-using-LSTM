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

public class AngleUtils {

	/**
	 * Add multiples of 2*PI such that the result is in [-PI,PI]
	 * 
	 * @param angle
	 *            angle to translate
	 * @return resulting angle in [-PI,PI]
	 */
	public static double mod2PI(double angle) {
		return angle - 2 * Math.PI * Math.round(angle / (2 * Math.PI));
	}

	/**
	 * Add multiples of 2*PI such that the result is in [0,2*PI]
	 * 
	 * @param angle
	 *            angle to translate
	 * @return resulting angle in [0,2*PI]
	 */
	public static double mod2PIpositive(double angle) {
		return angle - 2 * Math.PI * Math.floor(angle / (2 * Math.PI));
	}

	/**
	 * Add a multiple of 2*PI to an angle, such that it is as small as possible but >= a given minimum
	 * 
	 * @param angle
	 *            the angle to translate
	 * @param min
	 *            the lower bound for the translated angle
	 * @return smallest angle equivalent modulo 2PI but >= min
	 */
	public static double closestBigger(double angle, double min) {
		return min + mod2PIpositive(angle - min);
	}

	/**
	 * Check if an angle is in a range
	 * 
	 * @param angle
	 *            the angle to check
	 * @param min
	 *            the lower bound for range
	 * @param max
	 *            the upper bound for range, must be >= min
	 * @return true if angle is (modulo 2*PI) in the range
	 */
	public static boolean isInRange(double angle, double min, double max) {
		if (max < min)
			throw new IllegalArgumentException("max cannot be smaller min!");
		return closestBigger(angle, min) <= max;
	}

}
