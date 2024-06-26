/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBUtils {

	public static final Pattern LIKE_ESCAPE_PATTERN = Pattern.compile("([%\\\\_])");

	public static String escapeLikeParameter(String parameter) {

		return LIKE_ESCAPE_PATTERN.matcher(parameter).replaceAll("\\\\$1");
	}

	public static String getPreparedStatementQuestionMarks(Collection<? extends Object> objects) {

		if (objects.size() == 1) {

			return "?";
		}

		StringBuilder stringBuilder = new StringBuilder((objects.size() * 2) - 1);

		stringBuilder.append("?");

		for (int i = 1; i < objects.size(); i++) {

			stringBuilder.append(",?");
		}

		return stringBuilder.toString();
	}

	public static boolean tableExists(DataSource dataSource, String tableName) throws SQLException {

		Connection connection = null;

		try {
			connection = dataSource.getConnection();

			DatabaseMetaData meta = connection.getMetaData();

			return tableExists(meta, tableName);
		} finally {
			closeConnection(connection);
		}
	}

	public static boolean tableExists(DatabaseMetaData meta, String tableName) throws SQLException {

		ResultSet rs = null;

		try {
			rs = meta.getTables(null, null, tableName, null);

			if (rs.next()) {
				return true;
			}
		} finally {
			closeResultSet(rs);
		}

		return false;
	}

	public static ArrayList<String> listAllTables(Connection connection) throws SQLException {

		ResultSet rs = null;

		try {
			DatabaseMetaData meta = connection.getMetaData();

			rs = meta.getTables(null, null, null, null);

			ArrayList<String> tableList = new ArrayList<String>();

			while (rs.next()) {

				tableList.add(rs.getString(3));
			}

			return tableList;

		} finally {
			closeResultSet(rs);
			closeConnection(connection);
		}
	}

	public static DataSource getDataSource(String name) throws NamingException {

		Context initContext = new InitialContext();
		Context envContext = (Context) initContext.lookup("java:/comp/env");
		return (DataSource) envContext.lookup(name);
	}

	public static boolean containsColumn(ResultSet rs, String columnName) {

		try {
			rs.findColumn(columnName);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public static List<String> getTableColumns(Connection connection, String selectedTable) throws SQLException {

		ResultSet rs = null;

		try {
			DatabaseMetaData meta = connection.getMetaData();

			rs = meta.getColumns(null, null, selectedTable, null);

			ArrayList<String> columnNames = new ArrayList<String>();

			while (rs.next()) {

				columnNames.add(rs.getString(4));
			}

			return columnNames;

		} finally {
			closeResultSet(rs);
			closeConnection(connection);
		}
	}

	public static int getTableColumnCount(DataSource dataSource, String selectedTable) throws SQLException {

		Connection connection = null;
		ResultSet rs = null;

		try {

			connection = dataSource.getConnection();

			DatabaseMetaData meta = connection.getMetaData();

			rs = meta.getColumns(null, null, selectedTable, null);

			rs.last();

			return rs.getRow();

		} finally {
			closeResultSet(rs);
			closeConnection(connection);
		}
	}

	public static void closeConnection(Connection connection) {

		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {}
	}

	public static void closeResultSet(ResultSet rs) {

		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {}
	}

	public static void closePreparedStatement(PreparedStatement pstmt) {

		try {
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException e) {}
	}

	public static SQLSyntaxErrorException getDetailedSQLSyntaxErrorException(SQLSyntaxErrorException exception, PreparedStatement pstmt) {

		return new SQLSyntaxErrorException(exception.getMessage() + ". Query: " + pstmt.toString(), exception);
	}

	public static boolean dbExists(DataSource dataSource, String databaseName) throws SQLException {

		Connection connection = null;
		ResultSet resultSet = null;

		try {
			connection = dataSource.getConnection();

			resultSet = connection.getMetaData().getCatalogs();

			while (resultSet.next()) {

				String dbName = resultSet.getString(1);
				
				if (databaseName.equals(dbName)) {
					
					return true;
				}
			}

		} finally {
			closeResultSet(resultSet);
			closeConnection(connection);
		}
		
		return false;
	}

}
