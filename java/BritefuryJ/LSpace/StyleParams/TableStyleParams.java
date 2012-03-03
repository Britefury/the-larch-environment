//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.StyleParams;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.TableBackgroundPainter;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;


public class TableStyleParams extends ContainerStyleParams
{
	public static final TableStyleParams defaultStyleParams = new TableStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, 3.0, false, 3.0, false, null, new BasicStroke( 1.0f ), Color.BLACK );
	
	
	protected final double columnSpacing;
	protected final boolean columnExpand;

	protected final double rowSpacing;
	protected final boolean rowExpand;
	
	protected final Stroke cellBoundaryStroke;
	protected final Paint cellBoundaryPaint;
	
	protected final TableBackgroundPainter tableBackgroundPainter;


	public TableStyleParams(HAlignment hAlign, VAlignment vAlign, Painter background, Painter hoverBackground, Cursor pointerCursor, double columnSpacing,
			boolean columnExpand, double rowSpacing, boolean rowExpand,
			TableBackgroundPainter tableBackgroundPainter, Stroke cellBoundaryStroke, Paint cellBoundaryPaint)
	{
		super( hAlign, vAlign, background, hoverBackground, pointerCursor );
		
		this.columnSpacing = columnSpacing;
		this.columnExpand = columnExpand;

		this.rowSpacing = rowSpacing;
		this.rowExpand = rowExpand;
		
		this.cellBoundaryStroke = cellBoundaryStroke;
		this.cellBoundaryPaint = cellBoundaryPaint;
		
		this.tableBackgroundPainter = tableBackgroundPainter;
	}


	public double getColumnSpacing()
	{
		return columnSpacing;
	}

	public boolean getColumnExpand()
	{
		return columnExpand;
	}


	public double getRowSpacing()
	{
		return rowSpacing;
	}

	public boolean getRowExpand()
	{
		return rowExpand;
	}


	public Stroke getCellBoundaryStroke()
	{
		return cellBoundaryStroke;
	}
	
	public Paint getCellBoundaryPaint()
	{
		return cellBoundaryPaint;
	}
	
	
	public TableBackgroundPainter getTableBackgroundPainter()
	{
		return tableBackgroundPainter;
	}
	
	
	
	protected void buildFieldList(List<Object> fields)
	{
		super.buildFieldList( fields );
		fields.add( new HorizontalField( "Column spacing", Pres.coerceNonNull( columnSpacing ) ) );
		fields.add( new HorizontalField( "Column expand", Pres.coerceNonNull( columnExpand ) ) );
		fields.add( new HorizontalField( "Row spacing", Pres.coerceNonNull( rowSpacing ) ) );
		fields.add( new HorizontalField( "Row expand", Pres.coerceNonNull( rowExpand ) ) );
		fields.add( new HorizontalField( "Cell boundary stroke", Pres.coerceNonNull( cellBoundaryStroke ) ) );
		fields.add( new HorizontalField( "Cell boundary paint", Pres.coerceNonNull( cellBoundaryPaint ) ) );
		fields.add( new HorizontalField( "Table background painter", Pres.coerceNonNull( tableBackgroundPainter ) ) );
	}
}
