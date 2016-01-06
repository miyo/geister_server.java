package net.wasamon.geister.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class PlayerTest {

	@Test
	public void testPlayer() { // todo
		Board b = new Board();
		Item[] items = b.getPlayer(0).getItems();
		for(int i = 0; i < items.length; i++){
			assertEquals(i%4+1, items[i].getX());
			assertEquals(i/4+4, items[i].getY());
		}
	}

	@Test
	public void test_ntoi() { // todo
		try{
			Method method = Player.class.getDeclaredMethod("ntoi", String.class);
			method.setAccessible(true);
			Board b = new Board();
			assertEquals(0, (int)method.invoke(b.getPlayer(0), "A"));
		}catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
			fail(e.toString());
		}
	}

	@Test
	public void test_iton() { // todo
		try{
			Method method = Player.class.getDeclaredMethod("iton", int.class);
			method.setAccessible(true);
			Board b = new Board();
			assertEquals("A", (String)method.invoke(b.getPlayer(0), 0));
		}catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
			fail(e.toString());
		}
	}

}
