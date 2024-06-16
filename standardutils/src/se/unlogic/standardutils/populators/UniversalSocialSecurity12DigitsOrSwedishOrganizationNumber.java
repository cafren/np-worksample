package se.unlogic.standardutils.populators;

public class UniversalSocialSecurity12DigitsOrSwedishOrganizationNumber extends CombinedPopulator<String> {

	private static final SwedishSocialSecurity12DigitsMinusOptionalPopulator SOCIAL_SECURITY_POPULATOR = new SwedishSocialSecurity12DigitsMinusOptionalPopulator();
	private static final UniversalSwedishOrganizationNumberPopulator ORGANIZATION_NUMBER_POPULATOR = new UniversalSwedishOrganizationNumberPopulator();

	@SuppressWarnings("unchecked")
	public UniversalSocialSecurity12DigitsOrSwedishOrganizationNumber() {

		super(String.class, SOCIAL_SECURITY_POPULATOR, ORGANIZATION_NUMBER_POPULATOR);
	}

}
