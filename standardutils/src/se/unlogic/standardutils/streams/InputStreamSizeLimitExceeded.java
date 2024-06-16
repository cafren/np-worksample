package se.unlogic.standardutils.streams;

import java.io.IOException;

public class InputStreamSizeLimitExceeded extends IOException {

	private static final long serialVersionUID = 8139690895557465147L;
	
	private final long maxAllowedSize;

	public InputStreamSizeLimitExceeded(String message, long maxAllowedSize) {

		super(message);
		this.maxAllowedSize = maxAllowedSize;
	}

	public long getMaxAllowedSize() {

		return maxAllowedSize;
	}

}
