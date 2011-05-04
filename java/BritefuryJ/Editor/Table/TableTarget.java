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

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TableElement;
import BritefuryJ.DocPresent.Selection.SelectionPoint;
import BritefuryJ.DocPresent.Target.Target;

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
		DPElement tableElement = (DPElement)table;
		
		if ( tableElement.isRealised() )
		{
			int ex = editorInstance.tableXToElementX( x );
			int ey = editorInstance.tableYToElementY( y );
			
			double x0 = table.getColumnBoundaryX( ex );
			double y0 = table.getRowBoundaryY( ey );
			double x1 = table.getColumnBoundaryX( ex + 1 );
			double y1 = table.getRowBoundaryY( ey + 1 );
			AffineTransform current = tableElement.pushGraphicsTransform( graphics );
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
	public DPElement getElement()
	{
		return (DPElement)table;
	}

	@Override
	public SelectionPoint createSelectionPoint()
	{
		return new TableSelectionPoint( editorInstance, table, x, y );
	}
}
