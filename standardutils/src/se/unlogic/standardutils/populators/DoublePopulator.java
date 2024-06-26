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
import se.unlogic.standardutils.numbers.NumberUtils;
import se.unlogic.standardutils.validation.StringFormatValidator;

public class DoublePopulator extends BaseStringPopulator<Double> implements BeanResultSetPopulator<Double>, BeanStringPopulator<Double> {
	
	private static final DoublePopulator POPULATOR = new DoublePopulator();
	
	public static DoublePopulator getPopulator(){
		return POPULATOR;
	}

	public DoublePopulator() {
		super();
	}

	public DoublePopulator(String populatorID, StringFormatValidator formatValidator) {
		super(populatorID, formatValidator);
	}

	public DoublePopulator(String populatorID) {
		super(populatorID);
	}

	@Override
	public Double populate(ResultSet rs) throws SQLException {

		return rs.getDouble(1);
	}

	@Override
	public Double getValue(String value) {

		value = value.replace(',', '.');
		
		return Double.valueOf(value);
	}

	@Override
	public boolean validateDefaultFormat(String value) {

		return NumberUtils.isDoubleLocaleIndependent(value);
	}

	@Override
	public Class<? extends Double> getType() {

		return Double.class;
	}

}
