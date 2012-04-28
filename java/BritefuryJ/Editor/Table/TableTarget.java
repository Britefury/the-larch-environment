//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TableElement;
import BritefuryJ.LSpace.Focus.SelectionPoint;
import BritefuryJ.LSpace.Focus.Target;

public class TableTarget extends Target
{
	protected AbstractTableEditorInstance<?> editorInstance;
	protected TableElement table;
	protected int x, y;
	
	
	public TableTarget(AbstractTableEditorInstance<?> editorInstance, TableElement table, int x, int y)
	{
		this.editorInstance = editorInstance;
		this.table = table;
		this.x = x;
		this.y = y;
	}

	@Override
	public void draw(Graphics2D graphics)
	{
		LSElement tableElement = (LSElement)table;
		
		if ( tableElement.isRealised() )
		{
			int ex = editorInstance.tableXToElementX( x );
			int ey = editorInstance.tableYToElementY( y );
			
			double x0 = table.getColumnBoundaryX( ex );
			double y0 = table.getRowBoundaryY( ey );
			double x1 = table.getColumnBoundaryX( ex + 1 );
			double y1 = table.getRowBoundaryY( ey + 1 );
			AffineTransform current = tableElement.pushLocalToRootGraphicsTransform( graphics );
			Paint prevPaint = graphics.getPaint();
			Stroke prevStroke = graphics.getStroke();
			graphics.setPaint( new Color( 0.2f, 0.3f, 0.4f ) );
			graphics.setStroke( new BasicStroke( 2.0f ) );
			graphics.draw( new Rectangle2D.Double( x0, y0, x1 - x0, y1 - y0 ) );
			graphics.setPaint( prevPaint );
			graphics.setStroke( prevStroke );
			tableElement.popGraphicsTransform( graphics, current );
		}
	}

	@Override
	public LSElement getElement()
	{
		return (LSElement)table;
	}
	
	@Override
	public boolean isEditable()
	{
		return getElement().getRegion().isEditable();
	}

	@Override
	public SelectionPoint createSelectionPoint()
	{
		return new TableSelectionPoint( editorInstance, table, x, y );
	}

	
	private void notifyMoved()
	{
		notifyListenersOfChange();
		((LSElement)table).queueFullRedraw();
	}
	
	@Override
	public void moveLeft()
	{
		x = Math.max( x - 1, 0 );
		notifyMoved();
	}

	@Override
	public void moveRight()
	{
		x = Math.min( x + 1, editorInstance.getRowWidth( y ) - 1 );
		notifyMoved();
	}

	@Override
	public void moveUp()
	{
		y = Math.max( y - 1, 0 );
		int w = editorInstance.getRowWidth( y );
		x = Math.min( x, w - 1 );
		notifyMoved();
	}

	@Override
	public void moveDown()
	{
		y = Math.min( y + 1, editorInstance.getHeight() - 1 );
		int w = editorInstance.getRowWidth( y );
		x = Math.min( x, w - 1 );
		notifyMoved();
	}

	@Override
	public void moveToHome()
	{
		x = 0;
		notifyMoved();
	}

	@Override
	public void moveToEnd()
	{
		x = editorInstance.getRowWidth( y ) - 1;
		notifyMoved();
	}
}
