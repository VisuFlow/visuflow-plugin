package de.unipaderborn.visuflow.debug.monitoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MonitoringServer {

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private Thread t;
	private boolean running = true;

	public void start() {
		System.out.println("Monitoring server starting");
		t = new Thread() {
			@Override
			public void run() {
				try {
					serverSocket = new ServerSocket(6543);
					clientSocket = serverSocket.accept();

					BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String line;
					while(running && (line = br.readLine()) != null) {
						System.out.println("ANALYSIS " + line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		t.setDaemon(true);
		t.setName("");
		t.start();
	}

	public void stop() {
		System.out.println("Monitoring server stopping");
		running = false;
		t.interrupt();

		if(clientSocket != null && !clientSocket.isClosed()) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
