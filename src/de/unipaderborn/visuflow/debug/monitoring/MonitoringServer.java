package de.unipaderborn.visuflow.debug.monitoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class MonitoringServer {

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private Thread t;
	private boolean running = true;
	private DataModel dataModel = ServiceUtil.getService(DataModel.class);

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
						JSONObject json = new JSONObject(line);
						String unitFqn = json.getString("unit");
						if(json.has("in")) {
							String in = json.getString("in");
							dataModel.setInSet(unitFqn, "in", in);
						}
						if(json.has("out")) {
							String out = json.getString("out");
							dataModel.setOutSet(unitFqn, "out", out);
						}
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
