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
package org.dihedron.patterns;

import org.dihedron.core.library.Library;
import org.dihedron.core.library.Traits;


/**
 * @author Andrea Funto'
 */
public class PatternsLibrary extends Library {
		
	/**
	 * Returns the value of the give trait.
	 * 
	 * @param trait
	 *   the trait to retrieve.
	 * @return
	 *   the value of the trait.
	 */
	public static String valueOf(Traits trait) {
		synchronized(PatternsLibrary.class) {
			if(singleton == null) {
				singleton = new PatternsLibrary();
			}}
		return singleton.get(trait);
	}
	
	/**
	 * The name of the library.
	 */
	private static final String LIBRARY_NAME = "pattern";
	
	/**
	 * The single instance.
	 */
	private static PatternsLibrary singleton = new PatternsLibrary();

	/**
	 * Constructor.
	 */
	private PatternsLibrary() {
		super(LIBRARY_NAME);
	}
}