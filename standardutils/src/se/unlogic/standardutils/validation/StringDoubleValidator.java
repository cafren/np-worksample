package se.unlogic.standardutils.validation;

import java.io.Serializable;

import se.unlogic.standardutils.numbers.NumberUtils;


public class StringDoubleValidator extends StringNumberValidator<Double> implements Serializable {

	private static final long serialVersionUID = -5187401881758111966L;

	public StringDoubleValidator() {
		super(null, null);
	}

	public StringDoubleValidator(Double minValue, Double maxValue) {
		super(minValue,maxValue);
	}

	@Override
	public boolean validateFormat(String value) {

		Double numberValue = NumberUtils.toDouble(value);

		if(numberValue == null){

			return false;

		}else if(maxValue != null && minValue != null){

			return numberValue <= maxValue && numberValue > minValue;

		}else if(maxValue != null){

			return numberValue <= maxValue;

		}else if(minValue != null){

			return numberValue >= minValue;
		}
		else{
			return true;
		}
	}
}
