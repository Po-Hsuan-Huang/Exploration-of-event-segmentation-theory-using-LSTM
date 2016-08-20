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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import diskworld.Disk;
import diskworld.DiskMaterial;
import diskworld.Environment;
import diskworld.linalg2D.AngleUtils;
import diskworld.linalg2D.Utils;

/**
 * Sensor with a cone shaped sensitive area that detects parameters of the closest disk(s) in range.
 * It is possible to specify a set of DiskMaterials that will be ignored when determining the closest
 * Disks. Possible parameters are:
 * - position angle of closest point relative to centre angle of sensitive area
 * - distance to closest point on disk
 * - width angle that the disk occupies within the sensitive area
 * - disk material dependent response
 * The first measurement entry (in component [0]) is a double in [0,1] that indicates how many disks where
 * detected (as a fraction of maxNumDisks). If maxNumDisks == 1, the returned value in [0] is a boolean that
 * indicates if a disk is in the sensor shape or not.
 * 
 * @author Jan
 * 
 */
public class ClosestDiskSensor extends ConeSensor {

	private static final double DEFAULT_MIN_RANGE_RELATIVE = 1.5; // 50% more than disk radius
	public static final String SENSOR_NAME = "DiskDistance";

	private double centerAngle;
	private double openingAngle;
	private final Set<DiskMaterial> invisibleMaterials;
	private int maxNumDisks, offset, posIndex, distIndex, widthIndex, materialIndex;
	private DiskMaterialResponse materialResponseFunction;

	/**
	 * Constructor for a ClosestDiskSensor considering the maxNumDisks closest disks
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param centerAngle
	 *            the angle of the centre of the cone
	 * @param openingAngle
	 *            width of the cone
	 * @param minRangeRelative
	 *            minimum distance (inner radius), specified in multiples of the disk radius
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @param invisibleMaterials
	 *            a set of materials that are not detected by the sensor, if null: all disks are detected
	 * @param measurePosition
	 *            indicates if the angular position of the closest point of the detected disk is measured
	 * @param measureDistance
	 *            indicates if the distance to the closest point of the detected disk is measured
	 * @param measureWidth
	 *            indicates if the visible angle the detected disk is measured
	 * @param materialResponse
	 *            function which determines a DiskMaterial dependent response, can be null
	 * @param maxNumDisks
	 *            number of disks maximally detectable
	 */
	public ClosestDiskSensor(
			Environment environment,
			double centerAngle,
			double openingAngle,
			double minRangeRelative,
			double maxRangeAbsolute,
			Set<DiskMaterial> invisibleMaterials,
			boolean measurePosition,
			boolean measureDistance,
			boolean measureWidth,
			DiskMaterialResponse materialResponse,
			int maxNumDisks) {
		super(environment, centerAngle, openingAngle, minRangeRelative, maxRangeAbsolute, SENSOR_NAME);
		this.centerAngle = centerAngle;
		this.openingAngle = openingAngle;
		this.invisibleMaterials = invisibleMaterials;
		this.maxNumDisks = maxNumDisks;
		int index = 1;
		if (measurePosition) {
			posIndex = index;
			index++;
		} else {
			posIndex = -1;
		}
		if (measureDistance) {
			distIndex = index;
			index++;
		} else {
			distIndex = -1;
		}
		if (measureWidth) {
			widthIndex = index;
			index++;
		} else {
			widthIndex = -1;
		}
		materialResponseFunction = materialResponse;
		if (materialResponseFunction != null) {
			materialIndex = index;
			index += materialResponseFunction.getDim();
		} else {
			materialIndex = -1;
		}
		offset = index - 1;
		addVisualisationOptions();
	}

