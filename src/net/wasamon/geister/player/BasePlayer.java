package net.wasamon.geister.player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import net.wasamon.geister.server.Board;
import net.wasamon.geister.server.Item;
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

    public final int OwnPlayerId = 0;
    public final int OppositePlayerId = 1;
	
    /**
     * Constructor, opens TCP connection
     * @param pid player id, Player.ID.PLAYER_0 or BasicPlayer.ID.PLAYER_1
     */
    public final void init(ID id) throws IOException{
	int port = id == ID.PLAYER_0 ? Constant.PLAYER_1st_PORT : Constant.PLAYER_2nd_PORT;
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

    public final void close() throws IOException{
	channel.close();
    }

    private boolean verbose = false;
    public final void setVerbose(boolean flag){
	verbose = flag;
    }
	
    private String boardInfo = "";
	
    private String recv() throws IOException{
	String s = "";
	ByteBuffer bb = ByteBuffer.allocate(2048);
	
	do{
	    bb.clear();
	    int len = channel.read(bb);
	    if(len == -1){
		throw new RuntimeException("channel is not opend");
	    }
	    bb.flip();
	    s += Charset.defaultCharset().decode(bb).toString();
	    System.out.println(s);
	}while(!s.endsWith("\r\n"));
	       
	if(s.startsWith("MOV?")){
	    boardInfo = s;
	}else if(s.startsWith("WON")){
	    boardInfo = s;
	    won = true;
	}else if(s.startsWith("LST")){
	    boardInfo = s;
	    lost = true;
	}
	if(verbose) System.out.println(s);
	return s;
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