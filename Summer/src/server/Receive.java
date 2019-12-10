package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import utils.DataDeal;

public class Receive implements Runnable {

	private BufferedReader reader = null;
	
	private Socket client;
	private String message;
	private long receiveTime = System.currentTimeMillis();
	private HashMap<Long,Socket> socketMap;

	public Receive(Socket socket,HashMap<Long,Socket> socketMap) {
		client = socket;
		this.socketMap = socketMap;
	}

	public void run() {
		DataDeal dataDeal = new DataDeal(client,socketMap);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!Thread.currentThread().isInterrupted()) {
					if(System.currentTimeMillis()-receiveTime>30*1000) {
						System.out.println((System.currentTimeMillis()-receiveTime)/1000);
						if(dataDeal.userUtils!=null) {
							System.out.println("logout");
							dataDeal.userUtils.logout();
						}else {
							System.out.println("interrupt");
							Thread.currentThread().interrupt();
							try {
								client.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}).start();
		
		try {
			reader = new BufferedReader(new InputStreamReader(client.getInputStream()));		
			while ((message = reader.readLine())!=null) {
				receiveTime = System.currentTimeMillis();
				dataDeal.deal(message);
			}
		} catch (Exception e) {
			try {
				client.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
