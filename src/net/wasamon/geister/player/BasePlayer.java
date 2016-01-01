package net.wasamon.geister.player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import net.wasamon.geister.server.Board;
import net.wasamon.geister.server.Item;
import net.wasamon.geister.utils.Direction;

public abstract class BasePlayer {
	
    public enum ID{
	PLAYER_0,
	PLAYER_1
    }
	
    private SocketChannel channel;
    private boolean won;
    private boolean lost;
	
    /**
     * Constructor, opens TCP connection
     * @param pid player id, Player.ID.PLAYER_0 or BasicPlayer.ID.PLAYER_1
     */
    public final void init(ID id) throws IOException{
	int port = id == ID.PLAYER_0 ? 10000 : 10001;
	try{
	    channel = SocketChannel.open(new InetSocketAddress(port));
	    channel.configureBlocking(true);
	}catch(IOException e){
	    throw new RuntimeException(e);
	}
	won = false;
	lost = false;
	recv();
    }
	
    private String boardInfo = "";
	
    private String recv() throws IOException{
	ByteBuffer bb = ByteBuffer.allocate(2048);
	channel.read(bb);
	bb.flip();
	String s = Charset.defaultCharset().decode(bb).toString();
	if(s.startsWith("MOV?")){
	    boardInfo = s;
	}else if(s.startsWith("WON")){
	    boardInfo = s;
	    won = true;
	}else if(s.startsWith("LST")){
	    boardInfo = s;
	    lost = true;
	}
	System.out.println(s);
	return s;
    }

    private void send(String msg) throws IOException{
	System.out.println(msg);
	channel.write(ByteBuffer.wrap(msg.getBytes()));
    }
	
    private boolean isFailed(String mesg){
	return mesg.startsWith("NG");
    }
	
    public boolean setRedItems(String keys) throws IOException{
	if(keys.length() != 4){
	    return false;
	}
	send("SET:" + keys + "\r\n");
	return !isFailed(recv());
    }
	
    public boolean move(String key, Direction dir) throws IOException{
	if(isEnded() == false){
	    send("MOV:" + key + "," + dir + "\r\n");
	    return !isFailed(recv());
	}else{
	    return false;
	}
    }
	
    public boolean isWinner(){
	return won;
    }
	
    public boolean isLooser(){
	return lost;
    }
	
    public boolean isEnded(){
	return isWinner() || isLooser();
    }
	
    private Board getBoard(){
	String str = boardInfo.substring("MOV?".length());
	return Board.decode(str);
    }
	
    public void printBoard(){
	System.out.println(" opposite");
	System.out.println(getBoard().getBoardMap(0));
	System.out.println(" own side");
    }
	
    public Item[] getOwnItems(){
	return getBoard().getPlayer(0).getItems();
    }
    
    public Item[] getOppositeItems(){
	return getBoard().getPlayer(1).getItems();
    }

}
