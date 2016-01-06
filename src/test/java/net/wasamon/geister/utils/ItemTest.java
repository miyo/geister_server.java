package net.wasamon.geister.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class ItemTest {
	
	Board b = new Board();

	@Test
	public void testSetTaken() { // todo
		Item item = new Item(b.getPlayer(0), "A", 0, 0);
		item.setTaken();
		assertEquals(Constant.TAKEN_MARK, item.getX());
		assertEquals(Constant.TAKEN_MARK, item.getY());
	}

	@Test
	public void testWillEscape() { // todo
		Item item = new Item(b.getPlayer(0), "A", 0, 0);
		item.setColor(ItemColor.BLUE);
		assertEquals(true, item.willEscape(Direction.NORTH));
		item.setColor(ItemColor.RED);
		assertEquals(false, item.willEscape(Direction.NORTH));
	}

	@Test
	public void testIsMovable() { // todo
		Item item = new Item(b.getPlayer(0), "A", 0, 0);
		item.setColor(ItemColor.BLUE);
		assertEquals(true, item.isMovable(Direction.NORTH));
		item.setColor(ItemColor.RED);
		assertEquals(false, item.isMovable(Direction.NORTH));
	}

	@Test
	public void testMove() { // todo
		Item item = new Item(b.getPlayer(0), "A", 1, 0);
		item.move(Direction.NORTH);
		assertEquals(0, item.getY());
	}

}
