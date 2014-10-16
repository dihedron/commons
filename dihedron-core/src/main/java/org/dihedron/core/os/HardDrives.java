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
package org.dihedron.core.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dihedron.core.regex.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to get the list of drives (mount points) in a platform independent fashion.
 * 
 * @author Andrea Funto'
 */
public final class HardDrives {
	/**
	 * The logger.
	 */
	private final static Logger logger = LoggerFactory.getLogger(HardDrives.class);
	
	/**
	 * Retrieves a list of all mounted file systems (or drives on Windows) in
	 * a platform-independent fashion.
	 * 
	 * @param
	 *   the types of file systems to retrieve; only applicable on UNIX machines.
	 * @return
	 *   the list of mounted file systems.
	 */
	public static List<File> listAll(String ... fstypes) {
		
		List<File> drives = null; 
		switch(Platform.getCurrent()) {
		case WINDOWS_32:
		case WINDOWS_64:
			drives = listWindowsFileSystems();
		case LINUX_32:
		case LINUX_64:
		case UNIX_32:
		case UNIX_64:
			try {
				drives = listUnixFileSystems(fstypes);
			} catch (IOException e) {
				logger.error("error launching external mount command", e);
			}
			break;
		default:
			// TODO: what shall we do with MacOS-X? Is it a UNIX flavour in this respect?
			logger.error("unsupported platform: '{}'", Platform.getCurrent());
			break;
		}
		
		return drives;
	}
	
	/**
	 * Returns the list of Windows mounted drives.
	 * 
	 * @return
	 *  the list of Windows mounted drives.
	 */
	public static List<File> listWindowsFileSystems() {
		return Arrays.asList(File.listRoots());		
	}
	
	/**
	 * Retrieves the list of filesystems on UNIX-like boxes, by running the "mount" 
	 * shell command and parsing its output; all supported filesystem types are returned
	 * unless a list of filesystem types is provided. NOTE: no safety check is performed
	 * on the filesystem types: do not use user input values unless checked by you.
	 * 
	 * @param fstypes
	 *   an optional list of file system types (e.g. "ext3", "ntfs", "vfat"...); if 
	 *   provided, only filesystems of those types will be returned.
	 * @return
	 *   a list of mounted file systems.
	 * @throws IOException
	 */
	public static List<File> listUnixFileSystems(String ... fstypes) throws IOException {
		
		List<File> drives = new ArrayList<>();
		
		// we may want to get only some filesystem types
		StringBuilder command = new StringBuilder("mount");
		if(fstypes != null) {
			command.append(" -t ");
			boolean first = true;
			for(String fstype : fstypes) {
				if(!first) {
					command.append(",").append(fstype);
				}
				first = false;
			}
		}
		logger.trace("running command '{}'", command);
	    Process process = Runtime.getRuntime().exec(command.toString());
	    BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    
	    Regex regex = new Regex("^\\s*(.*)\\s+(?:on)\\s+(.*)\\s+(?:type)\\s+(.*)\\s+(?:\\(.*\\))\\s*$");
	    
	    String line = null;	    
	    while((line = output.readLine()) != null) {
	    	List<String[]> matches = regex.getAllMatches(line);
	    	if(matches != null && matches.size() > 0) {
	    		for(String [] group : matches) {
	    			logger.trace("found file system '{}' mounted at '{}' (type '{}')", group[0], group[1], group[2]);
	    			drives.add(new File(group[1])); 
	    		}
	    	}
	    }
	    output.close();
	    return drives;
	}
		
	/**
	 * Private constructor, to prevent improper instantiation.
	 */
	private HardDrives() {
		// TODO Auto-generated constructor stub
	}
}