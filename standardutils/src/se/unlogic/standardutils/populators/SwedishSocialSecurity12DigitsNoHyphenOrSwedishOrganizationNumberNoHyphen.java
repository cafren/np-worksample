package se.unlogic.standardutils.populators;

public class SwedishSocialSecurity12DigitsNoHyphenOrSwedishOrganizationNumberNoHyphen extends CombinedPopulator<String> {

	private static final SwedishSocialSecurity12DigitsWithoutMinusPopulator SOCIAL_SECURITY_POPULATOR = SwedishSocialSecurity12DigitsWithoutMinusPopulator.getPopulator();
	private static final SwedishOrganizationNumberWithoutMinusPopulator ORGANIZATION_NUMBER_POPULATOR = SwedishOrganizationNumberWithoutMinusPopulator.getPopulator();

	@SuppressWarnings("unchecked")
	public SwedishSocialSecurity12DigitsNoHyphenOrSwedishOrganizationNumberNoHyphen() {

		super(String.class, SOCIAL_SECURITY_POPULATOR, ORGANIZATION_NUMBER_POPULATOR);
	}

}
