package se.unlogic.standardutils.hash;

public enum HashAlgorithm {

	SHA1("SHA-1"), SHA256("SHA-256"), SHA384("SHA-384"), SHA512("SHA-512"), MD2("MD2"), MD5("MD5");

	private String value;

	HashAlgorithm(String value) {

		this.value = value;
	}

	public String getValue() {

		return value;
	}

	@Override
	public String toString() {

		return getValue();
	}
}