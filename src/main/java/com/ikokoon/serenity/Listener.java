package com.ikokoon.serenity;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.persistence.IDataBase;

public class Listener {

	private static Logger LOGGER = Logger.getLogger(Listener.class);

	static class ConnectionHandler implements Runnable {

		private IDataBase dataBase;

		public ConnectionHandler(IDataBase dataBase) {
			this.dataBase = dataBase;
		}

		public void run() {
			LOGGER.warn("Running : " + this);
			try {
				ServerSocket serverSocket = new ServerSocket(PORT);
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
						Object object = ois.readObject();
						LOGGER.warn("Client message : " + object);
						if (String.class.isAssignableFrom(object.getClass())) {
							if (object.equals(IConstants.REPORT)) {
								Reporter.report(dataBase);
							}
						}
					} catch (Exception e) {
						LOGGER.error("Exception listening on the prot : " + PORT, e);
					}
				}
			} catch (Exception e) {
				LOGGER.error("Exception in the listener : ", e);
			}
		}
	}

	protected static int PORT = 50005;

	public static void listen(IDataBase dataBase) {
		try {
			LOGGER.warn("Starting socket listener : ");
			new Thread(new ConnectionHandler(dataBase)).start();
			LOGGER.warn("Started socket listener : ");
		} catch (Exception e) {
			LOGGER.error("Exeption listening on the port : " + PORT, e);
		}
	}
}
