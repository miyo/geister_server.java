package net.wasamon.geister.utils;

/**
 * Item, which is blue or red
 * 
 * @author miyo
 *
 */
public class Item {

	private final Player player;
	private final String name;
	private int x, y;
	private ItemColor color;

	public Item(Player p, String n, int x, int y) {
		this.player = p;
		this.name = n;
		this.x = x;
		this.y = y;
		this.color = ItemColor.BLUE;
	}

	public String toString() {
		return name + ":" + color + "(" + x + "," + y + ")" + "@" + player;
	}

	public void setTaken() {
		x = Constant.TAKEN_MARK;
		y = Constant.TAKEN_MARK;
	}

	public boolean willEscape(Direction d) {
		// escape from the board
		if (x == 0 && y == 0 && (d == Direction.NORTH || d == Direction.WEST) && color == ItemColor.BLUE) {
			return true;
		} else if (x == 5 && y == 0 && (d == Direction.NORTH || d == Direction.EAST) && color == ItemColor.BLUE) {
			return true;
		}
		return false;
	}

	public boolean isMovable(Direction d) {
		if (willEscape(d))
			return true;
		if (isTaken())
			return false;

		boolean flag = true;
		// move in the board;
		int nx = x;
		int ny = y;
		if (d == Direction.NORTH && y > 0) {
			ny = ny - 1;
		} else if (d == Direction.EAST && x < Constant.SIZE - 1) {
			nx = nx + 1;
		} else if (d == Direction.WEST && x > 0) {
			nx = nx - 1;
		} else if (d == Direction.SOUTH && y < Constant.SIZE - 1) {
			ny = ny + 1;
		} else {
			flag = false;
		}

		Item item = player.getBoard().getItem(player, nx, ny);
		if (item != null && item.getPlayer() == player) {
			flag = false;
		}
		return flag;
	}
	
	public Item lastTakenItem = null;

	/**
	 * 
	 * @param d
	 *            move direction
	 * @return true = success to move/false = failure to move
	 */
	public boolean move(Direction d) {

		if (isTaken()) {
			System.out.println("[ERROR] already taken");
			return false;
		}

		// escape from the board
		if (willEscape(d)) {
			x = Constant.ESCAPED_MARK;
			y = Constant.ESCAPED_MARK;
			return true;
		}

		// move in the board;
		int nx = x;
		int ny = y;
		if (d == Direction.NORTH && y > 0) {
			ny = ny - 1;
		} else if (d == Direction.EAST && x < Constant.SIZE - 1) {
			nx = nx + 1;
		} else if (d == Direction.WEST && x > 0) {
			nx = nx - 1;
		} else if (d == Direction.SOUTH && y < Constant.SIZE - 1) {
			ny = ny + 1;
		} else {
			return false;
		}

		Item item = player.getBoard().getItem(player, nx, ny);
        lastTakenItem = null;
		if (item != null) {
			if (item.getPlayer() == player) {
				System.out.println("[ERROR] has been occupied: " + x + "," + y + " -> " + nx + "," + ny);
				return false;
			} else {
				item.setTaken();
				lastTakenItem = item;
			}
		}
		System.out.println("[SUCCESS]: " + x + "," + y + " -> " + nx + "," + ny);
		x = nx;
		y = ny;
		return true;
	}

	public String getName() {
		return name;
	}

	public Player getPlayer() {
		return player;
	}

	public void setColor(ItemColor c) {
		color = c;
	}

	public ItemColor getColor() {
		return color;
	}

	public boolean isPublic() {
		return x == Constant.TAKEN_MARK || x == Constant.ESCAPED_MARK;
	}

	public boolean isTaken() {
		return x == Constant.TAKEN_MARK;
	}

	public boolean isEscaped() {
		return x == Constant.ESCAPED_MARK;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
