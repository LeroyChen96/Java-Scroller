package com.XDScroller;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.XDScroller.Model.State;
import com.XDScroller.Overlay.OverlayScreen;

@SuppressWarnings("serial")
public class Game extends JPanel
{
	private View view;
	private Background bg;
	
	private Model model;
	private Character character;
	private boolean[][] board;
	private BufferedImage nw, n, ne, e, se, s, sw, w, middle;
	private BufferedImage single_top, single_bot, single_right, single_left, single_h, single_v, tile;
	
	// For handling movement / better code style
	private class MoveAction extends AbstractAction
	{
		int key;
		boolean release;
		
		MoveAction(int key, boolean release)
		{
			this.key = key;
			this.release = release;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (release)
				model.release(key);
			else
				model.press(key);
		}
	}
	
	public Game(View view, String filepath, boolean isSaveFile)
	{
		super();
		this.view = view;
		model = view.getModel();
		
		bg = new Background(view);

		character = new Character(view);
		setBackground(Color.WHITE);
		model.init(filepath, isSaveFile);
		board = model.getBoard();
		view.setScale();
		
		// Sprite loading
		try
		{
			nw = ImageIO.read(new File("res/icons/nw.png"));
			n  = ImageIO.read(new File("res/icons/n.png"));
			ne = ImageIO.read(new File("res/icons/ne.png"));
			e  = ImageIO.read(new File("res/icons/e.png"));
			se = ImageIO.read(new File("res/icons/se.png"));
			s  = ImageIO.read(new File("res/icons/s.png"));
			sw = ImageIO.read(new File("res/icons/sw.png"));
			w  = ImageIO.read(new File("res/icons/w.png"));
			middle = ImageIO.read(new File("res/icons/middle.png"));
			tile = ImageIO.read(new File("res/icons/tile.png"));
			
			single_top   = ImageIO.read(new File("res/icons/single_top.png"));
			single_bot   = ImageIO.read(new File("res/icons/single_bot.png"));
			single_left  = ImageIO.read(new File("res/icons/single_left.png"));
			single_right = ImageIO.read(new File("res/icons/single_right.png"));
			single_h 	 = ImageIO.read(new File("res/icons/single_h.png"));
			single_v	 = ImageIO.read(new File("res/icons/single_v.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Movement
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "UP");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "DOWN");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "LEFT");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "RIGHT");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "UP_RELEASE");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "DOWN_RELEASE");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "LEFT_RELEASE");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "RIGHT_RELEASE");
        this.getActionMap().put("UP", 			 new MoveAction(Model.UP, false));
        this.getActionMap().put("DOWN", 		 new MoveAction(Model.DOWN, false));
        this.getActionMap().put("LEFT", 		 new MoveAction(Model.LEFT, false));
        this.getActionMap().put("RIGHT", 		 new MoveAction(Model.RIGHT, false));
        this.getActionMap().put("UP_RELEASE", 	 new MoveAction(Model.UP, true));
        this.getActionMap().put("DOWN_RELEASE",  new MoveAction(Model.DOWN, true));
        this.getActionMap().put("LEFT_RELEASE",  new MoveAction(Model.LEFT, true));
        this.getActionMap().put("RIGHT_RELEASE", new MoveAction(Model.RIGHT, true));

        // Other
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "PAUSE");
        this.getActionMap().put("PAUSE", new AbstractAction()
		{
        	@Override
        	public void actionPerformed(ActionEvent e)
        	{
        		if (model.getState() == State.RUNNING)
        		{
        			model.pause();
        			view.setOverlay(OverlayScreen.PAUSED);
        		}
        		else if (model.getState() == State.PAUSED)
    			{
        			model.resume();
        			view.setOverlay(null);
    			}
        	}
		});
        
		run();
	}

	public void resetBoard()
	{
		board = model.getBoard();
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		bg.paintBackground(g);
		
		double scale = view.getScale();
		int offset = (int) (model.getOffset() * scale);
		Point2D posn = model.getPosn();
		
		Graphics2D g2d = (Graphics2D) g;
		
		// Obstacles
		for (int i = 0; i < board.length; i++)
		{
			for (int j = 0; j < board[0].length; j++)
			{
				if (board[i][j])
					g2d.drawImage(getTile(i, j), (int) (j * scale - offset), (int) (i * scale), (int) Math.ceil(scale), (int) Math.ceil(scale), null);
			}
		}
		
		// Ship
		BufferedImage ship = character.getShip();
		g2d.translate(-scale * model.getCharWidth() / 4, -scale * model.getCharHeight() / 4);
		double correction = 6/ (double) 4;
		g2d.drawImage(ship, (int) (posn.getX() * scale - offset), (int) (posn.getY() * scale), (int) (scale * model.getCharWidth() * correction), (int) (scale * model.getCharHeight() * correction), null);
		
		// When drawing the ship, we multiply by some factor to introduce a bit of leeway
		//	since we don't want the fins to count as "crashed"
	}
	
	private void run()
	{	
		Timer gameLoop = new Timer(1000/Model.FPS, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (model.getState() == State.RUNNING)
				{
					model.checkWin();
					model.checkPlayerCollisions();
					
					// We handle going off screen a little differently than regular movement.
					if (view.getScale() * (model.getPosn().getX() + 1 - model.getOffset()) + view.getInsets().right * 2 >= view.getWidth())
						model.release(Model.RIGHT);
					
					model.move();
					model.scroll();
				}
			}
		});
		
		gameLoop.start();
	}
	
	private BufferedImage getTile(int y, int x)
	{
		boolean above = false;
		boolean below = false;
		boolean left = false;
		boolean right = false;
		
		if (y > 0 && board[y - 1][x])
			above = true;
		if (y < view.getModel().getBlockHeight() - 1 && board[y + 1][x])
			below = true;
		if (x > 0 && board[y][x - 1])
			left = true;
		if (x < view.getModel().getBlockWidth() - 1 && board[y][x + 1])
			right = true;
		
		if ( above &&  left &&  right &&  below)	return middle;
		if (!above && !left && !right && !below)	return tile;
		
		if (!above && !left &&  right &&  below)	return nw;
		if (!above &&  left &&  right &&  below)	return n;
		if (!above &&  left && !right &&  below)	return ne;
		if ( above &&  left && !right &&  below)	return e;
		if ( above &&  left && !right && !below)	return se;
		if ( above &&  left &&  right && !below)	return s;
		if ( above && !left &&  right && !below)	return sw;
		if ( above && !left &&  right &&  below)	return w;
		
		if (!above && !left && !right &&  below)	return single_top;
		if ( above && !left && !right && !below)	return single_bot;
		if (!above && !left &&  right && !below)	return single_left;
		if (!above &&  left && !right && !below)	return single_right;
		if (!above &&  left &&  right && !below)	return single_h;
		if ( above && !left && !right && below)		return single_v;
		
		return null;
	}
}
