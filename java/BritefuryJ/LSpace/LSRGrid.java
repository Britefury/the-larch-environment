//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.BitSet;
import java.util.List;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeGridRow;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeRGrid;
import BritefuryJ.LSpace.StyleParams.TableStyleParams;
import BritefuryJ.Math.Point2;

public class LSRGrid extends LSContainerSequence implements TableElement
{
	private int numColumns;

	
	
	public LSRGrid(TableStyleParams styleParams)
	{
		this( styleParams, null );
	}
	
	public LSRGrid(TableStyleParams styleParams, LSElement[] items)
	{
		super( styleParams, items );
		
		layoutNode = new LayoutNodeRGrid( this );
	}
	
	
	
	private void refreshSize()
	{
		numColumns = 0;
		LayoutNodeRGrid gridLayout = (LayoutNodeRGrid)getLayoutNode();
		for (LSElement child: gridLayout.getLeaves())
		{
			if ( child instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)child;
				LayoutNodeGridRow rowLayout = (LayoutNodeGridRow)row.getLayoutNode();
				numColumns = Math.max( numColumns, rowLayout.getLeaves().size() );
			}
			else
			{
				numColumns = Math.max( numColumns, 1 );
			}
		}
	}
	
	
	public int getNumColumns()
	{
		refreshSize();
		return numColumns;
	}
	
	public int getNumRows()
	{
		LayoutNodeRGrid gridLayout = (LayoutNodeRGrid)getLayoutNode();
		return gridLayout.getLeaves().size();
	}
	
	
	@Override
	public boolean hasChildAt(int x, int y)
	{
		LayoutNodeRGrid gridLayout = (LayoutNodeRGrid)getLayoutNode();
		
		List<LSElement> rows = gridLayout.getLeaves();
		if ( y < rows.size() )
		{
			LSElement rowElem = rows.get( y );
			if ( rowElem instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)rowElem;
				LayoutNodeGridRow rowLayout = (LayoutNodeGridRow)row.getLayoutNode();
				return x < rowLayout.getLeaves().size();
			}
			else
			{
				return x == 0;
			}
		}
		
		return false;
	}
	
	@Override
	public LSElement getChildAt(int x, int y)
	{
		LayoutNodeRGrid gridLayout = (LayoutNodeRGrid)getLayoutNode();
		
		List<LSElement> rows = gridLayout.getLeaves();
		if ( y < rows.size() )
		{
			LSElement rowElem = rows.get( y );
			if ( rowElem instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)rowElem;
				LayoutNodeGridRow rowLayout = (LayoutNodeGridRow)row.getLayoutNode();
				List<LSElement> rowLeaves = rowLayout.getLeaves();
				return x < rowLeaves.size()  ?  rowLayout.getLeaves().get( x )  :  null;
			}
			else
			{
				return x == 0  ?  rowElem  :  null;
			}
		}
		
		return null;
	}
	
	public int getChildColSpan(int x, int y)
	{
		LayoutNodeRGrid gridLayout = (LayoutNodeRGrid)getLayoutNode();
		
		List<LSElement> rows = gridLayout.getLeaves();
		if ( y < rows.size() )
		{
			LSElement rowElem = rows.get( y );
			if ( rowElem instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)rowElem;
				LayoutNodeGridRow rowLayout = (LayoutNodeGridRow)row.getLayoutNode();
				return x < rowLayout.getLeaves().size()  ?  1  :  -1;
			}
			else
			{
				return x == 0  ?  getNumColumns()  :  -1;
			}
		}
		
		return -1;
	}

	public int getChildRowSpan(int x, int y)
	{
		return hasChildAt( x, y )  ?  1  :  -1;
	}
	
	@Override
	public int[] getPositionOfChildCoveringCell(int x, int y)
	{
		LayoutNodeRGrid gridLayout = (LayoutNodeRGrid)getLayoutNode();
		
		List<LSElement> rows = gridLayout.getLeaves();
		if ( y < rows.size() )
		{
			LSElement rowElem = rows.get( y );
			if ( rowElem instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)rowElem;
				LayoutNodeGridRow rowLayout = (LayoutNodeGridRow)row.getLayoutNode();
				return x < rowLayout.getLeaves().size()  ?  new int[] { x, y }  :  null;
			}
			else
			{
				return x < getNumColumns()  ?  new int[] { 0, y }  :  null;
			}
		}
		
		return null;
	}

	@Override
	public LSElement getChildCoveringCell(int x, int y)
	{
		return getChildAt( x, y );
	}

	public int[] getCellPositionUnder(Point2 localPos)
	{
		LayoutNodeRGrid layout = (LayoutNodeRGrid)getLayoutNode();
		return layout.getCellPositionUnder( localPos );
	}
	

	//
	//
	// CELL BOUNDARY LINES
	//
	//
	
	public double getColumnBoundaryX(int column)
	{
		LayoutNodeRGrid layout = (LayoutNodeRGrid)getLayoutNode();
		return layout.getColumnBoundaryX( column );
	}
	
	public double getRowBoundaryY(int row)
	{
		LayoutNodeRGrid layout = (LayoutNodeRGrid)getLayoutNode();
		return layout.getRowBoundaryY( row );
	}

	
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
		
		TableBackgroundPainter backgroundPainter = getTableBackgroundPainter();
		if ( backgroundPainter != null )
		{
			backgroundPainter.paintTableBackground( this, graphics );
		}
	}
		
	@Override
	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		Paint cellPaint = getCellBoundaryPaint();
		if ( cellPaint != null )
		{
			LayoutNodeRGrid layout = (LayoutNodeRGrid)getLayoutNode();
			
			Paint prevPaint = graphics.getPaint();
			graphics.setPaint( cellPaint );
			Stroke prevStroke = graphics.getStroke();
			graphics.setStroke( getCellBoundaryStroke() );
			
			layout.drawCellLines( graphics );
			
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


	public Stroke getCellBoundaryStroke()
	{
		return ((TableStyleParams) styleParams).getCellBoundaryStroke();
	}
	
	public Paint getCellBoundaryPaint()
	{
		return ((TableStyleParams) styleParams).getCellBoundaryPaint();
	}
	
	public TableBackgroundPainter getTableBackgroundPainter()
	{
		return ((TableStyleParams) styleParams).getTableBackgroundPainter();
	}
}
