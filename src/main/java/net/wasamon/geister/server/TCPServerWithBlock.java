package net.wasamon.geister.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.glassfish.tyrus.server.Server;

import net.wasamon.geister.utils.Constant;
import net.wasamon.mjlib.util.GetOpt;

public class TCPServerWithBlock {

	private final GameServer server;
	Server webSocketServer = new Server("localhost", 8080, "/ws", null, UIWebSocketServer.class);

	private ServerThread[] players = new ServerThread[2];
	
	private final int wait_time;

	private Object lock = new Object();

	public TCPServerWithBlock(GameServer server, int wait_time){
		this.server = server;
		this.wait_time = wait_time;
	}

	public void start() throws IOException{
		players[0] = new ServerThread(server, Constant.PLAYER_1st_PORT);
		players[1] = new ServerThread(server, Constant.PLAYER_2nd_PORT);
		players[0].start();
		players[1].start();
		try{
			players[0].join();
			players[1].join();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}


	class ServerThread extends Thread{

		final int port;
		final ServerSocketChannel server;
		SocketChannel ch;
		final GameServer game;
		
		public ServerThread(GameServer game, int port) throws IOException{
			this.game = game;
			this.port = port;
			this.server = ServerSocketChannel.open();
			this.server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			this.server.configureBlocking(true);
			this.server.bind(new InetSocketAddress(port));
		}

		private void doAccept(ServerSocketChannel server) throws IOException {
			SocketChannel c = server.accept();
			c.setOption(StandardSocketOptions.SO_LINGER, 10);
			if (this.port == Constant.PLAYER_1st_PORT && ch == null){
				System.out.println("1st Payer:" + c.socket().getRemoteSocketAddress());
				ch = c;
				send(c, "SET?\r\n");
			} else if (this.port == Constant.PLAYER_2nd_PORT && ch == null) {
				System.out.println("2nd Payer:" + c.socket().getRemoteSocketAddress());
				ch = c;
				send(c, "SET?\r\n");
			} else {
				c.close();
			}
		}

		private void send(SocketChannel ch, String msg) throws IOException {
			// should use OP_WRITE and check writable
			ByteBuffer bb = ByteBuffer.wrap(msg.getBytes());
			int len = 0;
			do {
				len += ch.write(bb);
			} while (len < msg.length());
		}

		private ByteBuffer bb = ByteBuffer.allocate(2048);
		private String restMesg = "";
		/**
		 * This method does action and returns a flag whether game should be continued or not.
		 */
		private boolean doRead(SocketChannel ch) throws IOException {
			if(ch == null || ch.isConnected() == false) return false;
			int pid = port == Constant.PLAYER_1st_PORT ? 0 : 1;
			bb.clear();
			int len = ch.read(bb);
			if (len < 0) { // channel has been closed
				System.out.println("connection closed: " + pid);
				doIrregularJudgement(pid);
				return false; // game end
			}
			bb.flip();
			String msg = Charset.defaultCharset().decode(bb).toString();
			restMesg += msg;
			boolean result = false;
			if (restMesg.indexOf("\r\n") > 0) { // at least, there is a message
				synchronized(lock){
					result = action(ch, restMesg, pid);
				}
			}
			if(result && game.getState() == GameServer.STATE.GAME_END){
				return false; // game end
			}else{
				return true;
			}
		}
		
		public void run(){
			try{
				body();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		private void body() throws IOException{
			doAccept(server);
			boolean flag = true;
			while(flag){
				try{
					flag = doRead(ch);
				}catch(IOException e){ // At least, a connection is disconnected during read.
					flag = false;
					if(port == Constant.PLAYER_1st_PORT){
						if(players[1] != null && players[1].ch != null && players[1].ch.isConnected()){
							send(players[1].ch, "WON:" + game.getEncodedBoard(1) + "\r\n");
						}
					}else{
						if(players[0] != null && players[0].ch != null && players[0].ch.isConnected()){
							send(players[0].ch, "WON:" + game.getEncodedBoard(0) + "\r\n");
						}
					}
				}
				if(flag == false){ // not should be continued.
					break;
				}
			}
			close();
		}

		private void close() throws IOException {
			if(ch != null){
				ch.shutdownOutput();
				ch.close();
			}
			if(server != null){
				server.close();
			}
		}
		
		private boolean action(SocketChannel chan, String str, int pid) throws IOException {
			boolean result = true;
			String lastTakenItemColor = "";

			int i = str.indexOf("\r\n");
			if (i == -1){
				// internal error; str should be a message. 
				return false;
			}
			String cmd = str.substring(0, i);
			str = str.substring(i + 2);
			result = game.parse(cmd, pid);
			lastTakenItemColor = game.getLastTakenItemColor();
			game.pp();
		
			restMesg = str;
		
			String stateLabel = "MOV:";
			if (result) {
				send(chan, String.format("OK%s\r\n", lastTakenItemColor));
				if (game.getState() == GameServer.STATE.WAIT_FOR_PLAYER_0) {
					send(players[0].ch, "MOV?" + game.getEncodedBoard(0) + "\r\n");
				} else if (game.getState() == GameServer.STATE.WAIT_FOR_PLAYER_1) {
					send(players[1].ch, "MOV?" + game.getEncodedBoard(1) + "\r\n");
				} else if (game.getState() == GameServer.STATE.GAME_END) {
					int winner = game.getWinner();
					if (winner == Constant.DRAW_MARK) {
						send(players[0].ch, "DRW:" + game.getEncodedBoard(0) + "\r\n");
						send(players[1].ch, "DRW:" + game.getEncodedBoard(1) + "\r\n");
						stateLabel = "DRW:";
					} else {
						int loser = winner == 0 ? 1 : 0;
						send(players[winner].ch, "WON:" + game.getEncodedBoard(winner) + "\r\n");
						send(players[loser].ch, "LST:" + game.getEncodedBoard(loser) + "\r\n");
						stateLabel = winner == 0 ? "WI0:" : "WI1:";
					}
				}
			} else {
				send(chan, "NG \r\n");
			}
			UIWebSocketServer.setMesg(stateLabel + game.getEncodedBoard(1, true)); // as global viewer mode
		
			return result;
		}

		private void doIrregularJudgement(int loser) throws IOException {
			int winner = loser == 0 ? 1 : 0;
			System.out.println("Connection closed by " + loser);
			if (players[winner].ch != null) {
				System.out.println("send a message for winner [" + winner + "]");
				send(players[winner].ch, "WON:" + game.getEncodedBoard(winner) + "\r\n");
			}
		}
		
	}

	public static void main(String[] args) throws Exception {
		System.out.println("TCPSrverWithBlock");
		GetOpt opt = new GetOpt("", "wait:", args);
		int wait_time = 100;
		if(opt.flag("wait")){
			wait_time = Integer.parseInt(opt.getValue("wait"));
		}
		TCPServerWithBlock s = new TCPServerWithBlock(new GameServer(), wait_time);
		s.webSocketServer.start();
		while(true){
            s.server.init();
			s.start();
			s.server.close();
		}
	}

}
