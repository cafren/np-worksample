/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.validation;

import java.io.Serializable;

import se.unlogic.standardutils.numbers.NumberUtils;


public class StringIntegerValidator extends StringNumberValidator<Integer> implements Serializable {

	private static final StringIntegerValidator INSTANCE = new StringIntegerValidator();
	private static final long serialVersionUID = 1724653805439617682L;
	
	public static StringIntegerValidator getInstance() {
		return INSTANCE;
	}

	public StringIntegerValidator() {
		super(null, null);
	}

	public StringIntegerValidator(Integer minValue,Integer maxValue) {
		super(minValue,maxValue);
	}

	@Override
	public boolean validateFormat(String value) {

		Integer numberValue = getIntegerValue(value);

		if(numberValue == null){

			return false;

		}else if(maxValue != null && minValue != null){

			return numberValue <= maxValue && numberValue >= minValue;

		}else if(maxValue != null){

			return numberValue <= maxValue;

		}else if(minValue != null){

			return numberValue >= minValue;
		}
		else{
			return true;
		}
	}

	protected Integer getIntegerValue(String value) {

		return NumberUtils.toInt(value);
	}
}
