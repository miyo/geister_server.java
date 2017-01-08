package net.wasamon.geister.player;

import net.wasamon.geister.utils.*;
import net.wasamon.geister.server.*;
import java.util.*;
import java.io.*;

public class HumanPlayer extends BasePlayer {

    public String readLine() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }

    public static void main(String[] args) throws Exception {
        HumanPlayer p = new HumanPlayer();
        p.init(args[0], Integer.parseInt(args[1]));
        System.out.println("red ghosts? (ex. BCDE)");
        System.out.println(p.setRedItems(p.readLine()));
        
        GAME_LOOP: while (true) {
            p.waitBoardInfo();
            p.printBoard();
            if (p.isEnded() == true)
                break GAME_LOOP;
            System.out.println("move ? (ex. A,N)");
            System.out.println(p.move(p.readLine()));
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
