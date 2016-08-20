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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Holds a list of VisualizationOption objects, arranged into groups. Provides a pop-up menu to make enable options and make choices.
 */
public class VisualizationOptions {

	// Default groups
	public static final String GROUP_GENERAL = "General";
	public static final String GROUP_SENSORS = "Sensors";
	public static final String GROUP_ACTUATORS = "Actuators";

	// Default option keys
	public static final String OPTION_FLOOR = "Floor";
	public static final String OPTION_WALLS = "Walls";
	public static final String OPTION_DISKS = "Disks";
	public static final String OPTION_TIME = "Time";
	public static final String OPTION_SKELETON = "Skeleton";
	public static final String OPTION_DISK_SYMBOLS = "Disk Symbols";
	private static final String[] DISK_SYMBOL_VARIANTS = new String[] { "Activity", "Fixed Color" };
	public static final String OPTION_GRID = "Grid";
	private static final String[] GRID_VARIANTS = new String[] { "Floor Cells", "Disk-Hash" };
	public static final String OPTION_COLLISIONS = "Collisions";
	public static final String OPTION_SHADOW = "Shadows";

	private Map<String, Map<String, VisualizationOption>> options;

	/**
	 * Constructs empty VisualizationOptions object
	 */
	public VisualizationOptions() {
		options = new TreeMap<String, Map<String, VisualizationOption>>();
		addDefaultOptions();
	}

	/**
	 * Add a new option without variants.
	 * 
	 * @param group
	 *            the name of the group into which the new option shall be placed
	 * @param name
	 *            the name of the option.
	 * @return the newly created VisualizationOption
	 */
	public synchronized VisualizationOption addOption(String group, String name) {
		return addOption(group, name, null);
	}

	/**
	 * Add a new option with variants.
	 * 
	 * @param group
	 *            the name of the group into which the new option shall be placed
	 * @param name
	 *            the name of the option.
	 * @param variants
	 *            the name of the variants
	 * @return the newly created VisualizationOption
	 */
	public synchronized VisualizationOption addOption(String group, String name, String[] variants) {
		Map<String, VisualizationOption> list = options.get(group);
		if (list == null) {
			list = new TreeMap<String, VisualizationOption>();
			options.put(group, list);
		}
		VisualizationOption newOption = new VisualizationOption(name, variants);
		list.put(name, newOption);
		return newOption;
	}

	/**
	 * Get an option by group name and name
	 * 
	 * @param group
	 *            name of the group
	 * @param name
	 *            name of the option
	 * @return the corresponding VisualizationOption, null if it does not exist
	 */
	public synchronized VisualizationOption getOption(String group, String name) {
		Map<String, VisualizationOption> list = options.get(group);
		return list == null ? null : list.get(name);
	}

	/**
	 * Get an option by group name and name, create if not existing
	 * 
	 * @param group
	 *            name of the group
	 * @param name
	 *            name of the option
	 * @return the corresponding VisualizationOption (created new with no variants, if it does not exist yet)
	 */
	public synchronized VisualizationOption getOrCreateOption(String group, String name) {
		VisualizationOption option = getOption(group, name);
		if (option == null) {
			option = addOption(group, name);
		}
		return option;
	}

	/**
	 * Creates a menu for making choices
	 * 
	 * @return a JPanel object to be used for making choices
	 */
	public synchronized JPanel createMenuPanel(Color foreground, Color background, final JComponent repaintedComponent) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(background);
		GridBagConstraints gbc = new GridBagConstraints();
		int y = 0;
		for (Entry<String, Map<String, VisualizationOption>> e : options.entrySet()) {
			gbc.gridx = 0;
			gbc.gridy = y;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			y++;
			JLabel label = new JLabel(e.getKey());
			label.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
			label.setForeground(foreground);
			label.setBackground(background);
			panel.add(label, gbc);
			for (final VisualizationOption opt : e.getValue().values()) {
				gbc.gridx = 0;
				gbc.gridy = y;
				y++;
				final JCheckBox checkBox = new JCheckBox(opt.getName());
				final JComboBox comboBox = opt.hasVariants() ? new JComboBox(opt.getVariantNames()) : null;
				checkBox.setSelected(opt.isEnabled());
				checkBox.setForeground(foreground);
				checkBox.setBackground(background);
				checkBox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						opt.setEnabled(checkBox.isSelected());
						if (comboBox != null) {
							comboBox.setEnabled(checkBox.isSelected());
						}
						if (repaintedComponent != null) {
							repaintedComponent.repaint();
						}
					}
				});
				if (opt.hasVariants()) {
					gbc.gridx = 0;
					panel.add(checkBox, gbc);
					comboBox.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							opt.setChosenOption(comboBox.getSelectedIndex());
							if (repaintedComponent != null) {
								repaintedComponent.repaint();
							}
						}
					});
					comboBox.setSelectedIndex(opt.chosenVariantIndex());
					comboBox.setForeground(foreground);
					//comboBox.setBackground(background);
					gbc.gridx = 1;
					panel.add(comboBox, gbc);
				} else {
					gbc.gridx = 0;
					gbc.gridwidth = 2;
					panel.add(checkBox, gbc);
					gbc.gridwidth = 1;
				}
			}
		}
		panel.validate();
		return panel;
	}

	private void addDefaultOptions() {
		addOption(GROUP_GENERAL, OPTION_DISKS);
		addOption(GROUP_GENERAL, OPTION_DISK_SYMBOLS, DISK_SYMBOL_VARIANTS);
		addOption(GROUP_GENERAL, OPTION_FLOOR);
		addOption(GROUP_GENERAL, OPTION_WALLS);
		addOption(GROUP_GENERAL, OPTION_GRID, GRID_VARIANTS);
		addOption(GROUP_GENERAL, OPTION_SKELETON);
		addOption(GROUP_GENERAL, OPTION_COLLISIONS);
		addOption(GROUP_GENERAL, OPTION_TIME);
	}

	public static final void main(String args[]) {
		VisualizationOptions vo = new VisualizationOptions();
		vo.addDefaultOptions();
		JFrame frame = new JFrame();
		frame.add(vo.createMenuPanel(Color.BLUE, Color.YELLOW, null));
		frame.setVisible(true);
	}

}
