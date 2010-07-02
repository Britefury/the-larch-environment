//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import BritefuryJ.AttributeTable.AttributeValues;
import BritefuryJ.DocPresent.DPAbstractBox;
import BritefuryJ.DocPresent.DPAspectRatioBin;
import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPBox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPShape;
import BritefuryJ.DocPresent.DPSpaceBin;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.Painter.OutlinePainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.Util.Range;
import BritefuryJ.Math.Point2;

public class ControlsStyleSheet extends StyleSheet
{
	private static final AttributeValues defaultLinkAttrs = new AttributeValues( new String[] { "editable", "fontFace", "fontSize", "foreground", "hoverForeground", "cursor" },
			new Object[] { false, "Sans serif", 14, Color.blue, Color.red, new Cursor( Cursor.HAND_CURSOR ) } );
	
	private static final double defaultButtonBorderThickness = 1.0;
	private static final double defaultButtonMargin = 3.0;
	private static final double defaultButtonRounding = 10.0;
	private static final Paint defaultButtonBorderPaint = new Color( 0.55f, 0.525f, 0.5f );
	private static final Paint defaultButtonBorderHighlightPaint = new Color( 0.0f, 0.5f, 0.5f );
	private static final Paint defaultButtonBackgPaint = new Color( 0.85f, 0.85f, 0.85f );
	private static final Paint defaultButtonBackgHighlightPaint = new Color( 0.925f, 0.925f, 0.925f );
	
	private static final Painter defaultMenuItemHoverBackground = new FillPainter( new Color( 0.6f, 0.7f, 0.85f ) );
	private static final double defaultMenuItemXPadding = 5.0;
	private static final double defaultMenuItemYPadding = 5.0;
	private static final AttributeValues defaultPopupMenuAttrs = new AttributeValues( new String[] { "border", "hboxSpacing" }, new Object[] { new SolidBorder( 1.0, 2.0, Color.black, null ), 10.0 } );
	
	private static final Border defaultTooltipBorder = new SolidBorder( 1.0, 2.0, 2.0, 2.0, Color.BLACK, new Color( 1.0f, 1.0f, 0.9f ) );
	
	private static final Painter defaultCheckboxHoverBackground = new OutlinePainter( new Color( 0.5f, 0.625f, 0.75f ) );
	private static final Border defaultCheckboxCheckBorder = new FilledBorder( 3.0, 3.0, 3.0, 3.0, 5.0, 5.0, new Color( 0.75f, 0.75f, 0.75f ) );
	private static final Paint defaultCheckboxCheckForeground = new Color( 0.0f, 0.2f, 0.4f );
	private static final double defaultCheckboxCheckSize = 10.0;
	private static final double defaultCheckboxSpacing = 8.0;
	
	private static final AttributeValues defaultTextAreaAttrs = new AttributeValues( new String[] { "border" }, new Object[] { new SolidBorder( 2.0, 5.0, 3.0, 3.0, new Color( 0.3f, 0.3f, 0.3f ), null ) } );
	
