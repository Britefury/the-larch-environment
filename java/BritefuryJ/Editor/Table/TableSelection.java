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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.TableElement;
import BritefuryJ.LSpace.Focus.Selection;

public class TableSelection extends Selection
{
	protected AbstractTableEditorInstance<?> editorInstance;
	protected TableElement table;
	private int x0, y0, x1, y1;		// bounds are INCLUSIVE
	
	
	public TableSelection(AbstractTableEditorInstance<?> editorInstance, TableElement table, int x0, int y0, int x1, int y1)
	{
		super( (LSElement)table );

		this.editorInstance = editorInstance;
		this.table = table;
		
		if ( x0 == -1 && x1 == -1 )
		{
			x0 = 0;
			x1 = editorInstance.getMaxRowWidth() - 1;
		}

		if ( y0 == -1 && y1 == -1 )
		{
			y0 = 0;
			y1 = editorInstance.getHeight() - 1;
		}

		this.x0 = Math.max( x0, 0 );
		this.y0 = Math.max( y0, 0 );
		this.x1 = Math.max( x1, 0 );
		this.y1 = Math.max( y1, 0 );
	}

	@Override
	public LSRegion getRegion()
	{
		LSElement tableElement = (LSElement)table;
		return tableElement.getRegion();
	}
	
	@Override
	public boolean isEditable()
	{
		return getRegion().isEditable();
	}

	
	@Override
	public void draw(Graphics2D graphics)
	{
		LSElement tableElement = (LSElement)table;
		
		if ( tableElement.isRealised() )
		{
			int ex0 = editorInstance.tableXToElementX( x0 );
			int ey0 = editorInstance.tableYToElementY( y0 );
			int ex1 = editorInstance.tableXToElementX( x1 );
			int ey1 = editorInstance.tableYToElementY( y1 );
			
			double rx0 = table.getColumnBoundaryX( ex0 );
			double ry0 = table.getRowBoundaryY( ey0 );
			double rx1 = table.getColumnBoundaryX( ex1 + 1 );
			double ry1 = table.getRowBoundaryY( ey1 + 1 );
			AffineTransform current = tableElement.pushGraphicsTransform( graphics );
			Shape shape = new Rectangle2D.Double( rx0, ry0, rx1 - rx0, ry1 - ry0 );
			Paint prevPaint = graphics.getPaint();
			Stroke prevStroke = graphics.getStroke();
			//graphics.setPaint( new Color( 0.5f, 0.6f, 0.7f, 0.25f ) );
			graphics.setPaint( new Color( 0.75f, 0.8f, 0.85f, 0.25f ) );
			graphics.fill( shape );
			graphics.setPaint( new Color( 0.0f, 0.0f, 1.0f, 0.65f ) );
			graphics.setStroke( new BasicStroke( 2.0f ) );
			graphics.draw( shape );
			graphics.setPaint( prevPaint );
			graphics.setStroke( prevStroke );
			tableElement.popGraphicsTransform( graphics, current );
		}
	}
	
	
	protected Object[][] getSelectedData()
	{
		return editorInstance.getSelectedData( this, (LSElement)table, x0, y0, x1, y1 );
	}
	
	
	protected int getX()
	{
		return x0;
	}
	
	protected int getY()
	{
		return y0;
	}
	
	protected int getWidth()
	{
		return x1 - x0 + 1;
	}
	
	protected int getHeight()
	{
		return y1 - y0 + 1;
	}
}
