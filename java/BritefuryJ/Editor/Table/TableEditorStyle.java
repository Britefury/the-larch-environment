//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table;

import java.awt.Color;
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Graphics.TableBackgroundCellPainter;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class TableEditorStyle
{
	public static final AttributeNamespace tableEditorNamespace = new AttributeNamespace( "tableEditor" );
	
	public static final InheritedAttributeNonNull tableAttrs = new InheritedAttributeNonNull( tableEditorNamespace, "tableAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.tableCellBoundaryPaint.as( new Color( 0.75f, 0.75f, 0.75f ) ),
				    Primitive.tableBorder.as( new SolidBorder( 1.0, 0.0, new Color( 0.5f, 0.5f, 0.5f ), null ) ),
				    Primitive.tableColumnSpacing.as( 5.0 ), Primitive.tableRowSpacing.as( 5.0 ) ) );
	public static final InheritedAttribute headerRowPaint = new InheritedAttribute( tableEditorNamespace, "headerRowPaint", Paint.class, new Color( 0.925f, 0.925f, 0.925f ) );
	public static final InheritedAttribute headerColumnPaints = new InheritedAttribute( tableEditorNamespace, "headerColumnPaints", Paint[].class,
			new Paint[] { new Color( 0.925f, 0.925f, 0.925f ) } );
	public static final InheritedAttribute bodyPaints = new InheritedAttribute( tableEditorNamespace, "bodyPaints", Paint[].class, null );
	public static final InheritedAttributeNonNull headerAttrs = new InheritedAttributeNonNull( tableEditorNamespace, "headerAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontBold.as( true ), Primitive.foreground.as( new Color( 0.35f, 0.35f, 0.35f ) ) ) );



	public static StyleSheet tableStyle(StyleValues style, boolean hasHeaderRow, boolean hasHeaderColumn)
	{
		Paint hrPaint = style.get( headerRowPaint, Paint.class );
		Paint hcPaints[] = style.get( headerColumnPaints, Paint[].class );
		Paint bPaints[] = style.get( bodyPaints, Paint[].class );
		TableBackgroundCellPainter cellPainter = new TableBackgroundCellPainter( bPaints );
		if ( hasHeaderRow )
		{
			cellPainter = cellPainter.headerRow( hrPaint );
		}
		if ( hasHeaderColumn )
		{
			cellPainter = cellPainter.headerColumnCycle( hcPaints );
		}
		return style.get( tableAttrs, StyleSheet.class ).withAttr( Primitive.tableBackgroundPainter, cellPainter );
	}
	
	public static StyleValues useTableAttrs(StyleValues style)
	{
		return style.useAttr( tableAttrs );
	}
}
