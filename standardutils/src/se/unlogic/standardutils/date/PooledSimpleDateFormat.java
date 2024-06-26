package se.unlogic.standardutils.date;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import se.unlogic.standardutils.pool.GenericObjectPool;


public class PooledSimpleDateFormat implements ThreadSafeDateFormat{

	protected GenericObjectPool<SimpleDateFormat> pool;

	public PooledSimpleDateFormat(String format){

		this.pool = new GenericObjectPool<SimpleDateFormat>(new SimpleDateFormatFactory(format));
	}

	public PooledSimpleDateFormat(String format, Locale locale){

		this.pool = new GenericObjectPool<SimpleDateFormat>(new SimpleDateFormatFactory(format,locale));
	}

	public PooledSimpleDateFormat(String format, Locale locale, TimeZone timeZone){

		this.pool = new GenericObjectPool<SimpleDateFormat>(new SimpleDateFormatFactory(format, locale, timeZone));
	}

	@Override
	public Date parse(String date) throws ParseException{

		SimpleDateFormat dateFormat = null;

		try{
			dateFormat = pool.borrowObject();

			ParsePosition pos = new ParsePosition(0);
			Date result = dateFormat.parse(date, pos);

			if (pos.getIndex() == 0) {
				
				throw new ParseException("Unparseable date: \"" + date + "\"", pos.getErrorIndex());
				
			} else if (pos.getIndex() != date.length()) {
				
				throw new ParseException("Unparsed remains at end: \"" + date + "\", parsed " + pos.getIndex() + " < length " + date.length(), pos.getIndex());
			}

			return result;

		} finally {

			pool.returnObject(dateFormat);
		}
	}

	@Override
	public String format(Date date){

		SimpleDateFormat dateFormat = null;

		try{
			dateFormat = pool.borrowObject();
			
			return dateFormat.format(date);

		}finally{

			pool.returnObject(dateFormat);
		}
	}

	public String format(Object date){

		SimpleDateFormat dateFormat = null;

		try{
			dateFormat = pool.borrowObject();
			
			return dateFormat.format(date);

		}finally{

			pool.returnObject(dateFormat);
		}
	}
	
	public int getObjectsCreated() {

		return pool.getObjectsCreated();
	}

	public int getObjectsInPool() {

		return pool.getObjectsInPool();
	}
}
