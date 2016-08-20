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

import java.awt.Color;
import java.awt.Graphics2D;

import diskworld.interfaces.DiskSymbol;

public abstract class AbstractDiskSymbol implements DiskSymbol {

	public void draw(Graphics2D g, double centerx, double centery, double radius, double angle, Color color, VisualizationSettings settings) {
		VisualizationOption option = settings.getOptions().getOption(VisualizationOptions.GROUP_GENERAL, VisualizationOptions.OPTION_DISK_SYMBOLS);
		if (option.isEnabled()) {
			g.setColor(color);
			int x = settings.mapX(centerx);
			int y = settings.mapY(centery);
			int x1 = settings.mapX(centerx - radius);
			int y1 = settings.mapY(centery + radius);
			draw(g, x, y, x - x1, y - y1, angle);
		}
	}

}
