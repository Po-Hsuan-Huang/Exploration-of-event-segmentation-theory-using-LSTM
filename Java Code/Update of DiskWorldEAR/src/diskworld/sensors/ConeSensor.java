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

import java.awt.Color;
import java.awt.Graphics2D;

import diskworld.Environment;
import diskworld.shape.CircleShape;
import diskworld.shape.RingSegmentShape;
import diskworld.shape.RingShape;
import diskworld.shape.Shape;
import diskworld.visualization.VisualizationSettings;

public abstract class ConeSensor extends ShapeBasedSensor {

	/**
	 * Sensor with a cone-shaped receptive field. Since special cases are handled by different implementations,
	 * different shape objects will be used. There are also specialised constructors to make this explicit.
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param centerAngle
	 *            the angle of the centre of the cone
	 * @param openingAngle
	 *            width of the cone
	 * @param minRangeRelativeToRadius
	 *            minimum distance (inner radius), specified in multiples of the disk radius
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @param sensorName
	 *            a name for the sensor (used in options pop-up menu)
	 */
	public ConeSensor(
			Environment environment,
			double centerAngle,
			double openingAngle,
			double minRangeRelativeToRadius,
			double maxRangeAbsolute,
			String sensorName) {
		super(environment, getTemplate(centerAngle, openingAngle, minRangeRelativeToRadius, maxRangeAbsolute), sensorName);
	}

	/**
	 * Special case of opening angle = 2*PI (shape is a full ring).
	 * This special case uses a different (more efficient) implementation, also the visualisation has to be handled slightly differently.
	 * So it is preferable to call this constructor, if it is a-priori known that opening angle = 2*PI!
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param minRangeRelativeToRadius
	 *            minimum distance (inner radius), specified in multiples of the disk radius
	 * @param maxRangeAbsolute
	 *            maximum distance (outer radius), in absolute units
	 * @param sensorName
	 *            a name for the sensor (used in options pop-up menu)
	 * @param visualisationOptions
	 *            the visualisation settings object
	 */
	public ConeSensor(
			Environment environment,
			double minRangeRelativeToRadius,
			double maxRangeAbsolute,
			String sensorName,
			ShapeVisualisationVariant[] visualisationOptions) {
		super(environment, getRingTemplate(minRangeRelativeToRadius, maxRangeAbsolute), sensorName);
	}

	/**
	 * Special case of opening angle = 2*PI and minRange = 0 (shape is a circle).
	 * This special case uses a different (more efficient) implementation, also the visualisation has to be handled slightly differently.
	 * So it is preferable to call this constructor, if it is a-priori known that opening angle = 2*PI and minRange = 0!
	 * 
	 * @param environment
	 *            environment the sensor lives in
	 * @param minRangeRelativeToRadius
	 *            minimum distance (inner radius), relative to the disk radius
	 * @param sensorName
	 *            a name for the sensor (used in options pop-up menu)
	 * @param visualisationOptions
	 *            the visualisation settings object
	 */
	public ConeSensor(
			Environment environment,
			double minRangeRelativeToRadius,
			String sensorName,
			ShapeVisualisationVariant[] visualisationOptions) {
		super(environment, getCircleTemplate(minRangeRelativeToRadius), sensorName);
	}

	private static ShapeTemplate getConeTemplate(final double centerAngle, final double openingAngle, final double minRangeRelativeToRadius, final double maxRangeAbsolute) {
		return new ShapeTemplate() {
			@Override
			public Shape getShape(double centerx, double centery, double radius, double angle) {
				return new RingSegmentShape(centerx, centery, minRangeRelativeToRadius * radius, maxRangeAbsolute, centerAngle - openingAngle * 0.5 + angle, openingAngle);
			}
		};
	}

	private static ShapeTemplate getTemplate(double centerAngle, double openingAngle, double minRangeRelativeToRadius, double maxRangeAbsolute) {
		if (openingAngle < 2.0 * Math.PI) {
			return getConeTemplate(centerAngle, openingAngle, minRangeRelativeToRadius, maxRangeAbsolute);
		}
		if (minRangeRelativeToRadius > 0.0) {
			return getRingTemplate(minRangeRelativeToRadius, maxRangeAbsolute);
		}
		return getCircleTemplate(maxRangeAbsolute);
	}

	private static ShapeTemplate getRingTemplate(final double minRangeRelativeToRadius, final double maxRangeAbsolute) {
		return new ShapeTemplate() {
			@Override
			public Shape getShape(double centerx, double centery, double radius, double angle) {
				return new RingShape(centerx, centery, minRangeRelativeToRadius * radius, maxRangeAbsolute);
			}
		};
	}

	private static ShapeTemplate getCircleTemplate(final double maxRangeAbsolute) {
		return new ShapeTemplate() {
			@Override
			public Shape getShape(double centerx, double centery, double radius, double angle) {
				return new CircleShape(centerx, centery, maxRangeAbsolute);
			}
		};
	}

	// visualization options to be used by subclasses 

	private static void drawArc(Graphics2D g, double measurement,
			Shape shape, double cx, double cy, VisualizationSettings settings) {
		double angles[] = shape.referenceAngles();
		int a1 = (int) Math.round(angles[0] * 180.0 / Math.PI);
		int a2 = (int) Math.round(angles[1] * 180.0 / Math.PI);
		double range[] = shape.referenceValues();
		double radius = measurement * (range[1] - range[0]) + range[0];
		int x1 = settings.mapX(cx - radius);
		int x2 = settings.mapX(cx + radius);
		int y1 = settings.mapY(cy + radius);
		int y2 = settings.mapY(cy - radius);
		g.setColor(settings.getSensorValueGraphicalColor());
		g.drawArc(x1, y1, x2 - x1, y2 - y1, a1, a2 - a1);
	}

