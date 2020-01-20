package pa.platform.targeting.channel;

//import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;








import pa.platform.core.PaConfiguration;
import pa.platform.model.Notification;

import com.sendgrid.Attachments;
import com.sendgrid.Content;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

public class EmailClient {
	
	private static Logger logger = Logger.getLogger(EmailClient.class);
	
	private Notification notif;
	
	
	public EmailClient(Notification notif) {
		super();
		this.notif = notif;
	}


	public void sendMail(){
		logger.info("notif.getEmailAddress()  " + notif.getEmailAddress());
		logger.info("notif.getFromAddress()  " + notif.getFromAddress());
		logger.info("notif.getNotifcationText()  " + notif.getNotifcationText());

		com.sendgrid.Email from = new com.sendgrid.Email("qapricingstrategy@fishbowl.com");
		com.sendgrid.Email to = new com.sendgrid.Email(notif.getEmailAddress());;
		
	
		Content content = new Content("text/html", notif.getNotifcationText());
		Mail mail = new Mail(from, "test mail", to, content);
		
		SendGrid sg = new SendGrid(PaConfiguration.getInstance().getConfiguration("sendgridapikey"));
		Request request = new Request(); 
		request.setMethod(Method.POST);
		request.setEndpoint("mail/send");
		try {
			request.setBody(mail.build());
			Response response = sg.api(request);
			System.out.println(response.getStatusCode());
			System.out.println(response.getBody());
			System.out.println(response.getHeaders());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void sendImpactSheetEMail(List<String> filePaths, List<String> fileNames){
		logger.info("To Email Address : " + notif.getEmailAddress());
		/*logger.info("notif.getFromAddress()  " + notif.getFromAddress());
		logger.info("notif.getNotifcationText()  " + notif.getNotifcationText());*/

		com.sendgrid.Email from = new com.sendgrid.Email("qapricingstrategy@fishbowl.com");
		com.sendgrid.Email to = new com.sendgrid.Email(notif.getEmailAddress());;
		//com.sendgrid.Email to = new com.sendgrid.Email("ngattu_ic@fishbowl.com");
	
		Content content = new Content("text/html", "Impact Simulator Sheet");
		Mail mail = new Mail(from, "Impact Simulator Sheet", to, content);
		//ByteArrayOutputStream byteArrayOutputStream = getStreamOnData(fileName);
		
		
		
		/*Email emailRequest = new Email();
		if (null != byteArrayOutputStream && byteArrayOutputStream.toByteArray().length > 0) {
			List<EmailAttachment> attachmentList = new ArrayList<EmailAttachment>();
			EmailAttachment attachment = new EmailAttachment();
			attachment.setContent(byteArrayOutputStream.toString());
			attachment.setMimeType("text/html");
			attachment.setEncoding("UTF-8");
			attachment.setAttachmentName("ImpactSimulator");
			attachmentList.add(attachment);
			emailRequest.setAttachmentLists(attachmentList);
		
		}*/
		
		
		if(!filePaths.isEmpty() && !fileNames.isEmpty()){
			int i = 0;
			for(String filePath : filePaths){
				byte[] byteArray = readBytesFromFile(new File(filePath));
				String valueDecoded = Base64.encodeBase64String(byteArray);
				Attachments attachments = new Attachments();
				attachments.setContent(valueDecoded);
				attachments.setType("xls");
				attachments.setFilename(fileNames.get(i));
				mail.addAttachments(attachments);
				i=i+1;
			}
		}
			
		SendGrid sg = new SendGrid(PaConfiguration.getInstance().getConfiguration("sendgridapikey"));
		Request request = new Request(); 
		request.setMethod(Method.POST);
		request.setEndpoint("mail/send");
		try {
			request.setBody(mail.build());
			Response response = sg.api(request);
			System.out.println(response.getStatusCode());
			System.out.println(response.getBody());
			System.out.println(response.getHeaders());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	private static byte[] readBytesFromFile(File file) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }
	

	/*private ByteArrayOutputStream getStreamOnData(String fileName) {
		FileInputStream fis;
		 ByteArrayOutputStream bos = null;
		try {
			fis = new FileInputStream(fileName);
		
			bos = new ByteArrayOutputStream();
		    byte[] buf = new byte[1024];
		    for (int readNum; (readNum = fis.read(buf)) != -1;)
		    {
		      bos.write(buf, 0, readNum); //no doubt here is 0
		    }
		} catch (IOException ex) {
		            
		}return bos;
	
		}*/
	}
