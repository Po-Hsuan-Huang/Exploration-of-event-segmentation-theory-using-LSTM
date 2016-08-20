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
package diskworld.collisions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A map that uses an undordered pair as key.
 * So for example after calling put(k1,k2,v), the method get(k2,k1) will return v.
 * The implementation is based on a HashMap with HashMaps as values.
 * 
 * @author Jan
 * 
 * @param <K>
 *            type used for keys
 * @param <V>
 *            type used for values
 */
class UnorderedPairMap<K, V> {

	private Map<K, Map<K, V>> map;

	public UnorderedPairMap() {
		map = new HashMap<K, Map<K, V>>();
	}

	public void put(K key1, K key2, V value) {
		if (!sortedModify(key1, key2, value)) {
			if (!sortedModify(key2, key1, value)) {
				sortedPut(key1, key2, value);
			}
		}
	}

	public void sortedPut(K key1, K key2, V value) {
		Map<K, V> m = map.get(key1);
		if (m == null) {
			m = new HashMap<K, V>();
			map.put(key1, m);
		}
		m.put(key2, value);
	}

	public V get(K key1, K key2) {
		V res = sortedGet(key1, key2);
		return res == null ? sortedGet(key2, key1) : res;
	}

	public boolean contains(K key1, K key2) {
		return sortedContains(key1, key2) || sortedContains(key2, key1);
	}

	public boolean sortedModify(K key1, K key2, V value) {
		Map<K, V> m = map.get(key1);
		if (m == null) {
			return false;
		} else {
			if (m.containsKey(key2)) {
				m.put(key2, value);
				return true;
			}
			return false;
		}
	}

	private boolean sortedContains(K key1, K key2) {
		Map<K, V> m = map.get(key1);
		return m == null ? false : m.containsKey(key2);
	}

	public V sortedGet(K key1, K key2) {
		Map<K, V> m = map.get(key1);
		return m == null ? null : m.get(key2);
	}

	public interface ElementMethod<K, V> {
		void forEach(K key1, K key2, V value);
	}

	public void doForAll(ElementMethod<K, V> method) {
		for (Entry<K, Map<K, V>> e1 : map.entrySet()) {
			K key1 = e1.getKey();
			for (Entry<K, V> e2 : e1.getValue().entrySet()) {
				K key2 = e2.getKey();
				method.forEach(key1, key2, e2.getValue());
			}
		}
	}

	public void clear() {
		map.clear();
	}

}
