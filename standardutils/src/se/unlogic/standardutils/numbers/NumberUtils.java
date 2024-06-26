/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.numbers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class NumberUtils {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\D*");

	public static boolean isLong(Double value) {

		if (value != null) {

			Long longValue = value.longValue();
			Double doubleValue = longValue.doubleValue();

			if (doubleValue.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isInt(String value) {

		if (value != null) {

			try {
				Integer.parseInt(value);
				return true;

			} catch (NumberFormatException e) {}
		}
		return false;
	}

	public static boolean isLong(String value) {

		if (value != null) {

			try {
				Long.parseLong(value);
				return true;

			} catch (NumberFormatException e) {}
		}
		return false;
	}

	public static boolean isFloat(String value) {

		if (value != null) {

			try {
				Float.parseFloat(value);
				return true;

			} catch (NumberFormatException e) {}
		}
		return false;
	}

	public static boolean isDouble(String value) {

		if (value != null) {
			try {
				Double.parseDouble(value);
				return true;

			} catch (NumberFormatException e) {}
		}
		return false;
	}
	
	public static boolean isDoubleLocaleIndependent(String value) {

		return toDoubleLocaleIndependent(value) != null;

	}
	
	public static boolean isBigDecimal(String value) {

		if (value != null) {
			try {
				new BigDecimal(value);
				return true;

			} catch (NumberFormatException e) {}
		}
		
		return false;
	}
	
	public static boolean isBigDecimalLocaleIndependent(String value) {

		return toBigDecimalLocaleIndependent(value) != null;
	}

	public static boolean isNumber(String value) {

		return isDouble(value) || isLong(value) ? true : false;
	}
	
	public static Integer toInt(Object value) {
		
		if (value == null) {
			return null;
		}
		
		return toInt(value.toString());
	}

	public static Integer toInt(String value) {

		if (value != null) {
			try {
				return Integer.parseInt(value);

			} catch (NumberFormatException e) {}
		}

		return null;
	}

	public static Integer toPrimitiveInt(String value) {

		return toPrimitiveInt(toInt(value));
	}
	
	public static Short toShort(String value) {

		if (value != null) {
			try {
				return Short.parseShort(value);

			} catch (NumberFormatException e) {}
		}

		return null;
	}
	
	public static List<Integer> toInt(Collection<? extends Object> list, Field field) throws IllegalArgumentException, IllegalAccessException {

		Type type = field.getType();

		if (list != null && type.equals(Integer.class) || type.equals(int.class)) {

			List<Integer> validIntegers = new ArrayList<Integer>();

			for (Object object : list) {

				Integer value = (Integer) field.get(object);

				if (value != null) {
					validIntegers.add(value);
				}

			}

			return validIntegers;
		}

		return null;

	}

	public static ArrayList<Integer> toInt(Collection<String> values) {

		if (values == null) {

			return null;

		} else {

			ArrayList<Integer> validIntegers = new ArrayList<Integer>();

			for (String value : values) {

				if (value != null) {

					Integer intValue = NumberUtils.toInt(value);

					if (intValue != null) {
						validIntegers.add(intValue);
					}
				}
			}

			if (validIntegers.isEmpty()) {

				return null;

			} else {

				return validIntegers;
			}
		}
	}

	public static ArrayList<Integer> toInt(String[] values) {

		if (values == null) {

			return null;

		} else {

			ArrayList<Integer> validIntegers = new ArrayList<Integer>();

			for (String value : values) {

				if (value != null) {

					Integer intValue = NumberUtils.toInt(value);

					if (intValue != null) {
						validIntegers.add(intValue);
					}
				}
			}

			if (validIntegers.isEmpty()) {

				return null;

			} else {

				return validIntegers;
			}
		}
	}

	public static Long toLong(String value) {

		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {}
		}

		return null;
	}

	public static Float toFloat(String value) {

		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {}
		}

		return null;
	}

	public static List<Float> toFloat(List<String> values) {

		if (values == null) {

			return null;

		} else {

			ArrayList<Float> validFloats = new ArrayList<Float>();

			for (String value : values) {

				if (value != null) {

					Float floatValue = NumberUtils.toFloat(value);

					if (floatValue != null) {
						validFloats.add(floatValue);
					}
				}
			}

			if (validFloats.isEmpty()) {
				return null;
			} else {
				return validFloats;
			}
		}
	}
	
	public static Double toDouble(String value) {

		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {}
		}

		return null;
	}
	
	public static Double toDoubleLocaleIndependent(String value) {

		if (value != null) {
			
			value = value.replace(',', '.');
			
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {}
		}

		return null;
	}

	public static List<Double> toDouble(List<String> values) {

		if (values == null) {

			return null;

		} else {

			ArrayList<Double> validDoubles = new ArrayList<Double>();

			for (String value : values) {

				if (value != null) {

					Double doubleValue = NumberUtils.toDouble(value);

					if (doubleValue != null) {
						validDoubles.add(doubleValue);
					}
				}
			}

			if (validDoubles.isEmpty()) {
				return null;
			} else {
				return validDoubles;
			}
		}
	}

	public static List<Long> toLong(List<String> values) {

		if (values == null) {
			return null;
		} else {
			ArrayList<Long> validLongs = new ArrayList<Long>();

			for (String value : values) {
				if (value != null) {
					Long LongValue = NumberUtils.toLong(value);

					if (LongValue != null) {
						validLongs.add(LongValue);
					}
				}
			}

			if (validLongs.isEmpty()) {
				return null;
			} else {
				return validLongs;
			}
		}
	}
	
	public static BigDecimal toBigDecimal(String value) {

		if (value != null) {
			try {
				return new BigDecimal(value);
			} catch (NumberFormatException e) {}
		}

		return null;
	}
	
	public static BigDecimal toBigDecimalLocaleIndependent(String value) {

		if (value != null) {
			
			value = value.replace(',', '.');
			
			try {
				return new BigDecimal(value);
			} catch (NumberFormatException e) {}
		}

		return null;
	}

	public static Long getNumbers(String revstring) {

		String result = NUMBER_PATTERN.matcher(revstring).replaceAll("");

		return toLong(result);
	}

	public static String formatNumber(Number value, int minDecimals, int maxDecimals, boolean grouping, boolean dotDecimalSymbol) {

		DecimalFormat formatter = new DecimalFormat();

		formatter.setMinimumFractionDigits(minDecimals);

		formatter.setMaximumFractionDigits(maxDecimals);

		if (dotDecimalSymbol) {

			formatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));

		}

		formatter.setGroupingUsed(grouping);

		return formatter.format(value);

	}

	public static Byte toByte(String value) {

		if (value != null) {
			try {
				return Byte.parseByte(value);
			} catch (NumberFormatException e) {}
		}

		return null;
	}

	/**
	 * The Luhn algorithm or Luhn formula, also known as the "modulus 10" or "mod 10" algorithm,
	 * is a simple checksum formula used to validate a variety of identification numbers,
	 * such as credit card numbers, IMEI numbers, National Provider Identifier numbers, and Social Insurance Numbers.
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isValidCC(String number) {

		final int[][] sumTable = {{0,1,2,3,4,5,6,7,8,9},{0,2,4,6,8,1,3,5,7,9}};
		int sum = 0, flip = 0;

		for (int i = number.length() - 1; i >= 0; i--) {
			sum += sumTable[flip++ & 0x1][Character.digit(number.charAt(i), 10)];
		}
		return sum % 10 == 0;
	}
	
	public static int getLuhnChecksum(String number) {

		final int[][] sumTable = {{0,1,2,3,4,5,6,7,8,9},{0,2,4,6,8,1,3,5,7,9}};
		int sum = 0, flip = 1;

		for (int i = number.length() - 1; i >= 0; i--) {
			sum += sumTable[flip++ & 0x1][Character.digit(number.charAt(i), 10)];
		}
		
		return (sum * 9) % 10;
	}

	public static int toPrimitiveInt(Integer integer) {

		if(integer == null){
			
			return 0;
		}
		
		return integer;
	}

	public static int getLowestValue(int value1, int value2) {

		if(value1 < value2){
			
			return value1;
			
		}else{
			
			return value2;
		}
	}
	
	public static int saturatingIntCast(long value) {
		
		if (value > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
			
		} else if (value < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}
		
		return (int) value;
	}
	
	public static int exceptingIntCast(long value) {
		
		if (value > Integer.MAX_VALUE) {
			throw new RuntimeException("Long " + value + " exceeds max integer range!");
			
		} else if (value < Integer.MIN_VALUE) {
			throw new RuntimeException("Long " + value + " exceeds min integer range!");
		}
		
		return (int) value;
	}
	
}
