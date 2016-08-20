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
import java.awt.image.BufferedImage;
import java.io.IOException;

import diskworld.Environment;
import diskworld.interfaces.MovieMaker;
import diskworld.interfaces.TimeStepListener;

public class VideoCapturer {

	private final MovieMaker movieMaker;
	private final VisualizationSettings settings;
	private final BufferedImage image;
	private final Graphics2D graphics;
	private int frameCount = 0;
	private int frameModulo;

	public VideoCapturer(MovieMaker movieMaker, VisualizationSettings settings, double frameRate, int droppedFrames) {
		this.movieMaker = movieMaker;
		this.settings = settings;
		this.frameModulo = droppedFrames + 1;
		if (!movieMaker.isInitialized()) {
			try {
				movieMaker.initialize(settings.getWidth(), settings.getHeight(), frameRate);
			} catch (IOException e) {
				throw new RuntimeException("IOError while initializing video");
			}
		}
		image = new BufferedImage(settings.getWidth(), settings.getHeight(), BufferedImage.TYPE_INT_RGB);
		graphics = image.createGraphics();
	}

	public void capture(final Environment env) {
		frameCount = 0;
		env.addTimeStepListener(new TimeStepListener() {
			@Override
			public void timeStepDone() {
				if (frameCount % frameModulo == 0) {
					env.getPaintableClone().paint(graphics, settings);
					try {
						movieMaker.addFrame(image);
					} catch (IOException e) {
						throw new RuntimeException("IOError while writing video frame");
					}
				}
				frameCount++;
			}
		});
	}

	public void done() {
		try {
			movieMaker.close();
		} catch (IOException e) {
			throw new RuntimeException("IOError while closing video frame");
		}
	}

}
