//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Paint;
import java.util.Arrays;

import org.python.core.PyObject;

import BritefuryJ.AttributeTable.AttributeValues;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class ControlsStyleSheet extends StyleSheet
{
	private static final Font defaultLinkFont = new Font( "Sans serif", Font.PLAIN, 14 );
	private static final Cursor defaultLinkCursor = new Cursor( Cursor.HAND_CURSOR );
	
	
	public static final ControlsStyleSheet instance = new ControlsStyleSheet();

	
	
	
	
	
	

	
	
	
	public ControlsStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyleSheet", PrimitiveStyleSheet.instance );

		initAttr( "linkAttrs", new AttributeValues( new String[] { "editable", "font", "foreground", "hoverForeground", "cursor" }, new Object[] { false, defaultLinkFont, Color.blue, Color.red, defaultLinkCursor } ) );
		
		initAttr( "buttonBorderThickness", 1.0 );
		initAttr( "buttonMargin", 3.0 );
		initAttr( "buttonRounding", 10.0 );
		initAttr( "buttonBorderPaint", new Color( 0.55f, 0.525f, 0.5f ) );
		initAttr( "buttonBorderHighlightPaint", new Color( 0.0f, 0.5f, 0.5f ) );
		initAttr( "buttonBackgPaint", new Color( 0.85f, 0.85f, 0.85f ) );
		initAttr( "buttonBackgHighlightPaint", new Color( 0.925f, 0.925f, 0.925f ) );
		
		initAttr( "textEntryTextAttrs", new AttributeValues() );
		initAttr( "textEntryBorderThickness", 3.0 );
		initAttr( "textEntryMargin", 3.0 );
		initAttr( "textEntryRounding", 10.0 );
		initAttr( "textEntryBorderPaint", new Color( 0.55f, 0.8f, 0.55f ) );
		initAttr( "textEntryBackgPaint", new Color( 0.9f, 1.0f, 0.9f ) );
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
	
	
	
	public TextEntry textEntry(String text, TextEntry.TextEntryListener listener)
	{
		PrimitiveStyleSheet textEntryStyle = getTextEntryStyleSheet();
		DPText textElement = textEntryStyle.text( text );
		DPElement line = textEntryStyle.hbox( Arrays.asList( new DPElement[] { textEntryStyle.segment( false, false, textElement ) } ) );
		DPRegion frame = textEntryStyle.region( line );
		DPBorder outerElement = textEntryStyle.border( frame );
		return new TextEntry( outerElement, frame, textElement, listener );
	}

	public TextEntry textEntry(String text, PyObject accept, PyObject cancel)
	{
		PrimitiveStyleSheet textEntryStyle = getTextEntryStyleSheet();
		DPText textElement = textEntryStyle.text( text );
		DPElement line = textEntryStyle.hbox( Arrays.asList( new DPElement[] { textEntryStyle.segment( false, false, textElement ) } ) );
		DPRegion frame = textEntryStyle.region( line );
		DPBorder outerElement = textEntryStyle.border( frame );
		return new TextEntry( outerElement, frame, textElement, accept, cancel );
	}
}
