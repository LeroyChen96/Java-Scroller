package com.XDScroller;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.XDScroller.Overlay.OverlayScreen;

@SuppressWarnings("serial")
public class View extends JFrame implements Observer
{
	public enum Screen
	{
		MAIN_MENU,
		GAME_LOBBY,
		LEVEL_EDITOR,
		OPTIONS
	}
	
	// Constants and members
	public static final int DEFAULT_WIDTH = 1280;
	public static final int DEFAULT_HEIGHT = 720;
	
	public static final int MIN_WIDTH = 640;
	public static final int MIN_HEIGHT = 480;
	
	private Model model;
	private JPanel panel;
	private CardLayout layout;
	private LevelEditor editor;
	private GameLobby lobby;
	private Game game;
	
	private double scale;
	
	// Ctor
	public View(Model model)
	{
		super("");
		this.model = model;
		View view = this;
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
					setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
					
					panel = new JPanel();
					layout = new CardLayout();
					panel.setLayout(layout);
					
					editor = new LevelEditor(view);
					lobby = new GameLobby(view);
					
					panel.add(new MainMenu(view), "mainMenu");
					panel.add(editor, "editor");
					panel.add(lobby, "lobby");
					
					add(panel);
					setContentPane(panel);
					
					load(Screen.MAIN_MENU);
					setVisible(true);
				}
				
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				setPreferredSize(getSize());
				setScale();
				panel.repaint();
			}
		});
	}

	// Main method for loading different screens
	public void load(Screen screen)
	{
		switch(screen)
		{	
			case MAIN_MENU:
				layout.show(panel, "mainMenu");
				break;
			case LEVEL_EDITOR:
				editor.getGrid().reset();
				layout.show(panel, "editor");
				editor.getGrid().resizeScreen();
				break;
			case GAME_LOBBY:
				lobby.reload();
				layout.show(panel, "lobby");
				break;
			default:
				break;
		}

		pack();
		setVisible(true);
	}
	
	// Game loading needs to be handled a little differently
	public void loadGame(String filepath, boolean isSaveFile)
	{
		if (model.isInit())
		{
			model.restart(filepath, isSaveFile);
			game.resetBoard();
		}
		else
		{
			game = new Game(this, filepath, isSaveFile);
			panel.add(game, "game");
		}
		
		layout.show(panel, "game");
		
		pack();
		setVisible(true);
	}
	
	public void setOverlay(OverlayScreen overlay)
	{
		if (overlay != null)
		{
			Overlay o = new Overlay(this, overlay);
			setGlassPane(o);
			o.setOpaque(false);
			o.setVisible(true);
			pack();
		}
		else
			getGlassPane().setVisible(false);
	}
	
	@Override
	public void update()
	{
		switch (model.getState())
		{
			case GAMEOVER:
				setOverlay(OverlayScreen.GAMEOVER);
				break;
			case WIN:
				setOverlay(OverlayScreen.WIN);
				break;
			default:
				break;
		}
	
		panel.repaint();
	}
	
	// Getters / setters.
	public Model getModel()
	{
		return model;
	}
	
	public double getScale()
	{
		return scale;
	}
	
	public void setScale()
	{
		// Account for borders
		scale = (getHeight() - getInsets().top - getInsets().bottom) / (double) model.getBlockHeight();
	}
}