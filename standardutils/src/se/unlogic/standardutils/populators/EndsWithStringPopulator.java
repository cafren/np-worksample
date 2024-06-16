package se.unlogic.standardutils.populators;

import java.util.Arrays;
import java.util.List;

public class EndsWithStringPopulator implements BeanStringPopulator<String> {

	private final List<String> validEndStrings;
	
	public EndsWithStringPopulator(List<String> validEndStrings) {

		super();
		
		if(validEndStrings == null) {
			
			throw new NullPointerException("validEndStrings cannot be null");
		}
		
		this.validEndStrings = validEndStrings;
	}
	
	public EndsWithStringPopulator(String... validEndStrings) {
		
		this(Arrays.asList(validEndStrings));
	}

	@Override
	public boolean validateFormat(String value) {

		for(String endString : validEndStrings) {
			
			if(value.endsWith(endString)) {
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public String getValue(String value) {

		return value;
	}

	@Override
	public Class<? extends String> getType() {

		return String.class;
	}

	@Override
	public String getPopulatorID() {

		return null;
	}

}
