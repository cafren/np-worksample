package se.unlogic.standardutils.populators;

public class SwedishSocialSecurity12DigitsHyphenOrSwedishOrganizationNumberHyphen extends CombinedPopulator<String> {

	private static final SwedishSocialSecurity12DigitsPopulator SOCIAL_SECURITY_POPULATOR = SwedishSocialSecurity12DigitsPopulator.getPopulator();
	private static final SwedishOrganizationNumberPopulator ORGANIZATION_NUMBER_POPULATOR = new SwedishOrganizationNumberPopulator(false);

	@SuppressWarnings("unchecked")
	public SwedishSocialSecurity12DigitsHyphenOrSwedishOrganizationNumberHyphen() {

		super(String.class, SOCIAL_SECURITY_POPULATOR, ORGANIZATION_NUMBER_POPULATOR);
	}

}
