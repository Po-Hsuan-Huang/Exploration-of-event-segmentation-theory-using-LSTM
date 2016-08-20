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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.border.Border;

import diskworld.Environment;
import diskworld.interfaces.TimeStepListener;

public class EnvironmentPanel extends JPanel {

	private static final int REPAINT_INTERVAL_MS = 40; // Extract paintable clone for max 25 fps update
	private static final int POPUP_BORDER_THICKNESS = 3;
	private static final Color POPUP_BORDER = Color.GRAY;
	private static final Color POPUP_FOREGROUND = Color.BLACK;
	private static final Color POPUP_BACKGROUND = new Color(255, 240, 240);
	private static final long serialVersionUID = 1L;
	protected static final int POPUPINSET = 15;
	private Environment environment;
	private long lastRepaintTime;
	private AtomicReference<PaintableEnvironmentClone> paintableEnvironmentCloneReference;
	private VisualizationSettings settings;
	private final AtomicReference<Popup> popup;
	private final TimeStepListener timeStepListener;

	public EnvironmentPanel() {
		super(true);
		environment = null;
		lastRepaintTime = System.currentTimeMillis()-REPAINT_INTERVAL_MS-1;
		paintableEnvironmentCloneReference = new AtomicReference<PaintableEnvironmentClone>();
		settings = null;
		popup = new AtomicReference<Popup>();
		timeStepListener = new TimeStepListener() {
			@Override
			public void timeStepDone() {
				prepareRepaint();
			}
		};
	}


	private void prepareRepaint() {
		long time = System.currentTimeMillis();
		if (time > lastRepaintTime + REPAINT_INTERVAL_MS) {
			paintableEnvironmentCloneReference.set(environment.getPaintableClone());
			repaint();
		};
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		if (environment != null)
			environment.removeTimeStepListener(timeStepListener);
		this.environment = environment;
		environment.addTimeStepListener(timeStepListener);
	}

	public VisualizationSettings getSettings() {
		if (settings == null) {
			setSettings(new VisualizationSettings());
		}
		return settings;
	}

	public void setSettings(final VisualizationSettings settings) {
		this.settings = settings;
		final JPanel thisPanel = this;
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Popup wind = popup.get();
				if (wind != null) {
					wind.hide();
					popup.set(null);
				} else {
					if (e.getButton() == MouseEvent.BUTTON3) {
						popup.set(wind);
						JPanel popupPanel = settings.getOptions().createMenuPanel(POPUP_FOREGROUND, POPUP_BACKGROUND, thisPanel);
						Border outsideBorder = BorderFactory.createLineBorder(POPUP_BORDER, POPUP_BORDER_THICKNESS);
						Border insideBorder = BorderFactory.createEmptyBorder(POPUPINSET, POPUPINSET, POPUPINSET, POPUPINSET);
						popupPanel.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
						int x = e.getX() + thisPanel.getLocationOnScreen().x;
						int y = e.getY() + thisPanel.getLocationOnScreen().y;
						Popup popupWind = PopupFactory.getSharedInstance().getPopup(thisPanel, popupPanel, x,y);
						popupWind.show();
						popup.set(popupWind);
					}
				}
			}
		});
	}

	@Override
	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		if (environment != null) {
			getSettings().setViewDimension(getWidth(), getHeight());
			PaintableEnvironmentClone pec = paintableEnvironmentCloneReference.get();
			if (pec != null) {
				pec.paint(g, settings);
			}
		}
	}

}
