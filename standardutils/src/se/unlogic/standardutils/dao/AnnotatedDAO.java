/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.sql.DataSource;

import se.unlogic.standardutils.annotations.UnsupportedFieldTypeException;
import se.unlogic.standardutils.bool.BooleanSignal;
import se.unlogic.standardutils.dao.annotations.DAOManaged;
import se.unlogic.standardutils.dao.annotations.Key;
import se.unlogic.standardutils.dao.annotations.ManyToMany;
import se.unlogic.standardutils.dao.annotations.ManyToOne;
import se.unlogic.standardutils.dao.annotations.OneToMany;
import se.unlogic.standardutils.dao.annotations.OneToOne;
import se.unlogic.standardutils.dao.annotations.OrderBy;
import se.unlogic.standardutils.dao.annotations.SimplifiedRelation;
import se.unlogic.standardutils.dao.annotations.Table;
import se.unlogic.standardutils.dao.querys.ArrayListQuery;
import se.unlogic.standardutils.dao.querys.BooleanQuery;
import se.unlogic.standardutils.dao.querys.ObjectQuery;
import se.unlogic.standardutils.dao.querys.PreparedStatementQuery;
import se.unlogic.standardutils.dao.querys.UpdateQuery;
import se.unlogic.standardutils.datatypes.SimpleEntry;
import se.unlogic.standardutils.db.DBUtils;
import se.unlogic.standardutils.enums.Order;
import se.unlogic.standardutils.numbers.IntegerCounter;
import se.unlogic.standardutils.populators.BeanStringPopulator;
import se.unlogic.standardutils.populators.EnumPopulator;
import se.unlogic.standardutils.populators.IntegerPopulator;
import se.unlogic.standardutils.populators.QueryParameterPopulator;
import se.unlogic.standardutils.populators.QueryParameterPopulatorRegistery;
import se.unlogic.standardutils.populators.annotated.AnnotatedResultSetPopulator;
import se.unlogic.standardutils.reflection.ReflectionUtils;
import se.unlogic.standardutils.string.StringUtils;

public class AnnotatedDAO<T> {

	private static final OrderByComparator ORDER_BY_COMPARATOR = new OrderByComparator();

	protected final AnnotatedResultSetPopulator<T> populator;
	protected final DataSource dataSource;
	protected final Class<T> beanClass;

	protected final List<QueryParameterPopulator<?>> queryParameterPopulators;

	protected final ArrayList<Column<T, ?>> simpleKeys = new ArrayList<Column<T, ?>>();
	protected final ArrayList<Column<T, ?>> simpleColumns = new ArrayList<Column<T, ?>>();
	protected final HashMap<Field, Column<T, ?>> columnMap = new HashMap<Field, Column<T, ?>>();

	protected final ArrayList<ColumnKeyCollector<T>> columnKeyCollectors = new ArrayList<ColumnKeyCollector<T>>();

	protected final HashMap<Field, ManyToOneRelation<T, ?, ?>> manyToOneRelations = new HashMap<Field, ManyToOneRelation<T, ?, ?>>();
	protected final HashMap<Field, ManyToOneRelation<T, ?, ?>> manyToOneRelationKeys = new HashMap<Field, ManyToOneRelation<T, ?, ?>>();
	protected final HashMap<Field, OneToManyRelation<T, ?>> oneToManyRelations = new HashMap<Field, OneToManyRelation<T, ?>>();
	protected final HashMap<Field, OneToOneRelation<T, ?>> oneToOneRelations = new HashMap<Field, OneToOneRelation<T, ?>>();
	protected final HashMap<Field, ManyToManyRelation<T, ?>> manyToManyRelations = new HashMap<Field, ManyToManyRelation<T, ?>>();

	protected final ArrayList<Entry<OrderBy, Column<T, ?>>> columnOrderList = new ArrayList<Entry<OrderBy, Column<T, ?>>>();

	protected final ArrayList<Field> autoAddRelations = new ArrayList<Field>();
	protected final ArrayList<Field> autoGetRelations = new ArrayList<Field>();
	protected final ArrayList<Field> autoUpdateRelations = new ArrayList<Field>();

	protected final ArrayList<SimpleColumn<T, ?>> dontUpdateIfNullCoulumns = new ArrayList<SimpleColumn<T, ?>>();

	protected String tableName;

	protected String insertSQL;
	protected String updateSQL;
	protected String deleteSQL;
	protected String checkIfExistsSQL;
	protected String deleteByFieldSQL;
	protected String getSQL;
	protected String booleanSQL;
	protected String defaultSortingCriteria;

	public AnnotatedDAO(DataSource dataSource, Class<T> beanClass, AnnotatedDAOFactory daoFactory) {

		this(dataSource, beanClass, daoFactory, new AnnotatedResultSetPopulator<T>(beanClass), null, null);
	}

	public AnnotatedDAO(DataSource dataSource, Class<T> beanClass, AnnotatedDAOFactory daoFactory, AnnotatedResultSetPopulator<T> populator, QueryParameterPopulator<?>... queryParameterPopulators) {

		this(dataSource, beanClass, daoFactory, populator, Arrays.asList(queryParameterPopulators), null);
	}

	public AnnotatedDAO(DataSource dataSource, Class<T> beanClass, AnnotatedDAOFactory daoFactory, List<? extends QueryParameterPopulator<?>> queryParameterPopulators, List<? extends BeanStringPopulator<?>> typePopulators) {

		this(dataSource, beanClass, daoFactory, new AnnotatedResultSetPopulator<T>(beanClass, typePopulators), queryParameterPopulators, typePopulators);
	}

