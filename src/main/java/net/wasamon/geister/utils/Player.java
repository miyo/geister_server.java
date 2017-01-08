package net.wasamon.geister.utils;

import java.util.ArrayList;

/**
 * A agent of Gesiter player on a server board
 * 
 * @author miyo
 *
 */
public class Player {

	private final Board board;

	private final Item[] items;

	public Player(Board b) {
		this.board = b;
		this.items = new Item[8];
		for (int i = 0; i < 8; i++) {
			String n = iton(i);
			int x = i % 4 + 1;
			int y = i / 4 + 4;
			items[i] = new Item(this, n, x, y);
		}
	}

	public void setItems(Item[] items) {
		for (int i = 0; i < items.length; i++) {
			this.items[i] = items[i];
		}
	}

	private String iton(int i) {
		return (new Character((char) ('A' + i))).toString();
	}

	private int ntoi(String n) {
		Character c = n.charAt(0);
		return c - 'A';
	}

	public Board getBoard() {
		return board;
	}

	public void setItemsColor(String keys, ItemColor c) {
		for (String k : keys.split("")) {
			items[ntoi(k)].setColor(c);
		}
	}
	
	private Item lastTakenItem = null;

	public boolean move(String name, Direction d) {
		System.out.println("move: " + name);
		Item item = items[ntoi(name)];
		if (items != null) {
		    boolean f = item.move(d);
		    lastTakenItem = item.lastTakenItem;
		    return f;
		} else {
			return false;
		}
	}

	public Item[] getItems() {
		return items;
	}
	
    public Item getLastTakenItem(){
        return lastTakenItem; 
    }

	public Item[] getTakenItems() {
		ArrayList<Item> items = new ArrayList<>();
		for (Item item : getItems()) {
			if (item.isTaken()) {
				items.add(item);
			}
		}
		return items.toArray(new Item[] {});
	}

	public Item[] getEscapedItems() {
		ArrayList<Item> items = new ArrayList<>();
		for (Item item : getItems()) {
			if (item.isEscaped()) {
				items.add(item);
			}
		}
		return items.toArray(new Item[] {});
	}
}
