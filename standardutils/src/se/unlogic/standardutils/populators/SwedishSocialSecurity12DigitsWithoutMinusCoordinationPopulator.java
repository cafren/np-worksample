package se.unlogic.standardutils.populators;

public class SwedishSocialSecurity12DigitsWithoutMinusCoordinationPopulator extends SwedishSocialSecurityPopulator {

	private static final SwedishSocialSecurity12DigitsWithoutMinusCoordinationPopulator POPULATOR = new SwedishSocialSecurity12DigitsWithoutMinusCoordinationPopulator();

	public SwedishSocialSecurity12DigitsWithoutMinusCoordinationPopulator() {

		super(false, true, true, true);
	}

	public static SwedishSocialSecurity12DigitsWithoutMinusCoordinationPopulator getPopulator() {

		return POPULATOR;
	}

}