	private static final AttributeValues defaultTextEntryTextAttrs = new AttributeValues();
	private static final Border defaultTextEntryBorder = new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.0f, 0.3f, 0.0f ), new Color( 0.9f, 0.95f, 0.9f ) );
	private static final Border defaultTextEntryInvalidBorder = new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.6f, 0.0f, 0.0f ), new Color( 1.0f, 0.85f, 0.85f ) );
	
	private static final double defaultSpinEntryArrowSize = 16.0;
	private static final double defaultSpinEntryArrowFilletSize = 4.0;
	private static final AttributeValues defaultSpinEntryArrowAttrs = new AttributeValues( new String[] { "vboxSpacing", "shapePainter", "hoverShapePainter" },
			new Object[] { 2.0,
				new FilledOutlinePainter( new Color( 0.7f, 0.85f, 0.7f ), new Color( 0.0f, 0.25f, 0.0f ), new BasicStroke( 1.0f ) ),
				new FilledOutlinePainter( new Color( 0.85f, 1.0f, 0.85f ), new Color( 0.0f, 0.45f, 0.0f ), new BasicStroke( 1.0f ) ) } );
	private static final double defaultSpinEntryHSpacing = 2.0;
	
	private static final Border defaultOptionMenuBorder = new SolidBorder( 1.0, 3.0, 5.0, 5.0, Color.BLACK, new Color( 0.9f, 0.95f, 0.9f ) );
	private static final Border defaultOptionMenuHoverBorder = new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.0f, 0.3f, 0.0f ), new Color( 0.95f, 1.0f, 0.95f ) );
	private static final double defaultOptionMenuContentsSpacing = 5.0;
	private static final double defaultOptionMenuArrowSize = 16.0;
	private static final double defaultOptionMenuArrowFilletSize = 4.0;
	private static final Painter defaultOptionMenuArrowPainter = new FilledOutlinePainter( new Color( 0.7f, 0.85f, 0.7f ), new Color( 0.0f, 0.25f, 0.0f ), new BasicStroke( 1.0f ) );
	
	private static final Painter defaultScrollBarArrowPainter = new FilledOutlinePainter( new Color( 0.7f, 0.85f, 1.0f ), new Color( 0.0f, 0.5f, 1.0f ), new BasicStroke( 1.0f ) );
	private static final Painter defaultScrollBarDragBackgroundPainter = new FilledOutlinePainter( new Color( 0.9f, 0.9f, 0.9f ), new Color( 0.75f, 0.75f, 0.75f ), new BasicStroke( 1.0f ) );
	
	private static final double defaultScrollBarArrowPadding = 0.0;
	private static final double defaultScrollBarArrowSpacing = 2.0;
	private static final double defaultScrollBarArrowFilletSize = 4.0;
	private static final double defaultScrollBarDragBoxPadding = 3.0;
	private static final double defaultScrollBarDragBoxRounding = 4.0;
	private static final double defaultScrollBarSize = 20.0;
	
	
	public static final ControlsStyleSheet instance = new ControlsStyleSheet();

	
	
	
	
	public ControlsStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyle", PrimitiveStyleSheet.instance );
		
		initAttr( "closePopupOnActivateFlag", false );

		initAttr( "linkAttrs", defaultLinkAttrs );
		
		initAttr( "buttonBorderThickness", defaultButtonBorderThickness );
		initAttr( "buttonMargin", defaultButtonMargin );
		initAttr( "buttonRounding", defaultButtonRounding );
		initAttr( "buttonBorderPaint", defaultButtonBorderPaint );
		initAttr( "buttonBorderHighlightPaint", defaultButtonBorderHighlightPaint );
		initAttr( "buttonBackgPaint", defaultButtonBackgPaint );
		initAttr( "buttonBackgHighlightPaint", defaultButtonBackgHighlightPaint );
		
		initAttr( "menuItemHoverBackground", defaultMenuItemHoverBackground );
		initAttr( "menuItemXPadding", defaultMenuItemXPadding );
		initAttr( "menuItemYPadding", defaultMenuItemYPadding );
		
		initAttr( "popupMenuAttrs", defaultPopupMenuAttrs );
		
		initAttr( "tooltipBorder", defaultTooltipBorder );
		
		initAttr( "checkboxHoverBackground", defaultCheckboxHoverBackground );
		initAttr( "checkboxCheckBorder", defaultCheckboxCheckBorder );
		initAttr( "checkboxCheckForeground", defaultCheckboxCheckForeground );
		initAttr( "checkboxCheckSize", defaultCheckboxCheckSize );
		initAttr( "checkboxSpacing", defaultCheckboxSpacing );
		
		initAttr( "textAreaAttrs", defaultTextAreaAttrs );
		
		initAttr( "textEntryTextAttrs", defaultTextEntryTextAttrs );
		initAttr( "textEntryBorder", defaultTextEntryBorder );
		initAttr( "textEntryInvalidBorder", defaultTextEntryInvalidBorder );
		
		initAttr( "spinEntryArrowSize", defaultSpinEntryArrowSize );
		initAttr( "spinEntryArrowFilletSize", defaultSpinEntryArrowFilletSize );
		initAttr( "spinEntryArrowAttrs", defaultSpinEntryArrowAttrs );
		initAttr( "spinEntryHSpacing", defaultSpinEntryHSpacing );

		initAttr( "optionMenuBorder", defaultOptionMenuBorder );
		initAttr( "optionMenuHoverBorder", defaultOptionMenuHoverBorder );
		initAttr( "optionMenuContentsSpacing", defaultOptionMenuContentsSpacing );
		initAttr( "optionMenuArrowSize", defaultOptionMenuArrowSize );
		initAttr( "optionMenuArrowFilletSize", defaultOptionMenuArrowFilletSize );
		initAttr( "optionMenuArrowPainter", defaultOptionMenuArrowPainter );
		
		initAttr( "scrollBarArrowPainter", defaultScrollBarArrowPainter );
		initAttr( "scrollBarArrowHoverPainter", new FilledOutlinePainter( new Color( 0.8f, 0.9f, 1.0f ), new Color( 0.5f, 0.75f, 1.0f ), new BasicStroke( 1.0f ) ) );
		initAttr( "scrollBarArrowPadding", defaultScrollBarArrowPadding );
		initAttr( "scrollBarArrowSpacing", defaultScrollBarArrowSpacing );
		initAttr( "scrollBarArrowFilletSize", defaultScrollBarArrowFilletSize );
		initAttr( "scrollBarDragBackgroundPainter", defaultScrollBarDragBackgroundPainter );
		initAttr( "scrollBarDragBackgroundHoverPainter", new FilledOutlinePainter( new Color( 0.85f, 0.85f, 0.85f ), new Color( 0.5f, 0.5f, 0.5f ), new BasicStroke( 1.0f ) ) );
		initAttr( "scrollBarDragBoxPainter", defaultScrollBarArrowPainter );
		initAttr( "scrollBarDragBoxPadding", defaultScrollBarDragBoxPadding );
		initAttr( "scrollBarDragBoxRounding", defaultScrollBarDragBoxRounding );
		initAttr( "scrollBarSize", defaultScrollBarSize );
	}

	
	protected StyleSheet newInstance()
	{
		return new ControlsStyleSheet();
	}
	

	
	public ControlsStyleSheet withPrimitiveStyleSheet(PrimitiveStyleSheet styleSheet)
	{
		return (ControlsStyleSheet)withAttr( "primitiveStyle", styleSheet );
	}
	
	
	public ControlsStyleSheet withClosePopupOnActivateFlag(boolean bClosePopup)
	{
		return (ControlsStyleSheet)withAttr( "closePopupOnActivateFlag", bClosePopup );
	}
	
	public ControlsStyleSheet withClosePopupOnActivate()
	{
		return withClosePopupOnActivateFlag( true );
	}
	
	public ControlsStyleSheet withLeavePopupOnActivate()
	{
		return withClosePopupOnActivateFlag( false );
	}
	
	
	public ControlsStyleSheet withLinkAttrs(AttributeValues attrs)
	{
		return (ControlsStyleSheet)withAttr( "linkAttrs", attrs );
	}
	
	
	public ControlsStyleSheet withButtonBorderThickness(double thickness)
	{
		return (ControlsStyleSheet)withAttr( "buttonBorderThickness", thickness );
	}
	
	public ControlsStyleSheet withButtonBorderMargin(double margin)
	{
		return (ControlsStyleSheet)withAttr( "buttonMargin", margin );
	}
	
	public ControlsStyleSheet withButtonRounding(double rounding)
	{
		return (ControlsStyleSheet)withAttr( "buttonRounding", rounding );
	}
	
	public ControlsStyleSheet withButtonBorderPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "buttonBorderPaint", paint );
	}
	
	public ControlsStyleSheet withButtonBorderHighlightPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "buttonBorderHighlightPaint", paint );
	}
	
	public ControlsStyleSheet withButtonBackgPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "buttonBackgPaint", paint );
	}
	
	public ControlsStyleSheet withButtonBackgHighlightPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "buttonBackgHighlightPaint", paint );
	}
	
	
	public ControlsStyleSheet withMenuItemHoverBackground(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "menuItemHoverBackground", painter );
	}
	
	public ControlsStyleSheet withMenuItemXPadding(double xPadding)
	{
		return (ControlsStyleSheet)withAttr( "menuItemXPadding", xPadding );
	}
	
	public ControlsStyleSheet withMenuItemYPadding(double yPadding)
	{
		return (ControlsStyleSheet)withAttr( "menuItemYPadding", yPadding );
	}
	
	
	public ControlsStyleSheet withPopupMenuAttrs(AttributeValues attrs)
	{
		return (ControlsStyleSheet)withAttr( "popupMenuAttrs", attrs );
	}
	
	
	public ControlsStyleSheet withTooltipBorder(Border border)
	{
		return (ControlsStyleSheet)withAttr( "tooltipBorder", border );
	}
	
	
	public ControlsStyleSheet withCheckboxHoverBackground(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "checkboxHoverBackground", painter );
	}
	
	public ControlsStyleSheet withCheckboxCheckBorder(Border border)
	{
		return (ControlsStyleSheet)withAttr( "checkboxCheckBorder", border );
	}
	
	public ControlsStyleSheet withCheckboxCheckForeground(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "checkboxCheckForeground", paint );
	}
	
	public ControlsStyleSheet withCheckboxCheckSize(double size)
	{
		return (ControlsStyleSheet)withAttr( "checkboxCheckSize", size );
	}
	
	public ControlsStyleSheet withCheckboxSpacing(double spacing)
	{
		return (ControlsStyleSheet)withAttr( "checkboxSpacing", spacing );
	}
	
	
	
	public ControlsStyleSheet withTextAreaAttrs(AttributeValues attrs)
	{
		return (ControlsStyleSheet)withAttr( "textAreaAttrs", attrs );
	}
	

	
	public ControlsStyleSheet withTextEntryTextAttrs(AttributeValues attrs)
	{
		return (ControlsStyleSheet)withAttr( "textEntryTextAttrs", attrs );
	}
	
	public ControlsStyleSheet withTextEntryBorderThickness(double thickness)
	{
		return (ControlsStyleSheet)withAttr( "textEntryBorderThickness", thickness );
	}
	
	public ControlsStyleSheet withTextEntryBorderMargin(double margin)
	{
		return (ControlsStyleSheet)withAttr( "textEntryMargin", margin );
	}
	
	public ControlsStyleSheet withTextEntryRounding(double rounding)
	{
		return (ControlsStyleSheet)withAttr( "textEntryRounding", rounding );
	}
	
	public ControlsStyleSheet withTextEntryBorderPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "textEntryBorderPaint", paint );
	}
	
	public ControlsStyleSheet withTextEntryBackgPaint(Paint paint)
	{
		return (ControlsStyleSheet)withAttr( "textEntryBackgPaint", paint );
	}
	
	
	
	public ControlsStyleSheet withSpinEntryArrowSize(double size)
	{
		return (ControlsStyleSheet)withAttr( "spinEntryArrowSize", size );
	}
	
	public ControlsStyleSheet withSpinEntryArrowFilletSize(double size)
	{
		return (ControlsStyleSheet)withAttr( "spinEntryArrowFilletSize", size );
	}
	
	public ControlsStyleSheet withSpinEntryArrowAttrs(AttributeValues attrs)
	{
		return (ControlsStyleSheet)withAttr( "spinEntryArrowAttrs", attrs );
	}
	
	public ControlsStyleSheet withSpinEntryHSpacing(double spacing)
	{
		return (ControlsStyleSheet)withAttr( "spinEntryHSpacing", spacing );
	}
	

	
	
	public ControlsStyleSheet withOptionMenuBorder(Border border)
	{
		return (ControlsStyleSheet)withAttr( "optionMenuBorder", border );
	}
	
	public ControlsStyleSheet withOptionMenuHoverBorder(Border border)
	{
		return (ControlsStyleSheet)withAttr( "optionMenuHoverBorder", border );
	}
	
	public ControlsStyleSheet withOptionMenuContentsSpacing(double spacing)
	{
		return (ControlsStyleSheet)withAttr( "optionMenuContentsSpacing", spacing );
	}
	
	public ControlsStyleSheet withOptionMenuArrowSize(double size)
	{
		return (ControlsStyleSheet)withAttr( "optionMenuArrowSize", size );
	}
	
	public ControlsStyleSheet withOptionMenuArrowFilletSize(double size)
	{
		return (ControlsStyleSheet)withAttr( "optionMenuArrowFilletSize", size );
	}
	
	public ControlsStyleSheet withOptionMenuArrowPainter(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "optionMenuArrowPainter", painter );
	}
	
	
	
	public ControlsStyleSheet withScrollBarArrowPainter(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowPainter", painter );
	}
	
	public ControlsStyleSheet withScrollBarArrowHoverPainter(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowHoverPainter", painter );
	}
	
	public ControlsStyleSheet withScrollBarArrowPadding(double padding)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowPadding", padding );
	}
	
	public ControlsStyleSheet withScrollBarArrowSpacing(double spacing)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowSpacing", spacing );
	}
	
	public ControlsStyleSheet withScrollBarArrowFilletSize(double fillet)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarArrowFilletSize", fillet );
	}
	
	public ControlsStyleSheet withScrollBarDragBackgroundPainter(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarDragBackgroundPainter", painter );
	}
	
	public ControlsStyleSheet withScrollBarDragBackgroundHoverPainter(Painter painter)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarDragBackgroundHoverPainter", painter );
	}
	
	public ControlsStyleSheet withScrollBarDragBoxPadding(double padding)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarDragBoxPadding", padding );
	}
	
	public ControlsStyleSheet withScrollBarDragBoxRounding(double rounding)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarDragBoxRounding", rounding );
	}
	
	public ControlsStyleSheet withScrollBarSize(double size)
	{
		return (ControlsStyleSheet)withAttr( "scrollBarSize", size );
	}
	
	
	
	
	private Path2D.Double[] createArrowPaths(double size, double filletSize)
	{
		double arrowRadius = size * 0.5;
		
		Point2 a = new Point2( 0.0, arrowRadius );
		Point2 b = new Point2( arrowRadius * Math.sin( Math.toRadians( 120.0 ) ), arrowRadius * Math.cos( Math.toRadians( 120.0 ) ) );
		Point2 c = new Point2( arrowRadius * Math.sin( Math.toRadians( 240.0 ) ), arrowRadius * Math.cos( Math.toRadians( 240.0 ) ) );
		
		Path2D.Double arrowShape = DPShape.filletedPath( new Point2[] { a, b, c }, true, filletSize );
		//Path2D.Double arrowShape = DPShape.path( new Point2[] { a, b, c }, true );
		
		Path2D.Double up = arrowShape;
		Path2D.Double right = (Path2D.Double)arrowShape.clone();
		Path2D.Double down = (Path2D.Double)arrowShape.clone();
		Path2D.Double left = (Path2D.Double)arrowShape.clone();
		
		right.transform( AffineTransform.getQuadrantRotateInstance( 3 ) );
		up.transform( AffineTransform.getQuadrantRotateInstance( 2 ) );
		left.transform( AffineTransform.getQuadrantRotateInstance( 1 ) );
		
		up.transform( AffineTransform.getTranslateInstance( -up.getBounds2D().getMinX(), -up.getBounds2D().getMinY() ) );
		right.transform( AffineTransform.getTranslateInstance( -right.getBounds2D().getMinX(), -right.getBounds2D().getMinY() ) );
		down.transform( AffineTransform.getTranslateInstance( -down.getBounds2D().getMinX(), -down.getBounds2D().getMinY() ) );
		left.transform( AffineTransform.getTranslateInstance( -left.getBounds2D().getMinX(), -left.getBounds2D().getMinY() ) );
			
		return new Path2D.Double[] { left, right, up, down }; 
	}
	
	

	private PrimitiveStyleSheet linkStyleSheet = null;

	private PrimitiveStyleSheet getLinkStyleSheet()
	{
		if ( linkStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues linkAttrs = getNonNull( "linkAttrs", AttributeValues.class, AttributeValues.identity );
			linkStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( linkAttrs );
		}
		return linkStyleSheet;
	}
	
	
	
	private Border buttonBorder = null;

	private Border getButtonBorder()
	{
		if ( buttonBorder == null )
		{
			double thickness = getNonNull( "buttonBorderThickness", Double.class, defaultButtonBorderThickness );
			double margin = getNonNull( "buttonMargin", Double.class, defaultButtonMargin );
			double rounding = getNonNull( "buttonRounding", Double.class, defaultButtonRounding );
			Paint borderPaint = getNonNull( "buttonBorderPaint", Paint.class, defaultButtonBorderPaint );
			Paint backgPaint = getNonNull( "buttonBackgPaint", Paint.class, defaultButtonBackgPaint );

			buttonBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return buttonBorder;
	}
	
	
	
	private Border buttonHighlightBorder = null;

	private Border getButtonHighlightBorder()
	{
		if ( buttonHighlightBorder == null )
		{
			double thickness = getNonNull( "buttonBorderThickness", Double.class, defaultButtonBorderThickness );
			double margin = getNonNull( "buttonMargin", Double.class, defaultButtonMargin );
			double rounding = getNonNull( "buttonRounding", Double.class, defaultButtonRounding );
			Paint borderPaint = getNonNull( "buttonBorderHighlightPaint", Paint.class, defaultButtonBorderHighlightPaint );
			Paint backgPaint = getNonNull( "buttonBackgHighlightPaint", Paint.class, defaultButtonBackgHighlightPaint );

			buttonHighlightBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return buttonHighlightBorder;
	}
	

	
	private PrimitiveStyleSheet buttonStyleSheet = null;

	private PrimitiveStyleSheet getButtonStyleSheet()
	{
		if ( buttonStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			buttonStyleSheet = primitive.withBorder( getButtonBorder() );
		}
		return buttonStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet menuItemStyleSheet = null;

	private PrimitiveStyleSheet getMenuItemStyleSheet()
	{
		if ( menuItemStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Painter hoverBackground = getNonNull( "menuItemHoverBackground", Painter.class, defaultMenuItemHoverBackground );
			menuItemStyleSheet = primitive.withHoverBackground( hoverBackground );
		}
		return menuItemStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet popupMenuStyleSheet = null;

	private PrimitiveStyleSheet getPopupMenuStyleSheet()
	{
		if ( popupMenuStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues attrs = getNonNull( "popupMenuAttrs", AttributeValues.class, defaultPopupMenuAttrs );
			popupMenuStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( attrs );
		}
		return popupMenuStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet tooltipStyleSheet = null;

	private PrimitiveStyleSheet getTooltipStyleSheet()
	{
		if ( tooltipStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Border border = getNonNull( "tooltipBorder", Border.class, defaultTooltipBorder );
			tooltipStyleSheet = primitive.withBorder( border );
		}
		return tooltipStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet checkboxStyleSheet = null;

	private PrimitiveStyleSheet getCheckboxStyleSheet()
	{
		if ( checkboxStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			double spacing = getNonNull( "checkboxSpacing", Double.class, defaultCheckboxSpacing );
			Painter background = getNonNull( "checkboxHoverBackground", Painter.class, defaultCheckboxHoverBackground );
			checkboxStyleSheet = primitive.withHBoxSpacing( spacing ).withHoverBackground( background );
		}
		return checkboxStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet checkboxCheckStyleSheet = null;

	private PrimitiveStyleSheet getCheckboxCheckStyleSheet()
	{
		if ( checkboxCheckStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Border border = getNonNull( "checkboxCheckBorder", Border.class, defaultCheckboxCheckBorder );
			checkboxCheckStyleSheet = primitive.withBorder( border );
		}
		return checkboxCheckStyleSheet;
	}
	
	
	private CheckboxHelper.CheckboxCheckedPainterInteractor checkboxCheckInteractor = null;
	
	private CheckboxHelper.CheckboxCheckedPainterInteractor getCheckboxCheckInteractor()
	{
		if ( checkboxCheckInteractor == null )
		{
			Paint foreground = getNonNull( "checkboxCheckForeground", Paint.class, defaultCheckboxCheckForeground );
			checkboxCheckInteractor = new CheckboxHelper.CheckboxCheckedPainterInteractor( foreground );
		}
		return checkboxCheckInteractor;
	}
	
	
	
	private PrimitiveStyleSheet textAreaStyleSheet = null;

	private PrimitiveStyleSheet getTextAreaStyleSheet()
	{
		if ( textAreaStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues attrs = getNonNull( "textAreaAttrs", AttributeValues.class, defaultTextAreaAttrs );
			textAreaStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( attrs );
		}
		return textAreaStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet textEntryTextStyleSheet = null;

	private PrimitiveStyleSheet getTextEntryTextStyleSheet()
	{
		if ( textEntryTextStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues textAttrs = getNonNull( "textEntryTextAttrs", AttributeValues.class, defaultTextEntryTextAttrs );
			textEntryTextStyleSheet = ((PrimitiveStyleSheet)primitive.withAttrValues( textAttrs ));
		}
		return textEntryTextStyleSheet;
	}
	
	
	
	private Path2D.Double spinEntryArrowPaths[] = null;
	
	private Path2D.Double[] getSpinEntryArrowPaths()
	{
		if ( spinEntryArrowPaths == null )
		{
			double arrowSize = get( "spinEntryArrowSize", Double.class, defaultSpinEntryArrowSize );
			double arrowFilletSize = get( "spinEntryArrowFilletSize", Double.class, defaultSpinEntryArrowFilletSize );
			
			spinEntryArrowPaths = createArrowPaths( arrowSize, arrowFilletSize );
		}
		return spinEntryArrowPaths;
	}
	
	
	private PrimitiveStyleSheet spinEntryArrowStyleSheet = null;
	
	private PrimitiveStyleSheet getSpinEntryArrowStyleSheet()
	{
		if ( spinEntryArrowStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues arrowAttrs = getNonNull( "spinEntryArrowAttrs", AttributeValues.class, defaultSpinEntryArrowAttrs );
			spinEntryArrowStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( arrowAttrs );
		}
		return spinEntryArrowStyleSheet;
	}
	
	
	private PrimitiveStyleSheet spinEntryStyleSheet = null;
	
	private PrimitiveStyleSheet getSpinEntryStyleSheet()
	{
		if ( spinEntryStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			double hSpacing = getNonNull( "spinEntryHSpacing", Double.class, defaultSpinEntryHSpacing );
			spinEntryStyleSheet = primitive.withHBoxSpacing( hSpacing );
		}
		return spinEntryStyleSheet;
	}
	
	

	private Path2D.Double optionMenuArrowPaths[] = null;
	
	private Path2D.Double[] getOptionMenuArrowPaths()
	{
		if ( optionMenuArrowPaths == null )
		{
			double arrowSize = get( "optionMenuArrowSize", Double.class, defaultOptionMenuArrowSize );
			double arrowFilletSize = get( "optionMenuArrowFilletSize", Double.class, defaultOptionMenuArrowFilletSize );
			
			optionMenuArrowPaths = createArrowPaths( arrowSize, arrowFilletSize );
		}
		return optionMenuArrowPaths;
	}
	
	
	private PrimitiveStyleSheet optionMenuArrowStyleSheet = null;
	
	private PrimitiveStyleSheet getOptionMenuArrowStyleSheet()
	{
		if ( optionMenuArrowStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Painter arrowPainter = getNonNull( "optionMenuArrowPainter", Painter.class, defaultOptionMenuArrowPainter );
			optionMenuArrowStyleSheet = primitive.withShapePainter( arrowPainter );
		}
		return optionMenuArrowStyleSheet;
	}
	
	
	private PrimitiveStyleSheet optionMenuStyleSheet = null;

	private PrimitiveStyleSheet getOptionMenuStyleSheet()
	{
		if ( optionMenuStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Border optionMenuBorder = getNonNull( "optionMenuBorder", Border.class, defaultOptionMenuBorder );
			double contentsSpacing = getNonNull( "optionMenuContentsSpacing", Double.class, defaultOptionMenuContentsSpacing );
			optionMenuStyleSheet = primitive.withBorder( optionMenuBorder ).withHBoxSpacing( contentsSpacing );
		}
		return optionMenuStyleSheet;
	}
	
	
	
	
	private PrimitiveStyleSheet scrollBarArrowStyleSheet = null;
	
	private PrimitiveStyleSheet getScrollBarArrowStyleSheet()
	{
		if ( scrollBarArrowStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Painter arrowPainter = getNonNull( "scrollBarArrowPainter", Painter.class, defaultScrollBarArrowPainter );
			Painter arrowHoverPainter = get( "scrollBarArrowHoverPainter", Painter.class, null );
			scrollBarArrowStyleSheet = primitive.withShapePainter( arrowPainter ).withHoverShapePainter( arrowHoverPainter );
		}
		return scrollBarArrowStyleSheet;
	}
	
	
	
	private Path2D.Double scrollBarArrowPaths[] = null;
	
	private Path2D.Double[] getScrollBarArrowPaths()
	{
		if ( scrollBarArrowPaths == null )
		{
			double scrollBarSize = get( "scrollBarSize", Double.class, defaultScrollBarSize );
			double arrowPadding = get( "scrollBarArrowPadding", Double.class, defaultScrollBarArrowPadding );
			double arrowFilletSize = get( "scrollBarArrowFilletSize", Double.class, defaultScrollBarArrowFilletSize );
			
			scrollBarArrowPaths = createArrowPaths( scrollBarSize - arrowPadding * 2.0, arrowFilletSize );
		}
		return scrollBarArrowPaths;
	}
	
	
	private PrimitiveStyleSheet scrollBarDragBackgroundStyleSheet = null;
	
	private PrimitiveStyleSheet getScrollBarDragBackgroundStyleSheet()
	{
		if ( scrollBarDragBackgroundStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Painter backgroundPainter = getNonNull( "scrollBarDragBackgroundPainter", Painter.class, defaultScrollBarDragBackgroundPainter );
			Painter backgroundHoverPainter = get( "scrollBarDragBackgroundHoverPainter", Painter.class, null );
			scrollBarDragBackgroundStyleSheet = primitive.withShapePainter( backgroundPainter ).withHoverShapePainter( backgroundHoverPainter );
		}
		return scrollBarDragBackgroundStyleSheet;
	}
	
	
	
	
	
	public Hyperlink link(String txt, Location targetLocation)
	{
		boolean bClosePopupOnActivate = getNonNull( "closePopupOnActivateFlag", Boolean.class, false );
		DPText element = getLinkStyleSheet().staticText( txt );
		return new Hyperlink( element, targetLocation, bClosePopupOnActivate, this );
	}
	
	public Hyperlink link(String txt, Hyperlink.LinkListener listener)
	{
		boolean bClosePopupOnActivate = getNonNull( "closePopupOnActivateFlag", Boolean.class, false );
		DPText element = getLinkStyleSheet().staticText( txt );
		return new Hyperlink( element, listener, bClosePopupOnActivate, this );
	}
	
	
	
	public Button button(DPElement child, Button.ButtonListener listener)
	{
		boolean bClosePopupOnActivate = getNonNull( "closePopupOnActivateFlag", Boolean.class, false );
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener, bClosePopupOnActivate );
	}

	public Button buttonWithLabel(String text, Button.ButtonListener listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		boolean bClosePopupOnActivate = getNonNull( "closePopupOnActivateFlag", Boolean.class, false );
		DPElement child = primitive.staticText( text );
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener, bClosePopupOnActivate );
	}
	
	
	
	public MenuItem menuItem(DPElement child, MenuItem.MenuItemListener listener)
	{
		boolean bClosePopupOnActivate = getNonNull( "closePopupOnActivateFlag", Boolean.class, false );
		double xPadding = getNonNull( "menuItemXPadding", Double.class, defaultMenuItemXPadding );
		double yPadding = getNonNull( "menuItemYPadding", Double.class, defaultMenuItemYPadding );
		PrimitiveStyleSheet menuItemStyle = getMenuItemStyleSheet();
		DPBin element = menuItemStyle.bin( child.alignHExpand().pad( xPadding, yPadding ) );
		return new MenuItem( (DPBin)element.alignHExpand(), listener, bClosePopupOnActivate );
	}

	public MenuItem menuItemWithLabel(String text, MenuItem.MenuItemListener listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return menuItem( primitive.staticText( text ), listener );
	}

	public MenuItem subMenuItemRight(DPElement child, PopupMenu subMenu)
	{
		PrimitiveStyleSheet menuItemStyle = getMenuItemStyleSheet();
		double xPadding = getNonNull( "menuItemXPadding", Double.class, defaultMenuItemXPadding );
		double yPadding = getNonNull( "menuItemYPadding", Double.class, defaultMenuItemYPadding );
		DPBin element = menuItemStyle.bin( child.alignHExpand().pad( xPadding, yPadding ) );
		return new MenuItem( (DPBin)element.alignHExpand(), subMenu, MenuItem.SubmenuPopupDirection.RIGHT, false );
	}

	public MenuItem subMenuItemDown(DPElement child, PopupMenu subMenu)
	{
		PrimitiveStyleSheet menuItemStyle = getMenuItemStyleSheet();
		double xPadding = getNonNull( "menuItemXPadding", Double.class, defaultMenuItemXPadding );
		double yPadding = getNonNull( "menuItemYPadding", Double.class, defaultMenuItemYPadding );
		DPBin element = menuItemStyle.bin( child.alignHExpand().pad( xPadding, yPadding ) );
		return new MenuItem( (DPBin)element.alignHExpand(), subMenu, MenuItem.SubmenuPopupDirection.DOWN, false );
	}

	public MenuItem subMenuItemRightWithLabel(String text, PopupMenu subMenu)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return subMenuItemRight( primitive.staticText( text ), subMenu );
	}

	public MenuItem subMenuItemDownWithLabel(String text, PopupMenu subMenu)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return subMenuItemDown( primitive.staticText( text ), subMenu );
	}

	public PopupMenu hpopupMenu(DPElement items[])
	{
		PrimitiveStyleSheet popupMenuStyle = getPopupMenuStyleSheet();
		DPAbstractBox menuBox = popupMenuStyle.hbox( items );
		DPBorder menuElement = popupMenuStyle.border( menuBox );
		return new PopupMenu( menuElement, menuBox );
	}

	public PopupMenu hpopupMenu(List<DPElement> items)
	{
		PrimitiveStyleSheet popupMenuStyle = getPopupMenuStyleSheet();
		DPAbstractBox menuBox = popupMenuStyle.hbox( items );
		DPBorder menuElement = popupMenuStyle.border( menuBox );
		return new PopupMenu( menuElement, menuBox );
	}

	public PopupMenu vpopupMenu(DPElement items[])
	{
		PrimitiveStyleSheet popupMenuStyle = getPopupMenuStyleSheet();
		DPAbstractBox menuBox = popupMenuStyle.vbox( items );
		DPBorder menuElement = popupMenuStyle.border( menuBox );
		return new PopupMenu( menuElement, menuBox );
	}

	public PopupMenu vpopupMenu(List<DPElement> items)
	{
		PrimitiveStyleSheet popupMenuStyle = getPopupMenuStyleSheet();
		DPAbstractBox menuBox = popupMenuStyle.vbox( items );
		DPBorder menuElement = popupMenuStyle.border( menuBox );
		return new PopupMenu( menuElement, menuBox );
	}
	
	
	
	public TimedPopup tooltip(String text, double timeout)
	{
		PrimitiveStyleSheet tooltipStyle = getTooltipStyleSheet();
		String lineTexts[] = text.split( "\\r?\\n" );
		ArrayList<DPElement> lines = new ArrayList<DPElement>();
		for (String line: lineTexts)
		{
			lines.add( tooltipStyle.staticText( line ) );
		}
		DPElement contents = tooltipStyle.border( tooltipStyle.vbox( lines ) );
		return new TimedPopup( contents, timeout, false );
	}

	
	
	public Checkbox checkbox(DPElement child, boolean state, Checkbox.CheckboxListener listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		PrimitiveStyleSheet checkStyle = getCheckboxCheckStyleSheet();
		double checkSize = getNonNull( "checkboxCheckSize", Double.class, defaultCheckboxCheckSize );
		DPElement unchecked = primitive.spacer( checkSize, checkSize );
		DPElement checked = primitive.spacer( checkSize, checkSize );
		checked.addInteractor( getCheckboxCheckInteractor() );
		DPBorder check = checkStyle.border( state  ?  checked  :  unchecked );
		DPElement hbox = getCheckboxStyleSheet().hbox( new DPElement[] { check.alignVCentre(), child.alignVCentre() } );
		DPElement element = primitive.bin( hbox );
		Checkbox checkbox = new Checkbox( element, check, unchecked, checked, state, listener );
		hbox.addInteractor( new CheckboxHelper.CheckboxCheckInteractor( checkbox ) );
		return checkbox;
	}
	
	public Checkbox checkboxWithLabel(String labelText, boolean state, Checkbox.CheckboxListener listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return checkbox( primitive.staticText( labelText ), state, listener );
	}
	
	
	
	public TextArea textArea(String text, TextArea.TextAreaListener listener)
	{
		PrimitiveStyleSheet textAreaStyle = getTextAreaStyleSheet();
		DPVBox textBox = textAreaStyle.vbox( new DPElement[] {} );
		DPRegion region = textAreaStyle.region( textBox );
		DPBorder element = textAreaStyle.border( region );
		return new TextArea( element, region, textBox, listener, textAreaStyle, text );
	}
	
	
	
	public TextEntry textEntry(String text, TextEntry.TextEntryListener listener)
	{
		return textEntry( text, listener, null );
	}

	public TextEntry textEntry(String text, TextEntry.TextEntryListener listener, TextEntry.TextEntryValidator validator)
	{
		PrimitiveStyleSheet textEntryTextStyle = getTextEntryTextStyleSheet();
		DPText textElement = textEntryTextStyle.text( text );
		DPElement line = textEntryTextStyle.hbox( new DPElement[] { textEntryTextStyle.segment( false, false, textElement ) } );
		DPRegion region = textEntryTextStyle.region( line );
		DPBorder outerElement = textEntryTextStyle.border( region );
		Border validBorder = getNonNull( "textEntryBorder", Border.class, defaultTextEntryBorder );
		Border invalidBorder = getNonNull( "textEntryInvalidBorder", Border.class, defaultTextEntryBorder );
		return new TextEntry( outerElement, region, textElement, listener, validator, this, validBorder, invalidBorder );
	}

	public TextEntry textEntry(String text, TextEntry.TextEntryListener listener, Pattern validatorRegex, String validationFailMessage)
	{
		PrimitiveStyleSheet textEntryTextStyle = getTextEntryTextStyleSheet();
		DPText textElement = textEntryTextStyle.text( text );
		DPElement line = textEntryTextStyle.hbox( new DPElement[] { textEntryTextStyle.segment( false, false, textElement ) } );
		DPRegion region = textEntryTextStyle.region( line );
		DPBorder outerElement = textEntryTextStyle.border( region );
		Border validBorder = getNonNull( "textEntryBorder", Border.class, defaultTextEntryBorder );
		Border invalidBorder = getNonNull( "textEntryInvalidBorder", Border.class, defaultTextEntryBorder );
		return new TextEntry( outerElement, region, textElement, listener, validatorRegex, validationFailMessage, this, validBorder, invalidBorder );
	}

	
	public RealSpinEntry realSpinEntry(double value, double min, double max, double stepSize, double pageSize, RealSpinEntry.RealSpinEntryListener listener)
	{
		PrimitiveStyleSheet spinEntryStyle = getSpinEntryStyleSheet();
		PrimitiveStyleSheet arrowStyle = getSpinEntryArrowStyleSheet();
		
		Path2D.Double upPath = getSpinEntryArrowPaths()[2];
		Path2D.Double downPath = getSpinEntryArrowPaths()[3];
		
		DPShape upArrow = arrowStyle.shape( upPath );
		DPShape downArrow = arrowStyle.shape( downPath );
		DPElement arrowsBox = arrowStyle.vbox( new DPElement[] { upArrow, downArrow } );
		
		SpinEntry.SpinEntryTextListener textListener = new SpinEntry.SpinEntryTextListener();
	
		TextEntry textEntry = textEntry( String.valueOf( value ), textListener,
				Pattern.compile( "[\\-]?(([0-9]+\\.[0-9]*)|(\\.[0-9]+))(e[\\-]?[0-9]+)?" ), "Please enter a real number." );
		
		DPElement element = spinEntryStyle.hbox( new DPElement[] { textEntry.getElement().alignHExpand().alignVCentre(), arrowsBox.alignVCentre() } );

		return new RealSpinEntry( element, textEntry, upArrow, downArrow, textListener, value, min, max, stepSize, pageSize, listener );
	}

	public IntSpinEntry intSpinEntry(int value, int min, int max, int stepSize, int pageSize, IntSpinEntry.IntSpinEntryListener listener)
	{
		PrimitiveStyleSheet spinEntryStyle = getSpinEntryStyleSheet();
		PrimitiveStyleSheet arrowStyle = getSpinEntryArrowStyleSheet();
		
		Path2D.Double upPath = getSpinEntryArrowPaths()[2];
		Path2D.Double downPath = getSpinEntryArrowPaths()[3];
		
		DPShape upArrow = arrowStyle.shape( upPath );
		DPShape downArrow = arrowStyle.shape( downPath );
		DPElement arrowsBox = arrowStyle.vbox( new DPElement[] { upArrow, downArrow } );
		
		SpinEntry.SpinEntryTextListener textListener = new SpinEntry.SpinEntryTextListener();
		
		TextEntry textEntry = textEntry( String.valueOf( value ), textListener,
				Pattern.compile( "[\\-]?[0-9]+" ), "Please enter an integer." );
		
		DPElement element = spinEntryStyle.hbox( new DPElement[] { textEntry.getElement().alignHExpand().alignVCentre(), arrowsBox.alignVCentre() } );
		
		return new IntSpinEntry( element, textEntry, upArrow, downArrow, textListener, value, min, max, stepSize, pageSize, listener );
	}

	
	
	public OptionMenu optionMenu(DPElement optionChoices[], DPElement menuChoices[], int initialChoice, OptionMenu.OptionMenuListener listener)
	{
		return optionMenu( Arrays.asList( optionChoices ), Arrays.asList( menuChoices ), initialChoice, listener );
	}
	
	public OptionMenu optionMenu(List<DPElement> optionChoices, List<DPElement> menuChoices, int initialChoice, OptionMenu.OptionMenuListener listener)
	{
		PrimitiveStyleSheet arrowStyle = getOptionMenuArrowStyleSheet();
		Border optionMenuBorder = getNonNull( "optionMenuBorder", Border.class, defaultOptionMenuBorder );
		Border optionMenuHoverBorder = getNonNull( "optionMenuHoverBorder", Border.class, defaultOptionMenuHoverBorder );

		Path2D.Double downPath = getOptionMenuArrowPaths()[3];
		DPShape downArrow = arrowStyle.shape( downPath );
		PrimitiveStyleSheet optionMenuStyle = getOptionMenuStyleSheet();
		DPBin choiceBin = PrimitiveStyleSheet.instance.bin( optionChoices.get( initialChoice ) );
		DPHBox optionContents = optionMenuStyle.hbox( new DPElement[] { choiceBin.alignHExpand().alignVCentre(), downArrow.alignVCentre() } );
		DPBorder element = getOptionMenuStyleSheet().border( optionContents.alignHExpand() );
		return new OptionMenu( element, choiceBin, optionChoices, menuChoices, initialChoice, listener, optionMenuBorder, optionMenuHoverBorder, this );
	}
	
	
	
	public ScrollBar horizontalScrollBar(Range range)
	{
		PrimitiveStyleSheet arrowStyle = getScrollBarArrowStyleSheet();
		PrimitiveStyleSheet dragBackgroundStyle = getScrollBarDragBackgroundStyleSheet();
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		
		Path2D.Double leftPath = getScrollBarArrowPaths()[0];
		Path2D.Double rightPath = getScrollBarArrowPaths()[1];
		
		double arrowPadding = get( "scrollBarArrowPadding", Double.class, defaultScrollBarArrowPadding );
		double arrowSpacing = get( "scrollBarArrowSpacing", Double.class, defaultScrollBarArrowSpacing );

		double scrollBarSize = get( "scrollBarSize", Double.class, defaultScrollBarSize );
		Painter scrollBarDragBoxPainter = getNonNull( "scrollBarDragBoxPainter", Painter.class, defaultScrollBarArrowPainter );
		double scrollBarDragBoxPadding = get( "scrollBarDragBoxPadding", Double.class, defaultScrollBarDragBoxPadding );
		double scrollBarDragBoxRounding = get( "scrollBarDragBoxRounding", Double.class, defaultScrollBarDragBoxRounding );
		
		
		DPShape leftArrow = arrowStyle.shape( leftPath );
		leftArrow.addInteractor( new ScrollBarHelper.ScrollBarArrowInteractor( ScrollBarHelper.ScrollBarArrowInteractor.Direction.DECREASE, range ) );
		
		DPShape rightArrow = arrowStyle.shape( rightPath );
		rightArrow.addInteractor( new ScrollBarHelper.ScrollBarArrowInteractor( ScrollBarHelper.ScrollBarArrowInteractor.Direction.INCREASE, range ) );
		
		DPBox dragBar = dragBackgroundStyle.box( 0.0, scrollBarSize );
		dragBar.addInteractor( new ScrollBarHelper.ScrollBarDragBarInteractor( dragBar, ScrollBarHelper.ScrollBarDragBarInteractor.Direction.HORIZONTAL, range, scrollBarDragBoxPadding, scrollBarDragBoxRounding,
				scrollBarDragBoxPainter ) );
		
		DPElement element = primitive.withHBoxSpacing( arrowSpacing ).hbox( new DPElement[] { leftArrow.pad( arrowPadding, arrowPadding ).alignVCentre(),
				dragBar.alignHExpand().alignVCentre(),
				rightArrow.pad( arrowPadding, arrowPadding ).alignVCentre() } );
		
		return new ScrollBar( range, element.alignHExpand() );
	}
	
	public ScrollBar verticalScrollBar(Range range)
	{
		PrimitiveStyleSheet arrowStyle = getScrollBarArrowStyleSheet();
		PrimitiveStyleSheet dragBackgroundStyle = getScrollBarDragBackgroundStyleSheet();
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		
		Path2D.Double upPath = getScrollBarArrowPaths()[2];
		Path2D.Double downPath = getScrollBarArrowPaths()[3];
		
		double arrowPadding = get( "scrollBarArrowPadding", Double.class, defaultScrollBarArrowPadding );
		double arrowSpacing = get( "scrollBarArrowSpacing", Double.class, defaultScrollBarArrowSpacing );

		double scrollBarSize = get( "scrollBarSize", Double.class, defaultScrollBarSize );
		Painter scrollBarDragBoxPainter = getNonNull( "scrollBarDragBoxPainter", Painter.class, defaultScrollBarArrowPainter );
		double scrollBarDragBoxPadding = get( "scrollBarDragBoxPadding", Double.class, defaultScrollBarDragBoxPadding );
		double scrollBarDragBoxRounding = get( "scrollBarDragBoxRounding", Double.class, defaultScrollBarDragBoxRounding );
		
		
		DPShape upArrow = arrowStyle.shape( upPath );
		upArrow.addInteractor( new ScrollBarHelper.ScrollBarArrowInteractor( ScrollBarHelper.ScrollBarArrowInteractor.Direction.DECREASE, range ) );
		
		DPShape downArrow = arrowStyle.shape( downPath );
		downArrow.addInteractor( new ScrollBarHelper.ScrollBarArrowInteractor( ScrollBarHelper.ScrollBarArrowInteractor.Direction.INCREASE, range ) );
		
		DPBox dragBar = dragBackgroundStyle.box( scrollBarSize, 0.0 );
		dragBar.addInteractor( new ScrollBarHelper.ScrollBarDragBarInteractor( dragBar, ScrollBarHelper.ScrollBarDragBarInteractor.Direction.VERTICAL, range, scrollBarDragBoxPadding, scrollBarDragBoxRounding,
				scrollBarDragBoxPainter ) );
		
		DPElement element = primitive.withVBoxSpacing( arrowSpacing ).vbox( new DPElement[] { upArrow.pad( arrowPadding, arrowPadding ).alignHCentre(),
				dragBar.alignVExpand().alignHCentre(),
				downArrow.pad( arrowPadding, arrowPadding ).alignHCentre() } );
		
		return new ScrollBar( range, element.alignVExpand() );
	}
	
	
	public ScrolledViewport scrolledViewport(DPElement child, double minWidth, double minHeight, PersistentState state)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		double scrollBarSize = get( "scrollBarSize", Double.class, defaultScrollBarSize );

		Range xRange = new Range( 0.0, 1.0, 0.0, 1.0, 0.1 );
		Range yRange = new Range( 0.0, 1.0, 0.0, 1.0, 0.1 );
		DPViewport viewport = primitive.viewport( child, xRange, yRange, state );
		DPSpaceBin space = primitive.spaceBin( viewport.alignHExpand().alignVExpand(), minWidth, minHeight );
		ScrollBar xScroll = horizontalScrollBar( xRange );
		ScrollBar yScroll = verticalScrollBar( yRange );
		DPElement hbox0 = primitive.hbox( new DPElement[] { space.alignHExpand().alignVExpand(),  yScroll.getElement().alignVExpand() } );
		DPElement hbox1 = primitive.hbox( new DPElement[] { xScroll.getElement().alignHExpand(), primitive.spacer( scrollBarSize, scrollBarSize ) } );
		DPElement vbox = primitive.vbox( new DPElement[] { hbox0.alignHExpand().alignVExpand(), hbox1.alignHExpand() } );
		return new ScrolledViewport( viewport, vbox, xScroll, yScroll, xRange, yRange );
	}

	public ScrolledViewport aspectRatioScrolledViewport(DPElement child, double minWidth, double aspectRatio, PersistentState state)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyle", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		double scrollBarSize = get( "scrollBarSize", Double.class, defaultScrollBarSize );

		Range xRange = new Range( 0.0, 1.0, 0.0, 1.0, 0.1 );
		Range yRange = new Range( 0.0, 1.0, 0.0, 1.0, 0.1 );
		DPViewport viewport = primitive.viewport( child, xRange, yRange, state );
		DPAspectRatioBin space = primitive.aspectRatioBin( viewport.alignHExpand().alignVExpand(), minWidth, aspectRatio );
		ScrollBar xScroll = horizontalScrollBar( xRange );
		ScrollBar yScroll = verticalScrollBar( yRange );
		DPElement hbox0 = primitive.hbox( new DPElement[] { space.alignHExpand().alignVExpand(),  yScroll.getElement().alignVExpand() } );
		DPElement hbox1 = primitive.hbox( new DPElement[] { xScroll.getElement().alignHExpand(), primitive.spacer( scrollBarSize, scrollBarSize ) } );
		DPElement vbox = primitive.vbox( new DPElement[] { hbox0.alignHExpand().alignVExpand(), hbox1.alignHExpand() } );
		return new ScrolledViewport( viewport, vbox, xScroll, yScroll, xRange, yRange );
	}
}
