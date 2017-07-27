package com.XDScroller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.XDScroller.Model.State;
import com.XDScroller.View.Screen;

// For pause/other overlay menus.
@SuppressWarnings("serial")
public class Overlay extends JPanel
{
	private View view;
	private final Color GRAY = new Color(128, 128, 128, 220);
	
	public enum OverlayScreen
	{
		NEW_GAME,
		PAUSED,
		CONFIRM,
		EDITOR_CONFIRM,
		GAMEOVER,
		WIN
	}
	
	public Overlay(View view, OverlayScreen state)
	{
		super();
		this.view = view;
		
		SpringLayout s = new SpringLayout();
		setLayout(s);
	
		JLabel title = new JLabel();
		JButton btnCentre = new JButton();
		JButton btnExit = new JButton("Exit");
		
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setFont(new Font(Main.FONT, Font.PLAIN, 64));
		title.setBorder(null);
		
		switch (state)
		{
			case NEW_GAME:
			{
				title.setText("Choose a level!");
				
				JPanel panel = new JPanel();
				JPanel footer = new JPanel();
				DefaultListModel<String> listModel = new DefaultListModel<>();
				JList list = new JList(listModel);
				JScrollPane scrollPane = new JScrollPane(list);

				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				btnCentre.setText("Start Game!");
				btnExit.setText("Cancel");
				btnCentre.setContentAreaFilled(false);
				btnExit.setContentAreaFilled(false);
				footer.add(btnCentre);
				footer.add(btnExit);
				panel.setPreferredSize(new Dimension(600, 300));
				panel.setLayout(new BorderLayout());
				panel.add(scrollPane, BorderLayout.CENTER);
				panel.add(footer, BorderLayout.SOUTH);

				File dir = new File(Main.MAPS_DIR);
				if (!dir.exists())
					dir.mkdir();
				
				for (File file : dir.listFiles())
				{
					listModel.addElement(file.getName());
				}
				
				btnExit.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(null);
					}
				});

				btnCentre.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						Object selected = list.getSelectedValue();

