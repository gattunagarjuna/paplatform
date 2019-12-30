package pa.platform.event;

public class PaNotificationEvent extends Event{

	public int outCount = 1;
	public int brandId;
	private Integer reportId;
	private Integer reportType;
	private Integer userId;
	private String userName;

	
	public PaNotificationEvent(int brandId,int reportId,int reportType,int userId,String userName) {
		super();
		this.brandId = brandId;
		this.reportId = reportId;
		this.reportType = reportType;
		this.userId = userId;
		this.userName = userName;
	}

	public int getBrandId() {
		return brandId;
	}

	public void setBrandId(int brandId) {
		this.brandId = brandId;
	}

	public int getOutCount() {
		return outCount;
	}

	public void setOutCount(int outCount) {
		this.outCount = outCount;
	}

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}

	public Integer getReportType() {
		return reportType;
	}

	public void setReportType(Integer reportType) {
		this.reportType = reportType;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
}
