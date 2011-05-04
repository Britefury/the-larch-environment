//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import java.awt.Color;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class TableEditorStyle
{
	public static final AttributeNamespace tableEditorNamespace = new AttributeNamespace( "tableEditor" );
	
	public static final InheritedAttributeNonNull tableAttrs = new InheritedAttributeNonNull( tableEditorNamespace, "tableAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.tableCellBoundaryPaint, Color.BLACK ).withAttr( Primitive.tableBorder, new SolidBorder() )
			.withAttr( Primitive.tableColumnSpacing, 5.0 ).withAttr( Primitive.tableRowSpacing, 5.0 ) );



	public static StyleSheet tableStyle(StyleValues style)
	{
		return style.get( tableAttrs, StyleSheet.class );
	}
	
	public static StyleValues useTableAttrs(StyleValues style)
	{
		return style.useAttr( tableAttrs );
	}

}
