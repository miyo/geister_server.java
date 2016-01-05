package net.wasamon.geister.utils;

public enum Direction {

	NORTH,
	EAST,
	WEST,
	SOUTH;
	
	public static Direction dir(String s){
		switch(s){
		case "NORTH": return Direction.NORTH;
		case "EAST": return Direction.EAST;
		case "WEST": return Direction.WEST;
		case "SOUTH": return Direction.SOUTH;
		default: return null;
		}
	}
	
}
