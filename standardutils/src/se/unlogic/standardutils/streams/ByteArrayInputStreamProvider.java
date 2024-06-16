package se.unlogic.standardutils.streams;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ByteArrayInputStreamProvider implements InputStreamProvider {

	private final byte[] data;
	
	public ByteArrayInputStreamProvider(byte[] data) {

		this.data = data;
	}

	@Override
	public InputStream getInputStream() {

		return new ByteArrayInputStream(data);
	}

}
