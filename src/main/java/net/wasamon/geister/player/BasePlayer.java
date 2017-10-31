package net.wasamon.geister.player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import net.wasamon.geister.utils.Board;
import net.wasamon.geister.utils.Item;
import net.wasamon.geister.utils.Constant;
import net.wasamon.geister.utils.Direction;

public abstract class BasePlayer {
	
    public enum ID{
		PLAYER_0,
		PLAYER_1
    }
	
    private SocketChannel channel;
    private boolean won;
    private boolean lost;
    private boolean draw;

    public final int OwnPlayerId = 0;
    public final int OppositePlayerId = 1;
	
    /**
     * Constructor, opens TCP connection
     * @param host server's name
     * @param port server's port(1st player=10000, 2nd player=10001)
     */
    public final void init(String host, int port) throws IOException{
		try{
			channel = SocketChannel.open(new InetSocketAddress(InetAddress.getByName(host), port));
			channel.configureBlocking(true);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		won = false;
		lost = false;
		draw = false;
		recv();
    }

    public final void close() throws IOException{
		channel.close();
    }

    private boolean verbose = false;
    public final void setVerbose(boolean flag){
		verbose = flag;
    }
	
    private String boardInfo = "";
	
    private String lastTookColor = ""; 

	String restMesg = "";
    private String recv() throws IOException{
		ByteBuffer bb = ByteBuffer.allocate(2048);
        lastTookColor = "";
		while(!(restMesg.indexOf("\r\n") > 0)){
			bb.clear();
			int len = channel.read(bb);
			if(len == -1){
				throw new RuntimeException("channel is not opend.");
			}
			bb.flip();
			restMesg += Charset.defaultCharset().decode(bb).toString();
		}
		
		int i = restMesg.indexOf("\r\n");
		String s = restMesg.substring(0, i);
		restMesg = restMesg.substring(i + 2);
	       
		System.out.println(s);
		if(s.startsWith("MOV?")){
			boardInfo = s;
		}else if(s.startsWith("WON")){
			boardInfo = s;
			won = true;
		}else if(s.startsWith("LST")){
			boardInfo = s;
			lost = true;
		}else if(s.startsWith("DRW")){
			boardInfo = s;
			draw = true;
		}else if(s.startsWith("OK")){
		    lastTookColor = s.substring(2, 3);
		}
		if(verbose) System.out.println(s);
		System.out.println("rest:" + restMesg);
		return s;
    }
    
    public String getLastTookColor(){
        return lastTookColor;
    }
    
    public String waitBoardInfo() throws IOException{
        while(true){
            String s = recv();
            //System.out.println("waitBoardInfo:" + s);
            if(s.startsWith("MOV?") || s.startsWith("WON") || s.startsWith("LST") || s.startsWith("DRW")){
                return s;
            }
        }
    }

    private void send(String msg) throws IOException{
		if(verbose) System.out.println(msg);
		int len = 0;
		do{
			len += channel.write(ByteBuffer.wrap(msg.getBytes()));
		}while(len < msg.length());
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

    public boolean move(String cmd) throws IOException{
		if(isEnded() == false){
			send("MOV:" + cmd + "\r\n");
			return !isFailed(recv());
		}else{
			return false;
		}
    }

    public boolean isWinner(){
		return won;
    }
	
    public boolean isLoser(){
		return lost;
    }
    
    public boolean isDraw(){
		return draw;
    }
	
    public boolean isEnded(){
		return isWinner() || isLoser() || isDraw();
    }
	
    private Board getBoard(){
		String str = boardInfo.substring("MOV?".length());
		return Board.decode(str);
    }
	
    public Item[] getOwnItems(){
		return getBoard().getPlayer(OwnPlayerId).getItems();
    }
    
    public Item[] getOppositeItems(){
		return getBoard().getPlayer(OppositePlayerId).getItems();
    }

    public Item[] getOwnTakenItems(){
		return getBoard().getPlayer(OwnPlayerId).getTakenItems();
    }
    
    public Item[] getOppositeTakenItems(){
		return getBoard().getPlayer(OppositePlayerId).getTakenItems();
    }

    public void printBoard(){
		Board b = getBoard();
		System.out.println(" opposite");
		System.out.println(b.getBoardMap(OwnPlayerId));
		System.out.println(" own side");
		// print all items
        System.out.print("own items:");
        for(Item i: b.getPlayer(OwnPlayerId).getItems()){
			System.out.print(" " + i.getName() + ":" + i.getColor().getSymbol());
        }
        System.out.println();
        System.out.print("opposite player's items:");
        for(Item i: b.getPlayer(OppositePlayerId).getItems()){
			System.out.print(" " + i.getName().toLowerCase() + ":" + i.getColor().getSymbol());
        }
        System.out.println("");
        
        // print taken items
        System.out.print("taken own items:");
        for(Item i: b.getPlayer(OwnPlayerId).getTakenItems()){
			System.out.print(" " + i.getName() + ":" + i.getColor().getSymbol());
        }
        System.out.println("");
        System.out.print("taken opposite player's items:");
        for(Item i: b.getPlayer(OppositePlayerId).getTakenItems()){
			System.out.print(" " + i.getName().toLowerCase() + ":" + i.getColor().getSymbol());
        }
        System.out.println("");
        
        // print escaped items
        System.out.print("escaped own player's items:");
        for(Item i: b.getPlayer(OwnPlayerId).getEscapedItems()){
			System.out.print(" " + i.getName() + ":" + i.getColor().getSymbol());
        }
        System.out.println("");
        System.out.print("escaped opposite player's items:");
        for(Item i: b.getPlayer(OppositePlayerId).getEscapedItems()){
			System.out.print(" " + i.getName().toLowerCase() + ":" + i.getColor().getSymbol());
        }
        System.out.println("");
	
    }
}
