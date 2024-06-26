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
import java.sql.Timestamp;
import java.text.ParseException;

import se.unlogic.standardutils.dao.BeanResultSetPopulator;
import se.unlogic.standardutils.date.DateUtils;
import se.unlogic.standardutils.date.ThreadSafeDateFormat;
import se.unlogic.standardutils.validation.StringFormatValidator;

public class TimeStampPopulator extends BaseStringPopulator<Timestamp> implements BeanResultSetPopulator<Timestamp> {

	private final ThreadSafeDateFormat dateFormat;

	private static final TimeStampPopulator POPULATOR = new TimeStampPopulator();

	public TimeStampPopulator(){

		this.dateFormat = DateUtils.DATE_TIME_FORMATTER;
	}

	public TimeStampPopulator(ThreadSafeDateFormat dateFormat) {

		this.dateFormat = dateFormat;
	}

	public TimeStampPopulator(String populatorID, ThreadSafeDateFormat dateFormat) {

		super(populatorID);

		this.dateFormat = dateFormat;
	}

	public TimeStampPopulator(String populatorID, ThreadSafeDateFormat dateFormat, StringFormatValidator formatValidator) {

		super(populatorID,formatValidator);
		this.dateFormat = dateFormat;
	}

	@Override
	public Class<? extends Timestamp> getType() {

		return Timestamp.class;
	}

	@Override
	public Timestamp getValue(String value) {

		try {
			return new Timestamp(this.dateFormat.parse(value).getTime());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean validateDefaultFormat(String value) {

		return DateUtils.isValidDate(this.dateFormat, value);
	}

	public static TimeStampPopulator getPopulator(){
		return POPULATOR;
	}

	@Override
	public Timestamp populate(ResultSet rs) throws SQLException {

		return rs.getTimestamp(1);


	}
}