						if (selected != null)
						{
							view.setOverlay(null);
							view.loadGame(Main.MAPS_DIR + selected.toString(), false);
						}
					}
				});
				
			    ListCellRenderer renderer = new ListCellRenderer()
			    {
			        @Override
			        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			        {
			            JLabel label = new JLabel(value.toString(), SwingConstants.CENTER);
			            label.setFont(new Font(Main.FONT, Font.PLAIN, 24));
			            if (isSelected)
			            	label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
			            setOpaque(isSelected);
			            return label;
			        }
			    };
			    
			    list.setOpaque(false);
				list.setCellRenderer(renderer);
				scrollPane.setOpaque(false);

				s.putConstraint(SpringLayout.VERTICAL_CENTER, panel, 0, SpringLayout.VERTICAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, panel, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, title, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.NORTH, title, -100, SpringLayout.NORTH, panel);			

				add(panel);
				break;
			}
			case PAUSED:
			{
				title.setText("PAUSED");
				btnCentre.setText("Resume");
				JButton btnSave = new JButton("Save");
				
				btnSave.setPreferredSize(btnCentre.getPreferredSize());
				
				s.putConstraint(SpringLayout.NORTH, btnSave, 6, SpringLayout.SOUTH, btnCentre);
				s.putConstraint(SpringLayout.NORTH, btnExit, 6, SpringLayout.SOUTH, btnSave);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnSave, 0, SpringLayout.HORIZONTAL_CENTER, this);
				
				btnCentre.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(null);
						view.getModel().resume();
					}
				});
				
				btnExit.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(OverlayScreen.CONFIRM);
					}
				});
				
				btnSave.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						if (btnSave.getText() != "Saved!")
						{
							btnSave.setText("Saved!");
							view.getModel().save();
						}
					}
				});
				s.putConstraint(SpringLayout.NORTH, title, -100, SpringLayout.NORTH, btnCentre);			
				s.putConstraint(SpringLayout.VERTICAL_CENTER, btnCentre, 0, SpringLayout.VERTICAL_CENTER, this);

				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, title, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnCentre, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnExit, 0, SpringLayout.HORIZONTAL_CENTER, this);
				add(btnSave);
				add(btnCentre);
				add(btnExit);
				break;
			}
			case CONFIRM:
			{
				title.setText("Return to main menu?");
				title.setFont(new Font(Main.FONT, Font.BOLD, 36));
				btnExit.setText("Exit to main menu");
				
				btnCentre.setText("Back");
				
				btnCentre.setPreferredSize(btnExit.getPreferredSize());
				s.putConstraint(SpringLayout.NORTH, btnExit, 6, SpringLayout.SOUTH, btnCentre);

				btnExit.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.getModel().stop();
						view.setOverlay(null);
						view.load(Screen.MAIN_MENU);
					}
				});
				
				btnCentre.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						if (view.getModel().getState() == State.PAUSED)
							view.setOverlay(OverlayScreen.PAUSED);
						else
							view.setOverlay(null);
					}
				});
				s.putConstraint(SpringLayout.NORTH, title, -100, SpringLayout.NORTH, btnCentre);			
				s.putConstraint(SpringLayout.VERTICAL_CENTER, btnCentre, 0, SpringLayout.VERTICAL_CENTER, this);

				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, title, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnCentre, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnExit, 0, SpringLayout.HORIZONTAL_CENTER, this);
				add(btnCentre);
				add(btnExit);
				break;
			}	
			case GAMEOVER:
			{
				title.setText("LEVEL FAILED");
				btnCentre.setText("Retry");
				
				s.putConstraint(SpringLayout.NORTH, btnExit, 6, SpringLayout.SOUTH, btnCentre);
				
				btnCentre.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(null);
						view.getModel().restart();
					}
				});
				
				btnExit.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(null);
						view.load(Screen.MAIN_MENU);
					}
				});
				s.putConstraint(SpringLayout.NORTH, title, -100, SpringLayout.NORTH, btnCentre);			
				s.putConstraint(SpringLayout.VERTICAL_CENTER, btnCentre, 0, SpringLayout.VERTICAL_CENTER, this);

				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, title, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnCentre, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnExit, 0, SpringLayout.HORIZONTAL_CENTER, this);
				add(btnCentre);
				add(btnExit);
				break;
			}	
			case WIN:
			{
				title.setText("LEVEL COMPLETE!");
				btnCentre.setText("Replay");
				
				s.putConstraint(SpringLayout.NORTH, btnExit, 6, SpringLayout.SOUTH, btnCentre);

				btnCentre.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(null);
						view.getModel().restart();
					}
				});
				
				btnExit.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(null);
						view.load(Screen.MAIN_MENU);
					}
				});
				
				s.putConstraint(SpringLayout.NORTH, title, -100, SpringLayout.NORTH, btnCentre);			
				s.putConstraint(SpringLayout.VERTICAL_CENTER, btnCentre, 0, SpringLayout.VERTICAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, title, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnCentre, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnExit, 0, SpringLayout.HORIZONTAL_CENTER, this);
				add(btnCentre);
				add(btnExit);
				break;
			}	
			case EDITOR_CONFIRM:
			{
				title.setText("Clear the board? (This cannot be undone!)");
				title.setFont(new Font(Main.FONT, Font.BOLD, 36));
				
				btnExit.setText("No");
				btnCentre.setText("Yes");
				
				btnExit.setPreferredSize(btnCentre.getPreferredSize());
				s.putConstraint(SpringLayout.NORTH, btnExit, 6, SpringLayout.SOUTH, btnCentre);

				btnExit.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(null);
					}
				});
				
				btnCentre.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						view.setOverlay(null);
						view.load(Screen.LEVEL_EDITOR);
					}
				});
				
				s.putConstraint(SpringLayout.NORTH, title, -100, SpringLayout.NORTH, btnCentre);			
				s.putConstraint(SpringLayout.VERTICAL_CENTER, btnCentre, 0, SpringLayout.VERTICAL_CENTER, this);

				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, title, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnCentre, 0, SpringLayout.HORIZONTAL_CENTER, this);
				s.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnExit, 0, SpringLayout.HORIZONTAL_CENTER, this);
				add(btnCentre);
				add(btnExit);
				break;
			}
			default:
				break;
		}
		
		btnExit.setPreferredSize(btnCentre.getPreferredSize());
		
		addMouseListener(new MouseAdapter()				// Disable clicking through the glasspane
	    {
	        @Override
	        public void mouseClicked(MouseEvent e)
	        {
	            e.consume();
	        }

	        @Override
	        public void mousePressed(MouseEvent e)
	        {
	            e.consume();
	        }
	    });
		
		add(title);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		g.setColor(GRAY);
		g.fillRect(0, 0, view.getWidth(), view.getHeight());
	}
}