package com.XDScroller;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class Grid extends JPanel
{
	private static final int MOVE = 0;
	private static final int ADD = 1;
	private static final int DELETE = -1;
	private static final int ADD_HOVER = 2;
	private static final int DELETE_HOVER = -2;
	
	private JScrollPane parent;
	private LevelEditor parentPanel;
	
	private Motion motion;
	private int gridHeight, gridWidth;
	private double scale;
	private boolean hasCopy;
	Point init, hover, release, current, end, orig, origEnd;
	
	private Mode mode;
	
	private Stack<StackEvent> undo, redo;
	private GridSquare[][] grid;
	private HashMap<Point, Point> obstacles;
	
	public enum Mode
	{
		DRAW,
		ERASE,
		MOVE,
		COPYPASTE,
		CROP
	}
	
	public class StackEvent
	{
		public Point p1, p2;
		public Point newP1, newP2;
		public int action;
		
		public StackEvent(Point p1, Point p2, int action)
		{
			this.p1 = p1;
			this.p2 = p2;
			this.action = action;
		}
		
		public StackEvent(Point oldP1, Point oldP2, int action, Point newP1, Point newP2)
		{
			this(oldP1, oldP2, action);
			this.newP1 = newP1;
			this.newP2 = newP2;
		}
	}
	
	public class Motion implements MouseMotionListener, MouseListener
	{
		public Motion() {}
		
		public void gridAction(Point p1, Point p2, int action)
		{
			int x1 = Math.min(p1.x, p2.x);
			int x2 = Math.max(p1.x, p2.x);
			int y1 = Math.min(p1.y, p2.y);
			int y2 = Math.max(p1.y, p2.y);
			
			for (int i = y1; i <= y2; i++)
			{
				for (int j = x1; j <= x2; j++)
				{
					GridSquare tile = grid[i][j];
					
					switch (action)
					{
						case ADD:
							if (tile.key == null)
								tile.key = new Point(x1, y1);
							
							tile.val++;
							break;
							
						case DELETE:
							if (tile.val > 0)
							{
								if (tile.val == 3)		// In case of undoing during copy/paste mode.
									tile.val = 0;
								else
									tile.val--;
								
								if (tile.val == 0)
									tile.key = null;
							}
							break;
						
						case ADD_HOVER:
							tile.val += action;
							break;
							
						case DELETE_HOVER:
							if (tile.val > ADD_HOVER)
								tile.val += action;
							break;
							
						default:
							break;
					}
				}
			}
			
			repaint();
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			init = getHoveredSquare(e.getPoint());

			switch(mode)
			{
				case DRAW:
				{
					hover = init;
					gridAction(init, init, ADD);
					break;
				}
				case ERASE:
				{
					GridSquare clickedSquare = grid[init.y][init.x];
					
					if (clickedSquare.key != null)
					{
						end = obstacles.remove(clickedSquare.key);
						undo.push(new StackEvent(clickedSquare.key, end, DELETE));
						gridAction(clickedSquare.key, end, DELETE);
					}
					break;
				}
				case MOVE:
				case CROP:
				{
					hover = init;
					GridSquare clickedSquare = grid[init.y][init.x];
					init = clickedSquare.key;
					end = obstacles.get(init);
					orig = init;
					origEnd = end;
					break;
				}
				case COPYPASTE:
				{
					GridSquare clickedSquare = grid[init.y][init.x];
					
					if (clickedSquare.key != null)						// Clicked on an occupied square
					{
						if (hasCopy)									// Remove the old copy data and add new copy
							gridAction(orig, origEnd, DELETE_HOVER);							

						orig = clickedSquare.key;
						origEnd = obstacles.get(orig);
						gridAction(orig, origEnd, ADD_HOVER);
						parentPanel.setLabelText("Copied!");
						hasCopy = true;
					}
					else if (hasCopy)									// Clicked on an empty square + has copied data
					{
						int dx = origEnd.x - orig.x;
						int dy = origEnd.y - orig.y;

						Point endpoint = new Point(init.x + dx, init.y + dy);
						
						if (endpoint.x > gridWidth - 1)					// OOB checks
							endpoint.x = gridWidth - 1;
						
						if (endpoint.y > gridHeight - 1)
							endpoint.y = gridHeight - 1;
							
						gridAction(init, endpoint, ADD);
						
						if (validate(init, endpoint))
						{
							obstacles.put(init, endpoint);
							parentPanel.setLabelText("Pasted!");
							undo.push(new StackEvent(init, endpoint, ADD));
						}
						else
						{
							gridAction(init, endpoint, DELETE);
							parentPanel.setLabelText("Invalid paste");
						}
					}
					// If clicked on empty square + no copied data, do nothing
					break;
				}
				default:
					break;
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			current = getHoveredSquare(e.getPoint());
			
			switch(mode)
			{
				case DRAW:
				{
					if (current != init && current != hover)
					{
						gridAction(init, hover, DELETE);
						gridAction(init, current, ADD);
						
						hover = current;
					}
					break;
				}
				case ERASE:
				{
					GridSquare clickedSquare = grid[current.y][current.x];
					
					if (clickedSquare.key != null)
					{
						end = obstacles.remove(clickedSquare.key);
						undo.push(new StackEvent(clickedSquare.key, end, DELETE));
						gridAction(clickedSquare.key, end, DELETE);
					}
					break;
				}
				case MOVE:
				{
					if (end != null && current != hover)
					{
						int dx = current.x - hover.x;
						int dy = current.y - hover.y;
						
						// OOB checking
						int left = Math.min(init.x, end.x);
						int right = Math.max(init.x, end.x);
						int upper = Math.min(init.y, end.y);
						int lower = Math.max(init.y, end.y);
						
						if (right + dx > gridWidth - 1 || left + dx < 0)
							dx = 0;
						if (lower + dy > gridHeight - 1 || upper + dy < 0)
							dy = 0;
						
						gridAction(init, end, DELETE);					// Delete the old block
						init = new Point(init.x + dx, init.y + dy);		// Update the position
						end = new Point(end.x + dx, end.y + dy);
						gridAction(init, end, ADD);						// Draw the new block
						
						hover = current;								// Track the difference
					}
					break;
				}
				case CROP:
				{
					if (end != null && current != hover)
					{
						gridAction(init, end, DELETE);					// Delete the old block

						int dx = current.x - hover.x;
						int dy = current.y - hover.y;
						end = new Point(end.x + dx, end.y + dy);

						end.x = Math.min(end.x, gridWidth - 1);
						end.x = Math.max(end.x, 0);
						end.y = Math.min(end.y, gridHeight - 1);
						end.y = Math.max(end.y, 0);
						
						gridAction(init, end, ADD);						// Draw the new block
						
						hover = current;								// Track the difference
					}
					break;
				}
				default:
					break;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			release = getHoveredSquare(e.getPoint());
			
			switch (mode)
			{
				case DRAW:
				{
					if (validate(init, release))
					{
						Point key = new Point(Math.min(init.x, release.x), Math.min(init.y, release.y));
						Point val = new Point(Math.max(init.x, release.x), Math.max(init.y, release.y));
						
						undo.push(new StackEvent(init, release, ADD));	// Push action to stack
						obstacles.put(key, val);						// Track the obstacle.
						if (!redo.isEmpty())							// Clear the redo stack if there's actions there.
							redo.clear();
					}
					else
					{
						gridAction(init, release, DELETE);
					}
					break;
				}
				case CROP:
				{
					if (end != null)
					{
						if (validate(init, end))
						{
							Point p1 = new Point(Math.min(init.x, end.x), Math.min(init.y, end.y));
							Point p2 = new Point(Math.max(init.x, end.x), Math.max(init.y, end.y));
							
							obstacles.remove(orig);
							obstacles.put(p1, p2);
							
							
							undo.push(new StackEvent(orig, origEnd, MOVE, p1, p2));
							if (!redo.isEmpty())
								redo.clear();
						}
						else
						{
							gridAction(init, end, DELETE);
							gridAction(orig, origEnd, ADD);
						}
					}
					break;
				}
				case MOVE:
				{
					if (end != null)
					{
						if (validate(init, end))
						{
							obstacles.remove(orig);
							obstacles.put(init, end);
							
							undo.push(new StackEvent(orig, origEnd, MOVE, init, end));
							if (!redo.isEmpty())
								redo.clear();
						}
						else
						{
							gridAction(init, end, DELETE);
							gridAction(orig, origEnd, ADD);
						}
					}
					break;
				}
				default:
					break;
			}
		}
		
		@Override
		public void mouseMoved(MouseEvent e)
		{
			Point point = getHoveredSquare(e.getPoint());
			
			if (mode == Mode.MOVE && grid[point.y][point.x].val > 0)
			{
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			else
			{
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e)
		{
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		private Point getHoveredSquare(Point mousePosn)
		{
			int x = (int) (mousePosn.x / scale);
			int y = (int) (mousePosn.y / scale);
			
			if (x < 0)
				x = 0;
			if (y < 0)
				y = 0;
			
			if (x >= gridWidth)
				x = gridWidth - 1;
			if (y >= gridHeight)
				y = gridHeight - 1;
			
			return new Point(x, y);
		}
		
		private boolean validate(Point p1, Point p2)
		{
			int x1 = Math.min(p1.x, p2.x);
			int x2 = Math.max(p1.x, p2.x);
			int y1 = Math.min(p1.y, p2.y);
			int y2 = Math.max(p1.y, p2.y);
		
			for (int i = y1; i <= y2; i++)
			{
				for (int j = x1; j <= x2; j++)
				{
					// Disallow adding of overlapped obstacle.
					if (grid[i][j].val > 1)
						return false;
				}
			}
			
			return true;
		}
		
		// Boilerplate
		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
	}
	
	private class GridSquare
	{
		public Point key;	// Obstacles cannot overlap, so we use the initial point as our key
		public int val;
		
		public GridSquare()
		{
			key = null;
			val = 0;
		}
	}
	
	public Grid(JScrollPane parent, LevelEditor parentPanel)
	{
		super();
		this.parent = parent;
		this.parentPanel = parentPanel;
		
		motion = new Motion();
		undo = new Stack<StackEvent>();
		redo = new Stack<StackEvent>();
		obstacles = new HashMap<Point, Point>();
		mode = Mode.DRAW;
		
		addMouseListener(motion);
		addMouseMotionListener(motion);
		
		// Just defaults, can be edited by user
		this.gridHeight = 10;
		this.gridWidth = 100;
		grid = new GridSquare[gridHeight][gridWidth];
		
		setVisible(true);
		resizeScreen();
	}
	
	public void resizeScreen()
	{
		scale = (parent.getViewportBorderBounds().height - parent.getInsets().bottom) / (float) gridHeight;
		setBounds(parent.getViewportBorderBounds());
		setPreferredSize(new Dimension((int) Math.ceil(scale * gridWidth), (int) Math.ceil(scale * gridHeight)));
		repaint();
	}
	
	public void resizeGrid(int newHeight, int newWidth)
	{
		// Fast break if there's nothing to do.
		if (newHeight == gridHeight && newWidth == gridWidth)
			return;
		
		GridSquare[][] newGrid = new GridSquare[newHeight][newWidth];
		
		for (int i = 0; i < newHeight; i++)
		{
			for (int j = 0; j < newWidth; j++)
			{
				newGrid[i][j] = new GridSquare();
				
				if (i < gridHeight && j < gridWidth)
					newGrid[i][j] = grid[i][j];
			}
		}
		
		grid = newGrid;
		gridHeight = newHeight;
		gridWidth = newWidth;

		resizeScreen();
	}
	
	public void undo()
	{
		if (!undo.isEmpty())
		{
			StackEvent e = undo.pop();
			if (e.action == MOVE) 							// Crop treated as a "move"
			{
				motion.gridAction(e.newP1, e.newP2, DELETE);
				motion.gridAction(e.p1, e.p2, ADD);

				obstacles.remove(e.newP1);
				obstacles.put(e.p1, e.p2);
			}
			else // Drawing / erasing
			{
				e.action *= -1;								// We want to do the inverse action.
				motion.gridAction(e.p1, e.p2, e.action);
				
				if (e.action == ADD)
					obstacles.put(e.p1, e.p2);
				else
					obstacles.remove(e.p1);
			}
			redo.push(e);
		}
	}
	
	public void redo()
	{
		if (!redo.isEmpty())
		{
			StackEvent e = redo.pop();
			if (e.action == MOVE)
			{
				motion.gridAction(e.p1, e.p2, DELETE);
				motion.gridAction(e.newP1, e.newP2, ADD);
				
				obstacles.remove(e.p1);
				obstacles.put(e.newP1, e.newP2);
			}
			else
			{
				e.action *= -1;
				motion.gridAction(e.p1, e.p2, e.action);
				
				if (e.action == ADD)
					obstacles.put(e.p1, e.p2);
				else
					obstacles.remove(e.p1);
			}
			undo.push(e);
		}
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
	
		// Square drawing
		for (int i = 0; i < gridHeight; i++)
		{
			for (int j = 0; j < gridWidth; j++)
			{
				switch(grid[i][j].val)
				{
					case 1:
					{
						g.setColor(Color.DARK_GRAY);
						g.fillRect((int) Math.ceil(j * scale), (int) Math.ceil(i * scale), (int) Math.ceil(scale), (int) Math.ceil(scale));
						break;
					}
					case 2:		// Normal drawing invalidation.
					{
						g.setColor(Color.RED);
						g.fillRect((int) Math.ceil(j * scale), (int) Math.ceil(i * scale), (int) Math.ceil(scale), (int) Math.ceil(scale));
						break;
					}
					case 3:
					{
						g.setColor(Color.CYAN);
						g.fillRect((int) Math.ceil(j * scale), (int) Math.ceil(i * scale), (int) Math.ceil(scale), (int) Math.ceil(scale));
						break;
					}
					default:
						break;
				}
			}
		}
		
		// Grid drawing
		g.setColor(Color.BLACK);
		for (int i = 0; i <= gridWidth; i++)
		{
			g.drawLine((int) Math.ceil(i * scale), 0, (int) Math.ceil(i * scale), (int) Math.ceil(scale * gridHeight));
		}
		
		for (int i = 0; i <= gridHeight; i++)
		{
			g.drawLine(0, (int) Math.ceil(i * scale), (int) Math.ceil(scale * gridWidth), (int) Math.ceil(i * scale));
		}
	
	}
	
	public void reset()
	{
		System.out.println("resetting");
		for (int i = 0; i < gridHeight; i++)
		{
			for (int j = 0; j < gridWidth; j++)
			{
				grid[i][j] = new GridSquare();
			}
		}
		undo.clear();
		redo.clear();
		
		repaint();
	}
	
	public int getGridWidth()
	{
		return gridWidth;
	}
	
	public int getGridHeight()
	{
		return gridHeight;
	}
	
	public Mode getMode()
	{
		return mode;
	}
	
	public void setMode(Mode mode)
	{
		if (this.mode == Mode.COPYPASTE && hasCopy)
		{
			hasCopy = false;
			motion.gridAction(orig, origEnd, DELETE_HOVER);
		}
		
		this.mode = mode;
	}
	
	public HashMap<Point, Point> getObstacles()
	{
		return obstacles;
	}
}