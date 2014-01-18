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
package org.dihedron.commons.functional;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Funto'
 */
@SuppressWarnings("unchecked")
public class FunctionalTest {
	/**
	 * The logger.
	 */
	private final static Logger logger = LoggerFactory.getLogger(FunctionalTest.class);

	private static final int MAX_TEST_INTEGER = 1000;
	
	/**
	 * Test method for {@link org.dihedron.commons.functional.Functional#functionalList(java.util.List)}.
	 */
	@Test
	public void testFunctionalList() {
		Functional<Integer> fx = Functional.functionalList(new ArrayList<Integer>());
		
		for(int i = 1; i <= MAX_TEST_INTEGER; ++i) {
			((List<Integer>)fx).add(i);
		}
		
		int sum = fx.forEach(new $<Integer, Integer>() {
			public Integer _(Integer element, Integer sum) {
				if(sum == null) sum = new Integer(0);
				sum = sum + element;
				return sum;
			}			
		});		
		int result = ((MAX_TEST_INTEGER + 1) * MAX_TEST_INTEGER) / 2;
		
		logger.trace("returned: {} (expected {})", sum, result);
		assertTrue(sum == result);
	}
	
	/**
	 * Test method for {@link org.dihedron.commons.functional.Functional#functionalList(java.util.List)}.
	 */
	@Test
	public void testFunctionalSet() {
		Functional<Integer> fx = Functional.functionalSet(new HashSet<Integer>());
		
		for(int i = 1; i <= MAX_TEST_INTEGER; ++i) {
			((Set<Integer>)fx).add(i);
		}
		
		int sum = fx.forEach(new $<Integer, Integer>() {
			public Integer _(Integer element, Integer sum) {
				if(sum == null) sum = new Integer(0);
				sum = sum + element;
				return sum;
			}			
		});		
		int result = ((MAX_TEST_INTEGER + 1) * MAX_TEST_INTEGER) / 2;
		
		logger.trace("returned: {} (expected {})", sum, result);
		assertTrue(sum == result);
	}
	
}