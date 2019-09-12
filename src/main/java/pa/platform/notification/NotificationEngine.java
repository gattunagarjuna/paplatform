package pa.platform.notification;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import pa.platform.core.enums.MergeCodes;
import pa.platform.dao.impl.ReportDaoImpl;
import pa.platform.dao.impl.ReportEventNotificationDaoImpl;
import pa.platform.dao.impl.ReportTypeDaoImpl;
import pa.platform.dao.impl.TemplateDaoImpl;
import pa.platform.dao.impl.UserDaoImpl;
import pa.platform.mergecode.MergeCodeUtil;
import pa.platform.model.Notification;
import pa.platform.model.Report;
import pa.platform.model.ReportEventNotification;
import pa.platform.model.ReportType;
import pa.platform.model.Template;
import pa.platform.model.UserDetails;
import pa.platform.targeting.channel.EmailClient;
import pa.platform.targeting.channel.WebClient;

public class NotificationEngine {
	
	private static Logger logger = Logger.getLogger(NotificationEngine.class);
	
	Notification notif;
	
	public NotificationEngine(Notification notif) {
		super();
		this.notif = notif;
	}



	public void sendNotifications(){
		try{
			logger.info("start fetching user deatils");
			UserDetails userDetails = getUserDetails(notif.getUserId(), null,0).get(0);
			List<UserDetails> adminUsers = null;
			Report report = getReportById(notif.getReportId());
			ReportType reportType = getReportType(report.getType());
			notif.getMergeCodesMap().put(MergeCodes.REPORTTYPE.description(), reportType.getDescription());
			notif.getMergeCodesMap().put(MergeCodes.APPLICATIONURL.description(), getApplicationURL(notif));
			if(userDetails.getRoleId().contains(1001)){
				logger.info("loggedin user is a admin");
				notif.setRole("admin");
				notif.getMergeCodesMap().put(MergeCodes.REPORTNAME.description(),report.getName());
				UserDetails clientDetails = null;
				if(null != report.getRequestedOnBehalf()){
					clientDetails = getUserDetails(0,report.getRequestedOnBehalf(),0).get(0);
				}else{
					clientDetails = getUserDetails(0,report.getCreatedBy(),0).get(0);
				}
				notif.setEmailAddress(clientDetails.getEmail());
				notif.getMergeCodesMap().put(MergeCodes.ADMINUSERNAME.description(), userDetails.getUserName());
				notif.getMergeCodesMap().put(MergeCodes.CLIENTUSERNAME.description(), clientDetails.getUserName());
			}else{
				logger.info("loggedin user is a client");
				notif.setRole("client");
				adminUsers = getUserDetails(0,null,notif.getBrandId());
				notif.getMergeCodesMap().put(MergeCodes.CLIENTUSERNAME.description(),userDetails.getUserName());
				notif.getMergeCodesMap().put(MergeCodes.ADMINUSERNAME.description(),adminUsers.get(0).getUserName());
				notif.getMergeCodesMap().put(MergeCodes.REPORTNAME.description(),report.getName());
			}
			
			getUserName(userDetails);
			List<ReportEventNotification> reportEventNotifications = getReportEventNotifications(notif,userDetails.getRoleId().contains(1001));
			if(reportEventNotifications.isEmpty()){
				logger.info("no reporteventnotifications in db and hence returning");
				return;
			}
			sendNotifications(reportEventNotifications,adminUsers);
		
			
		}catch(Exception ex){
			
		}
		
		
		
	}
	
	private ReportType getReportType(int type) {
		ReportTypeDaoImpl reportTypeDaoImpl = new ReportTypeDaoImpl();
		ReportType reportType = reportTypeDaoImpl.getReportType(type);
		return reportType;
	}



	private Report getReportById(int reportId) {
		ReportDaoImpl reportDaoImpl = new ReportDaoImpl();
		Report report = reportDaoImpl.getReportById(reportId);
		return report;
	}



	private void sendNotifications(List<ReportEventNotification> reportEventNotifications,List<UserDetails> adminUsers) {
		
		for(ReportEventNotification notification :  reportEventNotifications){
			Template template = getTemplate(notification.getTemplateId());
			if(null != template){
				String webNotifContent = template.getTemplate();
				webNotifContent = MergeCodeUtil.resolveMergeCodes(webNotifContent,notif.getMergeCodesMap());
				notif.setNotifcationText(webNotifContent);
				switch (notification.getActionsId()){
					case 1:
						sendEmailNotification(reportEventNotifications,adminUsers);
						break;
					case 2:
						break;
					case 3:
						break;
					case 4:
						sendWebNotification();
						break;
					default :
						break;
				}
			}
			
		}
		
	}



