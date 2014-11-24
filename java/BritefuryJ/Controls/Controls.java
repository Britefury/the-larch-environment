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
import BritefuryJ.Graphics.*;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Controls
{
	public static final AttributeNamespace controlsNamespace = new AttributeNamespace( "controls" );
	
	
	public static final InheritedAttributeNonNull buttonBorder = new InheritedAttributeNonNull( controlsNamespace, "buttonBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, new Color( 0.7f, 0.7f, 0.7f ), new Color( 0.925f, 0.925f, 0.925f  ), new Color( 0.6f, 0.6f, 0.6f ), new Color( 0.875f, 0.875f, 0.875f ) ) );
	public static final InheritedAttributeNonNull buttonAttrs = new InheritedAttributeNonNull( controlsNamespace, "buttonAttrs", StyleSheet.class,
			    StyleSheet.style( Primitive.foreground.as( new Color( 0.1f, 0.225f, 0.35f ) ), Primitive.fontSize.as( 12 ) ) );


	public static final InheritedAttributeNonNull toggleButtonInactiveBorder = new InheritedAttributeNonNull( controlsNamespace, "toggleButtonInactiveBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, new Color( 0.65f, 0.65f, 0.65f ), new Color( 0.95f, 0.95f, 0.95f  ), new Color( 0.65f, 0.65f, 0.65f ), new Color( 0.9f, 0.9f, 0.9f ) ) );
	public static final InheritedAttributeNonNull toggleButtonActiveBorder = new InheritedAttributeNonNull( controlsNamespace, "toggleButtonActiveBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, new Color( 0.6f, 0.6f, 0.6f ), new Color( 0.825f, 0.825f, 0.825f  ), new Color( 0.6f, 0.6f, 0.6f ), new Color( 0.775f, 0.775f, 0.775f ) ) );


//	public static final InheritedAttributeNonNull hyperlinkAttrs = new InheritedAttributeNonNull( controlsNamespace, "hyperlinkAttrs", StyleSheet.class,
//		    StyleSheet.style( Primitive.editable.as( false ), Primitive.foreground.as( new Color( 222, 92, 66 ) ), Primitive.hoverForeground.as( Color.red ), Primitive.cursor.as( new Cursor( Cursor.HAND_CURSOR ) ) ) );

	public static final InheritedAttributeNonNull hyperlinkAttrs = new InheritedAttributeNonNull( controlsNamespace, "hyperlinkAttrs", StyleSheet.class,
			    StyleSheet.style( Primitive.editable.as( false ), Primitive.foreground.as( new Color( 0, 111, 128 ) ), Primitive.hoverForeground.as( new Color( 0.0f, 0.5f, 0.0f ) ), Primitive.cursor.as( new Cursor( Cursor.HAND_CURSOR ) ) ) );

	public static final InheritedAttributeNonNull checkboxHoverBackground = new InheritedAttributeNonNull( controlsNamespace, "checkboxHoverBackground", Painter.class,
			new FilledOutlinePainter( new Color( 0.95f, 0.95f, 0.95f  ), new Color( 0.8f, 0.8f, 0.8f  ) ) );
	public static final InheritedAttributeNonNull checkboxCheckBorder = new InheritedAttributeNonNull( controlsNamespace, "checkboxCheckBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, new Color( 0.7f, 0.7f, 0.7f ), new Color( 0.925f, 0.925f, 0.925f  ) ) );
	public static final InheritedAttributeNonNull checkboxCheckForeground = new InheritedAttributeNonNull( controlsNamespace, "checkboxCheckForeground", Paint.class, new Color( 0.4f, 0.4f, 0.4f ) );
	public static final InheritedAttributeNonNull checkboxCheckSize = new InheritedAttributeNonNull( controlsNamespace, "checkboxCheckSize", Double.class, 8.0 );
	public static final InheritedAttributeNonNull checkboxSpacing = new InheritedAttributeNonNull( controlsNamespace, "checkboxSpacing", Double.class, 5.0 );
	public static final InheritedAttributeNonNull checkboxPadding = new InheritedAttributeNonNull( controlsNamespace, "checkboxPadding", Double.class, 1.0 );

	
	public static final InheritedAttributeNonNull optionMenuBorder = new InheritedAttributeNonNull( controlsNamespace, "optionMenuBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 2.0, new Color( 0.7f, 0.7f, 0.7f ), new Color( 0.925f, 0.925f, 0.925f  ), new Color( 0.6f, 0.6f, 0.6f ), new Color( 0.875f, 0.875f, 0.875f ) ) );
	public static final InheritedAttributeNonNull optionMenuContentsSpacing = new InheritedAttributeNonNull( controlsNamespace, "optionMenuContentsSpacing", Double.class, 5.0 );
	public static final InheritedAttributeNonNull optionMenuArrowSize = new InheritedAttributeNonNull( controlsNamespace, "optionMenuArrowSize", Double.class, 14.0 );
	public static final InheritedAttributeNonNull optionMenuArrowPainter = new InheritedAttributeNonNull( controlsNamespace, "optionMenuArrowPainter", Painter.class,
			 new FilledOutlinePainter( new Color( 0.775f, 0.775f, 0.85f ), new Color( 0.525f, 0.525f, 0.6f ), new BasicStroke( 1.0f ) ) );

	
	public static final InheritedAttributeNonNull dropDownExpanderHeaderContentsSpacing = new InheritedAttributeNonNull( controlsNamespace, "dropDownExpanderHeaderContentsSpacing", Double.class, 5.0 );
	public static final InheritedAttributeNonNull dropDownExpanderHeaderArrowSize = new InheritedAttributeNonNull( controlsNamespace, "dropDownExpanderHeaderArrowSize", Double.class, 12.0 );
	public static final InheritedAttributeNonNull dropDownExpanderHeaderArrowPainter = new InheritedAttributeNonNull( controlsNamespace, "dropDownExpanderHeaderArrowPainter", Painter.class,
			new FillPainter( new Color( 0, 111, 128 ) ) );
	public static final InheritedAttributeNonNull dropDownExpanderHeaderBorder = new InheritedAttributeNonNull( controlsNamespace, "dropDownExpanderHeaderBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 2.0, 4.0, 4.0, new Color( 0.75f, 0.75f, 0.75f ), new Color( 0.95f, 0.95f, 0.95f ), new Color( 0.7f, 0.7f, 0.7f ), new Color( 0.9f, 0.9f, 0.9f ) ) );
	public static final InheritedAttributeNonNull dropDownExpanderPadding = new InheritedAttributeNonNull( controlsNamespace, "dropDownExpanderPadding", Double.class, 21.0 );
	
	
	public static final InheritedAttributeNonNull tabbedBoxRounding = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxRounding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull tabbedBoxContentsPadding = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxContentsPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull tabbedBoxTabPadding = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxTabPadding", Double.class, 6.0 );
	public static final InheritedAttributeNonNull tabbedBoxHeaderSpacing = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxHeaderSpacing", Double.class, 5.0 );
	public static final InheritedAttributeNonNull tabbedBoxHeaderTabRounding = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxHeaderTabRounding", Double.class, 3.0 );
	public static final InheritedAttributeNonNull tabbedBoxHeaderFillPaint = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxHeaderFillPaint", Paint.class,
			new Color( 0.9f, 0.9f, 0.9f ) );
	public static final InheritedAttributeNonNull tabbedBoxHeaderOutlinePaint = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxHeaderOutlinePaint", Paint.class,
			new Color( 0.6f, 0.6f, 0.6f ) );
	public static final InheritedAttributeNonNull tabbedBoxHeaderInactiveTabFillPaint = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxHeaderInactiveTabFillPaint", Paint.class,
			new Color( 0.95f, 0.95f, 0.95f ) );
	public static final InheritedAttributeNonNull tabbedBoxHeaderInactiveTabOutlinePaint = new InheritedAttributeNonNull( controlsNamespace, "tabbedBoxHeaderInactiveTabOutlinePaint", Paint.class,
			new Color( 0.7f, 0.7f, 0.7f ) );

	
	public static final InheritedAttributeNonNull textEntryBorder = new InheritedAttributeNonNull( controlsNamespace, "textEntryBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.4f, 0.4f, 0.5f ), new Color( 0.9f, 0.9f, 0.95f  ) ) );
	public static final InheritedAttributeNonNull textEntryInvalidBorder = new InheritedAttributeNonNull( controlsNamespace, "textEntryInvalidBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.6f, 0.0f, 0.0f ), new Color( 1.0f, 0.85f, 0.85f ) ) );
	public static final InheritedAttribute textEntryChangedBorder = new InheritedAttribute( controlsNamespace, "textEntryChangedBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.5f, 0.4f, 0.5f ), new Color( 0.95f, 0.9f, 0.95f  ) ) );
	public static final InheritedAttribute textEntryWordWrap = new InheritedAttribute( controlsNamespace, "textEntryWordWrap", Boolean.class, false );

	
	public static final InheritedAttributeNonNull textAreaAttrs = new InheritedAttributeNonNull( controlsNamespace, "textAreaAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.border.as( new SolidBorder( 2.0, 5.0, 3.0, 3.0, new Color( 0.3f, 0.3f, 0.3f ), null ) ) ) );

	
	public static final InheritedAttributeNonNull editableLabelHoverAttrs = new InheritedAttributeNonNull( controlsNamespace, "editableLabelHoverAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.hoverBackground.as( new FilledOutlinePainter( new Color( 0.9f, 0.9f, 0.9f ), new Color( 0.7f, 0.7f, 0.7f ), new BasicStroke( 1.0f ) ) ) ) );
	public static final InheritedAttributeNonNull editableLabelTextAttrs = new InheritedAttributeNonNull( controlsNamespace, "editableLabelTextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.3f, 0.5f ) ), Primitive.cursor.as( new Cursor( Cursor.TEXT_CURSOR ) )  ) );
	
	
	public static final InheritedAttributeNonNull numericLabelTextAttrs = new InheritedAttributeNonNull( controlsNamespace, "numericLabelTextAttrs", StyleSheet.class,
			    StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.3f, 0.5f ) ), Primitive.cursor.as( new Cursor( Cursor.E_RESIZE_CURSOR ) )  ) );
		
		
	public static final InheritedAttributeNonNull spinEntryArrowSize = new InheritedAttributeNonNull( controlsNamespace, "spinEntryArrowSize", Double.class, 14.0 );
	public static final InheritedAttributeNonNull spinEntryArrowAttrs = new InheritedAttributeNonNull( controlsNamespace, "spinEntryArrowAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.columnSpacing.as( 2.0 ),
				    Primitive.shapePainter.as( new FilledOutlinePainter( new Color( 0.85f, 0.85f, 0.85f ), new Color( 0.6f, 0.6f, 0.6f ), new BasicStroke( 1.0f ) ) ),
				    Primitive.hoverShapePainter.as( new FillPainter( new Color( 0, 111, 128 ) ) ) ) );
	public static final InheritedAttributeNonNull spinEntryHSpacing = new InheritedAttributeNonNull( controlsNamespace, "spinEntryHSpacing", Double.class, 2.0 );
	
	
	/*public static final InheritedAttributeNonNull scrollBarDragBoxPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragBoxPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.7f, 0.8f, 0.9f ), new Color( 0.0f, 0.25f, 0.5f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttributeNonNull scrollBarDragBarBackgroundPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragBarBackgroundPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.9f, 0.9f, 0.9f ), new Color( 0.75f, 0.75f, 0.75f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttribute scrollBarDragBackgroundHoverPainter = new InheritedAttribute( controlsNamespace, "scrollBarDragBackgroundHoverPainter", Painter.class,
			new FilledOutlinePainter( new Color( 1.0f, 1.0f, 1.0f ), new Color( 0.5f, 0.5f, 0.5f ), new BasicStroke( 1.0f ) ) );*/
	public static final InheritedAttributeNonNull scrollBarDragBoxPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragBoxPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.9f, 0.9f, 0.9f ), new Color( 0.8f, 0.8f, 0.8f ), new BasicStroke( 1.0f ) ) );
	/*public static final InheritedAttributeNonNull scrollBarDragBoxHoverPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragBoxHoverPainter", Painter.class,
			new FillPainter( new Color( 201, 45, 7 ) ) );*/
	public static final InheritedAttributeNonNull scrollBarDragBoxHoverPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragBoxHoverPainter", Painter.class,
			new FillPainter( new Color( 0, 111, 128 ) ) );
	public static final InheritedAttributeNonNull scrollBarDragBarBackgroundPainter = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragBarBackgroundPainter", Painter.class,
			new FillPainter( Color.WHITE ) );
	public static final InheritedAttribute scrollBarDragBackgroundHoverPainter = new InheritedAttribute( controlsNamespace, "scrollBarDragBackgroundHoverPainter", Painter.class,
			new FilledOutlinePainter( Color.WHITE, new Color( 0.75f, 0.75f, 0.75f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttributeNonNull scrollBarDragboxPadding = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragboxPadding", Double.class, 2.0 );
	public static final InheritedAttributeNonNull scrollBarDragboxRounding = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragboxRounding", Double.class, 4.0 );
	public static final InheritedAttributeNonNull scrollBarDragboxMinSize = new InheritedAttributeNonNull( controlsNamespace, "scrollBarDragboxMinSize", Double.class, 9.0 );
	public static final InheritedAttributeNonNull scrollBarSize = new InheritedAttributeNonNull( controlsNamespace, "scrollBarSize", Double.class, 16.0 );
	
	
	public static final InheritedAttribute sliderBackgroundPainter = new InheritedAttribute( controlsNamespace, "sliderBackgroundPainter", Painter.class,
			new OutlinePainter( new Color( 0.6f, 0.6f, 0.6f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttribute sliderBackgroundHoverPainter = new InheritedAttribute( controlsNamespace, "sliderBackgroundHoverPainter", Painter.class,
			new OutlinePainter( new Color( 0.5f, 0.5f, 0.5f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttributeNonNull sliderPivotPaint = new InheritedAttributeNonNull( controlsNamespace, "sliderPivotPaint", Paint.class, new Color( 255, 143, 0 ) );
	public static final InheritedAttributeNonNull sliderValueBoxPainter = new InheritedAttributeNonNull( controlsNamespace, "sliderValueBoxPainter", Painter.class,
			new FillPainter( new Color( 255, 217, 128) ) );
	public static final InheritedAttributeNonNull sliderValuePainter = new InheritedAttributeNonNull( controlsNamespace, "sliderValuePainter", Painter.class,
            new FilledOutlinePainter(new Color(255, 194, 51),
                    new Color(179, 152, 89)));
	public static final InheritedAttributeNonNull sliderValueHighlightPainter = new InheritedAttributeNonNull( controlsNamespace, "sliderValueHighlightPainter", Painter.class,
            new FilledOutlinePainter(new Color(255, 153, 51),
                    new Color(179, 134, 89)));
	public static final InheritedAttributeNonNull sliderRounding = new InheritedAttributeNonNull( controlsNamespace, "sliderDragboxRounding", Double.class, 6.0 );
	public static final InheritedAttributeNonNull sliderSize = new InheritedAttributeNonNull( controlsNamespace, "sliderSize", Double.class, 16.0 );
	
	
	public static final InheritedAttributeNonNull objectDropBoxClassNameStyle = new InheritedAttributeNonNull( controlsNamespace, "objectDropBoxClassNameStyle", StyleSheet.class,
			    StyleSheet.style( Primitive.fontItalic.as( true ) ) );

	
	public static final InheritedAttributeNonNull presentationButtonSpacing = new InheritedAttributeNonNull( controlsNamespace, "presentationButtonSpacing", Double.class, 2.0 );

	
	public static final InheritedAttributeNonNull bClosePopupOnActivate = new InheritedAttributeNonNull( controlsNamespace, "bClosePopupOnActivate", Boolean.class, false );
	
	public static final InheritedAttributeNonNull menuItemHoverBackground = new InheritedAttributeNonNull( controlsNamespace, "menuItemHoverBackground", Painter.class,
			new FillPainter( new Color( 0.775f, 0.8f, 0.825f ) ) );
	public static final InheritedAttributeNonNull menuItemXPadding = new InheritedAttributeNonNull( controlsNamespace, "menuItemXPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull menuItemYPadding = new InheritedAttributeNonNull( controlsNamespace, "menuItemYPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull menuItemSubmenuArrowSize = new InheritedAttributeNonNull( controlsNamespace, "menuItemSubmenuArrowSize", Double.class, 12.0 );
	public static final InheritedAttributeNonNull menuItemSubmenuArrowSpacing = new InheritedAttributeNonNull( controlsNamespace, "menuItemSubmenuArrowSpacing", Double.class, 5.0 );
	public static final InheritedAttributeNonNull menuItemSubmenuArrowPainter = new InheritedAttributeNonNull( controlsNamespace, "menuItemSubmenuArrowPainter", Painter.class,
			 new FilledOutlinePainter( new Color( 0.85f, 0.85f, 0.85f ), new Color( 0.6f, 0.6f, 0.6f ), new BasicStroke( 1.0f ) ) );
	
	public static final InheritedAttributeNonNull popupMenuAttrs = new InheritedAttributeNonNull( controlsNamespace, "popupMenuAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.border.as( new SolidBorder( 1.5, 2.0, new Color( 0.6f, 0.6f, 0.6f ), Color.white ) ), Primitive.rowSpacing.as( 10.0 ) ) );

	
	public static final InheritedAttributeNonNull tooltipBorder = new InheritedAttributeNonNull( controlsNamespace, "tooltipBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 2.0, 2.0, 2.0, Color.BLACK, new Color( 1.0f, 1.0f, 0.9f ) ) );



	public static final InheritedAttributeNonNull resizeableArrowPainter = new InheritedAttributeNonNull( controlsNamespace, "resizeableArrowPainter", Painter.class,
			new FillPainter( new Color( 0.25f, 0.3f, 0.35f ) ) );
	public static final InheritedAttributeNonNull resizeableArrowSize = new InheritedAttributeNonNull( controlsNamespace, "resizeableArrowSize", Double.class, 8.0 );
	public static final InheritedAttributeNonNull resizeableArrowSpacing = new InheritedAttributeNonNull( controlsNamespace, "resizeableArrowSpacing", Double.class, 9.0 );
	public static final InheritedAttributeNonNull resizeableArrowPadding = new InheritedAttributeNonNull( controlsNamespace, "resizeableArrowPadding", Double.class, 2.0 );
	public static final InheritedAttributeNonNull resizeableDragBarEdgeThickness = new InheritedAttributeNonNull( controlsNamespace, "resizeableDragBarEdgeThickness", Double.class, 2.0 );
	public static final InheritedAttributeNonNull resizeableDragBarBodyStyle = new InheritedAttributeNonNull( controlsNamespace, "resizeableDragBarBodyStyle", StyleSheet.class,
			StyleSheet.style( Primitive.background.as( new FillPainter( new Color( 0.80f,0.825f, 0.85f ) ) ),
					Primitive.hoverBackground.as( new FillPainter( new Color( 0.7f, 0.725f, 0.75f ) ) ),
					Primitive.shapePainter.as( new FillPainter( new Color( 0.25f, 0.3f, 0.35f ) ) ) ) );
	public static final InheritedAttributeNonNull resizeableDragBarPadding = new InheritedAttributeNonNull( controlsNamespace, "resizeableDragBarPadding", Double.class, 2.0 );


	public static final InheritedAttributeNonNull switchButtonBorder = new InheritedAttributeNonNull( controlsNamespace, "switchButtonBorder", AbstractBorder.class,
			new SolidBorder( 1.0, 0.0, 5.0, 5.0, new Color( 0.65f, 0.65f, 0.65f ), new Color( 0.95f, 0.95f, 0.975f  ) ) );
	public static final InheritedAttributeNonNull switchButtonInset = new InheritedAttributeNonNull( controlsNamespace, "switchButtonInset", Double.class, 4.0 );
	public static final InheritedAttributeNonNull switchButtonSpacing = new InheritedAttributeNonNull( controlsNamespace, "switchButtonSpacing", Double.class, 10.0 );
	public static final InheritedAttribute switchButtonSeparatorPaint = new InheritedAttribute( controlsNamespace, "switchButtonSeparatorPaint", Paint.class, new Color( 0.5f, 0.5f, 0.5f, 0.4f ) );
	public static final InheritedAttributeNonNull switchButtonSeparatorThickness = new InheritedAttributeNonNull( controlsNamespace, "switchButtonSeparatorThickness", Float.class, 1.0f );
	public static final InheritedAttributeNonNull switchButtonBackgroundSelected = new InheritedAttributeNonNull( controlsNamespace, "switchButtonBackgroundSelected", Paint.class, new Color( 0.825f, 0.825f, 0.85f  ) );
	public static final InheritedAttributeNonNull switchButtonBackgroundHover = new InheritedAttributeNonNull( controlsNamespace, "switchButtonBackgroundHover", Paint.class, new Color( 0.9f, 0.9f, 0.925f  ) );
	public static final InheritedAttributeNonNull switchButtonInternalStyle = new InheritedAttributeNonNull( controlsNamespace, "switchButtonInternalStyle", StyleSheet.class,
			StyleSheet.style( Primitive.foreground.as( new Color( 0.1f, 0.225f, 0.35f ) ), Primitive.fontSize.as( 11 ) ) );


	
	
	
	public static StyleValues useButtonAttrs(StyleValues style)
	{
		return style.useAttr( buttonBorder ).useAttr( buttonAttrs );
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
			return StyleSheet.style( Primitive.hoverBackground.as( background ), Primitive.rowSpacing.as( spacing ) );
		}
	};

	public static StyleValues useCheckboxAttrs(StyleValues style)
	{
		return style.useAttr( checkboxHoverBackground ).useAttr( checkboxCheckBorder ).useAttr( checkboxCheckForeground ).useAttr( checkboxCheckSize ).useAttr( checkboxSpacing ).useAttr( checkboxPadding );
	}
	
	

	public static StyleValues useOptionMenuAttrs(StyleValues style)
	{
		return style.useAttr( optionMenuBorder ).useAttr( optionMenuContentsSpacing ).useAttr( optionMenuArrowSize ).useAttr( optionMenuArrowPainter );
	}
	
	
	
	public static StyleValues useDropDownExpanderAttrs(StyleValues style)
	{
		return style.useAttr( dropDownExpanderHeaderContentsSpacing ).useAttr( dropDownExpanderHeaderArrowSize ).useAttr( dropDownExpanderHeaderArrowPainter ).useAttr( dropDownExpanderPadding );
	}
	
	
	
	public static StyleValues useTabsAttrs(StyleValues style)
	{
		return style;
	}
	
	
	
	public static StyleValues useTextEntryAttrs(StyleValues style)
	{
		return style.useAttr( textEntryBorder ).useAttr( textEntryInvalidBorder );
	}
	
	
	
	public static StyleValues useEditableLabelAttrs(StyleValues style)
	{
		return style.useAttr( editableLabelHoverAttrs ).useAttr( editableLabelTextAttrs );
	}
	
	
	
	protected static DerivedValueTable<StyleSheet> scrollBarDragBoxStyle = new DerivedValueTable<StyleSheet>( controlsNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			Painter backgroundPainter = attribs.get( scrollBarDragBarBackgroundPainter, Painter.class );
			Painter backgroundHoverPainter = attribs.get( scrollBarDragBackgroundHoverPainter, Painter.class );
			return StyleSheet.style( Primitive.shapePainter.as( backgroundPainter ), Primitive.hoverShapePainter.as( backgroundHoverPainter ) );
		}
	};


	
	public static StyleValues usePresentationButtonAttrs(StyleValues style)
	{
		return style.useAttr( presentationButtonSpacing );
	}
	

	
	
	public static StyleValues useMenuItemAttrs(StyleValues style)
	{
		return style.useAttr( menuItemHoverBackground ).useAttr( menuItemXPadding ).useAttr( menuItemYPadding ).useAttr( menuItemSubmenuArrowSpacing )
				.useAttr( menuItemSubmenuArrowPainter ).useAttr( menuItemSubmenuArrowSize );
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




	protected static DerivedValueTable<StyleSheet> resizeableBinArrowStyle = new DerivedValueTable<StyleSheet>( controlsNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			double spacing = attribs.get( resizeableArrowSpacing, Double.class );
			return StyleSheet.style( Primitive.shapePainter.as( attribs.get( resizeableArrowPainter, Painter.class ) ), Primitive.rowSpacing.as( spacing ), Primitive.columnSpacing.as( spacing ) );
		}
	};



	public static StyleValues useResizeableBinAttrs(StyleValues style)
	{
		return style.useAttr( resizeableArrowPainter ).useAttr( resizeableArrowSize ).useAttr( resizeableArrowSpacing ).useAttr( resizeableArrowPadding )
				.useAttr( resizeableDragBarEdgeThickness ).useAttr( resizeableDragBarBodyStyle ).useAttr( resizeableDragBarPadding );
	}




	public static StyleValues useSwitchButtonAttrs(StyleValues style)
	{
		return style.useAttr( switchButtonBorder )
				.useAttr( switchButtonInset ).useAttr( switchButtonSpacing ).useAttr( switchButtonSeparatorPaint ).useAttr( switchButtonSeparatorThickness )
				.useAttr( switchButtonBackgroundSelected ).useAttr( switchButtonBackgroundHover )
				.useAttr( switchButtonInternalStyle );
	}
}
