package de.unipaderborn.visuflow.debug.monitoring;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class MonitoringServer {

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private Thread t;
	private boolean running = true;
	private DataModel dataModel = ServiceUtil.getService(DataModel.class);
	private Logger logger = Visuflow.getDefault().getLogger();

	public void start() {
		logger.info("Monitoring server starting");
		t = new Thread() {
			@Override
			public void run() {
				try {
					serverSocket = new ServerSocket(6543);
					clientSocket = serverSocket.accept();

					DataInputStream in = new DataInputStream(clientSocket.getInputStream());
					while(running) {
						String unitFqn = in.readUTF();
						String inSet = in.readUTF();
						String outSet = in.readUTF();
						dataModel.setInSet(unitFqn, "in", inSet);
						dataModel.setOutSet(unitFqn, "out", outSet);
					}
				} catch (EOFException e) {
					logger.info("No more data. The client probably closed the connection");
				} catch (IOException e) {
					logger.error("Monitoring server threw an exception", e);
				}
			}
		};
		t.setDaemon(true);
		t.setName("Analysis Monitoring Server");
		t.start();
	}

	public void stop() {
		logger.info("Monitoring server stopping");
		running = false;
		t.interrupt();

		if(clientSocket != null && !clientSocket.isClosed()) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				logger.error("Couldn't close monitoring server connection", e);
			}
		}
		if(serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				logger.error("Couldn't close monitoring server connection", e);
			}
		}
	}
}
