package net.wasamon.geister.player;

import net.wasamon.geister.utils.Direction;

public class FooPlayer extends BasePlayer{

	public static void main(String[] args) throws Exception{
		TestPlayer p = new TestPlayer();
		p.init(args[0], Integer.parseInt(args[1]));
		p.close();
	}

}
