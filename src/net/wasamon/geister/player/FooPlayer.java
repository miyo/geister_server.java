package net.wasamon.geister.player;

import net.wasamon.geister.utils.Direction;

public class FooPlayer extends BasePlayer{

	public static void main(String[] args) throws Exception{
		TestPlayer p = new TestPlayer();
		int id = Integer.parseInt(args[0]);
		p.init(id == 0 ? BasePlayer.ID.PLAYER_0 : BasePlayer.ID.PLAYER_1);
		p.close();
	}

}
