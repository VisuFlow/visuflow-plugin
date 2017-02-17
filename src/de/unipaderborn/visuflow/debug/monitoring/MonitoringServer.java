package de.unipaderborn.visuflow.debug.monitoring;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private Lock lock = new ReentrantLock();

	public void start() {
		logger.info("Monitoring server starting");
		logger.info("Server launcher setting lock");
		lock.lock();
		t = new Thread() {
			@Override
			public void run() {
				try {
					logger.info("Monitoring server setting lock");
					lock.lock();
					serverSocket = new ServerSocket(6543);
					logger.info("Monitoring server unlock");
					lock.unlock();
					clientSocket = serverSocket.accept();

					DataInputStream in = new DataInputStream(clientSocket.getInputStream());
					DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
					while(running) {
						String msgType = in.readUTF();
						if(msgType.equals("CLOSE")) {
							logger.info("Client closed the connection");
							out.writeUTF("OK");
							out.flush();
							MonitoringServer.this.stop();
						} else if(msgType.equals("UNIT_UPDATE")) {
							String unitFqn = in.readUTF();
							String inSet = in.readUTF();
							String outSet = in.readUTF();
							dataModel.setInSet(unitFqn, "in", inSet);
							dataModel.setOutSet(unitFqn, "out", outSet);
						}
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
		logger.info("Server launcher unlock");
		lock.unlock();
	}

	public boolean waitForServer(int millis) {
		logger.info("Delegate setting lock");
		try {
			Thread.sleep(1000); // give it a second to start
			lock.tryLock(millis, TimeUnit.MILLISECONDS);
			lock.unlock();
			return true;
		} catch (InterruptedException e) {
			logger.error("Couldn't wait for server to start", e);
			return false;
		}
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
