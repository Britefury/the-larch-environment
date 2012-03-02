//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Path2D;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.TableBackgroundPainter;
import BritefuryJ.DocPresent.TableElement;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;
import BritefuryJ.Util.HashUtils;
import BritefuryJ.Util.WeakValueHashMap;

public class TableEditorStyle
{
	public static final AttributeNamespace tableEditorNamespace = new AttributeNamespace( "tableEditor" );
	
	public static final InheritedAttributeNonNull tableAttrs = new InheritedAttributeNonNull( tableEditorNamespace, "tableAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.tableCellBoundaryPaint.as( new Color( 0.75f, 0.75f, 0.75f ) ),
				    Primitive.tableBorder.as( new SolidBorder( 1.0, 0.0, new Color( 0.5f, 0.5f, 0.5f ), null ) ),
				    Primitive.tableColumnSpacing.as( 5.0 ), Primitive.tableRowSpacing.as( 5.0 ) ) );
	public static final InheritedAttribute headerBackgroundPaint = new InheritedAttribute( tableEditorNamespace, "headerBackgroundPaint", Paint.class, new Color( 0.9f, 0.9f, 0.9f ) );
	public static final InheritedAttributeNonNull headerAttrs = new InheritedAttributeNonNull( tableEditorNamespace, "headerAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontBold.as( true ) ) );



	public static StyleSheet tableStyle(StyleValues style, boolean hasHeaderRow, boolean hasHeaderColumn)
	{
		Paint backgroundPaint = style.get( headerBackgroundPaint, Paint.class );
		return style.get( tableAttrs, StyleSheet.class ).withValues( Primitive.tableBackgroundPainter.as( tableBackgroundPainter( backgroundPaint, hasHeaderRow, hasHeaderColumn ) ) );
	}
	
	public static StyleValues useTableAttrs(StyleValues style)
	{
		return style.useAttr( tableAttrs );
	}

	
	private static class TableBackgKey
	{
		private Paint headerPaint;
		private boolean hasHeaderRow, hasHeaderColumn;
		int hashCode;
		
		
		public TableBackgKey(Paint headerPaint, boolean hasHeaderRow, boolean hasHeaderColumn)
		{
			this.headerPaint = headerPaint;
			this.hasHeaderRow = hasHeaderRow;
			this.hasHeaderColumn = hasHeaderColumn;
			
			this.hashCode = HashUtils.tripleHash( headerPaint.hashCode(), Boolean.valueOf( hasHeaderRow ).hashCode(), Boolean.valueOf( hasHeaderColumn ).hashCode() );
		}
		
		
		@Override
		public int hashCode()
		{
			return hashCode;
		}
		
		@Override
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			else if ( x instanceof TableBackgKey )
			{
				TableBackgKey tx = (TableBackgKey)x;
				return headerPaint.equals( tx.headerPaint )  &&  hasHeaderRow == tx.hasHeaderRow  &&  hasHeaderColumn == tx.hasHeaderColumn;
			}
			else
			{
				return false;
			}
		}
	}
	
	private static final WeakValueHashMap<TableBackgKey, TableBackgroundPainter> backgPainters = new WeakValueHashMap<TableBackgKey, TableBackgroundPainter>();
	
	private static TableBackgroundPainter tableBackgroundPainter(final Paint headerPaint, final boolean hasHeaderRow, final boolean hasHeaderColumn)
	{
		if ( headerPaint != null  &&  ( hasHeaderRow || hasHeaderColumn ) )
		{
			TableBackgKey key = new TableBackgKey( headerPaint, hasHeaderRow, hasHeaderColumn );
			
			TableBackgroundPainter tablePainter = backgPainters.get( key );
			
			if ( tablePainter == null )
			{
				tablePainter = new TableBackgroundPainter()
				{
					@Override
					public void paintTableBackground(TableElement table, Graphics2D graphics)
					{
						Path2D.Double path = new Path2D.Double();

						if ( hasHeaderRow  &&  hasHeaderColumn )
						{
							int numColumns = table.getNumColumns();
							int numRows = table.getNumRows();
							
							double x1 = table.getColumnBoundaryX( 1 );
							double x2 = table.getColumnBoundaryX( numColumns );
							double y1 = table.getRowBoundaryY( 1 );
							double y2 = table.getRowBoundaryY( numRows );
							
							path.moveTo( 0.0, 0.0 );
							path.lineTo( x2, 0.0 );
							path.lineTo( x2, y1 );
							path.lineTo( x1, y1 );
							path.lineTo( x1, y2 );
							path.lineTo( 0.0, y2 );
							path.closePath();
						}
						else if ( hasHeaderRow  &&  !hasHeaderColumn )
						{
							int numColumns = table.getNumColumns();
							
							double x1 = table.getColumnBoundaryX( numColumns );
							double y1 = table.getRowBoundaryY( 1 );
							
							path.moveTo( 0.0, 0.0 );
							path.lineTo( x1, 0.0 );
							path.lineTo( x1, y1 );
							path.lineTo( 0.0, y1 );
							path.closePath();
						}
						else if ( !hasHeaderRow  &&  hasHeaderColumn )
						{
							int numRows = table.getNumRows();
							
							double x1 = table.getColumnBoundaryX( 1 );
							double y1 = table.getRowBoundaryY( numRows );
							
							path.moveTo( 0.0, 0.0 );
							path.lineTo( x1, 0.0 );
							path.lineTo( x1, y1 );
							path.lineTo( 0.0, y1 );
							path.closePath();
						}
						else
						{
							throw new RuntimeException( "Invalid combination of hasHeaderRow and hasHeaderColumn flags" );
						}
						
						Paint prevPaint = graphics.getPaint();
						
						graphics.setPaint( headerPaint );
						graphics.fill( path );
						graphics.setPaint( prevPaint );
					}
				};
				
				backgPainters.put( key, tablePainter );
			}
			
			return tablePainter;
		}
		else
		{
			return null;
		}
	}
}