	private void sendWebNotification() {
		WebClient webClient = new WebClient(notif);
		webClient.saveWebNotification();
		
	}



	private void sendEmailNotification(	List<ReportEventNotification> reportEventNotifications,List<UserDetails> adminUsers) {
		if(null != adminUsers && !adminUsers.isEmpty()){
			for(UserDetails adminUser : adminUsers){
				if(adminUser.getLastName() != null && adminUser.getLastName().trim().length()>0 
						&& (adminUser.getFirstName()== null&&adminUser.getFirstName().trim().length()==0)){
					notif.setUserName(adminUser.getLastName());
				}else if(adminUser.getFirstName() != null&& adminUser.getFirstName().trim().length()>0  && 
						(adminUser.getLastName() == null||adminUser.getLastName().trim().length()==0)){
					notif.setUserName(adminUser.getFirstName());
				}else if(adminUser.getLastName() != null && adminUser.getLastName().trim().length()>0 
						&& adminUser.getFirstName()!= null && adminUser.getFirstName().trim().length()>0){
					notif.setUserName(adminUser.getFirstName()+" "+adminUser.getLastName());
				}else{
					notif.setUserName(adminUser.getUserName());
				}
				notif.getMergeCodesMap().put(MergeCodes.ADMINUSERNAME.description(),adminUser.getUserName());
				Map< String, String> adminHashMap = new HashMap<String, String>();
				adminHashMap.put(MergeCodes.ADMINUSERNAME.description(),adminUser.getUserName());
				notif.setNotifcationText( MergeCodeUtil.resolveMergeCodes(notif.getNotifcationText(),adminHashMap));
				notif.setEmailAddress(adminUser.getEmail());
				//logger.info("eventNotif.getActionsId() with admin user  sendEmailNotification " + notif);
				EmailClient emailClient = new EmailClient(notif);
				emailClient.sendMail();
			}
		}else{
			//logger.info("eventNotif.getActionsId() without admin users " + notif);
			EmailClient emailClient = new EmailClient(notif);
			emailClient.sendMail();
		}
		
	}



	private void getUserName(UserDetails userDetails) {
		if(userDetails.getLastName() != null && userDetails.getLastName().trim().length()>0 
				&& (userDetails.getFirstName()== null&&userDetails.getFirstName().trim().length()==0)){
			notif.setUserName(userDetails.getLastName());
		}else if(userDetails.getFirstName() != null&& userDetails.getFirstName().trim().length()>0  && 
				(userDetails.getLastName() == null||userDetails.getLastName().trim().length()==0)){
			notif.setUserName(userDetails.getFirstName());
		}else if(userDetails.getLastName() != null && userDetails.getLastName().trim().length()>0 
				&& userDetails.getFirstName()!= null && userDetails.getFirstName().trim().length()>0){
			notif.setUserName(userDetails.getFirstName()+" "+userDetails.getLastName());
		}else{
			notif.setUserName(userDetails.getUserName());
		}
	}
	
	private List<UserDetails> getUserDetails(int userId,String userName,int brandId){
		UserDaoImpl userDao = new UserDaoImpl();
		//UserDetails userDetails = null;
		List<UserDetails> userDetailsList = new ArrayList<UserDetails>();
		try {
			if(null == userName && userId > 0){
				UserDetails userDetails = userDao.getUserDetailsByUserId(notif.getUserId());
				userDetailsList.add(userDetails);
			}else if(null!= userName && userId == 0){
				UserDetails userDetails = userDao.getUserDetailsByUserName(userName);
				userDetailsList.add(userDetails);
			}else{
				
				userDetailsList = userDao.getAdminUsersByBrandId(brandId);
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
		
		
		return userDetailsList;
		
	}
	
	private List<ReportEventNotification> getReportEventNotifications(Notification notif , boolean isAdmin){
		
		List<ReportEventNotification> reportEventNotifications = new ArrayList<ReportEventNotification>();
		ReportEventNotificationDaoImpl eventNotifs = new ReportEventNotificationDaoImpl();
		try {
			reportEventNotifications = eventNotifs.getReportEventNotification(notif.getBrandId(), notif.getEventId(), isAdmin);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportEventNotifications;
		
	}
	
	private Template getTemplate(int templateId){
		TemplateDaoImpl templateDaoImpl = new TemplateDaoImpl();
		return templateDaoImpl.getTemplate(templateId);
		
	}
	
	private String getApplicationURL(Notification notif){
		String appURL = "https://" +"pa2qa.fishbowl.com" + "/#/reports/details/"+notif.getReportId();
		logger.info("Application URL : "+appURL);
		return appURL;
	}

}
