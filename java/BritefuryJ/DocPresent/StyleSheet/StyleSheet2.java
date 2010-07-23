//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;
import java.util.HashMap;

import BritefuryJ.AttributeTable.Attribute;
import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.AttributeTable.AttributeTable2;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.ApplyStyleSheet;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;

public class StyleSheet2 extends AttributeTable2
{
	public static final InheritedAttributeNonNull fontFace = new InheritedAttributeNonNull( "primitive", "fontFace", String.class, "Sans serif" );
	public static final InheritedAttributeNonNull fontBold = new InheritedAttributeNonNull( "primitive", "fontBold", Boolean.class, false );
	public static final InheritedAttributeNonNull fontItalic = new InheritedAttributeNonNull( "primitive", "fontItalic", Boolean.class, false );
	public static final InheritedAttributeNonNull fontSize = new InheritedAttributeNonNull( "primitive", "fontSize", Integer.class, 14 );
	public static final InheritedAttributeNonNull fontScale = new InheritedAttributeNonNull( "primitive", "fontScale", Double.class, 1.0 );
	public static final InheritedAttributeNonNull border = new InheritedAttributeNonNull( "primitive", "border", Border.class, new SolidBorder( 1.0, 2.0, Color.black, null ) );
	public static final Attribute background = new Attribute( "primitive", "background", Painter.class, null );
	public static final Attribute hoverBackground = new Attribute( "primitive", "hoverBackground", Painter.class, null );
	public static final InheritedAttribute cursor = new InheritedAttribute( "primitive", "cursor", Cursor.class, null );
	public static final InheritedAttributeNonNull fractionVSpacing = new InheritedAttributeNonNull( "primitive", "fractionVSpacing", Double.class, 2.0 );
	public static final InheritedAttributeNonNull fractionHPadding = new InheritedAttributeNonNull( "primitive", "fractionHPadding", Double.class, 3.0 );
	public static final InheritedAttributeNonNull fractionRefYOffset = new InheritedAttributeNonNull( "primitive", "fractionRefYOffset", Double.class, 5.0 );
	public static final InheritedAttributeNonNull fractionFontScale = new InheritedAttributeNonNull( "primitive", "fractionFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull fractionMinFontScale = new InheritedAttributeNonNull( "primitive", "fractionMinFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull editable = new InheritedAttributeNonNull( "primitive", "editable", Boolean.class, true );
	public static final InheritedAttributeNonNull foreground = new InheritedAttributeNonNull( "primitive", "foreground", Paint.class, Color.black );
	public static final InheritedAttribute hoverForeground = new InheritedAttribute( "primitive", "hoverForeground", Paint.class, null );
	public static final InheritedAttributeNonNull hboxSpacing = new InheritedAttributeNonNull( "primitive", "hboxSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull mathRootThickness = new InheritedAttributeNonNull( "primitive", "mathRootThickness", Double.class, 1.5 );
	public static final InheritedAttributeNonNull paragraphSpacing = new InheritedAttributeNonNull( "primitive", "paragraphSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull paragraphLineSpacing = new InheritedAttributeNonNull( "primitive", "paragraphLineSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull paragraphIndentation = new InheritedAttributeNonNull( "primitive", "paragraphIndentation", Double.class, 0.0 );
	public static final InheritedAttributeNonNull shapePainter = new InheritedAttributeNonNull( "primitive", "shapePainter", Painter.class, new FillPainter( Color.black ) );
	public static final InheritedAttribute hoverShapePainter = new InheritedAttribute( "primitive", "hoverShapePainter", Painter.class, null );
	public static final InheritedAttributeNonNull scriptColumnSpacing = new InheritedAttributeNonNull( "primitive", "scriptColumnSpacing", Double.class, 1.0 );
	public static final InheritedAttributeNonNull scriptRowSpacing = new InheritedAttributeNonNull( "primitive", "scriptRowSpacing", Double.class, 1.0 );
	public static final InheritedAttributeNonNull scriptFontScale = new InheritedAttributeNonNull( "primitive", "scriptFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull scriptMinFontScale = new InheritedAttributeNonNull( "primitive", "scriptMinFontScale", Double.class, 0.9 );
	public static final InheritedAttributeNonNull tableColumnSpacing = new InheritedAttributeNonNull( "primitive", "tableColumnSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull tableColumnExpand = new InheritedAttributeNonNull( "primitive", "tableColumnExpand", Boolean.class, false );
	public static final InheritedAttributeNonNull tableRowSpacing = new InheritedAttributeNonNull( "primitive", "tableRowSpacing", Double.class, 0.0 );
	public static final InheritedAttributeNonNull tableRowExpand = new InheritedAttributeNonNull( "primitive", "tableRowExpand", Boolean.class, false );
	public static final InheritedAttribute textSquiggleUnderlinePaint = new InheritedAttribute( "primitive", "textSquiggleUnderlinePaint", Paint.class, null );
	public static final InheritedAttributeNonNull textSmallCaps = new InheritedAttributeNonNull( "primitive", "textSmallCaps", Boolean.class, false );
	public static final InheritedAttributeNonNull vboxSpacing = new InheritedAttributeNonNull( "primitive", "vboxSpacing", Double.class, 0.0 );

	
	
	public static StyleSheet2 instance = new StyleSheet2();
	
	
	protected StyleSheet2()
	{
		super();
	}
	
	
	protected StyleSheet2 newInstance()
	{
		return new StyleSheet2();
	}
	
	
	public ApplyStyleSheet applyTo(Pres child)
	{
		return new ApplyStyleSheet( this, child );
	}




	public StyleSheet2 withAttr(AttributeBase fieldName, Object value)
	{
		return (StyleSheet2)super.withAttr( fieldName, value );
	}
	
	public StyleSheet2 withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		return (StyleSheet2)super.withAttrs( valuesMap );
	}
		
	public StyleSheet2 withAttrs(AttributeTable2 attribs)
	{
		return (StyleSheet2)super.withAttrs( attribs );
	}
		
	public StyleSheet2 withoutAttr(AttributeBase fieldName)
	{
		return (StyleSheet2)super.withoutAttr( fieldName );
	}
}
