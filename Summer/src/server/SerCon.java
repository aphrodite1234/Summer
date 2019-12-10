package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class SerCon {

	private volatile HashMap<Long,Socket> socketMap= new HashMap<>();
	private final int PORT = 5679;
	private ServerSocket server;
	private Socket client;
	
	
	public SerCon() {
		try {
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void invoke() {
		try{       
	        while (true){
	        	client =server.accept();
	    		
	        	Date date = new Date();//Socket链接时间
	        	String sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);      	
	        	System.out.println(sd+client);
	        	
	        	Receive receive = new Receive(client,socketMap);
	        	new Thread(receive,"RECEIVE").start();
	        }       
	    } catch (Exception e){
	        e.printStackTrace();
	    }
	}
}
