//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeFlowGrid;
import BritefuryJ.LSpace.StyleParams.TableStyleParams;
import BritefuryJ.Math.Point2;


public class LSFlowGrid extends LSContainerSequence implements TableElement
{
	private int targetNumColumns = -1;
	
	public LSFlowGrid(TableStyleParams styleParams, int targetNumColumns, LSElement[] items)
	{
		super( styleParams, items );
		
		layoutNode = new LayoutNodeFlowGrid( this );
		this.targetNumColumns = targetNumColumns;
	}
	
	
	
	public int getTargetNumColumns()
	{
		return targetNumColumns;
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
			LayoutNodeFlowGrid layout = (LayoutNodeFlowGrid)getLayoutNode();

			Paint prevPaint = graphics.getPaint();
			graphics.setPaint( cellPaint );
			Stroke prevStroke = graphics.getStroke();
			graphics.setStroke( getCellBoundaryStroke() );
			
			layout.drawCellLines( graphics );
			
			graphics.setPaint( prevPaint );
			graphics.setStroke( prevStroke );
		}
	}



	@Override
	public int getNumColumns()
	{
		return ((LayoutNodeFlowGrid)layoutNode).getNumColumns();
	}

	@Override
	public int getNumRows()
	{
		return ((LayoutNodeFlowGrid)layoutNode).getNumRows();
	}

	@Override
	public boolean hasChildAt(int x, int y)
	{
		return ((LayoutNodeFlowGrid)layoutNode).hasChildAt( x, y );
	}
	
	@Override
	public LSElement getChildAt(int x, int y)
	{
		return ((LayoutNodeFlowGrid)layoutNode).getChildAt( x, y );
	}

	@Override
	public int getChildColSpan(int x, int y)
	{
		return 1;
	}

	@Override
	public int getChildRowSpan(int x, int y)
	{
		return 1;
	}

	@Override
	public int[] getPositionOfChildCoveringCell(int x, int y)
	{
		return ((LayoutNodeFlowGrid)layoutNode).getPositionOfChildCoveringCell( x, y );
	}

	@Override
	public LSElement getChildCoveringCell(int x, int y)
	{
		return ((LayoutNodeFlowGrid)layoutNode).getChildCoveringCell( x, y );
	}

	@Override
	public int[] getCellPositionUnder(Point2 localPos)
	{
		return ((LayoutNodeFlowGrid)layoutNode).getCellPositionUnder( localPos );
	}

	@Override
	public double getColumnBoundaryX(int column)
	{
		return ((LayoutNodeFlowGrid)layoutNode).getColumnBoundaryX( column );
	}

	@Override
	public double getRowBoundaryY(int row)
	{
		return ((LayoutNodeFlowGrid)layoutNode).getRowBoundaryY( row );
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
