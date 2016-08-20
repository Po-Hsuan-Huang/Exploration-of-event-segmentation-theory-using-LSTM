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
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import ch.randelshofer.media.quicktime.QuickTimeWriter;
import ch.randelshofer.media.quicktime.QuickTimeWriter.VideoFormat;
import diskworld.interfaces.MovieMaker;

public class QuicktimeMovieMaker implements MovieMaker {

	public enum Streaming {
		NONE, FASTSTART, FASTSTART_COMPRESSED_HEADERS
	};

	private int prevImgDuration;
	private QuickTimeWriter qtOut;
	private Graphics2D g;
	private int sizex, sizey, frameCount;
	private int[] data;
	private int[] prevData;
	private int duration;
	private BufferedImage prevImg;
	private Streaming streaming = Streaming.FASTSTART_COMPRESSED_HEADERS;
	private VideoFormat videoFormat = VideoFormat.RLE;
	private File movieFile;
	private File tmpFile;
	private final String filename;

	public QuicktimeMovieMaker(String filename) {
		this.filename = filename;
	}

	@Override
	public void initialize(int sizex, int sizey, double frameRate) throws IOException {
		movieFile = new File(filename);
		this.sizex = sizex;
		this.sizey = sizey;

		/** variable frame rate. */
		tmpFile = streaming.equals(Streaming.NONE) ? movieFile : new File(movieFile.getPath() + ".tmp");
		int timeScale = (int) (frameRate * 100.0);
		duration = 100;

		qtOut = new QuickTimeWriter(streaming == Streaming.NONE ? movieFile : tmpFile);
		qtOut.addVideoTrack(videoFormat, timeScale, sizex, sizey);
		qtOut.setSyncInterval(0, 30);

		BufferedImage img = new BufferedImage(sizex, sizey, BufferedImage.TYPE_INT_RGB);
		data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		prevImg = new BufferedImage(sizex, sizey, BufferedImage.TYPE_INT_RGB);
		prevData = ((DataBufferInt) prevImg.getRaster().getDataBuffer()).getData();
		g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		prevImgDuration = 0;
		frameCount = 0;
	}

	@Override
	public boolean isInitialized() {
		return qtOut != null;
	}

	@Override
	public void addFrame(Image frame) throws IOException {
		if ((frame.getWidth(null) != sizex) || (frame.getHeight(null) != sizey)) {
			throw new IllegalArgumentException("Frame does not have the correct size: " + frame.getWidth(null) + "," + frame.getHeight(null) + " should be " + sizex + "," + sizey);
		}
		g.drawImage(frame, 0, 0, sizex, sizey, null);
		if (frameCount != 0 && Arrays.equals(data, prevData)) {
			prevImgDuration += duration;
		} else {
			if (prevImgDuration != 0) {
				qtOut.writeFrame(0, prevImg, prevImgDuration);
			}
			prevImgDuration = duration;
			System.arraycopy(data, 0, prevData, 0, data.length);
		}
		frameCount++;
	}

	@Override
	public void close() throws IOException {
		if (prevImgDuration != 0) {
			qtOut.writeFrame(0, prevImg, prevImgDuration);
		}
		if (streaming.equals(Streaming.FASTSTART)) {
			qtOut.toWebOptimizedMovie(movieFile, false);
		} else if (streaming.equals(Streaming.FASTSTART_COMPRESSED_HEADERS)) {
			qtOut.toWebOptimizedMovie(movieFile, true);
		}
		qtOut.close();
		if (g != null) {
			g.dispose();
		}
		if (!streaming.equals(Streaming.NONE)) {
			tmpFile.delete();
		}
	}

}
