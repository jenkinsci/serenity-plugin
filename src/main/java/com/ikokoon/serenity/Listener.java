package com.ikokoon.serenity;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.persistence.IDataBase;

/**
 * This class listens on a port for the client to send a message to perform certain actions. At the time of writing the only action was to report,
 * i.e. to dump the collected data into the report files on the file system.
 *
 * @author Michael Couck
 * @since 19.06.10
 * @version 01.00
 */
public class Listener {

	private static Logger LOGGER = Logger.getLogger(Listener.class);
	protected static int PORT = 50005;

	/**
	 * This class handles the socket to listen to.
	 *
	 * @author Michael Couck
	 */
	static class ConnectionHandler implements Runnable {

		/** The database that will be used in the processing. */
		private IDataBase dataBase;

		/**
		 * Constructor takes the database currently in use in the Jvm.
		 *
		 * @param dataBase
		 *            the currently used open database for this profiling session
		 */
		public ConnectionHandler(IDataBase dataBase) {
			this.dataBase = dataBase;
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			LOGGER.warn("Running : " + this);
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(PORT);
			} catch (Exception e) {
				LOGGER.error("Exception opening a socket, could be the firewall : ");
				LOGGER.error("The listener will not be listening : ", e);
				return;
			}
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					Object object = ois.readObject();
					LOGGER.warn("Client message : " + object);
					if (String.class.isAssignableFrom(object.getClass())) {
						if (object.equals(IConstants.REPORT)) {
							Reporter.report(dataBase);
						} else if (object.equals(IConstants.LISTENING)) {
							ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
							oos.writeObject(IConstants.LISTENING);
						}
					}
				} catch (Exception e) {
					LOGGER.error("Exception listening on the prot : " + PORT, e);
				}
			}
		}
	}

	/**
	 * Sets up the listener to listen on a port for client instructions.
	 *
	 * @param dataBase
	 *            the database currently in use and open in the profiling session
	 */
	public static void listen(IDataBase dataBase) {
		try {
			LOGGER.warn("Starting socket listener on port : " + PORT);
			new Thread(new ConnectionHandler(dataBase)).start();
		} catch (Exception e) {
			LOGGER.error("Exeption listening on the port : " + PORT, e);
		}
	}
}
