package com.XDScroller;

import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import com.XDScroller.Grid.Mode;
import com.XDScroller.Overlay.OverlayScreen;

import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class LevelEditor extends JPanel
{
	private View view;
	private Grid grid;

	JLabel labelMode;
	
	public class Validator
	{
	    public boolean verifyInt(JComponent input, boolean isHeight)
	    {
	        String text = ((JTextField) input).getText();
	        try
	        {
	            int value = Integer.parseInt(text);
	            
	            if (isHeight)
	            	return value <= 15;
	            else
	            	return true;
	        }
	        catch (NumberFormatException e)
	        {
	            return false;
	        }
	    }
	    
	    public boolean verifyText(JComponent input)
	    {
	        String text = ((JTextField) input).getText();
	        if (text.matches("^[a-zA-Z0-9_]+$"))
	        	return true;
	        else
	        	return false;
	    }
	}
	
	// For handling shortcuts / better code style
	private class ButtonAction extends AbstractAction
	{
		JButton btn;
		
		ButtonAction(JButton btn)
		{
			this.btn = btn;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			btn.doClick();
		}
	}
	
	public LevelEditor(View view)
	{
		super();

		try
		{
			this.view = view;
			
			Validator v = new Validator();
			final Font font = new Font(Font.DIALOG, Font.PLAIN, 16);
			
			BorderLayout layout = new BorderLayout();
			setLayout(layout);

			JScrollPane scrollPane = new JScrollPane();
			grid = new Grid(scrollPane, this);
			scrollPane.setViewportView(grid);
			
			JToolBar tb = new JToolBar();
			JToolBar tbSave = new JToolBar();
			JToolBar tbConfirm = new JToolBar();

			JButton btnBack = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "back.png"))));
			JButton btnReset = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "reset.png"))));
			JButton btnUndo = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "undo.png"))));
			JButton btnRedo = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "redo.png"))));
			JButton btnSave = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "save.png"))));

			JButton btnPencil = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "pencil.png"))));
			JButton btnEraser = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "eraser.png"))));
			JButton btnMove = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "move.png"))));
			JButton btnCopy = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "copy.png"))));
			JButton btnCrop = new JButton(new ImageIcon(ImageIO.read(new File(Main.ICONS_DIR + "crop.png"))));
			
			JButton[] btns = {btnBack, btnReset, btnUndo, btnRedo, btnSave};
			JButton[] toggles = {btnPencil, btnEraser, btnMove, btnCopy, btnCrop};
			
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "BACK");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "RESET");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "DRAW");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "ERASE");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "MOVE");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "COPY");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "CROP");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "UNDO");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "REDO");
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "SAVE");
			this.getActionMap().put("BACK",  new ButtonAction(btnBack));
			this.getActionMap().put("RESET", new ButtonAction(btnReset));
			this.getActionMap().put("DRAW",  new ButtonAction(btnPencil));
			this.getActionMap().put("ERASE", new ButtonAction(btnEraser));
			this.getActionMap().put("MOVE",  new ButtonAction(btnMove));
			this.getActionMap().put("COPY",  new ButtonAction(btnCopy));
			this.getActionMap().put("CROP",  new ButtonAction(btnCrop));
			this.getActionMap().put("UNDO",  new ButtonAction(btnUndo));
			this.getActionMap().put("REDO",  new ButtonAction(btnRedo));
			this.getActionMap().put("SAVE",  new ButtonAction(btnSave));
			btnBack.setToolTipText("Back to Main Menu (Esc)");
			btnReset.setToolTipText("Reset Board (F5)");
			btnUndo.setToolTipText("Undo (Ctrl + Z)");
			btnRedo.setToolTipText("Redo (Ctrl + Y)");
			btnSave.setToolTipText("Save (Ctrl + S)");
			btnPencil.setToolTipText("Draw Mode (D)");
			btnEraser.setToolTipText("Erase Mode (E)");
			btnMove.setToolTipText("Move Mode (M)");
			btnCopy.setToolTipText("Copy/Paste Mode (C)");
			btnCrop.setToolTipText("Crop Mode (V)");
			
			for (JButton btn : btns)
			{
				btn.setContentAreaFilled(false);
				btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				
				btn.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent e)
					{
						btn.setBorder(BorderFactory.createLoweredBevelBorder());
					}
					
					@Override
					public void mouseEntered(MouseEvent e)
					{
					    btn.setBorder(BorderFactory.createRaisedBevelBorder());
					}	
					
					@Override
					public void mouseExited(MouseEvent e)
					{
						btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
					}
					
					@Override
					public void mouseReleased(MouseEvent e)
					{
						btn.setBorder(BorderFactory.createRaisedBevelBorder());
					}
				});
			}
			
			for (JButton btn : toggles)
			{
				btn.setContentAreaFilled(false);
				btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				
				btn.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						btn.setBorder(BorderFactory.createLoweredBevelBorder());
						btn.setSelected(true);
					}
					
					@Override
					public void mouseEntered(MouseEvent e)
					{
						if (!btn.isSelected())
							btn.setBorder(BorderFactory.createRaisedBevelBorder());
					}	
					
					@Override
					public void mouseExited(MouseEvent e)
					{
						if (!btn.isSelected())
							btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
					}
					
				});
			}
			
			JLabel labelHeight = new JLabel("Grid Height (max. 15):");
			JLabel labelWidth = new JLabel("Grid Width:");
			labelMode = new JLabel("Draw Mode");

			labelHeight.setFont(font);
			labelWidth.setFont(font);
			labelMode.setFont(font);
			labelMode.setPreferredSize(labelHeight.getPreferredSize());

			JTextField tfWidth = new JTextField("100");
			JTextField tfHeight = new JTextField("10");

			tfWidth.setColumns(3);
			tfWidth.setFont(font);
			tfWidth.setMaximumSize(tfWidth.getPreferredSize());
			tfHeight.setColumns(3);
			tfHeight.setFont(font);
			tfHeight.setMaximumSize(tfHeight.getPreferredSize());
			
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
			scrollPane.setColumnHeaderView(tb);
			
			// Button bindings
			btnBack.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					view.setOverlay(OverlayScreen.CONFIRM);
				}
			});
	
			btnSave.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					scrollPane.setColumnHeaderView(tbSave);
				}
			});
			
			btnReset.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					view.setOverlay(OverlayScreen.EDITOR_CONFIRM);
				}
			});
			
			btnUndo.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					grid.undo();
				}
			});
			
			btnRedo.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					grid.redo();
				}
			});			
			
			btnPencil.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setLabelText("Draw Mode");
					toggle(toggles, btnPencil, Mode.DRAW);
				}
			});
			
			btnEraser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setLabelText("Erase Mode");
					toggle(toggles, btnEraser, Mode.ERASE);
				}
			});
			
			btnMove.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setLabelText("Move Mode");
					toggle(toggles, btnMove, Mode.MOVE);
				}
			});
			
			btnCopy.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setLabelText("Copy/Paste Mode");
					toggle(toggles, btnCopy, Mode.COPYPASTE);
				}
			});
			
			btnCrop.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setLabelText("Crop Mode");
					toggle(toggles, btnCrop, Mode.CROP);
				}
			});
			
			// Other bindings
			tfHeight.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (v.verifyInt(tfHeight, true))
						grid.resizeGrid(Integer.parseInt(tfHeight.getText()), grid.getGridWidth());
					else
					{
						tfHeight.setText("15");
						grid.resizeGrid(15, grid.getGridWidth());
					}
				}
			});
			
			tfWidth.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (v.verifyInt(tfWidth, false))
						grid.resizeGrid(grid.getGridHeight(), Integer.parseInt(tfWidth.getText()));
					else
						tfWidth.setText(Integer.toString(grid.getGridWidth()));
				}
			});
			
		    scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener()
		    {
		        @Override
		        public void adjustmentValueChanged(AdjustmentEvent e)
		        {
		            repaint();
		        }
		    });
		    
			addComponentListener(new ComponentAdapter()
			{
				@Override
				public void componentResized(ComponentEvent e)
				{
					grid.resizeScreen();
				}
			});

			JPanel west = new JPanel();
			west.add(btnBack);
			west.add(btnSave);
			west.add(labelMode);

			JPanel centre = new JPanel();
			centre.add(btnReset);
			centre.add(btnUndo);
			centre.add(btnRedo);
			centre.add(btnPencil);
			centre.add(btnEraser);
			centre.add(btnMove);
			centre.add(btnCopy);
			centre.add(btnCrop);
			
			JPanel east = new JPanel();
			east.add(labelHeight);
			east.add(tfHeight);
			east.add(labelWidth);
			east.add(tfWidth);
			
			tb.setLayout(new BorderLayout());
			tb.add(west, BorderLayout.WEST);
			tb.add(centre, BorderLayout.CENTER);
			tb.add(east, BorderLayout.EAST);
			tb.setFloatable(false);

			add(scrollPane, BorderLayout.CENTER);

			// Defaults
			btnPencil.setBorder(BorderFactory.createLoweredBevelBorder());
			btnPencil.setSelected(true);
			
			setOpaque(false);
			setVisible(true);
			view.pack();
			
			//-----------------//											 	// Save toolbar stuff
			
			JPanel savePanel = new JPanel();
			JLabel saveLabel = new JLabel("Save as...");
			JButton saveBtn = new JButton("Save");
			JButton saveCancel = new JButton("Cancel");
			JTextField savetf = new JTextField();
			JLabel errMsg = new JLabel("");
			
			saveBtn.setContentAreaFilled(false);
			saveCancel.setContentAreaFilled(false);
			savetf.setColumns(20);
			saveLabel.setFont(font);
			savetf.setFont(font);
			saveBtn.setFont(font);
			saveCancel.setFont(font);
			errMsg.setFont(font);
			saveBtn.setPreferredSize(saveCancel.getPreferredSize());

			savetf.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					saveBtn.doClick();		// Simulate clicking the save button
				}
			});
			
			saveBtn.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (!v.verifyText(savetf))
					{
						errMsg.setText("Invalid level name.");
					}
					else
					{
						errMsg.setText("");
						File save = new File(Main.MAPS_DIR + savetf.getText());
						
						if (save.exists())
							scrollPane.setColumnHeaderView(tbConfirm);
						else
						{
							if (save(savetf.getText()))
								setLabelText("Saved!");
							else
								setLabelText("Save error!");
							
							scrollPane.setColumnHeaderView(tb);
						}
					}
				}
			});
			
			saveCancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					scrollPane.setColumnHeaderView(tb);
					savetf.setText("");
					errMsg.setText("");
				}
			});
			
			savePanel.add(saveLabel);
			savePanel.add(savetf);
			savePanel.add(saveBtn);
			savePanel.add(saveCancel);
			savePanel.add(errMsg);
			
			tbSave.setLayout(new BorderLayout());
			tbSave.add(savePanel, BorderLayout.CENTER);
			tbSave.setFloatable(false);
			tbSave.setPreferredSize(tb.getPreferredSize());
			
			//-----------------//											 	// Confirm toolbar stuff

			JPanel confirmPanel = new JPanel();
			JLabel confirmLabel = new JLabel("A map with that name already exists. Overwrite?");
			JButton confirmOverwrite = new JButton("Overwrite");
			JButton confirmCancel = new JButton("Cancel");
			
			confirmCancel.setPreferredSize(confirmOverwrite.getPreferredSize());
			confirmCancel.setContentAreaFilled(false);
			confirmOverwrite.setContentAreaFilled(false);
			confirmLabel.setFont(font);
			confirmCancel.setFont(font);
			confirmOverwrite.setFont(font);
			confirmCancel.setPreferredSize(confirmOverwrite.getPreferredSize());
			
			confirmOverwrite.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (save(savetf.getText()))
						setLabelText("Saved!");
					else
						setLabelText("Save error!");
					
					scrollPane.setColumnHeaderView(tb);
				}
			});
			
			confirmCancel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					scrollPane.setColumnHeaderView(tbSave);
				}
			});
			
			confirmPanel.add(confirmLabel);
			confirmPanel.add(confirmOverwrite);
			confirmPanel.add(confirmCancel);
			
			tbConfirm.setLayout(new BorderLayout());
			tbConfirm.add(confirmPanel, BorderLayout.CENTER);
			tbConfirm.setFloatable(false);
			tbConfirm.setPreferredSize(tb.getPreferredSize());
			
			grid.reset();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Grid getGrid()
	{
		return grid;
	}
	
	private void toggle(JButton[] toggleBtns, JButton toggle, Mode mode)
	{
		grid.setMode(mode);
		
		for (JButton btn : toggleBtns)
		{
			if (btn == toggle)
			{
				btn.setSelected(true);
				btn.setBorder(BorderFactory.createLoweredBevelBorder());
			}
			else
			{
				btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				btn.setSelected(false);
			}
		}
	}
	
	public void setLabelText(String text)
	{
		labelMode.setText(text);
	}
	
	private boolean save(String filename)
	{
		File dir = new File(Main.MAPS_DIR);
		File file = new File(Main.MAPS_DIR + filename);
		String saveData = "";
		saveData += grid.getGridHeight() + System.lineSeparator();
		saveData += grid.getGridWidth() + System.lineSeparator();
		
		if (!dir.exists())
			dir.mkdir();
		
		HashMap<Point, Point> obstacles = grid.getObstacles();
		for (HashMap.Entry<Point, Point> i : obstacles.entrySet())
		{
			Point p1 = i.getKey();
			Point p2 = i.getValue();
			saveData += p1.x + " " + p1.y + " " + p2.x + " " + p2.y + System.lineSeparator();
		}
		
		if (Main.DEBUG)
		{
			System.out.println("Map data:");
			System.out.println(saveData);
		}
		
		try
		{
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(saveData);
			bw.close();
			fw.close();
			
			if (Main.DEBUG)
				System.out.println("Saved map to " + file.toString());
			
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
