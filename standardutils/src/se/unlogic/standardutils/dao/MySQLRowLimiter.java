package se.unlogic.standardutils.dao;

public class MySQLRowLimiter implements RowLimiter {

	public static final RowLimiter SINGLE_ROW = new MySQLRowLimiter(1);
	
	public final int start;
	public final int rows;

	public MySQLRowLimiter(int rows) {

		super();
		this.start = 0;
		this.rows = rows;
	}

	public MySQLRowLimiter(int start, int rows) {

		super();
		this.start = start;
		this.rows = rows;
	}

	public int getStart() {

		return start;
	}

	public int getRows() {

		return rows;
	}

	@Override
	public String getLimitSQL() {

		if(start == 0){

			return "LIMIT " + rows;
		}

		return "LIMIT " + start + "," + rows;
	}
}
