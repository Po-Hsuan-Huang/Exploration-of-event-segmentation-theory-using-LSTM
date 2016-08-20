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
package diskworld.tests;

import diskworld.Environment;
import diskworld.demos.DemoLauncher;
import diskworld.demos.DemoLauncher.Demo;
import diskworld.demos.TypesOfDiskActions;
import diskworld.interfaces.MovieMaker;
import diskworld.visualization.QuicktimeMovieMaker;
import diskworld.visualization.VideoCapturer;
import diskworld.visualization.VisualizationSettings;

public class TestVideoCapturing {

	private static final int SIZEX = 1280;
	private static final int SIZEY = 720;
	private static final int FRAME_RATE = 20; // frames per second
	private static final int DROP_FRAMES = 4; // show only every fifth frame, with DT_PER_TIME_STEP=0.01 this makes video_time == simulation time

	public static void main(String[] args) {
		MovieMaker mm = new QuicktimeMovieMaker("E:/Temp/video.mov");
		final VisualizationSettings vs = new VisualizationSettings();
		vs.setViewDimension(SIZEX, SIZEY);
		double viewx = 120;
		double offsy = viewx * (1 - (double) SIZEY / (double) SIZEX) * 0.5;
		vs.setViewedRect(0, offsy, viewx, viewx - offsy);
		final VideoCapturer vc = new VideoCapturer(mm, vs, FRAME_RATE, DROP_FRAMES);
		Demo captureDemo = new TypesOfDiskActions() {
			@Override
			public Environment getEnvironment() {
				Environment env = super.getEnvironment();
				vc.capture(env);
				return env;
			}
		};
		DemoLauncher.runDemo(captureDemo);
		vc.done();
	}
}
