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

public class RelativePosition implements Position {

	private CoordinateSystem coordinates;
	private double relx, rely, relAngle;
	private double distanceToOrigin;
	private double absx, absy, absAngle;
	private int coordinateVersion;

	public RelativePosition(CoordinateSystem coordinates) {
		this.relx = 0;
		this.rely = 0;
		this.distanceToOrigin = 0.0;
		this.relAngle = 0;
		this.coordinates = coordinates;
		coordinateVersion = -1;
	}

	@Override
	public double getAbsX() {
		updateAbsValues();
		return absx;
	}

	@Override
	public double getAbsY() {
		updateAbsValues();
		return absy;
	}

	@Override
	public double getAbsAngle() {
		updateAbsValues();
		return absAngle;
	}

	private void updateAbsValues() {
		if (coordinateVersion != coordinates.getVersion()) {
			absx = coordinates.rel2absX(relx, rely);
			absy = coordinates.rel2absY(relx, rely);
			absAngle = coordinates.rel2absAngle(relAngle);
			coordinateVersion = coordinates.getVersion();
		}
	}

	public double getRelX() {
		return relx;
	}

	public double getRelY() {
		return rely;
	}

	public void setAbsPosition(double absx, double absy) {
		relx = coordinates.abs2relX(absx, absy);
		rely = coordinates.abs2relY(absx, absy);
		distanceToOrigin = Math.sqrt(relx * relx + rely * rely);
		this.absx = absx;
		this.absy = absy;
	}

	public void setAbsAngle(double absAngle) {
		relAngle = coordinates.abs2relAngle(absAngle);
		this.absAngle = absAngle;
	}

	public double getDistanceToOrigin() {
		return distanceToOrigin;
	}

	public void changeCoordinates(CoordinateSystem newCoordinates) {
		double absx = getAbsX();
		double absy = getAbsY();
		double absAngle = getAbsAngle();
		coordinates = newCoordinates;
		coordinateVersion = coordinates.getVersion();
		setAbsPosition(absx, absy);
		setAbsAngle(absAngle);
	}
}
