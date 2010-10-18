//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.BitSet;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeGridRow;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeRGrid;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;

public class DPRGrid extends DPContainerSequence
{
	private int numColumns;

	
	
	public DPRGrid()
	{
		this( TableStyleParams.defaultStyleParams);
	}
	
	public DPRGrid(TableStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeRGrid( this );
	}
	
	protected DPRGrid(DPRGrid element)
	{
		super( element );
		
		layoutNode = new LayoutNodeRGrid( this );
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPRGrid clone = new DPRGrid( this );
		clone.clonePostConstuct( this );
		return clone;
	}
	
	
	
	private void refreshSize()
	{
		numColumns = 0;
		LayoutNodeRGrid gridLayout = (LayoutNodeRGrid)getLayoutNode();
		for (DPElement child: gridLayout.getLeaves())
		{
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				LayoutNodeGridRow rowLayout = (LayoutNodeGridRow)row.getLayoutNode();
				numColumns = Math.max( numColumns, rowLayout.getLeaves().size() );
			}
			else
			{
				numColumns = Math.max( numColumns, 1 );
			}
		}
	}
	
	
	public int width()
	{
		refreshSize();
		return numColumns;
	}
	
	
	

	//
	//
	// CELL BOUNDARY LINES
	//
	//
	
	protected static int[] getSpanFromBitSet(BitSet bits, int startIndex)
	{
		int start = bits.nextSetBit( startIndex );
		if ( start == -1 )
		{
			return new int[] { -1, -1 };
		}
		int end = bits.nextClearBit( start );
		if ( end == -1 )
		{
			end = bits.length();
		}
		return new int[] { start, end - 1 };
	}
	
	


	//
	//
	// DRAW BACKGROUND
	//
	//
	
	@Override
	protected void drawBackground(Graphics2D graphics)
	{
		super.drawBackground( graphics );
		
		Paint cellPaint = getCellPaint();
		if ( cellPaint != null )
		{
			LayoutNodeRGrid layout = (LayoutNodeRGrid)getLayoutNode();
			double columnLines[][] = layout.getColumnLines();
			double rowLines[][] = layout.getRowLines();
			
			Paint prevPaint = graphics.getPaint();
			graphics.setPaint( cellPaint );
			Stroke prevStroke = graphics.getStroke();
			graphics.setStroke( getCellStroke() );
			
			for (double col[]: columnLines)
			{
				double x = col[0];
				for (int i = 1; i < col.length; i += 2)
				{
					double y1 = col[i], y2 = col[i+1];
					Line2D.Double line = new Line2D.Double( x, y1, x, y2 );
					graphics.draw( line );
				}
			}
			
			for (double row[]: rowLines)
			{
				double y = row[0];
				for (int i = 1; i < row.length; i += 2)
				{
					double x1 = row[i], x2 = row[i+1];
					Line2D.Double line = new Line2D.Double( x1, y, x2, y );
					graphics.draw( line );
				}
			}
			
			graphics.setPaint( prevPaint );
			graphics.setStroke( prevStroke );
		}
	}

	
	
	

	//
	//
	// STYLE METHODS
	//
	//
	
	protected double getColumnSpacing()
	{
		return ((TableStyleParams) styleParams).getColumnSpacing();
	}

	protected boolean getColumnExpand()
	{
		return ((TableStyleParams) styleParams).getColumnExpand();
	}

	
	protected double getRowSpacing()
	{
		return ((TableStyleParams) styleParams).getRowSpacing();
	}

	protected boolean getRowExpand()
	{
		return ((TableStyleParams) styleParams).getRowExpand();
	}


	public Stroke getCellStroke()
	{
		return ((TableStyleParams) styleParams).getCellStroke();
	}
	
	public Paint getCellPaint()
	{
		return ((TableStyleParams) styleParams).getCellPaint();
	}
}
