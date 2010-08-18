//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.AbstractBorder;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.Painter.OutlinePainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Controls
{
	public static final AttributeNamespace controlsNamespace = new AttributeNamespace( "controls" );
	
	
	public static final InheritedAttributeNonNull buttonBorder = new InheritedAttributeNonNull( controlsNamespace, "buttonBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 10.0, 10.0, new Color( 0.55f, 0.525f, 0.5f ), new Color( 0.85f, 0.85f, 0.85f ) ) );
	public static final InheritedAttributeNonNull buttonHighlightBorder = new InheritedAttributeNonNull( controlsNamespace, "buttonBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 10.0, 10.0, new Color( 0.0f, 0.5f, 0.5f ), new Color( 0.925f, 0.925f, 0.925f ) ) );
	
	
	public static final InheritedAttributeNonNull hyperlinkAttrs = new InheritedAttributeNonNull( controlsNamespace, "hyperlinkAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.editable, false ).withAttr( Primitive.fontFace, "Sans serif" ).withAttr( Primitive.foreground, Color.blue )
			.withAttr( Primitive.hoverForeground, Color.red ).withAttr( Primitive.cursor, new Cursor( Cursor.HAND_CURSOR ) ) );

	
	public static final InheritedAttributeNonNull checkboxHoverBackground = new InheritedAttributeNonNull( controlsNamespace, "checkboxHoverBackground", Painter.class,
			new OutlinePainter( new Color( 0.5f, 0.625f, 0.75f ) ) );
	public static final InheritedAttributeNonNull checkboxCheckBorder = new InheritedAttributeNonNull( controlsNamespace, "checkboxCheckBorder", AbstractBorder.class,
			new FilledBorder( 3.0, 3.0, 3.0, 3.0, 5.0, 5.0, new Color( 0.75f, 0.75f, 0.75f ) ) );
	public static final InheritedAttributeNonNull checkboxCheckForeground = new InheritedAttributeNonNull( controlsNamespace, "checkboxCheckForeground", Paint.class, new Color( 0.0f, 0.2f, 0.4f ) );
	public static final InheritedAttributeNonNull checkboxCheckSize = new InheritedAttributeNonNull( controlsNamespace, "checkboxCheckSize", Double.class, 10.0 );
	public static final InheritedAttributeNonNull checkboxSpacing = new InheritedAttributeNonNull( controlsNamespace, "checkboxSpacing", Double.class, 8.0 );

	
	public static final InheritedAttributeNonNull optionMenuBorder = new InheritedAttributeNonNull( controlsNamespace, "optionMenuBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, Color.BLACK, new Color( 0.9f, 0.95f, 0.9f ) ) );
	public static final InheritedAttributeNonNull optionMenuHoverBorder = new InheritedAttributeNonNull( controlsNamespace, "optionMenuHoverBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.0f, 0.3f, 0.0f ), new Color( 0.95f, 1.0f, 0.95f ) ) );
	public static final InheritedAttributeNonNull optionMenuContentsSpacing = new InheritedAttributeNonNull( controlsNamespace, "optionMenuContentsSpacing", Double.class, 5.0 );
	public static final InheritedAttributeNonNull optionMenuArrowSize = new InheritedAttributeNonNull( controlsNamespace, "optionMenuArrowSize", Double.class, 16.0 );
	public static final InheritedAttributeNonNull optionMenuArrowPainter = new InheritedAttributeNonNull( controlsNamespace, "optionMenuArrowPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.7f, 0.85f, 0.7f ), new Color( 0.0f, 0.25f, 0.0f ), new BasicStroke( 1.0f ) ) );

	
	public static final InheritedAttributeNonNull textEntryBorder = new InheritedAttributeNonNull( controlsNamespace, "textEntryBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.0f, 0.3f, 0.0f ), new Color( 0.9f, 0.95f, 0.9f ) ) );
	public static final InheritedAttributeNonNull textEntryInvalidBorder = new InheritedAttributeNonNull( controlsNamespace, "textEntryInvalidBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.6f, 0.0f, 0.0f ), new Color( 1.0f, 0.85f, 0.85f ) ) );

	
	public static final InheritedAttributeNonNull textAreaAttrs = new InheritedAttributeNonNull( controlsNamespace, "textAreaAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 5.0, 3.0, 3.0, new Color( 0.3f, 0.3f, 0.3f ), null ) ) );

	
	public static final InheritedAttributeNonNull spinEntryArrowSize = new InheritedAttributeNonNull( controlsNamespace, "spinEntryArrowSize", Double.class, 16.0 );
	public static final InheritedAttributeNonNull spinEntryArrowAttrs = new InheritedAttributeNonNull( controlsNamespace, "spinEntryArrowAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.columnSpacing, 2.0 ).
			withAttr( Primitive.shapePainter, new FilledOutlinePainter( new Color( 0.7f, 0.85f, 0.7f ), new Color( 0.0f, 0.25f, 0.0f ), new BasicStroke( 1.0f ) ) ).
			withAttr( Primitive.hoverShapePainter, new FilledOutlinePainter( new Color( 0.85f, 1.0f, 0.85f ), new Color( 0.0f, 0.45f, 0.0f ), new BasicStroke( 1.0f ) ) ) );
	public static final InheritedAttributeNonNull spinEntryHSpacing = new InheritedAttributeNonNull( controlsNamespace, "spinEntryHSpacing", Double.class, 2.0 );
	
	
	public static final InheritedAttributeNonNull scrollBarArrowPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarArrowPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.7f, 0.85f, 1.0f ), new Color( 0.0f, 0.5f, 1.0f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttributeNonNull scrollBarDragBoxPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragBoxPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.7f, 0.85f, 1.0f ), new Color( 0.0f, 0.5f, 1.0f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttributeNonNull scrollBarDragBarBackgroundPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragBarBackgroundPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.9f, 0.9f, 0.9f ), new Color( 0.75f, 0.75f, 0.75f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttribute scrollBarDragBackgroundHoverPainter = new InheritedAttribute( controlsNamespace, "scrollBarDragBackgroundHoverPainter", Painter.class, null );
	public static final InheritedAttributeNonNull scrollBarArrowPadding = new InheritedAttributeNonNull( controlsNamespace, "scrollBarArrowPainter", Double.class, 0.0 );
	public static final InheritedAttributeNonNull scrollBarArrowSpacing = new InheritedAttributeNonNull( controlsNamespace, "scrollBarArrowPainter", Double.class, 2.0 );
	public static final InheritedAttributeNonNull scrollBarArrowDragboxPadding = new InheritedAttributeNonNull( controlsNamespace, "scrollBarArrowPainter", Double.class, 3.0 );
	public static final InheritedAttributeNonNull scrollBarArrowDragboxRounding = new InheritedAttributeNonNull( controlsNamespace, "scrollBarArrowPainter", Double.class, 4.0 );
	public static final InheritedAttributeNonNull scrollBarSize = new InheritedAttributeNonNull( controlsNamespace, "scrollBarArrowPainter", Double.class, 20.0 );

	
	public static final InheritedAttributeNonNull bClosePopupOnActivate = new InheritedAttributeNonNull( controlsNamespace, "bClosePopupOnActivate", Boolean.class, false );
	
	public static final InheritedAttributeNonNull menuItemHoverBackground = new InheritedAttributeNonNull( controlsNamespace, "menuItemHoverBackground", Painter.class,
			new FillPainter( new Color( 0.6f, 0.7f, 0.85f ) ) );
	public static final InheritedAttributeNonNull menuItemXPadding = new InheritedAttributeNonNull( controlsNamespace, "menuItemXPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull menuItemYPadding = new InheritedAttributeNonNull( controlsNamespace, "menuItemYPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull popupMenuAttrs = new InheritedAttributeNonNull( controlsNamespace, "popupMenuAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 1.0, 2.0, Color.black, null ) ).withAttr( Primitive.rowSpacing, 10.0 ) );

	
	public static final InheritedAttributeNonNull tooltipBorder = new InheritedAttributeNonNull( controlsNamespace, "tooltipBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 2.0, 2.0, 2.0, Color.BLACK, new Color( 1.0f, 1.0f, 0.9f ) ) );
	
	
	
	
	
	public static StyleValues useButtonAttrs(StyleValues style)
	{
		return style.useAttr( buttonBorder ).useAttr( buttonHighlightBorder );
	}
	
	
	public static StyleValues hyperlinkStyle(StyleValues style)
	{
		return style.withAttrs( style.get( hyperlinkAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useHyperlinkAttrs(StyleValues style)
	{
		return style.useAttr( hyperlinkAttrs );
	}
	
	
	protected static DerivedValueTable<StyleSheet> checkboxStyle = new DerivedValueTable<StyleSheet>( controlsNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			double spacing = attribs.get( checkboxSpacing, Double.class );
			Painter background = attribs.get( checkboxHoverBackground, Painter.class );
			return StyleSheet.instance.withAttr( Primitive.hoverBackground, background ).withAttr( Primitive.rowSpacing, spacing );
		}
	};

	public static StyleValues useCheckboxAttrs(StyleValues style)
	{
		return style.useAttr( checkboxHoverBackground ).useAttr( checkboxCheckBorder ).useAttr( checkboxCheckForeground ).useAttr( checkboxCheckSize ).useAttr( checkboxSpacing );
	}
	
	

	public static StyleValues useOptionMenuAttrs(StyleValues style)
	{
		return style.useAttr( optionMenuBorder ).useAttr( optionMenuHoverBorder ).useAttr( optionMenuContentsSpacing ).useAttr( optionMenuArrowSize ).useAttr( optionMenuArrowPainter );
	}
	
	
	
	public static StyleValues useTextEntryAttrs(StyleValues style)
	{
		return style.useAttr( textEntryBorder ).useAttr( textEntryInvalidBorder );
	}
	
	
	
	protected static DerivedValueTable<StyleSheet> scrollBarDragBoxStyle = new DerivedValueTable<StyleSheet>( controlsNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			Painter backgroundPainter = attribs.get( scrollBarDragBarBackgroundPainter, Painter.class );
			Painter backgroundHoverPainter = attribs.get( scrollBarDragBackgroundHoverPainter, Painter.class );
			return StyleSheet.instance.withAttr( Primitive.shapePainter, backgroundPainter ).withAttr( Primitive.hoverShapePainter, backgroundHoverPainter );
		}
	};


	
	public static StyleValues useMenuItemAttrs(StyleValues style)
	{
		return style.useAttr( checkboxHoverBackground ).useAttr( checkboxCheckBorder ).useAttr( checkboxCheckForeground ).useAttr( checkboxCheckSize ).useAttr( checkboxSpacing );
	}
	
	
	public static StyleValues popupMenuStyle(StyleValues style)
	{
		return style.withAttrs( style.get( popupMenuAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues usePopupMenuAttrs(StyleValues style)
	{
		return style.useAttr( popupMenuAttrs );
	}




	public static StyleValues useTooltipAttrs(StyleValues style)
	{
		return style.useAttr( tooltipBorder );
	}
}