	public AnnotatedDAO(DataSource dataSource, Class<T> beanClass, AnnotatedDAOFactory daoFactory, AnnotatedResultSetPopulator<T> populator, List<? extends QueryParameterPopulator<?>> queryParameterPopulators, List<? extends BeanStringPopulator<?>> typePopulators) {

		super();
		this.populator = populator;
		this.dataSource = dataSource;
		this.beanClass = beanClass;

		if (queryParameterPopulators != null) {

			this.queryParameterPopulators = new ArrayList<QueryParameterPopulator<?>>(queryParameterPopulators);

		} else {

			this.queryParameterPopulators = null;
		}

		Table table = beanClass.getAnnotation(Table.class);

		if (table == null) {

			throw new RuntimeException("No @Table annotation found in  " + beanClass);
		} else {
			tableName = table.name();
		}

		this.tableName = table.name();

		List<Field> fields = ReflectionUtils.getFields(beanClass);

		int generatedKeyColumnIndex = 1;

		for (Field field : fields) {

			DAOManaged daoManaged = field.getAnnotation(DAOManaged.class);
			OrderBy orderBy = field.getAnnotation(OrderBy.class);

			if (daoManaged != null) {

				ReflectionUtils.fixFieldAccess(field);

				if (field.isAnnotationPresent(OneToOne.class)) {

					this.checkAutoGeneration(field, daoManaged);

					this.checkOrderByAnnotation(field, orderBy);

					OneToOne oneToOne = field.getAnnotation(OneToOne.class);

					//Find local key field
					Field localKeyField = DefaultOneToOneRelation.getKeyField(oneToOne, beanClass, field);

					this.oneToOneRelations.put(field, DefaultOneToOneRelation.getGenericInstance(beanClass, field.getType(), localKeyField.getType(), field, localKeyField, daoFactory, daoManaged));

					if (oneToOne.autoAdd()) {
						this.autoAddRelations.add(field);
					}

					if (oneToOne.autoUpdate()) {
						this.autoUpdateRelations.add(field);
					}

					if (oneToOne.autoGet()) {
						this.autoGetRelations.add(field);
					}

				} else if (field.isAnnotationPresent(OneToMany.class)) {

					OneToMany oneToMany = field.getAnnotation(OneToMany.class);

					this.checkOrderByAnnotation(field, orderBy);

					if (field.getType() != List.class) {

						throw new UnsupportedFieldTypeException("The annotated field " + field.getName() + " in  " + beanClass + " is of unsupported type " + field.getType() + ". Fields annotated as @OneToMany have to be a genericly typed " + List.class, field, OneToMany.class, beanClass);
					}

					if (ReflectionUtils.getGenericlyTypeCount(field) != 1) {

						throw new UnsupportedFieldTypeException("The annotated field " + field.getName() + " in  " + beanClass + " is genericly typed. Fields annotated as @OneToMany have to be a genericly typed " + List.class, field, OneToMany.class, beanClass);
					}

					// This is a bit ugly but still necessary until someone else
					// comes up with something smarter...
					Class<?> remoteClass = (Class<?>) ReflectionUtils.getGenericType(field);

					SimplifiedRelation simplifiedRelation = field.getAnnotation(SimplifiedRelation.class);

					if (simplifiedRelation != null) {

						this.oneToManyRelations.put(field, SimplifiedOneToManyRelation.getGenericInstance(beanClass, remoteClass, field, this, typePopulators, queryParameterPopulators));

					} else {

						// Use this class pks, no extra field
						this.oneToManyRelations.put(field, DefaultOneToManyRelation.getGenericInstance(beanClass, remoteClass, oneToMany.remoteField(), field, daoFactory, daoManaged));
					}

					if (oneToMany.autoAdd()) {
						this.autoAddRelations.add(field);
					}

					if (oneToMany.autoUpdate()) {
						this.autoUpdateRelations.add(field);
					}

					if (oneToMany.autoGet()) {
						this.autoGetRelations.add(field);
					}

				} else if (field.isAnnotationPresent(ManyToOne.class)) {

					this.checkAutoGeneration(field, daoManaged);

					ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);

					List<Field> remoteClassFields = ReflectionUtils.getFields(field.getType());

					Field matchingRemoteField = null;

					if (remoteClassFields != null) {

						for (Field remoteField : remoteClassFields) {

							if (remoteField.isAnnotationPresent(DAOManaged.class) && remoteField.isAnnotationPresent(OneToMany.class) && remoteField.getType() == List.class && ReflectionUtils.isGenericlyTyped(remoteField) && ((Class<?>) ReflectionUtils.getGenericType(remoteField) == this.beanClass)) {

								matchingRemoteField = remoteField;

								break;
							}
						}
					}

					if (matchingRemoteField == null) {

						throw new RuntimeException("No corresponding @OneToMany annotated field found in  " + field.getType() + " matching @ManyToOne relation of field " + field.getName() + " in  " + beanClass + "!");
					}

					Field remoteKeyField = null;

					if (!StringUtils.isEmpty(manyToOne.remoteKeyField())) {

						remoteKeyField = ReflectionUtils.getField(field.getType(), manyToOne.remoteKeyField());

						//TODO Check if the remote key field is @DAOManaged annotated
						if (remoteKeyField == null) {

							throw new RuntimeException("Unable to find @Key annotated field " + manyToOne.remoteKeyField() + " in " + field.getType() + " specified for @ManyToOne annotated field " + field.getName() + " in " + beanClass);
						}

					} else {

						for (Field remoteField : remoteClassFields) {

							if (remoteField.isAnnotationPresent(DAOManaged.class) && remoteField.isAnnotationPresent(Key.class)) {

								if (remoteKeyField != null) {

									throw new RuntimeException("Found multiple @Key annotated fields in " + field.getType() + ", therefore the remoteKeyField property needs to be specified for the @ManyToOne annotated field " + field.getName() + " in " + beanClass);
								}

								remoteKeyField = remoteField;
							}
						}

						if (remoteKeyField == null) {

							throw new RuntimeException("Unable to find @Key annotated field in " + field.getType() + " while parsing @ManyToOne annotated field " + field.getName() + " in " + beanClass);
						}
					}

					ManyToOneRelation<T, ?, ?> relation = null;

					if (field.isAnnotationPresent(Key.class)) {

						relation = getManyToOneRelationInstance(beanClass, field.getType(), remoteKeyField.getType(), field, remoteKeyField, daoManaged, daoFactory);

						manyToOneRelationKeys.put(field, relation);

					} else {

						relation = getManyToOneRelationInstance(beanClass, field.getType(), remoteKeyField.getType(), field, remoteKeyField, daoManaged, daoFactory);

						this.manyToOneRelations.put(field, relation);
					}

					this.columnMap.put(field, relation);

					if (orderBy != null) {
						this.columnOrderList.add(new SimpleEntry<OrderBy, Column<T, ?>>(orderBy, relation));
					}

					if (manyToOne.autoAdd()) {
						this.autoAddRelations.add(field);
					}

					if (manyToOne.autoUpdate()) {
						this.autoUpdateRelations.add(field);
					}

					if (manyToOne.autoGet()) {
						this.autoGetRelations.add(field);
					}

				} else if (field.isAnnotationPresent(ManyToMany.class)) {

					this.checkAutoGeneration(field, daoManaged);

					this.checkOrderByAnnotation(field, orderBy);

					if (field.getType() != List.class) {

						throw new UnsupportedFieldTypeException("The annotated field " + field.getName() + " in  " + beanClass + " is of unsupported type " + field.getType() + ". Fields annotated as @ManyToMany have to be a genericly typed " + List.class, field, ManyToMany.class, beanClass);
					}

					if (ReflectionUtils.getGenericlyTypeCount(field) != 1) {

						throw new UnsupportedFieldTypeException("The annotated field " + field.getName() + " in  " + beanClass + " is genericly typed. Fields annotated as @ManyToMany have to be a genericly typed " + List.class, field, ManyToMany.class, beanClass);
					}

					// This is a bit ugly but still necessary until someone else
					// comes up with something smarter...
					Class<?> remoteClass = (Class<?>) ReflectionUtils.getGenericType(field);

					this.manyToManyRelations.put(field, DefaultManyToManyRelation.getGenericInstance(beanClass, remoteClass, field, daoFactory, daoManaged));

					ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);

					if (manyToMany.autoAdd()) {
						this.autoAddRelations.add(field);
					}

					if (manyToMany.autoUpdate()) {
						this.autoUpdateRelations.add(field);
					}

					if (manyToMany.autoGet()) {
						this.autoGetRelations.add(field);
					}

				} else {

					QueryParameterPopulator<?> queryPopulator = this.getQueryParameterPopulator(field.getType());

					Method method = null;

					if (queryPopulator == null) {

						method = PreparedStatementQueryMethods.getQueryMethod(field.getType());

						if (method == null && field.getType().isEnum()) {

							queryPopulator = EnumPopulator.getInstanceFromField(field);
						}

						if (method == null && queryPopulator == null) {

							throw new RuntimeException("No query method or query parameter populator found for @DAOManaged annotate field " + field.getName() + " in  " + beanClass);
						}
					}

					String columnName = daoManaged.columnName();

					if (StringUtils.isEmpty(columnName)) {

						columnName = field.getName();
					}

					SimpleColumn<T, ?> simpleColumn = null;

					Key primaryKey = field.getAnnotation(Key.class);

					if (primaryKey != null) {

						simpleColumn = SimpleColumn.getGenericInstance(beanClass, field.getType(), field, method, queryPopulator, columnName, daoManaged.autoGenerated());

						this.simpleKeys.add(simpleColumn);

					} else {

						simpleColumn = SimpleColumn.getGenericInstance(beanClass, field.getType(), field, method, queryPopulator, columnName, daoManaged.autoGenerated());

						this.simpleColumns.add(simpleColumn);

						if (daoManaged.dontUpdateIfNull()) {

							dontUpdateIfNullCoulumns.add(simpleColumn);
						}
					}

					this.columnMap.put(field, simpleColumn);

					if (daoManaged.autoGenerated()) {

						if (daoManaged.autGenerationColumnIndex() != 0) {

							this.columnKeyCollectors.add(new ColumnKeyCollector<T>(field, populator, daoManaged.autGenerationColumnIndex()));

						} else {

							this.columnKeyCollectors.add(new ColumnKeyCollector<T>(field, populator, generatedKeyColumnIndex));

							generatedKeyColumnIndex++;
						}
					}

					if (orderBy != null) {
						this.columnOrderList.add(new SimpleEntry<OrderBy, Column<T, ?>>(orderBy, simpleColumn));
					}
				}
			}
		}

		if (this.simpleKeys.isEmpty() && this.manyToOneRelationKeys.isEmpty()) {

			throw new RuntimeException("No @Key annotated field found in  " + beanClass + "!");
		}

		// Genearate SQL statements
		this.generateInsertSQL();
		this.generateUpdateSQL();
		this.generateDeleteSQL();
		this.generateCheckIfExistsSQL();
		this.generateDeleteByFieldSQL();
		this.generateDefaultGetSQL();
		this.generateDefaultSortingCriteria();
		this.generateBooleanSQL();
	}

	public <LT, RT, RKT> ManyToOneRelation<LT, RT, RKT> getManyToOneRelationInstance(Class<LT> beanClass, Class<RT> remoteClass, Class<RKT> remoteKeyClass, Field field, Field remoteField, DAOManaged daoManaged, AnnotatedDAOFactory daoFactory) {

		return DefaultManyToOneRelation.getGenericInstance(beanClass, remoteClass, remoteKeyClass, field, remoteField, daoManaged, daoFactory);
	}

	public DataSource getDataSource() {

		return dataSource;
	}

	private void checkAutoGeneration(Field field, DAOManaged daoManaged) {

		if (daoManaged.autoGenerated()) {

			throw new RuntimeException("Invalid @DAOManaged annotation on field " + field.getName() + " in " + this.beanClass + ", fields with relations cannot be auto generated!");
		}
	}

	@SuppressWarnings("unchecked")
	public <QPT> QueryParameterPopulator<QPT> getQueryParameterPopulator(Class<QPT> type) {

		if (queryParameterPopulators != null) {

			for (QueryParameterPopulator<?> queryParameterPopulator : queryParameterPopulators) {

				if (type.equals(queryParameterPopulator.getType())) {

					return (QueryParameterPopulator<QPT>) queryParameterPopulator;
				}
			}
		}

		for (QueryParameterPopulator<?> queryParameterPopulator : QueryParameterPopulatorRegistery.getQueryParameterPopulators()) {

			if (type.equals(queryParameterPopulator.getType())) {

				return (QueryParameterPopulator<QPT>) queryParameterPopulator;
			}
		}

		return null;
	}

	private void checkOrderByAnnotation(Field field, OrderBy orderBy) {

		if (orderBy != null) {

			throw new RuntimeException("Invalid @OrderBy annotation on field " + field.getName() + " in " + this.beanClass + ", the @OrderBy annotation is not allowed on @OneToOne, @OneToMany and @ManyToMany annotated fields.");
		}
	}

	private void generateDefaultSortingCriteria() {

		if (!this.columnOrderList.isEmpty()) {

			Collections.sort(columnOrderList, ORDER_BY_COMPARATOR);

			StringBuilder stringBuilder = new StringBuilder(" ORDER BY ");

			boolean first = true;

			for (Entry<OrderBy, Column<T, ?>> entry : this.columnOrderList) {

				if (first) {

					first = false;

				} else {

					stringBuilder.append(", ");
				}

				stringBuilder.append(entry.getValue().getColumnName() + " " + entry.getKey().order().toString());
			}

			this.defaultSortingCriteria = stringBuilder.toString();

		} else {

			this.defaultSortingCriteria = "";
		}
	}

	protected void generateInsertSQL() {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("INSERT INTO " + this.tableName + " (");

		boolean first = true;

		for (Column<T, ?> column : this.columnMap.values()) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append(", ");
			}

			stringBuilder.append(column.getColumnName());
		}

		stringBuilder.append(") VALUES (");

		first = true;

		for (@SuppressWarnings("unused")
		Column<T, ?> column : this.columnMap.values()) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append(", ");
			}

			stringBuilder.append("?");
		}

		stringBuilder.append(")");

		this.insertSQL = stringBuilder.toString();
	}

	protected void generateUpdateSQL() {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("UPDATE " + this.tableName + " SET ");

		boolean first = true;

		for (Column<T, ?> column : this.columnMap.values()) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append(", ");
			}

			stringBuilder.append(column.getColumnName() + " = ?");
		}

		stringBuilder.append(" WHERE ");

		this.appendPrimaryKeyWhereStatement(stringBuilder);

		this.updateSQL = stringBuilder.toString();
	}

	protected void generateCheckIfExistsSQL() {

		StringBuilder stringBuilder = new StringBuilder("SELECT 1 FROM " + tableName + " WHERE ");

		this.appendPrimaryKeyWhereStatement(stringBuilder);

		this.checkIfExistsSQL = stringBuilder.toString();
	}

	private void appendPrimaryKeyWhereStatement(StringBuilder stringBuilder) {

		boolean first = true;

		for (Column<T, ?> column : this.simpleKeys) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append(" AND ");
			}

			stringBuilder.append(column.getColumnName() + " = ?");
		}

		for (Column<T, ?> column : this.manyToOneRelationKeys.values()) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append(" AND ");
			}

			stringBuilder.append(column.getColumnName() + " = ?");
		}
	}

	protected void generateDeleteSQL() {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("DELETE FROM ");
		stringBuilder.append(tableName);
		stringBuilder.append(" WHERE ");

		this.appendPrimaryKeyWhereStatement(stringBuilder);

		this.deleteSQL = stringBuilder.toString();
	}

	protected void generateDeleteByFieldSQL() {

		this.deleteByFieldSQL = "DELETE FROM " + tableName;
	}

	protected void generateBooleanSQL() {

		this.booleanSQL = "SELECT 1 FROM " + tableName;
	}

	protected void generateDefaultGetSQL() {

		this.getSQL = "SELECT * FROM " + tableName;
	}

	protected String generateGetSQL(HighLevelQuery<T> query) {

		if (!RelationQuery.hasExcludedFields(query)) {

			return getSQL;
		}

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("SELECT ");

		boolean first = true;

		for (Column<T, ?> column : this.columnMap.values()) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append(", ");
			}

			if (query.containsExcludedField(column.getBeanField())) {

				stringBuilder.append("NULL AS " + column.getColumnName());

			} else {

				stringBuilder.append(column.getColumnName());
			}
		}

		stringBuilder.append(" FROM " + tableName);

		return stringBuilder.toString();
	}

	protected String generateUpdateSQL(RelationQuery query, T bean) {

		if (!RelationQuery.hasExcludedFields(query) && dontUpdateIfNullCoulumns.isEmpty()) {

			return updateSQL;
		}

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("UPDATE " + this.tableName + " SET ");

		boolean first = true;

		for (Column<T, ?> column : this.columnMap.values()) {

			if ((query != null && query.containsExcludedField(column.getBeanField())) || (dontUpdateIfNullCoulumns.contains(column) && column.getBeanValue(bean) == null)) {

				continue;
			}

			if (first) {

				first = false;

			} else {

				stringBuilder.append(", ");
			}

			stringBuilder.append(column.getColumnName() + " = ?");
		}

		stringBuilder.append(" WHERE ");

		this.appendPrimaryKeyWhereStatement(stringBuilder);

		return stringBuilder.toString();
	}

	public TransactionHandler createTransaction() throws SQLException {

		return new TransactionHandler(dataSource);
	}

	public void add(T bean, int retryCount) throws SQLException {

		while (true) {

			try {

				add(bean);

				return;

			} catch (SQLTransactionRollbackException e) {

				if (retryCount > 0) {

					retryCount--;

				} else {

					throw e;
				}
			}
		}
	}

	public void add(T bean) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.add(bean, transactionHandler.getConnection(), null);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void add(T bean, RelationQuery relationQuery) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.add(bean, transactionHandler.getConnection(), relationQuery);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void addAll(Collection<T> beans, RelationQuery relationQuery) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.addAll(beans, transactionHandler.getConnection(), relationQuery);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void add(T bean, TransactionHandler transactionHandler, RelationQuery relationQuery) throws SQLException {

		this.add(bean, transactionHandler.getConnection(), relationQuery);
	}

	public void addAll(List<T> beans, TransactionHandler transactionHandler, RelationQuery relationQuery) throws SQLException {

		this.addAll(beans, transactionHandler.getConnection(), relationQuery);
	}

	public void addAll(Collection<T> beans, Connection connection, RelationQuery relationQuery) throws SQLException {

		for (T bean : beans) {

			this.add(bean, connection, relationQuery);
		}
	}

	public void add(T bean, Connection connection, RelationQuery relationQuery) throws SQLException {

		this.preAddRelations(bean, connection, relationQuery);

		UpdateQuery query = null;

		try {
			query = new UpdateQuery(connection, false, this.insertSQL);

			IntegerCounter integerCounter = new IntegerCounter();

			setQueryValues(bean, query, null, integerCounter, this.columnMap.values(), false);

			this.executeUpdateQuery(query, bean);

			this.addRelations(bean, connection, relationQuery);

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	private void executeUpdateQuery(UpdateQuery query, T bean) throws SQLException {

		if (!this.columnKeyCollectors.isEmpty()) {

			query.executeUpdate(new ColumnKeyCollectorWrapper<T>(this.columnKeyCollectors, bean));

		} else {

			query.executeUpdate();
		}
	}

	private void preAddRelations(T bean, Connection connection, RelationQuery relationQuery) throws SQLException {

		if (RelationQuery.hasRelations(relationQuery)) {

			for (Field relation : relationQuery.getRelations()) {

				preAddRelation(bean, connection, relationQuery, relation);
			}
		}

		if (relationQuery == null || !relationQuery.isDisableAutoRelations()) {

			for (Field relation : autoAddRelations) {

				if (relationQuery == null || (!relationQuery.containsRelation(relation) && !relationQuery.containsExcludedRelation(relation))) {

					preAddRelation(bean, connection, relationQuery, relation);
				}
			}
		}
	}

	private void preAddRelation(T bean, Connection connection, RelationQuery relationQuery, Field relation) throws SQLException {

		ManyToOneRelation<T, ?, ?> manyToOneRelation = this.manyToOneRelations.get(relation);

		if (manyToOneRelation != null) {

			manyToOneRelation.add(bean, connection, relationQuery);
			return;
		}

		manyToOneRelation = this.manyToOneRelationKeys.get(relation);

		if (manyToOneRelation != null) {

			manyToOneRelation.add(bean, connection, relationQuery);
			return;
		}

		OneToOneRelation<T, ?> oneToOneRelation = oneToOneRelations.get(relation);

		if (oneToOneRelation != null && oneToOneRelation.preAdd()) {

			oneToOneRelation.add(bean, connection, relationQuery);
			return;
		}
	}

	private void addRelations(T bean, Connection connection, RelationQuery relationQuery) throws SQLException {

		if (RelationQuery.hasRelations(relationQuery)) {

			for (Field relation : relationQuery.getRelations()) {

				addRelation(bean, connection, relationQuery, relation);
			}
		}

		if (relationQuery == null || !relationQuery.isDisableAutoRelations()) {
			for (Field relation : autoAddRelations) {

				if (relationQuery == null || (!relationQuery.containsRelation(relation) && !relationQuery.containsExcludedRelation(relation))) {

					addRelation(bean, connection, relationQuery, relation);
				}
			}
		}

	}

	private void addRelation(T bean, Connection connection, RelationQuery relationQuery, Field relation) throws SQLException {

		OneToManyRelation<T, ?> oneToManyRelation = this.oneToManyRelations.get(relation);

		if (oneToManyRelation != null) {

			oneToManyRelation.add(bean, connection, relationQuery);
			return;
		}

		ManyToManyRelation<T, ?> manyToManyRelation = this.manyToManyRelations.get(relation);

		if (manyToManyRelation != null) {

			manyToManyRelation.add(bean, connection, relationQuery);
			return;
		}

		OneToOneRelation<T, ?> oneToOneRelation = oneToOneRelations.get(relation);

		if (oneToOneRelation != null && !oneToOneRelation.preAdd()) {

			oneToOneRelation.add(bean, connection, relationQuery);
			return;
		}
	}

	private void preUpdateRelations(T bean, Connection connection, RelationQuery relationQuery) throws SQLException {

		if (RelationQuery.hasRelations(relationQuery)) {

			for (Field relation : relationQuery.getRelations()) {

				preUpdateRelation(bean, connection, relationQuery, relation);
			}
		}

		if (relationQuery == null || !relationQuery.isDisableAutoRelations()) {

			for (Field relation : autoUpdateRelations) {

				if (relationQuery == null || (relationQuery == null || (!relationQuery.containsRelation(relation) && !relationQuery.containsExcludedRelation(relation)))) {

					preUpdateRelation(bean, connection, relationQuery, relation);
				}
			}
		}
	}

	private void preUpdateRelation(T bean, Connection connection, RelationQuery relationQuery, Field relation) throws SQLException {

		ManyToOneRelation<T, ?, ?> manyToOneRelation = this.manyToOneRelations.get(relation);

		if (manyToOneRelation != null) {

			manyToOneRelation.update(bean, connection, relationQuery);
			return;
		}

		manyToOneRelation = this.manyToOneRelationKeys.get(relation);

		if (manyToOneRelation != null) {

			manyToOneRelation.update(bean, connection, relationQuery);
			return;
		}

		OneToOneRelation<T, ?> oneToOneRelation = oneToOneRelations.get(relation);

		if (oneToOneRelation != null && oneToOneRelation.preAdd()) {

			oneToOneRelation.update(bean, connection, relationQuery);
			return;
		}
	}

	private void updateRelations(T bean, Connection connection, RelationQuery relationQuery) throws SQLException {

		if (RelationQuery.hasRelations(relationQuery)) {

			for (Field relation : relationQuery.getRelations()) {

				updateRelation(bean, connection, relationQuery, relation);
			}
		}

		if (relationQuery == null || !relationQuery.isDisableAutoRelations()) {

			for (Field relation : autoUpdateRelations) {

				if (relationQuery == null || (!relationQuery.containsRelation(relation) && !relationQuery.containsExcludedRelation(relation))) {

					updateRelation(bean, connection, relationQuery, relation);
				}
			}
		}
	}

	private void updateRelation(T bean, Connection connection, RelationQuery relationQuery, Field relation) throws SQLException {

		OneToManyRelation<T, ?> oneToManyRelation = this.oneToManyRelations.get(relation);

		if (oneToManyRelation != null) {

			oneToManyRelation.update(bean, connection, relationQuery);
			return;
		}

		ManyToManyRelation<T, ?> manyToManyRelation = this.manyToManyRelations.get(relation);

		if (manyToManyRelation != null) {

			manyToManyRelation.update(bean, connection, relationQuery);
			return;
		}

		OneToOneRelation<T, ?> oneToOneRelation = oneToOneRelations.get(relation);

		if (oneToOneRelation != null && !oneToOneRelation.preAdd()) {

			oneToOneRelation.update(bean, connection, relationQuery);
			return;
		}
	}

	private void setQueryValues(T bean, PreparedStatementQuery query, RelationQuery relationQuery, IntegerCounter integerCounter, Collection<? extends Column<T, ?>> columns, boolean enableExcludedFields) throws SQLException {

		boolean hasExcludedFields = enableExcludedFields && (RelationQuery.hasExcludedFields(relationQuery) || !dontUpdateIfNullCoulumns.isEmpty());

		for (Column<T, ?> column : columns) {

			if (hasExcludedFields && ((relationQuery != null && relationQuery.containsExcludedField(column.getBeanField())) || (dontUpdateIfNullCoulumns.contains(column) && column.getBeanValue(bean) == null))) {

				continue;
			}

			if (column.getQueryParameterPopulator() != null) {

				column.getQueryParameterPopulator().populate(query, integerCounter.increment(), column.getBeanValue(bean));

			} else {

				try {
					column.getQueryMethod().invoke(query, integerCounter.increment(), column.getBeanValue(bean));

				} catch (IllegalArgumentException e) {

					throw new RuntimeException(e);

				} catch (IllegalAccessException e) {

					throw new RuntimeException(e);

				} catch (InvocationTargetException e) {

					throw new RuntimeException(e);
				}
			}
		}
	}

	private void setQueryValues(List<T> beans, PreparedStatementQuery query, IntegerCounter integerCounter, Collection<? extends Column<T, ?>> columns, Field excludedField) throws SQLException {

		for (Column<T, ?> column : columns) {

			if (column.getBeanField().equals(excludedField)) {
				continue;
			}

			for (T bean : beans) {

				if (column.getQueryParameterPopulator() != null) {

					column.getQueryParameterPopulator().populate(query, integerCounter.increment(), column.getBeanValue(bean));

				} else {

					try {
						column.getQueryMethod().invoke(query, integerCounter.increment(), column.getBeanValue(bean));

					} catch (IllegalArgumentException e) {

						throw new RuntimeException(e);

					} catch (IllegalAccessException e) {

						throw new RuntimeException(e);

					} catch (InvocationTargetException e) {

						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	public void addOrUpdateAll(Collection<T> beans, RelationQuery relationQuery) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.addOrUpdateAll(beans, transactionHandler.getConnection(), relationQuery);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void addOrUpdateAll(Collection<T> beans, TransactionHandler transactionHandler, RelationQuery relationQuery) throws SQLException {

		this.addOrUpdateAll(beans, transactionHandler.getConnection(), relationQuery);
	}

	public void addOrUpdateAll(Collection<T> beans, Connection connection, RelationQuery relationQuery) throws SQLException {

		for (T bean : beans) {
			this.addOrUpdate(bean, connection, relationQuery);
		}
	}

	public void addOrUpdate(T bean, RelationQuery relationQuery) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.addOrUpdate(bean, transactionHandler.getConnection(), relationQuery);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void addOrUpdate(T bean, TransactionHandler transactionHandler, RelationQuery relationQuery) throws SQLException {

		addOrUpdate(bean, transactionHandler.getConnection(), relationQuery);
	}

	public void addOrUpdate(T bean, Connection connection, RelationQuery relationQuery) throws SQLException {

		if (!isNewBean(bean, connection)) {

			this.update(bean, connection, relationQuery);

		} else {

			this.add(bean, connection, relationQuery);
		}
	}

	public boolean isNewBean(T bean, Connection connection) throws SQLException {

		if (!hasKeysSet(bean)) {

			return true;
		}

		//Unable to determine if beans is new or not so far, do db query to check
		return !beanExists(bean, connection);
	}

	public boolean hasKeysSet(T bean) throws SQLException {

		for (Column<T, ?> column : this.simpleKeys) {

			if (column.getBeanValue(bean) == null) {

				//Key not set, presume new bean
				return false;
			}
		}

		for (Column<T, ?> column : this.manyToOneRelationKeys.values()) {

			if (column.getBeanValue(bean) == null) {

				//Key not set, presume new bean
				return false;
			}
		}

		return true;
	}

	public boolean beanExists(T bean) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return beanExists(bean, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public boolean beanExists(T bean, TransactionHandler transactionHandler) throws SQLException {

		return beanExists(bean, transactionHandler.getConnection());
	}

	public boolean beanExists(T bean, Connection connection) throws SQLException {

		BooleanQuery query = null;

		try {

			query = new BooleanQuery(connection, false, this.checkIfExistsSQL);

			IntegerCounter integerCounter = new IntegerCounter();

			// Keys from SimpleColumns for where statement
			this.setQueryValues(bean, query, null, integerCounter, this.simpleKeys, false);

			// Keys from many to one relations for where statement
			this.setQueryValues(bean, query, null, integerCounter, this.manyToOneRelationKeys.values(), false);

			return query.executeQuery();

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	public void update(T bean) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.update(bean, transactionHandler.getConnection(), null);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void update(T bean, RelationQuery relationQuery) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.update(bean, transactionHandler.getConnection(), relationQuery);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void update(T bean, RelationQuery relationQuery, int retryCount) throws SQLException {

		while (true) {

			try {

				this.update(bean, relationQuery);

				return;

			} catch (SQLTransactionRollbackException e) {

				if (retryCount > 0) {

					retryCount--;

				} else {

					throw e;
				}
			}
		}
	}

	public void update(T bean, TransactionHandler transactionHandler, RelationQuery relationQuery) throws SQLException {

		this.update(bean, transactionHandler.getConnection(), relationQuery);
	}

	public Integer update(T bean, Connection connection, RelationQuery relationQuery) throws SQLException {

		UpdateQuery query = null;

		try {

			this.preUpdateRelations(bean, connection, relationQuery);

			query = new UpdateQuery(connection, false, generateUpdateSQL(relationQuery, bean));

			IntegerCounter integerCounter = new IntegerCounter();

			// All fields
			this.setQueryValues(bean, query, relationQuery, integerCounter, this.columnMap.values(), true);

			// Keys from SimpleColumns for where statement
			this.setQueryValues(bean, query, null, integerCounter, this.simpleKeys, false);

			// Keys from many to one relations for where statement
			this.setQueryValues(bean, query, null, integerCounter, this.manyToOneRelationKeys.values(), false);

			query.executeUpdate();

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}

		this.updateRelations(bean, connection, relationQuery);

		return query.getAffectedRows();
	}

	public void update(List<T> beans, RelationQuery relationQuery) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.update(beans, transactionHandler.getConnection(), relationQuery);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void update(List<T> beans, TransactionHandler transactionHandler, RelationQuery relationQuery) throws SQLException {

		this.update(beans, transactionHandler.getConnection(), relationQuery);
	}

	public void update(List<T> beans, Connection connection, RelationQuery relationQuery) throws SQLException {

		for (T bean : beans) {

			update(bean, connection, relationQuery);
		}
	}

	public void update(LowLevelQuery<T> lowLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			this.update(lowLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public void update(LowLevelQuery<T> lowLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		this.update(lowLevelQuery, transactionHandler.getConnection());
	}

	public void update(LowLevelQuery<T> lowLevelQuery, Connection connection) throws SQLException {

		UpdateQuery query = null;

		try {

			query = new UpdateQuery(connection, false, lowLevelQuery.getSql());

			this.setCustomQueryParameters(query, lowLevelQuery.getParameters());

			if (lowLevelQuery.getGeneratedKeyCollectors() != null) {

				query.executeUpdate(lowLevelQuery.getGeneratedKeyCollectors());

			} else {

				query.executeUpdate();
			}

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	public T get(LowLevelQuery<T> lowLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return this.get(lowLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public T get(LowLevelQuery<T> lowLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		return this.get(lowLevelQuery, transactionHandler.getConnection());
	}

	public T get(LowLevelQuery<T> lowLevelQuery, Connection connection) throws SQLException {

		BeanResultSetPopulator<T> populator = getPopulator(connection, lowLevelQuery, false);

		ObjectQuery<T> query = null;

		try {

			query = new ObjectQuery<T>(connection, false, lowLevelQuery.getSql(), populator);

			this.setCustomQueryParameters(query, lowLevelQuery.getParameters());

			T bean = query.executeQuery();

			if (bean != null && (RelationQuery.hasRelations(lowLevelQuery) || !autoGetRelations.isEmpty())) {

				this.populateRelations(bean, connection, lowLevelQuery);
			}

			return bean;

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	public boolean getBoolean(LowLevelQuery<T> lowLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return this.getBoolean(lowLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public boolean getBoolean(LowLevelQuery<T> lowLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		return this.getBoolean(lowLevelQuery, transactionHandler.getConnection());
	}

	public boolean getBoolean(LowLevelQuery<T> lowLevelQuery, Connection connection) throws SQLException {

		BooleanQuery query = null;

		try {

			query = new BooleanQuery(connection, false, lowLevelQuery.getSql());

			this.setCustomQueryParameters(query, lowLevelQuery.getParameters());

			return query.executeQuery();

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	public T get(HighLevelQuery<T> highLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return this.get(highLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public T get(HighLevelQuery<T> highLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		return this.get(highLevelQuery, transactionHandler.getConnection());
	}

	public T get(HighLevelQuery<T> highLevelQuery, Connection connection) throws SQLException {

		BeanResultSetPopulator<T> populator = getPopulator(connection, highLevelQuery, false);

		ObjectQuery<T> query = null;

		try {
			query = new ObjectQuery<T>(connection, false, this.generateGetSQL(highLevelQuery) + this.getCriterias(highLevelQuery, null, true, false, true), populator);

			if (highLevelQuery.getParameters() != null) {

				setQueryParameters(query, highLevelQuery, 1);
			}

			T bean = query.executeQuery();

			if (bean != null && (RelationQuery.hasRelations(highLevelQuery) || !autoGetRelations.isEmpty())) {

				this.populateRelations(bean, connection, highLevelQuery);
			}

			return bean;

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	public boolean getBoolean(HighLevelQuery<T> highLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return this.getBoolean(highLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public boolean getBoolean(HighLevelQuery<T> highLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		return this.getBoolean(highLevelQuery, transactionHandler.getConnection());
	}

	public boolean getBoolean(HighLevelQuery<T> highLevelQuery, Connection connection) throws SQLException {

		BooleanQuery query = null;

		try {
			query = new BooleanQuery(connection, false, this.booleanSQL + this.getCriterias(highLevelQuery, null, true, false, true));

			if (highLevelQuery.getParameters() != null) {

				setQueryParameters(query, highLevelQuery, 1);
			}

			return query.executeQuery();

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	private String getCriterias(HighLevelQuery<T> highLevelQuery, String customOrderByCriteria, boolean orderBy, boolean rowLimiter, boolean isFirstParam) {

		if (highLevelQuery == null) {

			if (orderBy) {

				return this.defaultSortingCriteria;

			} else {

				return "";
			}
		}

		StringBuilder stringBuilder = new StringBuilder();

		if (highLevelQuery.getParameters() != null) {

			boolean first = isFirstParam;

			for (QueryParameter<T, ?> queryParameter : highLevelQuery.getParameters()) {

				if (first) {

					stringBuilder.append(" WHERE ");

					first = false;

				} else {

					stringBuilder.append(" AND ");
				}

				// TODO disabled table name prefix for now
				//stringBuilder.append(getTableName());
				//stringBuilder.append(".");
				stringBuilder.append(queryParameter.getColumn().getColumnName());
				stringBuilder.append(" ");
				stringBuilder.append(queryParameter.getOperator());

				if (queryParameter.hasValues()) {

					if (queryParameter.hasMultipleValues()) {

						//stringBuilder.append(queryParameter.getOperator() + " " + queryParameter.getColumn().getColumnName());

						stringBuilder.append(" (?");

						if (queryParameter.getValues().size() > 1) {

							StringUtils.repeatString(",?", queryParameter.getValues().size() - 1, stringBuilder);
						}

						stringBuilder.append(")");

					} else {

						stringBuilder.append(" ?");
					}
				}
			}
		}

		if (customOrderByCriteria != null) {
			stringBuilder.append(customOrderByCriteria);
		}

		if (orderBy && highLevelQuery.getOrderByCriterias() != null) {

			boolean first = customOrderByCriteria == null;

			for (OrderByCriteria<T> criteria : highLevelQuery.getOrderByCriterias()) {

				if (first) {

					stringBuilder.append(" ORDER BY " + criteria.getColumn().getColumnName() + " " + criteria.getOrder().toString());

					first = false;

				} else {

					stringBuilder.append(", " + criteria.getColumn().getColumnName() + " " + criteria.getOrder().toString());
				}
			}

		} else if (customOrderByCriteria == null) {

			stringBuilder.append(this.defaultSortingCriteria);
		}

		if (rowLimiter && highLevelQuery.getRowLimiter() != null) {

			stringBuilder.append(" ");
			stringBuilder.append(highLevelQuery.getRowLimiter().getLimitSQL());
		}

		return stringBuilder.toString();
	}

	@SuppressWarnings("unchecked")
	private <ColumnType> Column<T, ? super ColumnType> getColumn(Field field, Class<ColumnType> paramClass) {

		Column<T, ?> column = this.columnMap.get(field);

		if (column == null) {

			throw new RuntimeException("Field " + field.getName() + " not found in  " + this.beanClass + "!");

		} else if (!column.getParamType().isAssignableFrom(paramClass)) {

			throw new RuntimeException(" " + paramClass + " is not compatible with type " + column.getParamType() + " of field " + field + " in  " + this.beanClass + "!");
		}

		return (Column<T, ColumnType>) column;
	}

	protected BeanResultSetPopulator<T> getPopulator(Connection connection, LowLevelQuery<T> query, boolean isGetAll) {

		BeanResultSetPopulator<T> populator = getPopulator(connection, (RelationQuery) query, isGetAll);

		if (query.hasChainedBeanResultSetPopulators()) {

			return new BeanChainPopulator<T>(query.getChainedResultSetPopulators(), populator);
		}

		return populator;
	}

	protected BeanResultSetPopulator<T> getPopulator(Connection connection, RelationQuery relationQuery, boolean isGetAll) {

		ArrayList<ManyToOneRelation<T, ?, ?>> manyToOneRelations = null;

		if (RelationQuery.hasRelations(relationQuery)) {

			for (Field relation : relationQuery.getRelations()) {

				ManyToOneRelation<T, ?, ?> manyToOneRelation = this.manyToOneRelations.get(relation);

				if (manyToOneRelation == null) {

					manyToOneRelation = this.manyToOneRelationKeys.get(relation);
				}

				if (manyToOneRelation != null) {

					if (manyToOneRelations == null) {

						manyToOneRelations = new ArrayList<ManyToOneRelation<T, ?, ?>>();
					}

					manyToOneRelations.add(manyToOneRelation);
				}
			}
		}

		if (!autoGetRelations.isEmpty() && (relationQuery == null || !relationQuery.isDisableAutoRelations())) {

			for (Field relation : autoGetRelations) {

				if (relationQuery == null || (!relationQuery.containsRelation(relation) && !relationQuery.containsExcludedRelation(relation))) {

					ManyToOneRelation<T, ?, ?> manyToOneRelation = this.manyToOneRelations.get(relation);

					if (manyToOneRelation == null) {

						manyToOneRelation = this.manyToOneRelationKeys.get(relation);
					}

					if (manyToOneRelation != null) {

						if (manyToOneRelations == null) {

							manyToOneRelations = new ArrayList<ManyToOneRelation<T, ?, ?>>();
						}

						manyToOneRelations.add(manyToOneRelation);
					}
				}
			}
		}

		if (manyToOneRelations != null) {

			return getBeanRelationPopulator(manyToOneRelations, connection, relationQuery, isGetAll);
		}

		return this.populator;
	}

	protected BeanResultSetPopulator<T> getBeanRelationPopulator(List<ManyToOneRelation<T, ?, ?>> manyToOneRelations, Connection connection, RelationQuery relationQuery, boolean isGetAll) {

		return new BeanRelationPopulator<T>(populator, manyToOneRelations, connection, relationQuery);
	}

	protected void populateRelations(List<T> beans, Connection connection, RelationQuery relationQuery, BeanResultSetPopulator<T> populator) throws SQLException {

		for (T bean : beans) {

			populateRelations(bean, connection, relationQuery);
		}
	}

	protected void populateRelations(T bean, Connection connection, RelationQuery relationQuery) throws SQLException {

		if (RelationQuery.hasRelations(relationQuery)) {

			for (Field relation : relationQuery.getRelations()) {

				populateRelation(bean, connection, relationQuery, relation);
			}
		}

		if (relationQuery == null || !relationQuery.isDisableAutoRelations()) {

			for (Field relation : autoGetRelations) {

				if (relationQuery == null || (!relationQuery.containsRelation(relation) && !relationQuery.containsExcludedRelation(relation))) {

					populateRelation(bean, connection, relationQuery, relation);
				}
			}
		}
	}

	protected void populateRelation(T bean, Connection connection, RelationQuery relationQuery, Field relation) throws SQLException {

		OneToManyRelation<T, ?> oneToManyRelation = this.oneToManyRelations.get(relation);

		if (oneToManyRelation != null) {

			oneToManyRelation.getRemoteValue(bean, connection, relationQuery);
			return;
		}

		ManyToManyRelation<T, ?> manyToManyRelation = this.manyToManyRelations.get(relation);

		if (manyToManyRelation != null) {

			manyToManyRelation.getRemoteValue(bean, connection, relationQuery);
			return;
		}

		OneToOneRelation<T, ?> oneToOneRelation = this.oneToOneRelations.get(relation);

		if (oneToOneRelation != null) {

			oneToOneRelation.getRemoteValue(bean, connection, relationQuery);
			return;
		}
	}

	public List<T> getAll(LowLevelQuery<T> lowLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return this.getAll(lowLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public List<T> getAll(LowLevelQuery<T> lowLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		return this.getAll(lowLevelQuery, transactionHandler.getConnection());
	}

	public List<T> getAll(LowLevelQuery<T> lowLevelQuery, Connection connection) throws SQLException {

		BeanResultSetPopulator<T> populator = getPopulator(connection, lowLevelQuery, true);

		ArrayListQuery<T> query = null;

		try {

			query = new ArrayListQuery<T>(connection, false, lowLevelQuery.getSql(), populator);

			setCustomQueryParameters(query, lowLevelQuery.getParameters());

			ArrayList<T> beans = query.executeQuery();

			if (beans != null && (RelationQuery.hasRelations(lowLevelQuery) || !autoGetRelations.isEmpty())) {

				populateRelations(beans, connection, lowLevelQuery, populator);
			}

			return beans;

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	private void setCustomQueryParameters(PreparedStatementQuery query, List<?> parameters) {

		if (parameters != null) {

			int i = 1;

			for (Object object : parameters) {

				Method queryMethod;

				if (object == null) {

					queryMethod = PreparedStatementQueryMethods.getObjectQueryMethod();

				} else if (object.getClass().isEnum()) {

					object = object.toString();

					queryMethod = PreparedStatementQueryMethods.getQueryMethod(String.class);

				} else {

					queryMethod = PreparedStatementQueryMethods.getQueryMethod(object.getClass());
				}

				if (queryMethod == null) {
					throw new RuntimeException("Unable to find suitable prepared statement query method for parameter " + object.getClass());
				}

				try {
					queryMethod.invoke(query, i++, object);

				} catch (IllegalArgumentException e) {

					throw new RuntimeException(e);

				} catch (IllegalAccessException e) {

					throw new RuntimeException(e);

				} catch (InvocationTargetException e) {

					throw new RuntimeException(e);
				}
			}
		}
	}

	protected List<T> getAllManyToMany(String sql, String orderByCriteria, CustomQueryParameter<?> queryParameter, Connection connection, RelationQuery relationQuery) throws SQLException {

		BeanResultSetPopulator<T> populator = getPopulator(connection, relationQuery, true);

		ArrayListQuery<T> query = null;

		try {
			query = new ArrayListQuery<T>(connection, false, sql + getCriterias(new HighLevelQuery<T>(relationQuery, beanClass), orderByCriteria, true, true, false), populator);

			if (queryParameter.getQueryParameterPopulator() != null) {

				queryParameter.getQueryParameterPopulator().populate(query, 1, queryParameter.getParamValue());

			} else {

				try {
					queryParameter.getQueryMethod().invoke(query, 1, queryParameter.getParamValue());

				} catch (IllegalArgumentException e) {

					throw new RuntimeException(e);

				} catch (IllegalAccessException e) {

					throw new RuntimeException(e);

				} catch (InvocationTargetException e) {

					throw new RuntimeException(e);
				}
			}

			if (relationQuery != null) {
				setQueryParameters(query, relationQuery.getRelationParameters(this.beanClass), 2);
			}

			ArrayList<T> beans = query.executeQuery();

			if (beans != null && (RelationQuery.hasRelations(relationQuery) || !autoGetRelations.isEmpty())) {

				populateRelations(beans, connection, relationQuery, populator);
			}

			return beans;

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	public List<T> getAll(HighLevelQuery<T> highLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return this.getAll(highLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public List<T> getAll(HighLevelQuery<T> highLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		return this.getAll(highLevelQuery, transactionHandler.getConnection());
	}

	public List<T> getAll(HighLevelQuery<T> highLevelQuery, Connection connection) throws SQLException {

		BeanResultSetPopulator<T> populator = getPopulator(connection, highLevelQuery, true);

		ArrayListQuery<T> query = null;

		try {
			query = new ArrayListQuery<T>(connection, false, this.generateGetSQL(highLevelQuery) + this.getCriterias(highLevelQuery, null, true, true, true), populator);

			setQueryParameters(query, highLevelQuery, 1);

			ArrayList<T> beans = query.executeQuery();

			if (beans != null && (RelationQuery.hasRelations(highLevelQuery) || !autoGetRelations.isEmpty())) {

				populateRelations(beans, connection, highLevelQuery, populator);
			}

			return beans;

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	public List<T> getAll() throws SQLException {

		return this.getAll((HighLevelQuery<T>) null);
	}

	public void delete(T bean) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.delete(bean, transactionHandler);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void delete(T bean, int retryCount) throws SQLException {

		while (true) {

			try {

				delete(bean);

				return;

			} catch (SQLTransactionRollbackException e) {

				if (retryCount > 0) {

					retryCount--;

				} else {

					throw e;
				}
			}
		}
	}

	public void delete(T bean, TransactionHandler transactionHandler) throws SQLException {

		this.delete(bean, transactionHandler.getConnection());
	}

	public void delete(T bean, Connection connection) throws SQLException {

		UpdateQuery query = null;

		try {

			query = new UpdateQuery(connection, false, this.deleteSQL);

			IntegerCounter integerCounter = new IntegerCounter();

			// Keys from SimpleColumns for where statement
			this.setQueryValues(bean, query, null, integerCounter, this.simpleKeys, false);

			// Keys from many to one relations for where statement
			this.setQueryValues(bean, query, null, integerCounter, this.manyToOneRelationKeys.values(), false);

			query.executeUpdate();

		} finally {
			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	public void delete(List<T> beans) throws SQLException {

		TransactionHandler transactionHandler = null;

		try {

			transactionHandler = new TransactionHandler(dataSource);

			this.delete(beans, transactionHandler);

			transactionHandler.commit();
		} finally {
			TransactionHandler.autoClose(transactionHandler);
		}
	}

	public void delete(List<T> beans, TransactionHandler transactionHandler) throws SQLException {

		this.delete(beans, transactionHandler.getConnection());
	}

	public void delete(List<T> beans, Connection connection) throws SQLException {

		for (T bean : beans) {

			delete(bean, connection);
		}
	}

	public Integer delete(HighLevelQuery<T> highLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return this.delete(highLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public void delete(HighLevelQuery<T> highLevelQuery, int retryCount) throws SQLException {

		while (true) {

			try {

				delete(highLevelQuery);

				return;

			} catch (SQLTransactionRollbackException e) {

				if (retryCount > 0) {

					retryCount--;

				} else {

					throw e;
				}
			}
		}
	}

	public Integer delete(HighLevelQuery<T> highLevelQuery, TransactionHandler transactionHandler, int retryCount) throws SQLException {

		while (true) {

			try {

				return delete(highLevelQuery, transactionHandler);

			} catch (SQLTransactionRollbackException e) {

				if (retryCount > 0) {

					retryCount--;

				} else {

					throw e;
				}
			}
		}
	}

	public Integer delete(HighLevelQuery<T> highLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		return this.delete(highLevelQuery, transactionHandler.getConnection());
	}

	public Integer delete(HighLevelQuery<T> highLevelQuery, Connection connection) throws SQLException {

		UpdateQuery query = null;

		try {
			query = new UpdateQuery(connection, false, this.deleteByFieldSQL + this.getCriterias(highLevelQuery, null, false, false, true));

			setQueryParameters(query, highLevelQuery, 1);

			query.executeUpdate();

			return query.getAffectedRows();

		} finally {

			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean deleteWhereNotIn(List<T> beans, TransactionHandler transactionHandler, Field excludedField, QueryParameter<T, ?>... queryParameters) throws SQLException {

		return this.deleteWhereNotIn(beans, transactionHandler.getConnection(), excludedField, queryParameters);
	}

	@SuppressWarnings("unchecked")
	public boolean deleteWhereNotIn(List<T> beans, Connection connection, Field excludedField, QueryParameter<T, ?>... queryParameters) throws SQLException {

		// Generate SQL
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("DELETE FROM ");
		stringBuilder.append(tableName);
		stringBuilder.append(" WHERE");

		//Filter away any beans that don't have their keys set
		ArrayList<T> filteredBeans = new ArrayList<T>(beans);

		Iterator<T> iterator = filteredBeans.iterator();

		while (iterator.hasNext()) {

			if (!hasKeysSet(iterator.next())) {

				iterator.remove();
			}
		}

		if (filteredBeans.isEmpty()) {

			//No beans left, abort
			return false;
		}

		int beanCount = filteredBeans.size();

		BooleanSignal signal = new BooleanSignal();

		this.generateColumWhereNotInSQL(this.simpleKeys, excludedField, stringBuilder, beanCount, signal);
		this.generateColumWhereNotInSQL(this.manyToOneRelationKeys.values(), excludedField, stringBuilder, beanCount, signal);

		if (queryParameters != null) {

			for (QueryParameter<T, ?> queryParameter : queryParameters) {

				stringBuilder.append(" AND " + queryParameter.getColumn().getColumnName() + " " + queryParameter.getOperator() + " ?");
			}
		}

		UpdateQuery query = null;

		try {

			query = new UpdateQuery(connection, false, stringBuilder.toString());

			IntegerCounter integerCounter = new IntegerCounter();

			// Keys values from SimpleColumns for where not in statement
			this.setQueryValues(filteredBeans, query, integerCounter, this.simpleKeys, excludedField);

			// Keys values from many to one relations for where not in statement
			this.setQueryValues(filteredBeans, query, integerCounter, this.manyToOneRelationKeys.values(), excludedField);

			if (queryParameters != null) {

				// Set query param values
				this.setQueryParameters(query, new HighLevelQuery<T>(queryParameters), integerCounter.increment());
			}

			query.executeUpdate();

		} finally {
			PreparedStatementQuery.autoCloseQuery(query);
		}

		return true;
	}

	public Integer getCount(HighLevelQuery<T> highLevelQuery) throws SQLException {

		Connection connection = null;

		try {

			connection = this.dataSource.getConnection();

			return getCount(highLevelQuery, connection);

		} finally {
			DBUtils.closeConnection(connection);
		}
	}

	public Integer getCount(HighLevelQuery<T> highLevelQuery, TransactionHandler transactionHandler) throws SQLException {

		return getCount(highLevelQuery, transactionHandler.getConnection());
	}

	public Integer getCount(HighLevelQuery<T> highLevelQuery, Connection connection) throws SQLException {

		ObjectQuery<Integer> query = null;

		try {
			query = new ObjectQuery<Integer>(connection, false, "SELECT COUNT(*) FROM " + tableName + this.getCriterias(highLevelQuery, null, false, false, true), IntegerPopulator.getPopulator());

			if (highLevelQuery != null && highLevelQuery.getParameters() != null) {

				setQueryParameters(query, highLevelQuery, 1);
			}

			return query.executeQuery();

		} finally {
			PreparedStatementQuery.autoCloseQuery(query);
		}
	}

	private void generateColumWhereNotInSQL(Collection<? extends Column<T, ?>> keyColumns, Field excludedField, StringBuilder stringBuilder, int beanCount, BooleanSignal signal) {

		for (Column<T, ?> column : keyColumns) {

			if (column.getBeanField().equals(excludedField)) {
				continue;
			}

			if (signal.isSignal()) {

				stringBuilder.append(" AND");

			} else {

				signal.setSignal(true);
			}

			stringBuilder.append(" ");
			stringBuilder.append(column.getColumnName());
			stringBuilder.append(" NOT IN (");

			this.addQuestionMarks(beanCount, stringBuilder);

			stringBuilder.append(")");
		}

	}

	private void addQuestionMarks(int size, StringBuilder stringBuilder) {

		stringBuilder.append("?");

		if (size > 1) {

			for (int i = 2; i <= size; i++) {

				stringBuilder.append(",?");
			}
		}
	}

	public <ParamType> QueryParameterFactory<T, ParamType> getParamFactory(Field field, Class<ParamType> paramClass) {

		return new QueryParameterFactory<T, ParamType>(this.getColumn(field, paramClass));
	}

	public <ParamType> QueryParameterFactory<T, ParamType> getParamFactory(String fieldName, Class<ParamType> paramClass) {

		Field field = ReflectionUtils.getField(beanClass, fieldName);

		if (field == null) {
			throw new RuntimeException("Field " + fieldName + " not found in  " + this.beanClass + "!");
		}

		return new QueryParameterFactory<T, ParamType>(this.getColumn(field, paramClass));
	}

	public OrderByCriteria<T> getOrderByCriteria(Field field, Order order) {

		Column<T, ?> column = this.columnMap.get(field);

		if (column == null) {
			throw new RuntimeException("No @DAOManaged annotated field with name " + field.getName() + " not found in  " + this.beanClass + "!");
		}

		return new OrderByCriteria<T>(order, column);
	}

	public OrderByCriteria<T> getOrderByCriteria(String fieldName, Order order) {

		Field field = ReflectionUtils.getField(beanClass, fieldName);

		if (field == null) {
			throw new RuntimeException("Field " + fieldName + " not found in  " + this.beanClass + "!");
		}

		return this.getOrderByCriteria(field, order);
	}

	public String getTableName() {

		return tableName;
	}

	Column<T, ?> getColumn(Field field) {

		return this.columnMap.get(field);
	}

	protected void setQueryParameters(PreparedStatementQuery query, HighLevelQuery<T> highLevelQuery, final int startIndex) throws SQLException {

		if (highLevelQuery != null) {
			setQueryParameters(query, highLevelQuery.getParameters(), startIndex);
		}
	}

	private void setQueryParameters(PreparedStatementQuery query, List<QueryParameter<T, ?>> queryParameters, final int startIndex) throws SQLException {

		if (queryParameters != null) {
			int index = startIndex;

			for (QueryParameter<?, ?> queryParameter : queryParameters) {

				if (queryParameter.hasValues()) {

					if (queryParameter.hasMultipleValues()) {

						for (Object value : queryParameter.getValues()) {

							setQueryParameter(queryParameter, value, query, index++);
						}

					} else {

						setQueryParameter(queryParameter, queryParameter.getValue(), query, index++);
					}
				}
			}
		}
	}

	private void setQueryParameter(QueryParameter<?, ?> queryParameter, Object value, PreparedStatementQuery query, int index) throws SQLException {

		Column<?, ?> column = queryParameter.getColumn();

		if (column.getQueryParameterPopulator() != null) {

			column.getQueryParameterPopulator().populate(query, index, column.getParamValue(value));

		} else {

			try {
				column.getQueryMethod().invoke(query, index, column.getParamValue(value));

			} catch (IllegalArgumentException e) {

				throw new RuntimeException(e);

			} catch (IllegalAccessException e) {

				throw new RuntimeException(e);

			} catch (InvocationTargetException e) {

				throw new RuntimeException(e);
			}
		}
	}

	public Class<T> getBeanClass() {

		return beanClass;
	}

	public <KeyType> AnnotatedDAOWrapper<T, KeyType> getWrapper(String keyField, Class<KeyType> keyClass) {

		return new AnnotatedDAOWrapper<T, KeyType>(this, keyField, keyClass);
	}

	/**
	 * This method creates a DAO wrapper using the first key found in the type
	 * 
	 * @param keyClass
	 * @return
	 */
	public <KeyType> AnnotatedDAOWrapper<T, KeyType> getWrapper(Class<KeyType> keyClass) {

		return new AnnotatedDAOWrapper<T, KeyType>(this, getFirstKeyField(), keyClass);
	}

	public Field getFirstKeyField() {

		if (!simpleKeys.isEmpty()) {

			return simpleKeys.get(0).getBeanField();
		}

		//		if(!manyToOneRelationKeys.isEmpty()){
		//
		//			return manyToOneRelationKeys.get(0).getBeanField();
		//		}

		throw new RuntimeException("No key field found!");
	}

	public <KeyType> AdvancedAnnotatedDAOWrapper<T, KeyType> getAdvancedWrapper(String keyField, Class<KeyType> keyClass) {

		return new AdvancedAnnotatedDAOWrapper<T, KeyType>(this, keyField, keyClass);
	}

	/**
	 * This method creates a advanced DAO wrapper using the first key found in the type
	 * 
	 * @param keyClass
	 * @return
	 */
	public <KeyType> AdvancedAnnotatedDAOWrapper<T, KeyType> getAdvancedWrapper(Class<KeyType> keyClass) {

		return new AdvancedAnnotatedDAOWrapper<T, KeyType>(this, getFirstKeyField(), keyClass);
	}

	public static String getTableName(Class<?> clazz) {

		Table table = clazz.getAnnotation(Table.class);

		if (table == null) {

			throw new RuntimeException("No @" + Table.class.getSimpleName() + " annotation found for " + clazz);
		}

		return table.name();
	}
}
