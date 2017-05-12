package org.komparator.mediator.ws;

import java.time.Duration;
import java.time.LocalDateTime;

import org.komparator.mediator.ws.cli.MediatorClient;

public class LifeProof extends Thread {
	private MediatorEndpointManager endpoint;
	private MediatorClient client = null;
	private int seconds = 5; 
	private boolean running = true;
	
	public LifeProof(MediatorEndpointManager endpoint){
		this.endpoint = endpoint;
	}
	
	public void run(){
		while (running){
			try {
				if (!endpoint.isSecondary()){
					if(client == null)
						client = new MediatorClient("http://localhost:8072/mediator-ws/endpoint");
					try{
						client.imAlive();
						System.out.println("Sent imAlive message to secondary.");
					} catch (Exception e){
						System.out.println("No secondary mediator running!");
					}
				} else {
					if (endpoint.getLastAliveDate() != null && Duration.between(endpoint.getLastAliveDate(), LocalDateTime.now()).getSeconds() > 5){
						//Secondary assumes primary role
						endpoint.setWsPort("8072");
						endpoint.setSecondary(false);
						endpoint.publishToUDDI();
						this.running = false;   //thread no longer needed after primary shutdown
					}	
				}
				Thread.sleep(seconds*1000);	
			} catch (Exception e){
				System.out.println("Cant sleep!");
			}
		}
	}
	
	public void terminate(){
		synchronized(this){
			this.running = false;
		}
	}
}
