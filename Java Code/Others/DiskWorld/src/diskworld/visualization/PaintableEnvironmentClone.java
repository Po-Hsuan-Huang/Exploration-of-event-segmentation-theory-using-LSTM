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
package diskworld.visualization;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import diskworld.Disk;
import diskworld.DiskComplex;
import diskworld.Environment;
import diskworld.environment.Floor;
import diskworld.environment.Wall;
import diskworld.grid.Grid;
import diskworld.grid.GridWithDiskMap;
import diskworld.linalg2D.Line;
import diskworld.linalg2D.Point;
import diskworld.skeleton.PermanentEdge;
import diskworld.skeleton.TransientEdge;
import diskworld.skeleton.Vertex;

public class PaintableEnvironmentClone {

	private static final int TIME_POS_X = 10;
	private static final int TIME_POS_Y = 20;
	private static final int GRID_TEXT_POS_X = 3;
	private static final int GRID_TEXT_POS_Y = 12;

	private final Floor floor;
	private final Collection<Wall> walls;
	private final Grid[] grids;
	private final int[][] numDisksInCell;
	private final List<PaintableDisk> disks;
	private final double time;
	private final List<Point> collisions, blockedCollisions;
	private final List<Line> permanentEdges;
	private final List<Line> transientEdges;

	public PaintableEnvironmentClone(Environment environment) {
		this.floor = environment.getFloor().createClone();
		this.walls = new LinkedList<Wall>();
		this.walls.addAll(environment.getWalls()); // clone the list of walls, because it may change, the Wall objects themselves are immutable, however 
		this.grids = new Grid[] { environment.getFloorGrid(), environment.getDiskGrid() }; // no need for cloning, grids do not change
		this.numDisksInCell = countDisks(environment.getDiskGrid());
		disks = new LinkedList<PaintableDisk>();
		permanentEdges = new LinkedList<Line>();
		transientEdges = new LinkedList<Line>();
		Map<Integer, List<PaintableDisk>> zLevelMap = new TreeMap<Integer, List<PaintableDisk>>();
		for (DiskComplex dc : environment.getDiskComplexes()) {
			for (Disk d : dc.getDisks()) {
				List<PaintableDisk> list = zLevelMap.get(d.getZLevel());
				if (list == null) {
					list = new LinkedList<PaintableDisk>();
					zLevelMap.put(d.getZLevel(), list);
				}
				list.add(new PaintableDisk(d));
				Vertex vertex = d.getSkeletonVertex();
				for (PermanentEdge e : vertex.getEdges()) {
					if (e.getVertex1() == vertex) {
						Disk d2 = e.getVertex2().getDisk();
						permanentEdges.add(new Line(d.getX(), d.getY(), d2.getX(), d2.getY()));
					}
				}
			}
			for (TransientEdge e : dc.getTransientEdges()) {
				Disk d1 = e.getVertex1().getDisk();
				Disk d2 = e.getVertex2().getDisk();
				transientEdges.add(new Line(d1.getX(), d1.getY(), d2.getX(), d2.getY()));
			}
		}
		// now add disks in increasing zLevel order
		for (List<PaintableDisk> list : zLevelMap.values()) {
			disks.addAll(list);
		}
		collisions = environment.getCollisionPoints(false);
		blockedCollisions = environment.getCollisionPoints(true);
		this.time = environment.getTime();
	}

