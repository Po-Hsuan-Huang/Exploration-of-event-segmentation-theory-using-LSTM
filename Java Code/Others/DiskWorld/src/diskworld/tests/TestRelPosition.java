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
package diskworld.tests;

import diskworld.linalg2D.CoordinateSystem;
import diskworld.linalg2D.RelativePosition;

class TestRelPosition {

	public static void main(String args[]) {
		CoordinateSystem coord = new CoordinateSystem();
		coord.setAngle(2.44);
		coord.setOrigin(1.4, 5.3);
		RelativePosition rp = new RelativePosition(coord);
		double absx = 1.24;
		double absy = 0.24;
		double relx = coord.abs2relX(absx, absy);
		double rely = coord.abs2relY(absx, absy);
		System.out.print(coord.rel2absX(relx, rely) - absx);
		System.out.print(",");
		System.out.println(coord.rel2absY(relx, rely) - absy);
		rp.setAbsPosition(absx, absy);
		System.out.print(rp.getAbsX() - absx);
		System.out.print(",");
		System.out.println(rp.getAbsY() - absy);
	}
}
