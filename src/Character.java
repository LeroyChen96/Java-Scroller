package com.XDScroller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import com.XDScroller.Model.State;

public class Character
{
	private View view;
	private BufferedImage state, ship1, ship2, ship3;
	
	public Character(View view)
	{
		this.view = view;
		
		try
		{
			ship1 = ImageIO.read(new File(Main.ICONS_DIR + "ship1.png"));
			ship2 = ImageIO.read(new File(Main.ICONS_DIR + "ship2.png"));
			ship3 = ImageIO.read(new File(Main.ICONS_DIR + "ship3.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		run();
	}
	
	public void run()
	{
		Timer timer = new Timer(50, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (view.getModel().getState() == State.RUNNING)
				{
					if (state == ship1)
						state = ship2;
					else if (state == ship2)
						state = ship3;
					else
						state = ship1;
				}
			}
		});
		
		timer.start();
	}
	
	public BufferedImage getShip()
	{
		return state;
	}
}
