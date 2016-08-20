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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Object for holding visualisation choices, possibly with variants. A visualisation option can be enabled or disabled, if enabled there may or may not be multiple variants that can be selected.
 * 
 * For instance an option to display vision sensors could have the variants "show direction" and "show receptive field".
 */
public class VisualizationOption {

	private final String name;
	private String variants[];
	private AtomicBoolean enabled;
	private AtomicInteger chosenVariant;

	/**
	 * Object for holding visualisation choices.
	 * 
	 * @param name
	 *            the name of the option, to be displayed in menus
	 * @param variants
	 *            the possible choices (may be null if there are not multiple variants for this option)
	 */
	public VisualizationOption(String name, String variants[]) {
		this.name = name;
		this.variants = variants;
		enabled = new AtomicBoolean(true);
		chosenVariant = new AtomicInteger(0);
	}

	/**
	 * Set the variants array
	 * 
	 * @param variants
	 */
	public void setVariants(String[] variants) {
		this.variants = variants;
	}

	/**
	 * Provides the variant name (to be displayed in a menu)
	 * 
	 * @return name of the variant
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the enabled property
	 * 
	 * @param enabled
	 *            true if this option shall be enabled
	 */
	public synchronized void setEnabled(boolean enabled) {
		this.enabled.set(enabled);
	}

	/**
	 * Set which variant shall be active
	 * 
	 * @param variantIndex
	 *            number in 0..variants.length-1
	 */
	public synchronized void setChosenOption(int variantIndex) {
		if (variants == null)
			throw new RuntimeException("Option has no variants");
		if ((variantIndex < 0) || (variantIndex >= variants.length))
			throw new IllegalArgumentException("illegal variant index");
		chosenVariant.set(variantIndex);
	}

	/**
	 * Indicates if the option is enabled
	 * 
	 * @return enabled property
	 */
	public synchronized boolean isEnabled() {
		return enabled.get();
	}

	/**
	 * Indicates which variant is selected
	 * 
	 * @return index of the variant
	 */
	public synchronized int chosenVariantIndex() {
		if (variants == null)
			throw new RuntimeException("Option has no variants");
		return chosenVariant.get();
	}

	/**
	 * Indicates which variant is selected
	 * 
	 * @return name of the variant
	 */
	public synchronized String chosenVariantString() {
		return variants[chosenVariant.get()];
	}

	/**
	 * Indicates if this option has multiple variants
	 * 
	 * @return true if there are multiple variants
	 */
	public synchronized boolean hasVariants() {
		return variants != null;
	}

	/**
	 * Indicates which variants are possible
	 * 
	 * @return array containing the names of the possible variant (or null if no variants)
	 */
	public synchronized String[] getVariantNames() {
		return variants;
	}

	/**
	 * Indicates whether the option is enabled and the specified variant is chosen.
	 * 
	 * @param variant
	 *            name of the variant to be tested
	 * @return true if the option is enabled and the specified variant is currently selected, false if there are no variants
	 */
	public synchronized boolean isVariantChosen(String variant) {
		return (variants != null) && enabled.get() && chosenVariantString().equals(variant);
	}

}
