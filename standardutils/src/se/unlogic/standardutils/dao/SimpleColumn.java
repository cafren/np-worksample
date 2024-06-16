/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import se.unlogic.standardutils.populators.QueryParameterPopulator;

public class SimpleColumn<BeanType,ColumnType> implements Column<BeanType,ColumnType> {

	private final Field beanField;
	private final Method queryMethod;
	private final QueryParameterPopulator<?> queryParameterPopulator;
	private final String columnName;
	private final boolean autoGenerated;

	public SimpleColumn(Field beanField, Method queryMethod, QueryParameterPopulator<?> queryPopulator, String columnName, boolean autoGenerated) {
		super();
		this.beanField = beanField;
		this.queryMethod = queryMethod;
		this.columnName = columnName;
		this.autoGenerated = autoGenerated;
		this.queryParameterPopulator = queryPopulator;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ColumnType getParamValue(Object paramValue) {

		return (ColumnType) paramValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ColumnType getBeanValue(BeanType bean) {

		try {
			return (ColumnType) beanField.get(bean);

		} catch (IllegalArgumentException e) {

			throw new RuntimeException(e);

		} catch (IllegalAccessException e) {

			throw new RuntimeException(e);
		}
	}

	@Override
	public Field getBeanField(){

		return beanField;
	}

	@Override
	public Class<?> getParamType(){

		return beanField.getType();
	}

	@Override
	public Method getQueryMethod() {
		return queryMethod;
	}

	@Override
	public String getColumnName() {
		return columnName;
	}

	@Override
	public boolean isAutoGenerated() {
		return autoGenerated;
	}

	@Override
	public QueryParameterPopulator<?> getQueryParameterPopulator() {
		return queryParameterPopulator;
	}

	public static <BT,CT> SimpleColumn<BT,CT> getGenericInstance(Class<BT> beanClass, Class<CT> fieldClass, Field beanField, Method queryMethod, QueryParameterPopulator<?> queryPopulator, String columnName, boolean autoGenerated) {

		return new SimpleColumn<BT, CT>(beanField, queryMethod, queryPopulator, columnName, autoGenerated);
	}
}
