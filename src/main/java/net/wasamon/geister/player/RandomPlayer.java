package net.wasamon.geister.player;

import net.wasamon.geister.utils.*;
import net.wasamon.geister.server.*;
import java.util.*;

public class RandomPlayer extends BasePlayer {

    public static void main(String[] args) throws Exception {
        RandomPlayer p = new RandomPlayer();
        p.init(args[0], Integer.parseInt(args[1]));
        System.out.println(p.setRedItems("BCDE"));
        Random r = new Random(Calendar.getInstance().getTimeInMillis());
        Direction[] dirs = new Direction[] { Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH };
		int cnt = 0;
		
        GAME_LOOP: while (true) {
            p.waitBoardInfo();
            p.printBoard();
            if (p.isEnded() == true)
                break GAME_LOOP;
            Item[] own = p.getOwnItems();
            MY_TURN: while (true) {
                int i = r.nextInt(own.length);
                int d = r.nextInt(dirs.length);
                if (own[i].isMovable(dirs[d])) {
                    p.move(own[i].getName(), dirs[d]);
                    p.printBoard();
                    break MY_TURN;
                }
            }
			System.out.println(cnt++);
        }
        if (p.isWinner()) {
            System.out.println("won");
        } else if (p.isLoser()) {
            System.out.println("lost");
        } else if (p.isDraw()) {
            System.out.println("draw");
        }
    }

}
