/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.populators;

import java.sql.ResultSet;
import java.sql.SQLException;

import se.unlogic.standardutils.dao.BeanResultSetPopulator;
import se.unlogic.standardutils.validation.StringFormatValidator;

public class StringPopulator extends BaseStringPopulator<String> implements BeanResultSetPopulator<String>, BeanStringPopulator<String>{

	public StringPopulator() {
		super();
	}

	public StringPopulator(StringFormatValidator validator) {

		super(null, validator);
	}

	public StringPopulator(String populatorID, StringFormatValidator validator) {

		super(populatorID, validator);
	}

	private static final StringPopulator POPULATOR = new StringPopulator();

	@Override
	public String populate(ResultSet rs) throws SQLException {
		return rs.getString(1);
	}

	public static StringPopulator getPopulator(){
		return POPULATOR;
	}

	@Override
	public String getValue(String value) {
		return value;
	}

	@Override
	public boolean validateDefaultFormat(String value) {

		return true;
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}
}
