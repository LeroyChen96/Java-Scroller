package com.XDScroller;

import java.awt.Font;

public class Main
{
	public static final boolean DEBUG = false;
	public static final int FONTSIZE_TEXT = 16;
	public static final String FONT = "Futura";
	public static final String MAPS_DIR = "res/maps/";
	public static final String SAVE_DIR = "res/saves/";
	public static final String ICONS_DIR = "res/icons/";
	
	public static void main(String[] args)
	{
		Model model = new Model();
		View view = new View(model);
		model.addObserver(view);
	}
}