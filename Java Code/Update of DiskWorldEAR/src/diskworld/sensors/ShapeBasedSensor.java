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
import java.util.LinkedList;
import java.util.Set;

import diskworld.Disk;
import diskworld.DiskType;
import diskworld.Environment;
import diskworld.environment.Floor;
import diskworld.environment.FloorCellType;
import diskworld.shape.Shape;
import diskworld.visualization.VisualisationItem;
import diskworld.visualization.VisualizationSettings;

/**
 * @author Jan
 * 
 *         Base class for sensors that use shapes to select disks or floor cells
 */
public abstract class ShapeBasedSensor extends AbstractSensor {

	private ShapeTemplate sensorShapeTemplate;
	private Shape sensorShape;
	private Disk disk;
	
	/**
	 * Generates a sensor based an a shape template and multiple visualisation variants.
	 * 
	 * @param environment
	 *            the environment in which the sensor is living
	 * @param sensorShape
	 *            a template for the sensor shape
	 * @param sensorName
	 *            a name for the sensor (used in visualisation option pop up menu)
	 */
	public ShapeBasedSensor(Environment environment, ShapeTemplate sensorShape, String sensorName) {
		super(environment,sensorName);
		this.sensorShapeTemplate = sensorShape;
		variants.add(0,SHAPE_NO_VALUE_VISUALISATION);
		variants.add(0,SHAPE_VALUES_IN_DISK);
		variants.add(0,SHAPE_VALUES_IN_SHAPE);
		for (int i = 0; i < getDimension(); i++) {
			variants.add(getBorderColorVisualization(i));
		}
	}

	/**
	 * Provide disk
	 * 
	 * @return disk object
	 */
	protected Disk getDisk() {
		return disk;
	}

	
	@Override
	public void doMeasurement(Disk disk, double[] values) {
		sensorShape = sensorShapeTemplate.getShape(disk.getX(), disk.getY(), disk.getRadius(), disk.getAngle());
		this.disk = disk;
		doMeasurement(values);
	}

	

	// protected methods

	/**
	 * Abstract method, implemented by sub-classed to actually perform the measurement
	 * 
	 * @param values
	 *            array of doubles to be filled with the sensor measurement
	 */
	protected abstract void doMeasurement(double[] values);

	// helper methods that may be used by sub-classes to in getMeasurement()s 

	/**
	 * Provide shape
	 * 
	 * @return shape object
	 */
	protected Shape getShape() {
		return sensorShape;
	}

	/**
	 * Provide indices of all floor tiles in the sensor shape
	 * 
	 * @return list of pairs (colIndex,rowIndex)
	 */
	protected LinkedList<int[]> getFloorIndicesInShape() {
		return environment.getFloorGrid().getCellIndicesIntersectingWithShape(sensorShape);
	}

	/**
	 * Provides a histogram of the floor tile types covered by the sensor shape
	 * 
	 * @return array of length 256, holding the absolute count of the floor types
	 */
	protected int[] getFloorTypeHistogram() {
		int[] histo = new int[FloorCellType.NUM_FLOOR_TYPES];
		Floor floor = environment.getFloor();
		for (int[] idx : getFloorIndicesInShape()) {
			histo[floor.getTypeIndex(idx[0], idx[1])]++;
		}
		return histo;
	}

	/**
	 * Provide all disks that intersect with the sensor shape
	 * 
	 * @return a set of Disk objects
	 */
	protected Set<Disk> getDisksInShape() {
		return environment.getDiskGrid().getDisksIntersectingWithShape(sensorShape);
	}

	/**
	 * Return a visualisation item, to be attached to DiskTypes that use the sensor
	 * 
	 * @return
	 *         a new visualisation item
	 */
	public VisualisationItem getVisualisationItem() {
		return new VisualisationItem() {
			@Override
			public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, double[] activity, double measurement[], VisualizationSettings settings, DiskType diskType) {
				VisualisationVariant visualisation = getChosenVisualisationVariant(settings);
				if (visualisation != null) {
					Shape shape = null;
					if (visualisation instanceof ShapeVisualisationVariant) {
						shape = sensorShapeTemplate.getShape(centerx, centery, radius, angle);
						ShapeVisualisationVariant shapeVisualisation = (ShapeVisualisationVariant)visualisation;
						Color fillcol = shapeVisualisation.getFillColor(measurement, settings);
						if (fillcol != null) {
							shape.fill(g, fillcol, settings);
						}
						Color bordercol = shapeVisualisation.getBorderColor(measurement, settings);
						if (bordercol != null) {
							shape.drawBorder(g, bordercol, settings);
						}						
					}
					visualisation.visualisation(g, measurement, shape, centerx, centery, radius, angle, settings);
				}
			}
		};
	}

	// visualization options to be used by subclasses 
	

	protected static final VisualisationVariant SHAPE_NO_VALUE_VISUALISATION = new ShapeVisualisationVariant("shape, no values") {
		@Override
		public Color getBorderColor(double[] measurement, VisualizationSettings settings) {
			return settings.getSensorShapeBorder();
		}

		@Override
		public Color getFillColor(double[] measurement, VisualizationSettings settings) {
			return settings.getSensorShapeFill();
		}

		@Override
		public void additionalVisualisation(Graphics2D g, double[] measurement, Shape sensorShape, double cx, double cy, double r, double angle, VisualizationSettings settings) {
		}

		@Override
		public void visualisation(Graphics2D g, double[] measurement,
				Object visualisationObject, double cx, double cy, double r,
				double angle, VisualizationSettings settings) {
			// TODO Auto-generated method stub
			
		}
	};

	protected static final VisualisationVariant SHAPE_VALUES_IN_DISK = new ShapeVisualisationVariant("shape, values in disk") {
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
			int x = settings.mapX(cx);
			int y = settings.mapY(cy);
			g.setColor(settings.getSensorValueTextColor());
			int numDigits = 2;
			drawMeasurement(g, x, y, measurement, numDigits);
		}
	};

	protected final static VisualisationVariant SHAPE_VALUES_IN_SHAPE = new ShapeVisualisationVariant("values in shape") {

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
			double refPoint[] = shape.referencePoint();
			int x = settings.mapX(refPoint[0]);
			int y = settings.mapY(refPoint[1]);
			g.setColor(settings.getSensorValueTextColor());
			int numDigits = 2;
			drawMeasurement(g, x, y, measurement, numDigits);
		}
	};

	private VisualisationVariant getBorderColorVisualization(final int index) {
		return new ShapeVisualisationVariant("border color (#" + index + ")") {
			@Override
			public Color getBorderColor(double[] measurement, VisualizationSettings settings) {
				return settings.getMeasurementColor(measurement[index]);
			}

			@Override
			public Color getFillColor(double[] measurement, VisualizationSettings settings) {
				return settings.getSensorShapeFill();
			}

			@Override
			public void additionalVisualisation(Graphics2D g, double[] measurement, Shape sensorShape, double cx, double cy, double r, double angle, VisualizationSettings settings) {
			}
		};
	}

	// private methods 


}
