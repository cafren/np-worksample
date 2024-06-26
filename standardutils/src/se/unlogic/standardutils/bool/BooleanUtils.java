/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.bool;

public class BooleanUtils {

	public static boolean valueOf(Boolean bool) {

		if (bool == null) {
			
			return false;
			
		} else {
			
			return bool;
		}
	}

	public static boolean toBoolean(String bool) {

		if (bool == null) {

			return false;
		}

		return Boolean.parseBoolean(bool);
	}
	
	public static boolean isFalse(Boolean... bools) {

		return isAll(false, bools);
	}
	
	public static boolean isTrue(Boolean... bools) {

		return isAll(true, bools);
	}
	
	public static boolean isAll(boolean wantedValue, Boolean... bools) {

		if (bools == null) {
			
			return false;
		}
		
		for (Boolean bool : bools) {
			
			if (bool == null) {
				
				return false;
				
			} else if (bool == !wantedValue){
				
				return false;
			}
		}
		
		return true;
	}
}
