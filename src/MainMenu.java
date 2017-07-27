package com.XDScroller;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;

import com.XDScroller.Overlay.OverlayScreen;
import com.XDScroller.View.Screen;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

@SuppressWarnings("serial")
public class MainMenu extends JPanel
{
	private View view;
	private Image bg, title;
	
	public MainMenu(View view)
	{
		super();
		this.view = view;

		SpringLayout s = new SpringLayout();
		setLayout(s);
		
		try
		{
			bg = ImageIO.read(new File(Main.ICONS_DIR + "main_bg.png"));
			title = ImageIO.read(new File(Main.ICONS_DIR + "logo.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		JPanel titlePanel = new JPanel()
		{
			@Override
			public void paintComponent(Graphics g)
			{
				Graphics2D g2d = (Graphics2D) g;
				BufferedImage bi = (BufferedImage) title;
			    int x = (this.getWidth() - bi.getWidth(null)) / 2;
			    int y = (this.getHeight() - bi.getHeight(null)) / 2;
				g2d.drawImage(bi, x, y, null);
				super.paintComponent(g);
			}
		};
		titlePanel.setPreferredSize(new Dimension(450, 100));
		titlePanel.setOpaque(false);
		
		JButton btnPlayGame = new JButton("New Game");
		JButton btnLoadGame = new JButton("Load Game");
		JButton btnLevelEditor = new JButton("Level Editor");
		JButton btnOptions = new JButton("Options");
		JButton btnExit = new JButton("Exit");

		JButton[] btns = {btnLevelEditor, btnPlayGame, btnLoadGame, btnOptions, btnExit};
		
		// Negative to keep title relative to buttons and not vice versa.
		s.putConstraint(SpringLayout.NORTH, titlePanel, -100, SpringLayout.NORTH, btnPlayGame);
		s.putConstraint(SpringLayout.VERTICAL_CENTER, btnPlayGame, 0, SpringLayout.VERTICAL_CENTER, this);
		
		s.putConstraint(SpringLayout.NORTH, btnLoadGame, 6, SpringLayout.SOUTH, btnPlayGame);
		s.putConstraint(SpringLayout.NORTH, btnLevelEditor, 6, SpringLayout.SOUTH, btnLoadGame);
		s.putConstraint(SpringLayout.NORTH, btnOptions, 6, SpringLayout.SOUTH, btnLevelEditor);
		s.putConstraint(SpringLayout.NORTH, btnExit, 6, SpringLayout.SOUTH, btnOptions);
		
		s.putConstraint(SpringLayout.HORIZONTAL_CENTER, titlePanel, 0, SpringLayout.HORIZONTAL_CENTER, this);
		
		btnPlayGame.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				view.setOverlay(OverlayScreen.NEW_GAME);
			}
		});
		
		btnLoadGame.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				view.load(Screen.GAME_LOBBY);
			}
		});
		
		btnLevelEditor.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				view.load(Screen.LEVEL_EDITOR);
			}
		});
		
		btnExit.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				setVisible(false);
				view.dispose();
			}
		});
		
		add(titlePanel);
		
		for (JButton btn : btns)
		{
			s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btn, 0, SpringLayout.HORIZONTAL_CENTER, this);
			btn.setFont(new Font(Main.FONT, Font.BOLD, Main.FONTSIZE_TEXT));
			btn.setPreferredSize(btnLevelEditor.getPreferredSize());
			btn.setContentAreaFilled(false);
			add(btn);
		}
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		double ratioHeight = View.DEFAULT_HEIGHT / (double) View.DEFAULT_WIDTH;
		double ratioWidth = View.DEFAULT_WIDTH / (double) View.DEFAULT_HEIGHT;
		
		int width = getWidth();
		int height = getHeight();
		
		int w = (int) (height * ratioWidth);
		int h = (int) (width * ratioHeight);
		
		g.drawImage(bg, 0, 0, Math.max(width, w), Math.max(height, h), this);
	}
}