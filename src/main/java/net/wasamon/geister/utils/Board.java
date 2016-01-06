package net.wasamon.geister.utils;

public class Board {

	private final Player[] players;

	public Board() {
		players = new Player[] { new Player(this), new Player(this) };
	}

	/**
	 * Getting items on the board from a player
	 * 
	 * @param viewer
	 *            viewing player
	 * @return item array
	 */
	public Item[] getBoardForPlayer(int viewer) {
		Item[] b = new Item[Constant.SIZE * Constant.SIZE];
		for (int i = 0; i < players.length; i++) {
			for (Item item : players[i].getItems()) {
				int x = item.getX();
				int y = item.getY();
				if (x > Constant.SIZE - 1) {
					continue; // this item is not on the board
				}
				if (i != viewer) { // rotate
					x = Constant.SIZE - 1 - x;
					y = Constant.SIZE - 1 - y;
				}
				b[y * Constant.SIZE + x] = item;
			}
		}
		return b;
	}

	/**
	 * Return encoded information of the item, xyc. The item color is red/blue =
	 * R/B when secret, or red/blue/unknown = r/b/u when public.
	 * 
	 * @param item
	 *            target item to encode
	 * @param mine
	 *            a flag whether the item is mine or not
	 * @param globalView
	 *            a flag to allow global view or not
	 * @return encoded info. of the item
	 */
	public String getEncodedItem(Item item, boolean mine, boolean globalView) {
		int x = item.getX();
		int y = item.getY();
		ItemColor c = item.getColor();
		String key;
		if (mine) {
			if (item.isPublic()) {
				key = c.getSymbol().toLowerCase();
			} else {
				key = c.getSymbol().toUpperCase();
			}
		} else {
			if (item.isPublic() || globalView) {
				key = c.getSymbol().toLowerCase();
			} else {
				key = "u"; // unknown
			}
			if (!(item.isTaken() || item.isEscaped())){
				x = Constant.SIZE - 1 - x; // rotate
				y = Constant.SIZE - 1 - y; // rotate
			}
		}
		return String.format("%d%d%s", x, y, key);
	}

	public static Board decode(String s) {
		Board b = new Board();
		{ // my own items
			Item[] items = new Item[8];
			for (int i = 0; i < 8; i++) {
				String key = (new Character((char) ('A' + i))).toString();
				int x = Integer.parseInt(s.substring(0, 1));
				int y = Integer.parseInt(s.substring(1, 2));
				ItemColor c = ItemColor.decode(s.substring(2, 3));
				items[i] = new Item(b.getPlayer(0), key, x, y);
				items[i].setColor(c);
				s = s.substring(3);
			}
			b.getPlayer(0).setItems(items);
		}
		{ // opposite player's items
			Item[] items = new Item[8];
			for (int i = 0; i < 8; i++) {
				String key = (new Character((char) ('A' + i))).toString();
				int x = Integer.parseInt(s.substring(0, 1));
				int y = Integer.parseInt(s.substring(1, 2));
				ItemColor c = ItemColor.decode(s.substring(2, 3));
				if (x < Constant.SIZE) { // rotate
					x = Constant.SIZE - 1 - x;
					y = Constant.SIZE - 1 - y;
				}
				items[i] = new Item(b.getPlayer(1), key, x, y);
				items[i].setColor(c);
				s = s.substring(3);
			}
			b.getPlayer(1).setItems(items);
		}
		return b;
	}
	
	/**
	 * Return encoded information of the items on this board.
	 * ex. 14R24R34R44R15B25B35B45B41u31u21u11u40u30u20u10u
	 * 
	 * @param viewer
	 *            player to view
	 * @param globalView
	 *            a flag to allow global view or not
	 * @return encoded info. of the items on this board
	 */
	public String getEncodedBoard(int viewer, boolean globalView) {
		String s = "";
		{
			Item[] items = players[viewer].getItems();
			for (Item item : items) {
				s += getEncodedItem(item, true, globalView);
			}
		}
		{
			int opposite = viewer == 0 ? 1 : 0;
			Item[] items = players[opposite].getItems();
			for (Item item : items) {
				//s += getEncodedItem(item, false);
				s += getEncodedItem(item, false, globalView); // only when globalView, items' color are visible 
			}
		}
		return s;
	}


	/**
	 * Return encoded information of the items on this board.
	 * ex. 14R24R34R44R15B25B35B45B41u31u21u11u40u30u20u10u
	 * 
	 * @param viewer
	 *            player to view
	 * @return encoded info. of the items on this board
	 */
	public String getEncodedBoard(int viewer) {
		return getEncodedBoard(viewer, false);
	}

	public Item getItem(int id, int x, int y) {
		Item[] b = getBoardForPlayer(id);
		return b[y * Constant.SIZE + x];
	}

	public Item getItem(Player player, int x, int y) {
		if (player == players[0]) {
			return getItem(0, x, y);
		} else if (player == players[1]) {
			return getItem(1, x, y);
		}
		return null;
	}

	public Player getPlayer(int id) {
		return players[id];
	}

	public static final String CRLF = System.getProperty("line.separator");

	public String getBoardMap(int viewer) {
		Item[] b = getBoardForPlayer(viewer);
		int opposite = viewer == 0 ? 1 : 0;
		String s = "  0 1 2 3 4 5";
		for (int y = 0; y < Constant.SIZE; y++) {
			s += CRLF;
			s += y + " ";
			for (int x = 0; x < Constant.SIZE; x++) {
				Item item = b[y * Constant.SIZE + x];
				String ss = " ";
				if (item != null) {
					ss = item.getName();
					if (item.getPlayer() == getPlayer(opposite)) { // opposite
																	// one
						ss = ss.toLowerCase();
					}
				}
				s += ss + " ";
			}
		}
		return s;
	}

}
