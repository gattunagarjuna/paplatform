package pa.platform.model;

import java.io.File;

public class EmailAttachment {
	String mimeType;
	String content;
	File file;
	String attachmentName;
	String encoding;
	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}
	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	/**
	 * @return the attachmentName
	 */
	public String getAttachmentName() {
		return attachmentName;
	}
	/**
	 * @param attachmentName the attachmentName to set
	 */
	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}
	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}
	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}