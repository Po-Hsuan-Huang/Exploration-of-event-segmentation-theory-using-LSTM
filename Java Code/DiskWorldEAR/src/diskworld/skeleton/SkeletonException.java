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
package diskworld.skeleton;

/**
 * Exception thrwon on varois occasions within the diskcomplexes.skeleton package
 * 
 * @author Jan
 * 
 */
public class SkeletonException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor with message string
	 * 
	 * @param string
	 *            message of exception
	 */
	public SkeletonException(String string) {
		super(string);
	}

}
