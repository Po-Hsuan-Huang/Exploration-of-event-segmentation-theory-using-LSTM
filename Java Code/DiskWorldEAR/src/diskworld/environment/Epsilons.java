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
package diskworld.environment;

public class Epsilons {
	public static final double MAX_RELATIVE_DISK_TIMESTEP_DISPLACEMENT = 0.10; // max. 10% of radius displacement per time step
	public static final double EPSILON_SPEED_DIFF = 1e-7;
	public static final double EPSILON_FRICTION_SPEED = 1e-7;
	public static final double EPSILON_WALL_COLLISION = 1e-7;
	public static final double EPSILON_DISK_INTERSECT = 1e-7;
	public static final double EPSILON_GRIP = 1e-5;

}
