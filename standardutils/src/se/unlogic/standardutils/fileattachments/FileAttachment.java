package se.unlogic.standardutils.fileattachments;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface FileAttachment {

	public InputStream getInputStream() throws FileNotFoundException;
	
	public long getLength();
	
	/**
	 * @return true if the file was deleted or false if the file was not found.
	 */
	public boolean delete();
}
