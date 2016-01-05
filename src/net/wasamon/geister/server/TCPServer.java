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

public class TCPServer{
	
	private static final int PORT_PLAYER_0 = 10000;
	
	private static final int PORT_PLAYER_1 = 10001;
	
	private final GameServer server = new GameServer();
	
	private Selector selector;
	private ServerSocketChannel[] serverChannels;
	private SocketChannel[] playerChannels;
	private String[] restMesg;
	
	public TCPServer() throws IOException{
		selector = Selector.open();
		serverChannels = new ServerSocketChannel[2];
		serverChannels[0] = initChannel(PORT_PLAYER_0);
		serverChannels[1] = initChannel(PORT_PLAYER_1);
		serverChannels[0].register(selector, SelectionKey.OP_ACCEPT);
		serverChannels[1].register(selector, SelectionKey.OP_ACCEPT);
	}
	
	private ServerSocketChannel initChannel(int port) throws IOException{
		ServerSocketChannel server = ServerSocketChannel.open();
		server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		server.configureBlocking(false);
		//server.setOption(StandardSocketOptions.SO_RCVBUF, 16384);
		//server.setOption(StandardSocketOptions.SO_SNDBUF, 16384);
		server.bind(new InetSocketAddress(port));
		return server;
	}

	private void closePlayers() throws IOException{
		for(SocketChannel ch: playerChannels){
			ch.close();
		}
	}


	private void send(SocketChannel ch, String msg) throws IOException{
		ch.write(ByteBuffer.wrap(msg.getBytes()));
	}
	
	private String action(SocketChannel chan, String str, int pid) throws IOException{
		boolean result = true;
		
		while(true){
	        int i = str.indexOf("\r\n");
	        if(i == -1) break;
	        String cmd = str.substring(0, i);
	        str = str.substring(i+2);
	        result = server.parse(cmd, pid);
	        server.pp();
		}
		
		if(result){
			if(server.getState() == GameServer.STATE.WAIT_FOR_PLAYER_0){
				send(playerChannels[0], "MOV?" + server.getEncodedBoard(0) + "\r\n");
			}else if(server.getState() == GameServer.STATE.WAIT_FOR_PLAYER_1){
				send(playerChannels[1], "MOV?" + server.getEncodedBoard(1) + "\r\n");
			}else if(server.getState() == GameServer.STATE.GAME_END){
				int winner = server.getWinner();
				int loser = winner == 0 ? 1 : 0; 
				send(playerChannels[winner], "WON:" + server.getEncodedBoard(0) + "\r\n");
				send(playerChannels[loser], "LST:" + server.getEncodedBoard(1) + "\r\n");
				server.init();
				closePlayers();
				start();
			}
		}else{
			send(chan, "NG\r\n");
		}
		
		return str;
		
	}	
	
	public void start() throws IOException{
		playerChannels = new SocketChannel[2];
		restMesg = new String[]{"", ""};
		
		while(selector.select() > 0 || selector.selectedKeys().size() > 0){
			for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
				SelectionKey key = it.next();
                it.remove();
                if(!key.isValid()){
                	
                }else if (key.isAcceptable()) {
                    doAccept(selector, (ServerSocketChannel)key.channel());
                } else if (key.isReadable()) {
                	doRead(selector, (SocketChannel)key.channel());
                }
            }			
		}
	}
	
	private void doAccept(Selector selector, ServerSocketChannel server) throws IOException{
		SocketChannel c = server.accept();
		c.configureBlocking(false);
		c.register(selector, SelectionKey.OP_READ);
		if(server == serverChannels[0] && playerChannels[0] == null){
			System.out.println("1st Payer:" + c.socket().getRemoteSocketAddress());
			playerChannels[0] = c;
			c.write(ByteBuffer.wrap("SET?\r\n".getBytes()));
		}else if(server == serverChannels[1] && playerChannels[1] == null){
			System.out.println("2nd Payer:" + c.socket().getRemoteSocketAddress());
			playerChannels[1] = c;
			c.write(ByteBuffer.wrap("SET?\r\n".getBytes()));
		}else{
			c.close();
		}
	}
	
	private void doRead(Selector selector, SocketChannel ch) throws IOException{
		ByteBuffer bb = ByteBuffer.allocate(2048);
		if(ch.read(bb) < 0){
			return;
		}
		int pid = 0;
		if(ch == playerChannels[0]){
			pid = 0;
		}else if(ch == playerChannels[1]){
			pid = 1;
		}else{ // should not receive from unknown socket... 
			ch.close();
			return;
		}
		bb.flip();
		String msg = Charset.defaultCharset().decode(bb).toString();
		restMesg[pid] += msg;
		restMesg[pid] = action(ch, restMesg[pid], pid);
	}
	
	public static void main(String[] args) throws IOException{
		TCPServer s = new TCPServer();
		s.start();
	}
	
}
