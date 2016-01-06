package net.wasamon.geister.server;

import org.glassfish.tyrus.server.Server;

public class UIWebSocketServerMain {

	public static void main(String[] args) throws Exception {
		Server server = new Server("localhost", 8080, "/ws", null, UIWebSocketServer.class);
		try {
			server.start();
			System.in.read();
		} finally {
			server.stop();
		}
	}
}
