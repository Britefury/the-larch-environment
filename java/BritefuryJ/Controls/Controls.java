//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.Color;
import java.awt.Cursor;

import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class Controls
{
	public static final InheritedAttributeNonNull hyperlinkAttrs = new InheritedAttributeNonNull( "controls", "hyperlinkAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.editable, false ).withAttr( Primitive.fontFace, "Sans serif" ).withAttr( Primitive.foreground, Color.blue )
			.withAttr( Primitive.hoverForeground, Color.red ).withAttr( Primitive.cursor, new Cursor( Cursor.HAND_CURSOR ) ) );

	public static final InheritedAttributeNonNull bClosePopupOnActivate = new InheritedAttributeNonNull( "controls", "bClosePopupOnActivate", Boolean.class, false );

	
	public static final InheritedAttributeNonNull menuItemHoverBackground = new InheritedAttributeNonNull( "controls", "menuItemHoverBackground", Painter.class,
			new FillPainter( new Color( 0.6f, 0.7f, 0.85f ) ) );
	public static final InheritedAttributeNonNull menuItemXPadding = new InheritedAttributeNonNull( "controls", "menuItemXPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull menuItemYPadding = new InheritedAttributeNonNull( "controls", "menuItemYPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull popupMenuAttrs = new InheritedAttributeNonNull( "controls", "popupMenuAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.border, new SolidBorder( 1.0, 2.0, Color.black, null ) ).withAttr( Primitive.hboxSpacing, 10.0 ) );

	
	
	
	
	public static StyleSheetValues hyperlinkStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( hyperlinkAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useHyperlinkAttrs(StyleSheetValues style)
	{
		return style.useAttr( hyperlinkAttrs );
	}
	
	
	public static StyleSheetValues popupMenuStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( popupMenuAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues usePopupMenuAttrs(StyleSheetValues style)
	{
		return style.useAttr( popupMenuAttrs );
	}
}
