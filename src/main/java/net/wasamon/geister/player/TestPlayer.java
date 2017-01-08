package net.wasamon.geister.player;

import net.wasamon.geister.utils.Direction;

public class TestPlayer extends BasePlayer{

	public static void main(String[] args) throws Exception{
		TestPlayer p = new TestPlayer();
		p.init(args[0], Integer.parseInt(args[1]));
		System.out.println(p.setRedItems("BCDE"));
		
		Direction[] dirs = {
				Direction.NORTH,
				Direction.NORTH,
				Direction.NORTH,
				Direction.EAST,
				Direction.WEST,
				Direction.SOUTH,
				Direction.NORTH,
				Direction.NORTH,
				Direction.WEST,
				Direction.WEST,
		};
        p.waitBoardInfo();
        p.printBoard();
		for(Direction d: dirs){
			Thread.sleep(2);
			if(p.isEnded() == true) break;
			System.out.println(p.move("A", d));
	        p.waitBoardInfo();
	        p.printBoard();
		}
		if(p.isWinner()){
			System.out.println("won");
		}else{
			System.out.println("lost");
		}
	}

}
