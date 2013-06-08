package com.ikokoon.serenity.process;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.persistence.IDataBase;

/**
 * This class listens on a port for the client to send a message to perform certain actions. At the time of writing the only action was to report, i.e. to dump
 * the collected data into the report files on the file system.
 * 
 * @author Michael Couck
 * @since 19.06.10
 * @version 01.00
 */
public class Listener extends AProcess {

	private IDataBase dataBase;

	public Listener(IProcess parent, IDataBase dataBase) {
		super(parent);
		this.dataBase = dataBase;
	}

	/**
	 * Sets up the listener to listen on a port for client instructions.
	 * 
	 * @param dataBase the database currently in use and open in the profiling session
	 */
	public void execute() {
		try {
			logger.warn("Starting socket listener on port : " + IConstants.PORT);
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					logger.warn("Running : " + this);
					ServerSocket serverSocket = null;
					try {
						serverSocket = new ServerSocket(IConstants.PORT);
						while (true) {
							try {
								Socket socket = serverSocket.accept();
								ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
								Object object = ois.readObject();
								logger.warn("Client message : " + object);
								if (String.class.isAssignableFrom(object.getClass())) {
									if (object.equals(IConstants.REPORT)) {
										new Reporter(null, dataBase).execute();
									} else if (object.equals(IConstants.LISTENING)) {
										ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
										oos.writeObject(IConstants.LISTENING);
									}
								}
							} catch (Exception e) {
								logger.error("Exception listening on the prot : " + IConstants.PORT, e);
							}
						}
					} catch (Exception e) {
						logger.error("Exception opening a socket, could be the firewall : ");
						logger.error("The listener will not be listening : ", e);
					} finally {
						try {
							serverSocket.close();
						} catch (IOException e) {
							logger.error("Exception closing the socket : ");
						}
					}
				}
			};
			timer.schedule(timerTask, 1000);
		} catch (Exception e) {
			logger.error("Exeption listening on the port : " + IConstants.PORT, e);
		}
	}
}
