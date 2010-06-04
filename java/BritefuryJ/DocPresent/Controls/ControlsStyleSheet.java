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

import org.python.core.PyObject;

import BritefuryJ.AttributeTable.AttributeValues;
import BritefuryJ.DocPresent.DPAspectRatioBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPBox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPShape;
import BritefuryJ.DocPresent.DPSpaceBin;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Browser.Location;
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
	private static final String defaultLinkFontFace = "Sans serif";
	private static final int defaultLinkFontSize = 14;
	private static final Cursor defaultLinkCursor = new Cursor( Cursor.HAND_CURSOR );
	
	private static final Painter defaultCheckboxHoverBackground = new OutlinePainter( new Color( 0.5f, 0.625f, 0.75f ) );
	private static final Border defaultCheckboxCheckBorder = new FilledBorder( 3.0, 3.0, 3.0, 3.0, 5.0, 5.0, new Color( 0.75f, 0.75f, 0.75f ) );
	private static final Paint defaultCheckboxCheckForeground = new Color( 0.0f, 0.2f, 0.4f );
	private static final double defaultCheckboxCheckSize = 10.0;
	private static final double defaultCheckboxSpacing = 8.0;
	
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
		
		initAttr( "primitiveStyleSheet", PrimitiveStyleSheet.instance );

		initAttr( "linkAttrs", new AttributeValues( new String[] { "editable", "fontFace", "fontSize", "foreground", "hoverForeground", "cursor" },
				new Object[] { false, defaultLinkFontFace, defaultLinkFontSize, Color.blue, Color.red, defaultLinkCursor } ) );
		
		initAttr( "buttonBorderThickness", 1.0 );
		initAttr( "buttonMargin", 3.0 );
		initAttr( "buttonRounding", 10.0 );
		initAttr( "buttonBorderPaint", new Color( 0.55f, 0.525f, 0.5f ) );
		initAttr( "buttonBorderHighlightPaint", new Color( 0.0f, 0.5f, 0.5f ) );
		initAttr( "buttonBackgPaint", new Color( 0.85f, 0.85f, 0.85f ) );
		initAttr( "buttonBackgHighlightPaint", new Color( 0.925f, 0.925f, 0.925f ) );
		
		initAttr( "checkboxHoverBackground", defaultCheckboxHoverBackground );
		initAttr( "checkboxCheckBorder", defaultCheckboxCheckBorder );
		initAttr( "checkboxCheckForeground", defaultCheckboxCheckForeground );
		initAttr( "checkboxCheckSize", defaultCheckboxCheckSize );
		initAttr( "checkboxSpacing", defaultCheckboxSpacing );
		
		initAttr( "textEntryTextAttrs", new AttributeValues() );
		initAttr( "textEntryBorderThickness", 3.0 );
		initAttr( "textEntryMargin", 3.0 );
		initAttr( "textEntryRounding", 10.0 );
		initAttr( "textEntryBorderPaint", new Color( 0.55f, 0.8f, 0.55f ) );
		initAttr( "textEntryBackgPaint", new Color( 0.9f, 1.0f, 0.9f ) );
		
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
		return (ControlsStyleSheet)withAttr( "primitiveStyleSheet", styleSheet );
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
	
	
	
	
	private PrimitiveStyleSheet linkStyleSheet = null;

	private PrimitiveStyleSheet getLinkStyleSheet()
	{
		if ( linkStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
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
			double thickness = getNonNull( "buttonBorderThickness", Double.class, 1.0 );
			double margin = getNonNull( "buttonMargin", Double.class, 5.0 );
			double rounding = getNonNull( "buttonRounding", Double.class, 10.0 );
			Paint borderPaint = getNonNull( "buttonBorderPaint", Paint.class, Color.black );
			Paint backgPaint = getNonNull( "buttonBackgPaint", Paint.class, new Color( 1.0f, 1.0f, 0.7f ) );

			buttonBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return buttonBorder;
	}
	
	
	
	private Border buttonHighlightBorder = null;

	private Border getButtonHighlightBorder()
	{
		if ( buttonHighlightBorder == null )
		{
			double thickness = getNonNull( "buttonBorderThickness", Double.class, 1.0 );
			double margin = getNonNull( "buttonMargin", Double.class, 5.0 );
			double rounding = getNonNull( "buttonRounding", Double.class, 10.0 );
			Paint borderPaint = getNonNull( "buttonBorderHighlightPaint", Paint.class, Color.black );
			Paint backgPaint = getNonNull( "buttonBackgHighlightPaint", Paint.class, new Color( 1.0f, 1.0f, 0.7f ) );

			buttonHighlightBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return buttonHighlightBorder;
	}
	

	
	private PrimitiveStyleSheet buttonStyleSheet = null;

	private PrimitiveStyleSheet getButtonStyleSheet()
	{
		if ( buttonStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			buttonStyleSheet = primitive.withBorder( getButtonBorder() );
		}
		return buttonStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet checkboxStyleSheet = null;

	private PrimitiveStyleSheet getCheckboxStyleSheet()
	{
		if ( checkboxStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
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
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
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
	
	
	
	private Border textEntryBorder = null;

	private Border getTextEntryBorder()
	{
		if ( textEntryBorder == null )
		{
			double thickness = getNonNull( "textEntryBorderThickness", Double.class, 3.0 );
			double margin = getNonNull( "textEntryMargin", Double.class, 3.0 );
			double rounding = getNonNull( "textEntryRounding", Double.class, 10.0 );
			Paint borderPaint = getNonNull( "textEntryBorderPaint", Paint.class, new Color( 0.55f, 0.8f, 0.55f ) );
			Paint backgPaint = getNonNull( "textEntryBackgPaint", Paint.class, new Color( 0.9f, 1.0f, 0.9f ) );

			textEntryBorder = new SolidBorder( thickness, margin, rounding, rounding, borderPaint, backgPaint );
		}
		return textEntryBorder;
	}
	

	
	private PrimitiveStyleSheet textEntryStyleSheet = null;

	private PrimitiveStyleSheet getTextEntryStyleSheet()
	{
		if ( textEntryStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues textAttrs = getNonNull( "textEntryTextAttrs", AttributeValues.class, AttributeValues.identity );
			textEntryStyleSheet = ((PrimitiveStyleSheet)primitive.withAttrValues( textAttrs )).withBorder( getTextEntryBorder() );
		}
		return textEntryStyleSheet;
	}
	
	
	
	private PrimitiveStyleSheet scrollBarArrowStyleSheet = null;
	
	private PrimitiveStyleSheet getScrollBarArrowStyleSheet()
	{
		if ( scrollBarArrowStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
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
			double arrowRadius = scrollBarSize * 0.5  -  arrowPadding;
			
			Point2 a = new Point2( 0.0, arrowRadius );
			Point2 b = new Point2( arrowRadius * Math.sin( Math.toRadians( 120.0 ) ), arrowRadius * Math.cos( Math.toRadians( 120.0 ) ) );
			Point2 c = new Point2( arrowRadius * Math.sin( Math.toRadians( 240.0 ) ), arrowRadius * Math.cos( Math.toRadians( 240.0 ) ) );
			
			Path2D.Double arrowShape = DPShape.filletedPath( new Point2[] { a, b, c }, true, arrowFilletSize );
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
			
			scrollBarArrowPaths = new Path2D.Double[] { up, right, down, left }; 
		}
		return scrollBarArrowPaths;
	}
	
	
	private PrimitiveStyleSheet scrollBarDragBackgroundStyleSheet = null;
	
	private PrimitiveStyleSheet getScrollBarDragBackgroundStyleSheet()
	{
		if ( scrollBarDragBackgroundStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Painter backgroundPainter = getNonNull( "scrollBarDragBackgroundPainter", Painter.class, defaultScrollBarDragBackgroundPainter );
			Painter backgroundHoverPainter = get( "scrollBarDragBackgroundHoverPainter", Painter.class, null );
			scrollBarDragBackgroundStyleSheet = primitive.withShapePainter( backgroundPainter ).withHoverShapePainter( backgroundHoverPainter );
		}
		return scrollBarDragBackgroundStyleSheet;
	}
	
	
	
	
	
	public Hyperlink link(String txt, Location targetLocation)
	{
		DPText element = getLinkStyleSheet().staticText( txt );
		return new Hyperlink( element, targetLocation );
	}
	
	public Hyperlink link(String txt, Hyperlink.LinkListener listener)
	{
		DPText element = getLinkStyleSheet().staticText( txt );
		return new Hyperlink( element, listener );
	}
	
	
	
	public Button button(DPElement child, Button.ButtonListener listener)
	{
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener );
	}

	public Button button(DPElement child, PyObject listener)
	{
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener );
	}

	public Button buttonWithLabel(String text, Button.ButtonListener listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		DPElement child = primitive.staticText( text );
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener );
	}

	public Button buttonWithLabel(String text, PyObject listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		DPElement child = primitive.staticText( text );
		DPBorder element = getButtonStyleSheet().border( child );
		return new Button( element, getButtonBorder(), getButtonHighlightBorder(), listener );
	}
	
	
	
	public Checkbox checkbox(DPElement child, boolean state, Checkbox.CheckboxListener listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
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
	
	public Checkbox checkboxWithLabel(String labelText, boolean state, PyObject listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return checkbox( primitive.staticText( labelText ), state, listener );
	}
	
	public Checkbox checkboxWithLabel(String labelText, boolean state, Checkbox.CheckboxListener listener)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return checkbox( primitive.staticText( labelText ), state, listener );
	}
	
	public Checkbox checkbox(DPElement child, boolean state, PyObject listener)
	{
		return checkbox( child, state, new Checkbox.PyCheckboxListener( listener ) );
	}
	
	
	
	public TextEntry textEntry(String text, TextEntry.TextEntryListener listener)
	{
		PrimitiveStyleSheet textEntryStyle = getTextEntryStyleSheet();
		DPText textElement = textEntryStyle.text( text );
		DPElement line = textEntryStyle.hbox( new DPElement[] { textEntryStyle.segment( false, false, textElement ) } );
		DPRegion region = textEntryStyle.region( line );
		DPBorder outerElement = textEntryStyle.border( region );
		return new TextEntry( outerElement, region, textElement, listener );
	}

	public TextEntry textEntry(String text, PyObject accept, PyObject cancel)
	{
		PrimitiveStyleSheet textEntryStyle = getTextEntryStyleSheet();
		DPText textElement = textEntryStyle.text( text );
		DPElement line = textEntryStyle.hbox( new DPElement[] { textEntryStyle.segment( false, false, textElement ) } );
		DPRegion region = textEntryStyle.region( line );
		DPBorder outerElement = textEntryStyle.border( region );
		return new TextEntry( outerElement, region, textElement, accept, cancel );
	}
	
	
	
	public ScrollBar horizontalScrollBar(Range range)
	{
		PrimitiveStyleSheet arrowStyle = getScrollBarArrowStyleSheet();
		PrimitiveStyleSheet dragBackgroundStyle = getScrollBarDragBackgroundStyleSheet();
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		
		Path2D.Double leftPath = getScrollBarArrowPaths()[3];
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
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		
		Path2D.Double upPath = getScrollBarArrowPaths()[0];
		Path2D.Double downPath = getScrollBarArrowPaths()[2];
		
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
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
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
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
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
