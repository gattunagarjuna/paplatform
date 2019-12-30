package pa.platform.model;



public class Email {
	java.util.List<String> toAddress;
	String subject;
	String body;

	java.util.List<EmailAttachment> attachmentLists;
	private String fromAddress;

	
	/**
	 * attachment list will contain mime type and content which will be base64 encoded  or file object
	 * @return the attachmentLists
	 */
	public java.util.List<EmailAttachment> getAttachmentLists() {
		return attachmentLists;
	}
	
	

	/**
	 * @param attachmentLists the attachmentLists to set
	 */
	public void setAttachmentLists(java.util.List<EmailAttachment> attachmentLists) {
		this.attachmentLists = attachmentLists;
	}
	public java.util.List<String> getToAddress() {
		return toAddress;
	}
	public void setToAddress(java.util.List<String> toAddress) {
		this.toAddress = toAddress;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	
	
}

