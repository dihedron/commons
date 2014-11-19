/**
 * Copyright (c) 2012-2014, Andrea Funto'. All rights reserved. See LICENSE for details.
 */ 
package org.dihedron.core.streams;

import java.io.IOException;

/**
 * @author Andrea Funto'
 */
public abstract class AbstractStreamsTest {

	protected String makeString(int iterations) {
		StringBuilder buffer = new StringBuilder("a test string");
		for(int i = 0; i < iterations; ++i) {
			buffer.append(", and another test string");
		}
		return buffer.toString();		
	}
	
	protected byte [] makeByteArray(int number) throws IOException {
		byte[] array = new byte[number];
		try(RandomInputStream input = new SecureRandomInputStream(Byte.MAX_VALUE)) {
			for(int i = 0; i < number; ++i) {
				array[i] = (byte)input.read();
			}
		}
		
		return array;
	}
}
