package com.ikokoon.serenity;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Messenger {

	private static Logger LOGGER = Logger.getLogger(Messenger.class.getName());

	public static void main(String[] args) {
		try {
			InetAddress host = InetAddress.getByName(args[0]);
			Socket socket = new Socket(host.getHostName(), 50005);
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			LOGGER.info("Sending message : ");
			oos.writeObject(args[1]);
			LOGGER.info("Sent message : ");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception sending the message to Serenity : ", e);
			LOGGER.log(Level.SEVERE, "Usage : =>java -jar serenity.jar [127.0.0.1] [report]");
			LOGGER.log(Level.SEVERE, "Where '127.0.0.1' is the ip of the machine where Serenity is running and 'report' is the command.");
		}
	}

}
