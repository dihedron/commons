/**
 * Copyright (c) 2012-2014, Andrea Funto'. All rights reserved.
 * 
 * This file is part of the Dihedron Common Utilities library ("Commons").
 *
 * "Commons" is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version.
 *
 * "Commons" is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with "Commons". If not, see <http://www.gnu.org/licenses/>.
 */
package org.dihedron.commons.filters.compound;

import org.dihedron.commons.filters.Filter;

/**
 * @author Andrea Funto'
 */
public class Or<T> extends CompoundFilter<T> {
	
	@SafeVarargs
	public Or(Filter<T> ... filters) {
		super(filters);
	}

	/**
	 * @see org.dihedron.commons.filters.Filter#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(T object) {
		boolean matched = false;
		for(Filter<T> filter : getSubFilters()) {
			matched = matched || filter.matches(object);
			if(matched) {
				return true;
			}
		}
		return false;
	}
}
