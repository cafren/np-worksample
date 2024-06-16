package se.unlogic.standardutils.fileattachments;

import java.io.IOException;
import java.io.InputStream;

public interface FileAttachmentHandler {

	/**
	 * Add an attachment at the given path
	 * 
	 * @param inputStream The file content
	 * @param path the path to the file without separators 
	 * @return
	 */
	public FileAttachment addFileAttchment(InputStream inputStream, String... path) throws IOException;
	
	/**
	 * Retrives a store file
	 * 
	 * @param path the path to the file without separators 
	 * @return the file or null if no file was found
	 */
	public FileAttachment getFileAttachment(String... path) throws IOException;
	
	/**
	 * This method will throw an exception if the given path does not point to a file
	 * 
	 * @param path the path to the file without separators 
	 * @return true if the file was deleted, false if the file was not found
	 */
	public boolean deleteAttachment(String... path) throws IOException;
	
	/**
	 * This method will throw an exception if the given path does not point to a folder
	 * 
	 * @param path the path to the folder without separators 
	 * @return true if the folder was deleted, false if the folder was not found
	 */	
	public boolean deleteAttachments(String... path) throws IOException;
}
