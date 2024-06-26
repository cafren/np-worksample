/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.dao.querys;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import javax.sql.DataSource;

import se.unlogic.standardutils.db.DBUtils;

public class BooleanQuery extends PreparedStatementQuery {

	public BooleanQuery(Connection connection, boolean closeConnectionOnExit, String query) throws SQLException {
		super(connection, closeConnectionOnExit, query);
	}

	public BooleanQuery(DataSource dataSource, String query) throws SQLException {
		super(dataSource, query);
	}

	public boolean executeQuery() throws SQLException {

		ResultSet rs = null;

		try {
			rs = pstmt.executeQuery();

			if (rs.next()) {
				return true;
			} else {
				return false;
			}
			
		} catch (SQLSyntaxErrorException sqle){
			
			throw DBUtils.getDetailedSQLSyntaxErrorException(sqle, pstmt);
			
		} catch (SQLException sqle) {
			throw sqle;
			
		} finally {
			DBUtils.closeResultSet(rs);
			DBUtils.closePreparedStatement(pstmt);

			if (this.closeConnectionOnExit) {
				DBUtils.closeConnection(connection);
			}

			this.closed = true;
		}
	}
}