	/**
	 * Convenience constructor for a ClosestDiskSensor considering only the closest disk (maxNumDisks = 1)
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param centerAngle
	 *            the angle of the centre of the cone
	 * @param openingAngle
	 *            width of the cone
	 * @param minRangeRelative
	 *            minimum distance (inner radius), specified in multiples of the disk radius
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @param invisibleMaterials
	 *            a set of materials that are not detected by the sensor, if null: all disks are detected
	 * @param measurePosition
	 *            indicates if the angular position of the closest point of the detected disk is measured
	 * @param measureDistance
	 *            indicates if the distance to the closest point of the detected disk is measured
	 * @param measureWidth
	 *            indicates if the visible angle the detected disk is measured
	 * @param materialResponse
	 *            function which determines a DiskMaterial dependent response, can be null
	 */
	public ClosestDiskSensor(
			Environment environment,
			double centerAngle,
			double openingAngle,
			double minRangeRelative,
			double maxRangeAbsolute,
			Set<DiskMaterial> invisibleMaterials,
			boolean measurePosition,
			boolean measureDistance,
			boolean measureWidth,
			DiskMaterialResponse materialResponse) {
		this(environment, centerAngle, openingAngle, minRangeRelative, maxRangeAbsolute, invisibleMaterials, measurePosition, measureDistance, measureWidth, materialResponse, 1);
	}

	/**
	 * Convenience method constructs sensor some parameters set to default, only measuring position angle
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param openingAngle
	 *            width of the cone
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @param invisibleMaterials
	 *            a set of materials that are not detected by the sensor, if null: all disks are detected
	 * @return new ClosestDiskSensor object
	 */
	public static ClosestDiskSensor getPositionAngleSensor(Environment environment, double openingAngle, double maxRangeAbsolute, Set<DiskMaterial> invisibleMaterials) {
		return new ClosestDiskSensor(environment, 0.0, openingAngle, DEFAULT_MIN_RANGE_RELATIVE, maxRangeAbsolute, invisibleMaterials, true, false, false, null);
	}

	/**
	 * Convenience method constructs sensor some parameters set to default, only measuring position angle
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param openingAngle
	 *            width of the cone
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @return new ClosestDiskSensor object
	 */
	public static ClosestDiskSensor getPositionAngleSensor(Environment environment, double openingAngle, double maxRangeAbsolute) {
		return new ClosestDiskSensor(environment, 0.0, openingAngle, DEFAULT_MIN_RANGE_RELATIVE, maxRangeAbsolute, null, true, false, false, null);
	}

	/**
	 * Convenience method constructs sensor some parameters set to default, only measuring distance
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param openingAngle
	 *            width of the cone
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @param invisibleMaterials
	 *            a set of materials that are not detected by the sensor, if null: all disks are detected
	 * @return new ClosestDiskSensor object
	 */
	public static ClosestDiskSensor getDistanceSensor(Environment environment, double openingAngle, double maxRangeAbsolute, Set<DiskMaterial> invisibleMaterials) {
		return new ClosestDiskSensor(environment, 0.0, openingAngle, DEFAULT_MIN_RANGE_RELATIVE, maxRangeAbsolute, invisibleMaterials, false, true, false, null);
	}

	/**
	 * Convenience method constructs sensor some parameters set to default, only measuring distance
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param openingAngle
	 *            width of the cone
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @return new ClosestDiskSensor object
	 */
	public static ClosestDiskSensor getDistanceSensor(Environment environment, double openingAngle, double maxRangeAbsolute) {
		return new ClosestDiskSensor(environment, 0.0, openingAngle, DEFAULT_MIN_RANGE_RELATIVE, maxRangeAbsolute, null, false, true, false, null);
	}

	private void addVisualisationOptions() {
		if (posIndex != -1) {
			variants.add(0, rayTextVisualization(posIndex, offset));
			variants.add(0, rayVisualization(posIndex, offset));
		}
		if (distIndex != -1) {
			variants.add(0, arcTextVisualization(distIndex, offset));
			variants.add(0, arcVisualization(distIndex, offset));
		}
		if ((distIndex != -1) && (posIndex != -1)) {
			variants.add(0, lineVisualization(posIndex, distIndex, offset));
		}
	}

	@Override
	public int getDimension() {
		return 1 + offset * maxNumDisks;
	}

	private class DiskData {
		Disk disk;
		//closest point dx,dy coordinate and distance  
		double dx, dy, d;
	}

