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

/**
 * A coordinate system is given by origin and rotation angle 
 */
public class CoordinateSystem {

	private double originx,originy,xx,xy,yx,yy,angle;
	private int version;

	public CoordinateSystem() {
		setOrigin(0.0,0.0);
		setAngle(0.0);
		version = 0;
	}
	
	public void setOrigin(double absx, double absy) {
		originx = absx;
		originy = absy;
		version++;
	}
	
	public void setAngle(double rad) {
		this.angle = rad;
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		xx = c;
		xy = -s;
		yx = s;
		yy = c;
		version++;
	}

	public int getVersion() {
		return version;
	}

	public double rel2absX(double relx, double rely) {
		return originx+xx*relx+xy*rely;
	}

	public double rel2absY(double relx, double rely) {
		return originy+yx*relx+yy*rely;
	}

	public double rel2absAngle(double relAngle) {
		return angle+relAngle;
	}

	public double abs2relX(double absx, double absy) {
		return (absx-originx)*xx+(absy-originy)*yx;
	}
	
	public double abs2relY(double absx, double absy) {
		return (absx-originx)*xy+(absy-originy)*yy;
	}
	
	public double abs2relAngle(double absAngle) {
		return absAngle-angle;
	}

	public double getOriginX() {
		return originx;
	}
	
	public double getOriginY() {
		return originy;
	}

	public double getAngle() {
		return angle;
	}

}
