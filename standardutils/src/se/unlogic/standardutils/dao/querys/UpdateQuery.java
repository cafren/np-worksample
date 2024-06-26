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
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import se.unlogic.standardutils.db.DBUtils;

public class UpdateQuery extends PreparedStatementQuery {

	private Integer affectedRows;

	public UpdateQuery(Connection connection, boolean closeConnectionOnExit, String query) throws SQLException {

		super(connection, closeConnectionOnExit, query);
	}

	public UpdateQuery(DataSource dataSource, String query) throws SQLException {

		super(dataSource, query);
	}

	public void executeUpdate() throws SQLException {

		ResultSet rs = null;

		try {
			affectedRows = this.pstmt.executeUpdate();
			
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

	public void executeUpdate(List<GeneratedKeyCollector> keyCollectors) throws SQLException {

		ResultSet rs = null;

		try {
			affectedRows = this.pstmt.executeUpdate();

			if (affectedRows > 0) {
				rs = pstmt.getGeneratedKeys();

				if (rs.next()) {

					for (GeneratedKeyCollector keyCollector : keyCollectors) {

						keyCollector.collect(rs);
					}
				}
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

	public void executeUpdate(GeneratedKeyCollector... keyCollectors) throws SQLException {

		executeUpdate(Arrays.asList(keyCollectors));
	}

	public Integer getAffectedRows() {

		return affectedRows;
	}
}
