package se.unlogic.standardutils.streams;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamProvider {

	public InputStream getInputStream() throws IOException;
}