	private static void drawRay(Graphics2D g, double angleMeasurement,
			Shape shape, double cx, double cy, VisualizationSettings settings) {
		double ranges[] = shape.referenceValues();
		drawRay(g, angleMeasurement, ranges[0], ranges[1], shape, cx, cy, settings);
	}

	private static void drawLine(Graphics2D g, double angleMeasurement, double distanceMeasurement,
			Shape shape, double cx, double cy, VisualizationSettings settings) {
		double ranges[] = shape.referenceValues();
		double dist = distanceMeasurement * (ranges[1] - ranges[0]) + ranges[0];
		drawRay(g, angleMeasurement, ranges[0], dist, shape, cx, cy, settings);
	}

	private static void drawRay(Graphics2D g, double angleMeasurement, double minr, double maxr,
			Shape shape, double cx, double cy, VisualizationSettings settings) {
		double angles[] = shape.referenceAngles();
		double absAngle = angleMeasurement * (angles[1] - angles[0]) / 2 + (angles[0] + angles[1]) / 2;
		double absAngleSin = Math.sin(absAngle);
		double absAngleCos = Math.cos(absAngle);
		int x1 = settings.mapX(cx + absAngleCos * minr);
		int y1 = settings.mapY(cy + absAngleSin * minr);
		int x2 = settings.mapX(cx + absAngleCos * maxr);
		int y2 = settings.mapY(cy + absAngleSin * maxr);
		g.setColor(settings.getSensorValueGraphicalColor());
		g.drawLine(x1, y1, x2, y2);
	}

	protected static ShapeVisualisationVariant arcVisualization(final int index, final int offset) {
		return new ShapeVisualisationVariant("graphical (dist)") {
			@Override
			public Color getBorderColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeBorder();
			}

			@Override
			public Color getFillColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeFill();
			}

			@Override
			public void additionalVisualisation(Graphics2D g, double[] measurement, Shape shape, double cx, double cy, double r, double angle, VisualizationSettings settings) {
				for (int i = 0; i < measurement[0]; i++) {
					drawArc(g, measurement[i * offset + index], shape, cx, cy, settings);
				}
			}
		};
	}

	protected static ShapeVisualisationVariant lineVisualization(final int angleIndex, final int lengthIndex, final int offset) {
		return new ShapeVisualisationVariant("graphical (dir+dist)") {
			@Override
			public Color getBorderColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeBorder();
			}

			@Override
			public Color getFillColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeFill();
			}

			@Override
			public void additionalVisualisation(Graphics2D g, double[] measurement, Shape shape, double cx, double cy, double r, double angle, VisualizationSettings settings) {
				for (int i = 0; i < measurement[0]; i++) {
					drawLine(g, measurement[i * offset + angleIndex], measurement[lengthIndex], shape, cx, cy, settings);
				}
			}
		};
	}

	protected static ShapeVisualisationVariant arcTextVisualization(final int index, final int offset) {
		return new ShapeVisualisationVariant("graphical+text") {
			@Override
			public Color getBorderColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeBorder();
			}

			@Override
			public Color getFillColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeFill();
			}

			@Override
			public void additionalVisualisation(Graphics2D g, double[] measurement, Shape shape, double cx, double cy, double r, double angle, VisualizationSettings settings) {
				for (int i = 0; i < measurement[0]; i++) {
					drawArc(g, measurement[offset * i + index], shape, cx, cy, settings);
				}
				int x = settings.mapX(cx);
				int y = settings.mapY(cy);
				g.setColor(settings.getSensorValueTextColor());
				int numDigits = 2;
				drawMeasurement(g, x, y, measurement, numDigits);
			}
		};
	};

	protected static ShapeVisualisationVariant rayVisualization(final int index, final int offset) {
		return new ShapeVisualisationVariant("graphical (dir)") {

			@Override
			public Color getBorderColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeBorder();
			}

			@Override
			public Color getFillColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeFill();
			}

			@Override
			public void additionalVisualisation(Graphics2D g, double[] measurement, Shape shape, double cx, double cy, double r, double angle, VisualizationSettings settings) {
				for (int i = 0; i < measurement[0]; i++)
					drawRay(g, measurement[i * offset + index], shape, cx, cy, settings);
			}
		};
	}

	protected static ShapeVisualisationVariant rayTextVisualization(final int index, final int offset) {
		return new ShapeVisualisationVariant("graphical+text") {
			@Override
			public Color getBorderColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeBorder();
			}

			@Override
			public Color getFillColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeFill();
			}

			@Override
			public void additionalVisualisation(Graphics2D g, double[] measurement, Shape shape, double cx, double cy, double r, double angle, VisualizationSettings settings) {
				for (int i = 0; i < measurement[0]; i++)
					drawRay(g, measurement[i * offset + index], shape, cx, cy, settings);
				int x = settings.mapX(cx);
				int y = settings.mapY(cy);
				g.setColor(settings.getSensorValueTextColor());
				int numDigits = 2;
				drawMeasurement(g, x, y, measurement, numDigits);
			}
		};
	}

}
