package se.unlogic.standardutils.fileattachments;

import java.util.Collection;

public class FileAttachmentUtils {

	public static void deleteFileAttachments(Collection<FileAttachment> fileAttachments) {
		
		if(fileAttachments != null) {
			
			for(FileAttachment fileAttachment : fileAttachments) {
				
				fileAttachment.delete();
			}
		}
	}
}
