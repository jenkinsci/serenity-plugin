package com.ikokoon.serenity;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This simple class sends a message to the profiler to dump the data into the report files.
 *
 * @author Michael Couck
 * @since 19.06.10
 * @version 01.00
 */
public class Messenger {

	private static Logger LOGGER = Logger.getLogger(Messenger.class.getName());
	private static int PORT = 50005;

	public static void main(String[] args) {
		try {
			LOGGER.info("Sending message to port : " + PORT);
			InetAddress host = InetAddress.getByName(args[0]);
			Socket socket = new Socket(host.getHostName(), PORT);
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(args[1]);
			LOGGER.info("Sent message to ip : " + args[0] + ", command : " + args[1]);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception sending the message to Serenity : ", e);
			LOGGER.log(Level.SEVERE, "Usage : =>java -jar serenity.jar [127.0.0.1] [report]");
			LOGGER.log(Level.SEVERE, "Where '127.0.0.1' is the ip of the machine where Serenity is running and 'report' is the command.");
		}
	}

}
