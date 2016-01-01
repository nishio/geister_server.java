package net.wasamon.geister.player;

import net.wasamon.geister.utils.*;
import net.wasamon.geister.server.*;
import java.util.*;

public class RandomPlayer extends BasePlayer{

    public static void main(String[] args) throws Exception{
	RandomPlayer p = new RandomPlayer();
	int id = Integer.parseInt(args[0]);
	p.init(id == 0 ? BasePlayer.ID.PLAYER_0 : BasePlayer.ID.PLAYER_1);
	System.out.println(p.setRedItems("BCDE"));
	p.printBoard();
	Random r = new Random(Calendar.getInstance().getTimeInMillis());
	Direction[] dirs = new Direction[]{Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH};
	
	GAME_LOOP: while(true){	    
	    if(p.isEnded() == true) break GAME_LOOP;
	    Item[] own = p.getOwnItems();
	    MY_TURN: while(true){
		int i = r.nextInt(own.length);
		int d = r.nextInt(dirs.length);
		if(own[i].isMovable(dirs[d])){
		    p.move(own[i].getName(), dirs[d]);
		    p.printBoard();
		    break MY_TURN;
		}
	    }
	}
	if(p.isWinner()){
	    System.out.println("won");
	}else{
	    System.out.println("lost");
	}
    }

}