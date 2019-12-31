
package pa.platform.queue.impl;


import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import pa.platform.core.PaConfiguration;
import pa.platform.core.PaConstants;
import pa.platform.event.Event;
import pa.platform.event.ImpactSimulatorEvent;
import pa.platform.event.PaNotificationEvent;
import pa.platform.event.ReportCommentEvent;
import pa.platform.event.ReportRequestEvent;
import pa.platform.event.ReportStatusEvent;
import pa.platform.queue.Messages;
import pa.platform.queue.QueuePublisher;






/*import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;*/
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;




public class QueuePublisherImpl implements QueuePublisher {
	private static Logger logger = Logger.getLogger(QueuePublisherImpl.class);
	public CloudStorageAccount azureStorageAccount = null;
	public CloudQueueClient queueClient = null;
	public CloudQueue queue=null;
	public String queueUrl;
	
	public QueuePublisherImpl() {
		super();
		initEventQueue();
	}
	
	private CloudStorageAccount createColudStorageAccount(){
		CloudStorageAccount storageAccount= null;
		try {
			storageAccount = CloudStorageAccount.parse(PaConfiguration.getInstance().getConfiguration(PaConstants.AZURE_STORAGE_ACCOUNT_CONNECTION));
        }
        catch (IllegalArgumentException|URISyntaxException e) {
        	logger.debug("AZURE Storage connection Error" +e.getMessage());
        }
        catch (InvalidKeyException e) {
        	logger.debug("AZURE Storage connection Error" +e.getMessage());
        }
		return storageAccount;

	}
	@Override
	public void initEventQueue() {
		azureStorageAccount=this.createColudStorageAccount();
		if(azureStorageAccount !=null){
			queueClient= azureStorageAccount.createCloudQueueClient();
		}
	}

	@Override
	public String createQueue(String queueName) {
		// Create a queue
		try {
		    CloudQueue cloudQueue = queueClient.getQueueReference(queueName.toLowerCase());
		    cloudQueue.createIfNotExists();
		    queue = cloudQueue;
		    queueUrl = queue.getUri().toString();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Queue not created : " + e.fillInStackTrace()); //$NON-NLS-1$
		}
		return queueUrl;
	}

	@Override
	public void sendEventToQueue(String message) {
		try {
			logger.info("sending azure queue message : " +queueUrl+ "  message  "+message);
			queue.addMessage(new CloudQueueMessage(message));
		} catch (Exception e) {
			logger.error(e.getMessage(), e.fillInStackTrace());
		}
	}
	
	private List<Event> getEventsFromQueueMethod(int numberOfMessages){

		List<Event> events = new ArrayList<Event>();
		//Messages messages = new Messages();
		try {
			for (CloudQueueMessage message : queue.retrieveMessages(numberOfMessages, 30, null, null)) {
				logger.debug(message.getMessageContentAsString());
				try {
					JSONObject tempobj = new JSONObject(message.getMessageContentAsString());
					Event event = null;
					String action = null;
					if(tempobj.has("action")){
					action = tempobj.getString("action");
					}else{
						logger.info("Incoming json do not have valid Action");
						continue;
					}
					int brandId = 0;
					if(tempobj.has("brandid")){
						brandId = tempobj.getInt("brandid");
					}
					String requestedOnBehalf = null;
					if(tempobj.has("requestedOnBehalf")){
						requestedOnBehalf = tempobj.getString("requestedOnBehalf");
					}
					
					if (action.equalsIgnoreCase("REPORTREQUESTEVENT")) {
						event = new ReportRequestEvent(action, brandId, tempobj.getInt("reportid"),
								tempobj.getInt("currentreportstage"), tempobj.getInt("reporttype"), tempobj.getInt("userid"),tempobj.getString("username"),
								requestedOnBehalf);
					}
					if (action.equalsIgnoreCase("REPORTCOMMENTEVENT")) {
						event = new ReportCommentEvent(action, brandId, tempobj.getInt("reportid"),
								tempobj.getInt("currentreportstage"), tempobj.getInt("reporttype"), tempobj.getInt("userid"),tempobj.getString("username"));
					}
					if (action.equalsIgnoreCase("REPORTSTATUSEVENT")) {
						event = new ReportStatusEvent(action, brandId, tempobj.getInt("reportid"),
								tempobj.getInt("currentreportstage"), tempobj.getInt("reporttype"), tempobj.getInt("userid"),tempobj.getString("username"),tempobj.getInt("newreportstage"));
					}
					if(action.equalsIgnoreCase("IMPACTSIMULATOREVENT")){
						logger.info("Action : "+action);
						logger.info("Forming impact simulator event");
						event = new ImpactSimulatorEvent(action,brandId,tempobj.getInt("userid"), tempobj.getBigInteger("projectid"), 
								tempobj.getBigInteger("scenarioid"));
						logger.info("impact simulator event is formed");
					}
					if (event != null){
						events.add(event);
					}
					
				} catch (Throwable e) {
					//e.printStackTrace();
					logger.error("Incoming json not valid: " + message.getMessageContentAsString() //$NON-NLS-1$
							+ " : ", e.fillInStackTrace()); //$NON-NLS-1$
				} finally {
					deleteEventFromQueue(message);
				}
			}
			} catch (StorageException e) {
				logger.error("Error while reading the messages from queue: "+e.getMessage());
			}
			return events;
	
		
	}

	@Override
	public List<Event> getEventsFromQueue() {
		return this.getEventsFromQueueMethod(1);
	}


	@Override
	public void deleteEventFromQueue(CloudQueueMessage message) {
		// Delete a message
		try {
			queue.deleteMessage(message);
		} catch (StorageException e) {
			logger.error("Error while deleting the message from queue :"+ e.getMessage());
		}	
	}

	
	@Override
	public List<Event> getEventsFromQueue(int numberOfMessages) {
		return this.getEventsFromQueueMethod(numberOfMessages);
	}

}
