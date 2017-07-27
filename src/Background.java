package com.XDScroller;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import com.XDScroller.Model.State;

public class Background
{
	private View view;
	private BufferedImage bg1, bg2, bg3;
	private int x1, x2, x3, offset, width;
	
	public Background(View view)
	{
		this.view = view;
		
		try
		{
			BufferedImage bg = ImageIO.read(new File(Main.ICONS_DIR + "game_bg.png"));
			bg1 = bg;
			bg2 = bg;
			bg3 = bg;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		x1 = 0;
		x2 = bg1.getWidth();
		x3 = bg1.getWidth() * 2;
		
		run();
	}
	
	public void paintBackground(Graphics g)
	{
		double ratioHeight = View.DEFAULT_HEIGHT / (double) View.DEFAULT_WIDTH;
		double ratioWidth = View.DEFAULT_WIDTH / (double) View.DEFAULT_HEIGHT;
		
		int width = view.getWidth();
		int height = view.getHeight();
		
		int w = (int) (height * ratioWidth);
		int h = (int) (width * ratioHeight);
		
		g.drawImage(bg1, x1 - offset, 0, Math.max(w, width), Math.max(h, height), null);
		g.drawImage(bg2, x2 - offset, 0, Math.max(w, width), Math.max(h, height), null);
		g.drawImage(bg3, x3 - offset, 0, Math.max(w, width), Math.max(h, height), null);
	}

	public void reset()
	{
		width = view.getWidth();
		
		offset = 0;
		x1 = 0;
		x2 = width;
		x3 = width * 2;
	}
	
	private void scroll()
	{
		if (offset >= width || view.getWidth() != width)
			reset();
		
		if (view.getModel().getState() == State.RUNNING)
			offset += bg1.getWidth() / (Model.FPS);
	}
	
	private void run()
	{	
		Timer timer = new Timer(1000/Model.FPS, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (view.getModel().getState() == State.RUNNING)
					scroll();
			}
		});
		
		timer.start();
	}
}
