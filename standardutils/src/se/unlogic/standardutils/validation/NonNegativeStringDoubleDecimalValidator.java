package se.unlogic.standardutils.validation;

import java.math.BigDecimal;

import se.unlogic.standardutils.numbers.NumberUtils;

public class NonNegativeStringDoubleDecimalValidator extends StringDoubleValidator {

	private static final long serialVersionUID = -6839218280209057543L;

	private Integer numberOfDecimals;

	public NonNegativeStringDoubleDecimalValidator(Integer numberOfDecimals) {

		super(0.0, null);
		
		this.numberOfDecimals = numberOfDecimals;
	}

	@Override
	public boolean validateFormat(String value) {

		if (value != null) {

			value = value.replace(" ", "");

			BigDecimal decimalValue = NumberUtils.toBigDecimalLocaleIndependent(value);
			
			if (decimalValue != null && super.validateFormat(decimalValue.toString()) && decimalValue.scale() == numberOfDecimals) {

				return true;
				
			} else {

				return false;
			}
		}

		return false;
	}
	
}
