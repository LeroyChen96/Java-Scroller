package com.XDScroller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.XDScroller.View.Screen;

@SuppressWarnings("serial")
public class GameLobby extends JPanel
{
	private View view;
	private BufferedImage bg;
	DefaultListModel<Item> listModel;
	
	public GameLobby(View view)
	{
		super();
		this.view = view;
		
		try
		{
			bg = ImageIO.read(new File(Main.ICONS_DIR + "stars_bg.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		setLayout(new BorderLayout());
		
		JPanel bottom = new JPanel();
		JPanel footer = new JPanel();
		JPanel deletePanel = new JPanel();
		listModel = new DefaultListModel<Item>();
		JList list = new JList(listModel);
		JScrollPane scrollPane = new JScrollPane(list);
		
		JButton btnStartGame = new JButton("Start Game");
		JButton btnCancel = new JButton("Back to Main Menu");
		JButton btnDel = new JButton ("Delete");
		
		reload();
		
		JButton[] btns = {btnStartGame, btnCancel, btnDel};
		for (JButton btn : btns)
		{
			btn.setContentAreaFilled(false);
			btn.setFont(new Font(Main.FONT, Font.PLAIN, Main.FONTSIZE_TEXT));
		}
		
		btnStartGame.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				Item selected = (Item) list.getSelectedValue();
				if (selected != null)
					view.loadGame(Main.SAVE_DIR + selected.left, true);
			}
		});
		
		btnCancel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				view.load(Screen.MAIN_MENU);
			}
		});
		
		btnDel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				Item selected = (Item) list.getSelectedValue();
				if (selected != null)
				{
					File file = new File(Main.SAVE_DIR + selected.left);
					file.delete();
					reload();
				}
			}
		});
		
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
	    {
	        @Override
	        public void adjustmentValueChanged(AdjustmentEvent e)
	        {
	            repaint();
	        }
	    });

		list.setOpaque(false);
		list.setCellRenderer(new GameCellRenderer());
		scrollPane.setOpaque(false);

		btnStartGame.setPreferredSize(btnCancel.getPreferredSize());
		deletePanel.add(btnDel);
		bottom.add(btnStartGame);
		bottom.add(btnCancel);
		footer.setLayout(new BorderLayout());
		footer.add(bottom, BorderLayout.CENTER);
		footer.add(deletePanel, BorderLayout.EAST);
		
		scrollPane.getViewport().setOpaque(false);
		setOpaque(false);
		
		add(scrollPane, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);
	}
	
	public void reload()
	{
		listModel.removeAllElements();
		
		File saves = new File(Main.SAVE_DIR);
		File maps = new File(Main.MAPS_DIR);
		
		if (!saves.exists())
			saves.mkdir();
		if (!maps.exists())
			maps.mkdir();
		
		for (File file : saves.listFiles())
		{
			try (BufferedReader br = new BufferedReader(new FileReader(Main.SAVE_DIR + file.getName())))
			{
				BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
				String saveName = file.getName();
				String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(attr.creationTime().toMillis()));
				String map = br.readLine();
				String progress = br.readLine();
				
				map = "Map: " + map.substring(Main.MAPS_DIR.length(), map.length()) + System.lineSeparator();		// Getting rid of "res/maps/"
				date = "Created: " + date + System.lineSeparator();
				progress = "Progress: " + progress + "%";
				
				String rightCol = map + date + progress;
				listModel.addElement(new Item(saveName, rightCol));
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
	}
	
	private static class GameCellRenderer extends JPanel implements ListCellRenderer<Item>
	{
        JLabel left = new JLabel();
        JTextPane right;

        public GameCellRenderer()
        {
            StyleContext context = new StyleContext();
            StyledDocument document = new DefaultStyledDocument(context);
            Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
            right = new JTextPane(document);
            right.setOpaque(false);
            
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            add(left);
            add(Box.createHorizontalGlue());
            add(right);
        }
        
		@Override
        public Component getListCellRendererComponent(JList list, Item value, int index, boolean isSelected, boolean cellHasFocus)
		{
			if (isSelected || cellHasFocus)
            	setBorder(new LineBorder(Color.BLACK, 3));
            else
            	setBorder(new LineBorder(Color.BLACK, 1));
            
			left.setText(value.left);
			right.setText(value.right);
			
			left.setFont(new Font(Main.FONT, Font.PLAIN, 24));
			right.setFont(new Font(Main.FONT, Font.PLAIN, 18));
			
			setOpaque(true);
            setBackground(new Color(255, 255, 255, 200));
            setComponentOrientation(list.getComponentOrientation());
            setPreferredSize(new Dimension(getPreferredSize().width, 80));
            return this;
		}
	};

	private static class Item
	{
		public String left, right;
		
		public Item(String left, String right)
		{
			this.left = left;
			this.right = right;
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