	private int[][] countDisks(GridWithDiskMap grid) {
		int nx = grid.getNumColumns();
		int ny = grid.getNumRows();
		int res[][] = new int[nx][ny];
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				res[i][j] = grid.getCell(i, j).getIntersectingDisks().size();
			}
		}
		return res;
	}

	public void paint(Graphics2D g, VisualizationSettings settings) {
		if (settings.isOptionEnabled(VisualizationOptions.OPTION_FLOOR)) {
			paintFloor(g, settings);
		} else {
			clear(g, settings);
		}
		if (settings.isOptionEnabled(VisualizationOptions.OPTION_GRID)) {
			VisualizationOption option = settings.getOptions().getOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_GRID);
			int index = option.chosenVariantIndex();
			paintGrid(g, grids[index], settings);
			if (index == 1) {
				paintCounts(g, grids[index], numDisksInCell, settings);
			}
		}
		if (settings.paintShadows()) {
			for (PaintableDisk d : disks) {
				d.paintShadow(g, settings);
			}
		}
		if (settings.isOptionEnabled(VisualizationOptions.OPTION_WALLS)) {
			for (Wall w : walls) {
				paintWall(g, w, settings);
			}
		}
		if (settings.isOptionEnabled(VisualizationOptions.OPTION_DISKS)) {
			for (PaintableDisk d : disks) {
				d.paint(g, settings);
			}
		}
		if (settings.isOptionEnabled(VisualizationOptions.OPTION_SKELETON)) {
			paintSkeleton(g, settings);
		}
		for (PaintableDisk d : disks) {
			d.paintAdditionalItems(g, settings);
		}
		if (settings.isOptionEnabled(VisualizationOptions.OPTION_COLLISIONS)) {
			paintCollisions(g, settings);
		}

		if (settings.isOptionEnabled(VisualizationOptions.OPTION_TIME)) {
			paintTime(g, settings);
		}
	}

	private void paintGrid(Graphics2D g, Grid grid, VisualizationSettings settings) {
		g.setColor(settings.getColorScheme().gridColor);
		double dx = grid.getCellWidth();
		double dy = grid.getCellHeight();
		int nx = grid.getNumColumns();
		int ny = grid.getNumRows();
		int minx = settings.mapX(0);
		int maxx = settings.mapX(nx * dx);
		int miny = settings.mapX(ny * dy);
		int maxy = settings.mapX(0);
		for (int i = 0; i <= nx; i++) {
			int x = settings.mapX(i * dx);
			g.drawLine(x, miny, x, maxy);
		}
		for (int i = 0; i <= ny; i++) {
			int y = settings.mapY(i * dy);
			g.drawLine(minx, y, maxx, y);
		}
	}

	private void paintCounts(Graphics2D g, Grid grid, int[][] numDisks, VisualizationSettings settings) {
		g.setColor(settings.getColorScheme().gridTextColor);
		double dx = grid.getCellWidth();
		double dy = grid.getCellHeight();
		int nx = grid.getNumColumns();
		int ny = grid.getNumRows();
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				int x = settings.mapX(i * dx);
				int y = settings.mapY((j + 1) * dy);
				g.drawString("" + numDisks[i][j], x + GRID_TEXT_POS_X, y + GRID_TEXT_POS_Y);
			}
		}
	}

	private void paintSkeleton(Graphics2D g, VisualizationSettings settings) {
		g.setColor(settings.getColorScheme().permanentEdgesColor);
		for (Line e : permanentEdges) {
			paintEdge(g, e, settings);
		}
		g.setColor(settings.getColorScheme().transientEdgesColor);
		for (Line e : transientEdges) {
			paintEdge(g, e, settings);
		}
	}

	private void paintCollisions(Graphics2D g, VisualizationSettings settings) {
		g.setColor(settings.getColorScheme().collisionsColor);
		for (Point p : collisions) {
			int x = settings.mapX(p.x);
			int y = settings.mapY(p.y);
			g.fill(settings.getCollisionShape(x, y));
		}
		g.setColor(settings.getColorScheme().blockingCollisionsColor);
		for (Point p : blockedCollisions) {
			int x = settings.mapX(p.x);
			int y = settings.mapY(p.y);
			g.fill(settings.getCollisionShape(x, y));
		}
	}

	private void paintEdge(Graphics2D g, Line e, VisualizationSettings settings) {
		g.drawLine(settings.mapX(e.getX1()), settings.mapY(e.getY1()), settings.mapX(e.getX2()), settings.mapY(e.getY2()));
	}

	private void paintTime(Graphics2D g, VisualizationSettings settings) {
		g.setColor(settings.getTimeColor());
		g.drawString("t=" + String.format("%3.2f", time), TIME_POS_X, TIME_POS_Y);
	}

	private static void paintWall(Graphics2D g, Wall w, VisualizationSettings settings) {
		g.setColor(settings.getWallColor());
		double x1 = w.getX1();
		double y1 = w.getY1();
		double x2 = w.getX2();
		double y2 = w.getY2();
		double d = w.getHalfThickness();
		double dx = w.getHalfThicknessX();
		double dy = w.getHalfThicknessY();
		paintWallEnd(g, x1, y1, d, settings);
		paintWallEnd(g, x2, y2, d, settings);
		paintRectangle(g, x1 - dx, y1 - dy, x1 + dx, y1 + dy, x2 + dx, y2 + dy, x2 - dx, y2 - dy, settings);
	}

	private static void paintWallEnd(Graphics2D g, double cx, double cy, double r, VisualizationSettings settings) {
		int x1 = settings.mapX(cx - r);
		int y1 = settings.mapY(cy + r);
		int x2 = settings.mapX(cx + r);
		int y2 = settings.mapY(cy - r);
		g.fillArc(x1 - 1, y1 - 1, x2 - x1 + 1, y2 - y1 + 1, 0, 360);
	}

	private static void paintRectangle(Graphics2D g, double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, VisualizationSettings settings) {
		g.fillPolygon(new int[] { settings.mapX(x1), settings.mapX(x2), settings.mapX(x3), settings.mapX(x4) },
				new int[] { settings.mapY(y1), settings.mapY(y2), settings.mapY(y3), settings.mapY(y4) }, 4);
	}

	private void paintFloor(Graphics2D g, VisualizationSettings settings) {
		int x1 = settings.mapX(floor.getPosX(0));
		for (int i = 0; i < floor.getNumX(); i++) {
			int y1 = settings.mapY(floor.getPosY(0));
			int x2 = settings.mapX(floor.getPosX(i + 1));
			for (int j = 0; j < floor.getNumY(); j++) {
				int y2 = settings.mapY(floor.getPosY(j + 1));
				g.setColor(settings.getFloorColor(floor.getTypeIndex(i, j)));
				g.fillRect(x1, y2, x2 - x1, y1 - y2);
				y1 = y2;
			}
			x1 = x2;
		}
	}

	private void clear(Graphics2D g, VisualizationSettings settings) {
		int x1 = settings.mapX(floor.getPosX(0));
		int y1 = settings.mapY(floor.getPosY(0));
		int x2 = settings.mapX(floor.getPosX(floor.getNumX()));
		int y2 = settings.mapY(floor.getPosY(floor.getNumY()));
		g.setColor(settings.getFloorColor(0));
		g.fillRect(x1, y2, x2 - x1, y1 - y2);
	}

}
