/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.populators;

public class StrictBooleanPopulator extends BaseStringPopulator<Boolean> {

	public static final StrictBooleanPopulator POPULATOR = new StrictBooleanPopulator();

	public static StrictBooleanPopulator getPopulator() {

		return POPULATOR;
	}

	@Override
	public Boolean getValue(String value) {

		if (value == null) {

			return null;
		}

		if (value.equalsIgnoreCase("true")) {

			return true;
		}

		if (value.equalsIgnoreCase("false")) {

			return false;
		}

		return null;
	}

	@Override
	public boolean validateDefaultFormat(String value) {

		return getValue(value) != null;
	}

	@Override
	public Class<? extends Boolean> getType() {

		return Boolean.class;
	}
}
