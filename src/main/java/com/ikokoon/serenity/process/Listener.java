package com.ikokoon.serenity.process;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.persistence.IDataBase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * This class listens on a port for the client to send a message to perform certain actions. At the time of writing the only action was to report, i.e. to dump
 * the collected data into the report files on the file system.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19.06.10
 */
public class Listener extends AProcess {

    private IDataBase dataBase;

    public Listener(IProcess parent, IDataBase dataBase) {
        super(parent);
        this.dataBase = dataBase;
    }

    /**
     * Sets up the listener to listen on a port for client instructions.
     */
    public void execute() {
        try {
            logger.info("Starting socket listener on port : " + IConstants.PORT);
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    logger.info("Running : " + this);
                    ServerSocket serverSocket = null;
                    try {
                        serverSocket = new ServerSocket(IConstants.PORT);
                        //noinspection InfiniteLoopStatement
                        while (true) {
                            try {
                                Socket socket = serverSocket.accept();
                                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                                Object object = ois.readObject();
                                logger.info("Client message : " + object);
                                if (String.class.isAssignableFrom(object.getClass())) {
                                    if (object.equals(IConstants.REPORT)) {
                                        new Reporter(null, dataBase).execute();
                                    } else if (object.equals(IConstants.LISTENING)) {
                                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                        oos.writeObject(IConstants.LISTENING);
                                    }
                                }
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Exception listening on the prot : " + IConstants.PORT, e);
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Exception opening a socket, could be the firewall, the listener will not be listening : ", e);
                    } finally {
                        try {
                            if (serverSocket != null) {
                                serverSocket.close();
                            }
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Exception closing the socket : ");
                        }
                    }
                }
            };
            timer.schedule(timerTask, 1000);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception listening on the port : " + IConstants.PORT, e);
        }
    }
}
