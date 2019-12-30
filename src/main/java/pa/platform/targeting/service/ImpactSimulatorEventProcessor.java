package pa.platform.targeting.service;

import org.apache.log4j.Logger;

import pa.platform.event.ImpactSimulatorEvent;
import pa.platform.notification.DataLoadingEngine;

public class ImpactSimulatorEventProcessor implements Runnable{

	private static Logger logger = Logger.getLogger(ImpactSimulatorEventProcessor.class);
	
	private ImpactSimulatorEvent event;
	
	public ImpactSimulatorEventProcessor(ImpactSimulatorEvent event) {
		this.event = event;
	}
	
	@Override
	public void run() {
		//String filename="D:/ImpactSimulator.xls" ;
		ImpactSimulatorEvent impSimEvent = (ImpactSimulatorEvent)event;
		startLoadingData(impSimEvent);
		
		
		
	}

	private void startLoadingData(ImpactSimulatorEvent impSimEvent) {
		DataLoadingEngine dataLoadingEngine = new DataLoadingEngine(impSimEvent);
		dataLoadingEngine.startLoadingData();
		
	}


		

	
}
