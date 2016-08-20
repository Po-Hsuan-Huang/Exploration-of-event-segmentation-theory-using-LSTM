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
package diskworld.actions;

import java.util.Map;

import diskworld.Disk;

class SetRadius extends ChangeRadius implements DiskAction {

	private final boolean linear;
	private final double coeff1, coeff2;

	public SetRadius(String name, Disk disk, double maxGrowingSpeed, double[] range, boolean linear) {
		super(name, disk, maxGrowingSpeed, range);
		this.linear = linear;
		if (linear) {
			coeff1 = (range[0] + range[1]) / 2;
			coeff2 = (range[1] - range[0]) / 2;
		} else {
			double f = Math.sqrt(range[1] / range[0]);
			coeff1 = range[0] * f;
			coeff2 = Math.log(f);
		}
	}

	@Override
	public Map<Disk, DiskModification> translateIntoDiskModifications(double actionValue, double timestep) {
		double maxfactor = Math.exp(timestep * logMaxGrowingRate);
		double minfactor = 1.0 / maxfactor;
		double factor;
		if (linear) {
			factor = (coeff1 + coeff2 * actionValue) / disk.getRadius();
		} else {
			factor = coeff1 * Math.exp(coeff2 * actionValue) / disk.getRadius();
		}
		factor = Math.min(factor, maxfactor);
		factor = Math.max(factor, minfactor);
		return multiplyRadius(factor);
	}

	@Override
	public double getMinActionValue() {
		return -1;
	}

	@Override
	public double getMaxActionValue() {
		return 1;
	}

}
