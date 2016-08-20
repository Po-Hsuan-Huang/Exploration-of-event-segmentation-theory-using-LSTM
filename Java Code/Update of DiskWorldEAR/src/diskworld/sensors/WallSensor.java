package diskworld.sensors;

import java.util.Collection;
import java.util.LinkedList;

import diskworld.Environment;
import diskworld.environment.Wall;
import diskworld.linalg2D.AngleUtils;
import diskworld.linalg2D.Utils;

public class WallSensor extends ConeSensor {
	
	private double maxRange;
	private double openingAngle;
	private Collection<Wall> walls;
	private Environment env;

	public WallSensor(Environment environment, double centerAngle,
			double openingAngle, double minRangeRelativeToRadius,
			double maxRangeAbsolute, String sensorName) {
		super(environment, centerAngle, openingAngle, minRangeRelativeToRadius,
				maxRangeAbsolute, sensorName);
		this.maxRange = maxRangeAbsolute;
		this.openingAngle = openingAngle;
		walls = environment.getWalls();
		this.env = environment;
	}

	@Override
	public int getDimension() {
		// 1: boolean, whether wall is in cone
		// 2: distance to wall
		// 3: angle
		return 3;
	}

	@Override
	public double getRealWorldInterpretation(double[] measurement, int index) {

		if (index == 1) {
			double range[] = getShape().referenceValues();
			return range[0] + measurement[index] * (range[1] - range[0]);
		}
		if (index == 2) {
			return measurement[index] * openingAngle / 2;
		}
		return measurement[index];
	}

	@Override
	public String getRealWorldMeaning(int index) {
		if(index == 0) {
			return "Is wall in sight?";
		} else if(index == 1) {
			return "Distance to next wall (-1 if no wall in sight)";
		} else {
			return "Relative angle to next wall (-1 if no wall in sight)";
		}
	}
	
	private Collection<Wall> getWallsInShape() {
		LinkedList<Wall> wallsInShape = new LinkedList<Wall>();
		for(Wall w : walls) {
			if(getShape().intersectsRectangle(w.getX1(), w.getY1(), w.getX2(), w.getY2())) {
				wallsInShape.add(w);
			}
		}
		return wallsInShape;
	}

	@Override
	protected void doMeasurement(double[] values) {
		
		double centerx = getDisk().getX();
		double centery = getDisk().getY();
		double direction = getDisk().getAngle();
		double min = maxRange;
		double dx = -1;
		double dy = -1;
		Wall closestWall = null;
		for (Wall w : getWallsInShape()) {
			dx = Math.abs(w.getX1() - centerx) < Math.abs(w.getX2() - centerx) ? w.getX1() - centerx : w.getX2() - centerx;
			dy = Math.abs(w.getY1() - centery) < Math.abs(w.getY2() - centery) ? w.getY1() - centery : w.getY2() - centery;
			
			double dist = Math.sqrt(dx * dx + dy * dy);

			if (dist <= min) {
				min = dist;
				closestWall = w;
			}
		}
		
		//if no wall in cone, set first measurement value to 0 and the others to -1
		if (closestWall == null) {
			double[] boundingBox = getShape().referencePoint();
			if( boundingBox[0] > env.getMaxX() && boundingBox[0] < 0 && 
				boundingBox[1] > env.getMaxY() && boundingBox[1] <0 ) {
				
				values[0] = 1;
			} else {
				values[0] = 0;
			}

			values[1] = -1;
			values[2] = -1;
		} else {
			//set first measurement to 1, because a wall was found in the cone
			values[0] = 1.0;
			
			double range[] = getShape().referenceValues();
			values[1] = Utils.clip_01((min - range[0]) / (range[1] - range[0]));

			double angle = Math.atan2(dy, dx);
			values[2] = Utils.clip_pm1(AngleUtils.mod2PI(angle - direction) / openingAngle * 2);
		}
	}

}
