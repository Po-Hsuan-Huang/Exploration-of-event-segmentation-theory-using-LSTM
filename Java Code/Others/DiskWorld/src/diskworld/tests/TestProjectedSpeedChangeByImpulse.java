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

import java.util.Random;

import diskworld.DiskComplex;
import diskworld.DiskComplexEnsemble;
import diskworld.DiskMaterial;
import diskworld.DiskType;

class TestProjectedSpeedChangeByImpulse {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DiskComplexEnsemble dce = new DiskComplexEnsemble();
		DiskComplex dc = dce.createNewDiskComplex();
		DiskType dt = new DiskType(DiskMaterial.METAL);
		dc.addNewDisk(0.5, 0.5, 0.5, 10, dt);
		dc.addNewDisk(0.7, 0.5, 2.0, 160, dt);
		Random r = new Random();
		for (int i = 0; i < 10; i++) {
			double pointx = r.nextDouble();
			double pointy = r.nextDouble();
			double dx = r.nextDouble();
			double dy = r.nextDouble();

			double before = dc.getProjectedSpeed(pointx, pointy, dx, dy);
			double change = dc.getProjectedSpeedChangeByImpulse(pointx, pointy, dx, dy);
			dc.applyImpulse(dx, dy, pointx, pointy);
			double after = dc.getProjectedSpeed(pointx, pointy, dx, dy);
			System.out.println("change " + (after - before) + " predicted: " + change + " okay: " + (Math.abs(after - before - change) < 1e-7));
		}
	}

}
