package com.XDScroller;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Model {
	public enum State
	{
		IDLE,
		RUNNING,
		PAUSED,
		GAMEOVER,
		WIN
	}
	
	// Useful game constants
	public static final int UP = 0;
	public static final int DOWN = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	
	public static final int FPS = 60;
	
	// Note: LOWER the scroll/movespeed, the faster.
	// Speed in terms of 1/x blocks/sec.
	private int scrollSpeed = 10;
	private int moveSpeed = 10;
	
    private List<Observer> observers;
    private int[][] debug;				// For testing uses.
    private boolean[][] board;
    private boolean[] pressed;
    
    private boolean init;
    private String filepath;
    
    private int blockHeight, blockWidth;
    private double charHeight, charWidth;
    private double offset;
    private Point2D.Double posn;
    
    private State state;
    
    // Ctor
    public Model()
    {
        this.observers = new ArrayList<Observer>();
        pressed = new boolean[4];
        charHeight = 0.5;
        charWidth = 1;
        state = State.IDLE;
        init = false;
    }
    
    public void init(String filepath, boolean isSaveFile)
    {
    	try
		{
    		BufferedReader br = new BufferedReader(new FileReader(filepath));
    		
    		init = true;
    		this.filepath = filepath;
    		
		    String line;
		    String[] arr;
		    offset = 0;
		    posn = new Point2D.Double();
		    
		    if (isSaveFile)
		    {
		    	String mapPath = br.readLine();
		    	this.filepath = mapPath;
		    	br.readLine();		// Skip progress % number
		    	posn.x = Double.parseDouble(br.readLine());
		    	posn.y = Double.parseDouble(br.readLine());
		    	offset = Double.parseDouble(br.readLine());
		    	br.close();
		    	br = new BufferedReader(new FileReader(mapPath));
		    }
		    
		    // Setting board dimens.
		    blockHeight = Integer.parseInt(br.readLine());
		    blockWidth = Integer.parseInt(br.readLine());
		    
		    if (!isSaveFile)
		    	posn = new Point2D.Double(0, (blockHeight - 1) / (double) 2);
		    
		    board = new boolean[blockHeight][blockWidth];
		    
		    if (Main.DEBUG)
		    	debug = new int[blockHeight][blockWidth];
		    
		    line = br.readLine();
		    while (line != null)
		    {
		    	arr = line.split(" ");
		    	int[] ints = new int [arr.length];
		    	
		    	for (int i = 0; i < arr.length; i++)
		    	{
		    		ints[i] = Integer.parseInt(arr[i]);
		    	}
		    	
		    	int x1 = ints[0];
		    	int y1 = ints[1];
		    	int x2 = ints[2];
		    	int y2 = ints[3];
		    	
		    	for (int i = y1; i <= y2; i++)
		    	{
		    		for (int j = x1; j <= x2; j++)
		    		{
		    			board[i][j] = true;
		    			
		    			if (Main.DEBUG)
		    				debug[i][j] = 1;
		    		}
		    	}
		    	
		    	line = br.readLine();
		    }
		    
		    setState(State.RUNNING);
		    br.close();
		} 
    	
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
    	
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }
    
    public void move()
    {
    	if (pressed[UP])
    	{
    		posn.y = posn.y - 1 / (double) moveSpeed;
    		// Out of bounds check
    		if (posn.y <= 0)
    			posn.y = 0;
    	}
    	
    	if (pressed[DOWN])
    	{
    		posn.y = posn.y + 1 / (double) moveSpeed;

    		if (posn.y >= blockHeight - charHeight)
    			posn.y = blockHeight - charHeight;
    	}
    	
    	if (pressed[LEFT])
    	{
    		posn.x = posn.x - 1 / (double) moveSpeed;
    		
    		if (posn.x <= offset)
    			posn.x = offset;
    	}
    	
    	if (pressed[RIGHT])
    	{
    		posn.x = posn.x + 1 / (double) moveSpeed;
    		// Bound checking for right movement handled
    		//	in the view. Impossible to tell from model alone.
    	}
    }

    public void scroll()
    {
    	offset += 1 / (double) scrollSpeed;
    	posn.x += 1 / (double) scrollSpeed;
    	notifyObservers();
    }
    
    public void checkPlayerCollisions()
    {
    	int x1 = (int) Math.floor(posn.x);
    	int x2 = (int) Math.floor(posn.x + charWidth);
    	int y1 = (int) Math.floor(posn.y);
    	int y2 = (int) Math.floor(posn.y + charHeight);
    	
    	// Fixing rounding issues.
    	if (y2 >= blockHeight)
    		y2 = blockHeight - 1;
    	
    	// Accounting for win condition.
    	if (x2 >= blockWidth)
    		x2 = blockWidth - 1;
    	
    	for (int i = x1; i <= x2; i++)
    	{
    		for (int j = y1; j <= y2; j++)
    		{
    			if (board[j][i])
    			{
    				if (Main.DEBUG)
    					System.out.println("PLAYER COLLISION");
    				
    				setState(State.GAMEOVER);
    				break;
    			}
    		}
    	}
    }

    public void checkWin()
    {
    	if (posn.x > blockWidth)
    	{
    		if (Main.DEBUG)
    			System.out.println("PLAYER WIN");
    		
    		setState(State.WIN);
    	}
    }
    
    public void pause()
    {
    	switch(state)
    	{
    		case RUNNING:
    			setState(State.PAUSED);
    			break;
    		default:
    			break;
    	}
    }
    
    public void resume()
    {
    	switch(state)
    	{
    		case PAUSED:
				setState(State.RUNNING);
				break;
			default:
				break;
    	}
    }
    
    public void restart()
    {
    	init(this.filepath, false);
    }
    
    public void restart(String filepath, boolean isSaveFile)
    {
    	init(filepath, isSaveFile);
    }
    
    public void save()
    {
    	String title = filepath.substring(Main.MAPS_DIR.length(), filepath.length());    	// Removing "res/maps/"
    	
    	File dir = new File(Main.SAVE_DIR);
    	
    	if (!dir.exists())
    		dir.mkdir();
    	
    	for (int i = 1; ; i++)
    	{
    		String check = title + i;
    		File newSave = new File(Main.SAVE_DIR + check);
    		if (!newSave.exists())
    		{
    			String saveData = "";
    			saveData += filepath + System.lineSeparator();								// Map filepath
    			saveData += ((int) posn.x * 100 / blockWidth) + System.lineSeparator();		// Progress
    			saveData += posn.x + System.lineSeparator();								// x posn
    			saveData += posn.y + System.lineSeparator();								// y posn
    			saveData += offset;															// Screen scroll amount
    			
    			try
    			{
    				FileWriter fw = new FileWriter(newSave);
    				BufferedWriter bw = new BufferedWriter(fw);
    				
    				bw.write(saveData);
    				bw.close();
    				fw.close();
    				
    				if (Main.DEBUG)
    					System.out.println("Saved as " + newSave.toString());
    			}
    			catch (IOException e)
    			{
    				e.printStackTrace();
    			}
    			
    			break;
    		}
    	}
    }
    
    // Observer functions
    public void addObserver(Observer observer)
    {
        this.observers.add(observer);
    }

    public void removeObserver(Observer observer)
    {
        this.observers.remove(observer);
    }

    public void notifyObservers()
    {
        for (Observer observer : this.observers)
        {
            observer.update();
        }
    }
    
    // Getters / setters
    public boolean isInit()
    {
    	return init;
    }
    
    public int getBlockHeight()
    {
    	return blockHeight;
    }
    
    public int getBlockWidth()
    {
    	return blockWidth;
    }
    
    public double getOffset()
    {
    	return offset;
    }
    
    public Point2D getPosn()
    {
    	return posn;
    }
    
    public boolean[][] getBoard()
    {
    	return board;
    }
    
    public double getCharHeight()
    {
    	return charHeight;
    }
    
    public double getCharWidth()
    {
    	return charWidth;
    }
    
    public void press(int key)
    {
    	pressed[key] = true;
    }
    
    public void release(int key)
    {
    	pressed[key] = false;
    }
    
    public State getState()
    {
    	return state;
    }
    
    public void setState(State state)
    {
    	this.state = state;
    	notifyObservers();
    }
    
    public void stop()
    {
    	setState(State.IDLE);
    }
    
    // Debug functions
    public void printBoard()
    {
    	int x1 = (int) Math.floor(posn.x);
    	int x2 = (int) Math.floor(posn.x + charWidth);
    	int y1 = (int) Math.floor(posn.y);
    	int y2 = (int) Math.floor(posn.y + charHeight);
    	
    	// Fixing rounding issues.
    	if (y2 >= blockHeight)
    		y2 = blockHeight - 1;
    	
    	// Accounting for win condition.
    	if (x2 >= blockWidth)
    		x2 = blockWidth - 1;
    	
    	for (int i = x1; i <= x2; i++)
    	{
    		for (int j = y1; j <= y2; j++)
    		{
    			debug[j][i] = 2;
    		}
    	}
    	
	    for (int i = 0; i < blockHeight; i++)
	    {
	    	for (int j = 0; j < blockWidth; j++)
	    	{
	    		if (debug[i][j] == 2)
	    			System.out.print("x");
	    		else
	    			System.out.print(debug[i][j]);
	    	}
	    	System.out.print(System.lineSeparator());
	    }
	    System.out.println(System.lineSeparator());
    }
}
