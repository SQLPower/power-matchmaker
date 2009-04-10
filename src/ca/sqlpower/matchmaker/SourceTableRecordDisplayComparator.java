/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

import java.util.Comparator;

/**
 * This comparator compares {@link SourceTableRecord}s based on each of their
 * display values in turn. If a display value component cannot be compared
 * between the two records (because of a type mismatch) then it will be skipped.
 * If no display values are comparable then the two records will be considered
 * equal. If one source table record has fewer display values then it will be
 * less than the other if their set of display values are the same.
 */
public class SourceTableRecordDisplayComparator implements Comparator<SourceTableRecord> {

	public int compare(SourceTableRecord str0, SourceTableRecord str1) {
		for (int i = 0; i < str0.getDisplayValues().size(); i++) {
			if (str1.getDisplayValues().size() == i) {
				return 1;
			}
			Object str0Value = str0.getDisplayValues().get(i);
			Object str1Value = str1.getDisplayValues().get(i);
			
			if (str0Value == null && str1Value == null) {
			    continue;
			} else if (str1Value == null) {
				return -1;
			} else if (str0Value == null) {
				return 1;
			}
			
			if (!str0Value.getClass().equals(str1Value.getClass())) {
				continue;
			}
			
			if (str0Value instanceof Comparable) {
				int retVal = ((Comparable) str0Value).compareTo(str1Value);
				if (retVal == 0) {
					continue;
				}
				return retVal;
			}
		}
		if (str0.getDisplayValues().size() < str1.getDisplayValues().size()) {
			return -1;
		}
		return 0;
	}

}