	@Override
	protected void doMeasurement(double measurement[]) {
		double direction = getDisk().getAngle() + centerAngle;
		DiskData[] closestDisks = determineClosestDisks(maxNumDisks);
		measurement[0] = (double) closestDisks.length / (double) maxNumDisks;
		for (int i = 1; i < measurement.length; i++) {
			measurement[i] = 0.0;
		}
		for (int i = 0; i < closestDisks.length; i++) {
			DiskData dd = closestDisks[i];
			if (posIndex != -1) {
				double angle = Math.atan2(dd.dy, dd.dx);
				measurement[posIndex + i * offset] = Utils.clip_pm1(AngleUtils.mod2PI(angle - direction) / openingAngle * 2);
				// TODO: this is not correct if the center of the disk is outside the cone!
			}
			if (distIndex != -1) {
				double range[] = getShape().referenceValues();
				measurement[distIndex + i * offset] = Utils.clip_01((dd.d - range[0]) / (range[1] - range[0]));
				// TODO: this is not correct if the center of the disk is outside the cone!
			}
			if (widthIndex != -1) {
				double cdx = getDisk().getX() - dd.disk.getX();
				double cdy = getDisk().getY() - dd.disk.getY();
				double cdist = Math.sqrt(cdx * cdx + cdy * cdy);
				double angle = 2 * Math.atan2(dd.disk.getRadius(), cdist);
				measurement[widthIndex + i * offset] = Utils.clip_01(angle / openingAngle * 2);
				// TODO: this is not correct if the centre of the disk is outside the cone!
			}
			if (materialResponseFunction != null) {
				materialResponseFunction.putResponse(dd.disk.getDiskType().getMaterial(), measurement, materialIndex + i * offset);
			}
		}
	}

	private DiskData[] determineClosestDisks(int maxNumDisks) {
		Set<Disk> disks = getDisksInShape();
		List<DiskData> diskDataList = new LinkedList<ClosestDiskSensor.DiskData>();
		for (Disk d : disks) {
			DiskMaterial material = d.getDiskType().getMaterial();
			if ((invisibleMaterials == null) || (!invisibleMaterials.contains(material))) {
				diskDataList.add(determineDiskData(d));
			}
		}
		Collections.sort(diskDataList, new Comparator<DiskData>() {
			@Override
			public int compare(DiskData dd1, DiskData dd2) {
				return Double.compare(dd1.d, dd2.d);
			}
		});
		DiskData res[] = new DiskData[Math.min(diskDataList.size(), maxNumDisks)];
		int i = 0;
		for (DiskData dd : diskDataList) {
			if (i < res.length) {
				res[i] = dd;
			}
			i++;
		}
		return res;
	}

	private DiskData determineDiskData(Disk disk2) {
		DiskData res = new DiskData();
		res.disk = disk2;
		res.dx = disk2.getX() - getDisk().getX();
		res.dy = disk2.getY() - getDisk().getY();
		res.d = Math.sqrt(res.dx * res.dx + res.dy * res.dy) - disk2.getRadius();
		return res;
	}

	@Override
	public double getRealWorldInterpretation(double measurement[], int index) {
		int diskIndex = (index - 1) / offset;
		int modindex = index - diskIndex * offset;
		if (modindex == distIndex) {
			double range[] = getShape().referenceValues();
			return range[0] + measurement[index] * (range[1] - range[0]);
		}
		if (modindex == posIndex) {
			return measurement[index] * openingAngle / 2;
		}
		if (modindex == widthIndex) {
			return measurement[index] * openingAngle / 2;
		}
		return measurement[index];
	}

	@Override
	public String getRealWorldMeaning(int index) {
		if (index == 0) {
			return "num disks in range [bool]";
		}
		int diskIndex = (index - 1) / offset;
		int modindex = index - diskIndex * offset;
		String diskStr = maxNumDisks > 1 ? " of disk #" + diskIndex : "";
		if (modindex == distIndex) {
			return "distance to closest point" + diskStr;
		}
		if (modindex == posIndex) {
			return "relative angle to closest point [rad]" + diskStr;
		}
		if (modindex == widthIndex) {
			return "visible width angle [rad]" + diskStr;
		}
		return "material response #" + (modindex - materialIndex) + diskStr;
	}
}
