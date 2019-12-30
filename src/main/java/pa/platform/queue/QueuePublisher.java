package pa.platform.queue;

import java.util.List;

import pa.platform.event.Event;
import pa.platform.event.PaNotificationEvent;

import com.microsoft.azure.storage.queue.CloudQueueMessage;



public interface QueuePublisher {
	public void initEventQueue();
	public String createQueue(String queueName);
	public void sendEventToQueue(String message);
	public List<Event> getEventsFromQueue();
	public List<Event> getEventsFromQueue(int numberOfMessages);
	public void deleteEventFromQueue(CloudQueueMessage message);
	
}
