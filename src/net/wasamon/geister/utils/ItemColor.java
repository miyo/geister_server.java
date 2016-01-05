package net.wasamon.geister.utils;

public enum ItemColor {
	BLUE("B"),
	RED("R"),
	UNKNOWN("U");
	
	private String sym;
	
	private ItemColor(String sym){
		this.sym = sym;
	}
	
	public String getSymbol(){
		return sym;
	}
	
	public static ItemColor decode(String s){
		switch(s.toUpperCase()){
		case "B": return BLUE;
		case "R": return RED;
		default: return UNKNOWN;
		}
		
	}
}
