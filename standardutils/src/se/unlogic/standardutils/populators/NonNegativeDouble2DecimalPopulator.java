package se.unlogic.standardutils.populators;

import se.unlogic.standardutils.validation.NonNegativeStringDoubleDecimalValidator;

public class NonNegativeDouble2DecimalPopulator extends DoublePopulator {
	
	public NonNegativeDouble2DecimalPopulator() {
		
		super(null, new NonNegativeStringDoubleDecimalValidator(2));
	}
}