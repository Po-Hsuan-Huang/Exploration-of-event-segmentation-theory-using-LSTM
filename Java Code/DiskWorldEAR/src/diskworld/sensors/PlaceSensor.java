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
package diskworld.sensors;

import diskworld.Disk;
import diskworld.Environment;
import diskworld.linalg2D.Utils;

/**
 * Works like a GPS, measures absolute position in x and y direction
 * 
 * @author jph
 * 
 */
public class PlaceSensor extends AbstractSensor {

	private static final String NAME = "Place sensor";
	private final double sizex;
	private final double sizey;

	//default constructor
	public PlaceSensor(Environment environment) {
		super(environment, NAME);
		this.sizex = environment.getMaxX();
		this.sizey = environment.getMaxY();
	}

	@Override
	public int getDimension() {
		return 2;
	}

	@Override
	/**
	 * @see diskworld.interfaces.Sensor#getMeasurement(diskworld.diskcomplexes.Disk, double[])
	 * double[] is interpreted as 3D-Array of position and angle (x,y,angle)
	 */
	public void doMeasurement(Disk disk, double pos[]) {
		pos[0] = Utils.clip_pm1(disk.getX() / sizex);
		pos[1] = Utils.clip_pm1(disk.getY() / sizey);
	}

	@Override
	public double getRealWorldInterpretation(double[] measurement, int index) {
		return measurement[index] * (index == 0 ? sizex : sizey);
	}

	@Override
	public String getRealWorldMeaning(int index) {
		return "absolute " + (index == 0 ? "x" : "y") + " position";
	}

}
