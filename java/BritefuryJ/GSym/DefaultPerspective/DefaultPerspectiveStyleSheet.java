//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.DefaultPerspective;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeValues;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class DefaultPerspectiveStyleSheet extends StyleSheet
{
	private static final double defaultObjectBorderThickness = 1.0;
	private static final double defaultObjectBorderInset = 3.0;
	private static final double defaultObjectBorderRounding = 1.0;
	private static final Color defaultObjectBorderPaint = new Color( 0.35f, 0.35f, 0.35f );
	private static final Color defaultObjectTitlePaint = new Color( 0.35f, 0.35f, 0.35f );
	private static final AttributeValues defaultObjectTitleAttrs = new AttributeValues( new String[] { "font", "textSmallCaps" }, new Object[] { new Font( "Sans serif", Font.PLAIN, 10 ), true } );
	private static final double defaultObjectContentPadding = 5.0;
	private static final Paint defaultObjectFieldTitlePaint = new Color( 0.0f, 0.25f, 0.5f );
	private static final double defaultObjectFieldIndentation = 5.0;
	private static final double defaultObjectFieldSpacing = 2.0;
	private static final AttributeValues defaultStringEscapeAttrs = new AttributeValues( new String[] { "foreground", "background" },
			new Object[] { new Color( 0.0f, 0.15f, 0.35f ), new FillPainter( new Color( 0.8f, 0.8f, 1.0f ) ) } );
	
	public enum PresentationSize
	{
		FULL,
		ONELINE
	}
	
	
	
	public static final DefaultPerspectiveStyleSheet instance = new DefaultPerspectiveStyleSheet();

	
	
	public DefaultPerspectiveStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyleSheet", PrimitiveStyleSheet.instance );
		
		initAttr( "presentationSize", PresentationSize.FULL );
		
		initAttr( "objectBorderThickness", defaultObjectBorderThickness );
		initAttr( "objectBorderInset", defaultObjectBorderInset );
		initAttr( "objectBorderRounding", defaultObjectBorderRounding );
		initAttr( "objectBorderPaint", defaultObjectBorderPaint );
		initAttr( "objectBorderBackground", null );
		initAttr( "objectTitlePaint", defaultObjectTitlePaint );
		initAttr( "objectTitleAttrs", defaultObjectTitleAttrs );
		initAttr( "objectContentPadding", defaultObjectContentPadding );
		
		initAttr( "objectFieldTitlePaint", defaultObjectFieldTitlePaint );
		initAttr( "objectFieldIndentation", defaultObjectFieldIndentation );

		initAttr( "objectFieldSpacing", defaultObjectFieldSpacing );
		
		initAttr( "stringContentAttrs", AttributeValues.identity );
		initAttr( "stringEscapeAttrs", defaultStringEscapeAttrs );
	}
	
		
	protected StyleSheet newInstance()
	{
		return new DefaultPerspectiveStyleSheet();
	}
	
	
	
	private static PrimitiveStyleSheet objectBorderStyleSheet = null;
	
	private PrimitiveStyleSheet getObjectBorderStyleSheet()
	{
		if ( objectBorderStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			double objectBorderThickness = getNonNull( "objectBorderThickness", Double.class, defaultObjectBorderThickness );
			double objectBorderInset = getNonNull( "objectBorderInset", Double.class, defaultObjectBorderInset );
			double objectBorderRounding = getNonNull( "objectBorderRounding", Double.class, defaultObjectBorderRounding );
			Paint objectBorderPaint = getNonNull( "objectBorderPaint", Paint.class, defaultObjectBorderPaint );
			Paint objectBorderBackground = get( "objectBorderBackground", Paint.class, null );
			objectBorderStyleSheet = primitive.withBorder( new SolidBorder( objectBorderThickness, objectBorderInset, objectBorderRounding, objectBorderRounding, objectBorderPaint, objectBorderBackground ) );
		}
		return objectBorderStyleSheet;
	}
	
	
	
	private static PrimitiveStyleSheet objectTitleStyleSheet = null;
	
	private PrimitiveStyleSheet getObjectTitleStyleSheet()
	{
		if ( objectTitleStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Paint objectTitlePaint = getNonNull( "objectTitlePaint", Paint.class, defaultObjectTitlePaint );
			AttributeValues objectTitleAttrs = getNonNull( "objectTitleAttrs", AttributeValues.class, defaultObjectTitleAttrs );
			objectTitleStyleSheet = ( (PrimitiveStyleSheet)primitive.withAttrValues( objectTitleAttrs ) ).withForeground( objectTitlePaint );
		}
		return objectTitleStyleSheet;
	}
	
	
	
	private static PrimitiveStyleSheet objectFieldListStyleSheet = null;
	
	private PrimitiveStyleSheet getObjectFieldListStyleSheet()
	{
		if ( objectFieldListStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			double fieldSpacing = getNonNull( "objectFieldSpacing", Double.class, defaultObjectFieldSpacing );
			objectFieldListStyleSheet = primitive.withVBoxSpacing( fieldSpacing );
		}
		return objectFieldListStyleSheet;
	}
	
	
	
	private static PrimitiveStyleSheet objectFieldStyleSheet = null;
	
	private PrimitiveStyleSheet getObjectFieldStyleSheet()
	{
		if ( objectFieldStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			Paint objectFieldTitlePaint = getNonNull( "objectFieldTitlePaint", Paint.class, defaultObjectFieldTitlePaint );
			double objectFieldIndentation = getNonNull( "objectFieldIndentation", Double.class, defaultObjectFieldIndentation );
			objectFieldStyleSheet = primitive.withForeground( objectFieldTitlePaint ).withParagraphIndentation( objectFieldIndentation );
		}
		return objectFieldStyleSheet;
	}
	
	
	
	private static PrimitiveStyleSheet stringContentStyleSheet = null;
	
	private PrimitiveStyleSheet getStringContentStyleSheet()
	{
		if ( stringContentStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues attrs = getNonNull( "stringContentAttrs", AttributeValues.class, AttributeValues.identity );
			stringContentStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( attrs );
		}
		return stringContentStyleSheet;
	}
	
	
	
	private static PrimitiveStyleSheet stringEscapeStyleSheet = null;
	
	private PrimitiveStyleSheet getStringEscapeStyleSheet()
	{
		if ( stringEscapeStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues contentAttrs = getNonNull( "stringContentAttrs", AttributeValues.class, AttributeValues.identity );
			AttributeValues escapeAttrs = getNonNull( "stringEscapeAttrs", AttributeValues.class, AttributeValues.identity );
			stringEscapeStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( contentAttrs ).withAttrValues( escapeAttrs );
		}
		return stringEscapeStyleSheet;
	}
	

	
	public DefaultPerspectiveStyleSheet withPrimitiveStyleSheet(PrimitiveStyleSheet styleSheet)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "primitiveStyleSheet", styleSheet );
	}
	
	
	public DefaultPerspectiveStyleSheet withPresentationSize(PresentationSize size)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "presentationSize", size );
	}
	
	
	public DefaultPerspectiveStyleSheet withObjectBorderThickness(double thickness)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectBorderThickness", thickness );
	}
	
	public DefaultPerspectiveStyleSheet withObjectBorderInset(double inset)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectBorderInset", inset );
	}
	
	public DefaultPerspectiveStyleSheet withObjectBorderRounding(double rounding)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectBorderRounding", rounding );
	}
	
	public DefaultPerspectiveStyleSheet withObjectBorderPaint(Paint paint)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectBorderPaint", paint );
	}
	
	public DefaultPerspectiveStyleSheet withObjectBorderBackground(Paint paint)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectBorderBackground", paint );
	}
	
	public DefaultPerspectiveStyleSheet withObjectTitlePaint(Paint paint)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectTitlePaint", paint );
	}
	
	public DefaultPerspectiveStyleSheet withObjectTitleAttrs(AttributeValues attrs)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectTitleAttrs", attrs );
	}
	
	public DefaultPerspectiveStyleSheet withObjectContentPadding(double padding)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectContentPadding", padding );
	}
	
	
	
	public DefaultPerspectiveStyleSheet withObjectFieldTitlePaint(Paint paint)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectFieldTitlePaint", paint );
	}
	
	public DefaultPerspectiveStyleSheet withObjectFieldIndentation(double indentation)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectFieldIndentation", indentation );
	}

	
	public DefaultPerspectiveStyleSheet withObjectFieldSpacing(double spacing)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "objectFieldSpacing", spacing );
	}
	
	
	public DefaultPerspectiveStyleSheet withStringContentAttrs(AttributeValues attrs)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "stringContentAttrs", attrs );
	}
	
	public DefaultPerspectiveStyleSheet withStringEscapeAttrs(AttributeValues attrs)
	{
		return (DefaultPerspectiveStyleSheet)withAttr( "stringEscapeAttrs", attrs );
	}
	


	public PresentationSize getPresentationSize()
	{
		return getNonNull( "presentationSize", PresentationSize.class, PresentationSize.FULL );
	}
	
	
	public DPElement objectBox(String title, DPElement contents)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		PrimitiveStyleSheet borderStyle = getObjectBorderStyleSheet();
		PrimitiveStyleSheet titleStyle = getObjectTitleStyleSheet();
		double padding = getNonNull( "objectContentPadding", Double.class, defaultObjectContentPadding );
		
		DPElement titleElement = titleStyle.staticText( title );
		DPElement contentsBox = primitive.layoutWrap( contents ).padX( padding );
		return borderStyle.border( primitive.vbox( new DPElement[] { titleElement, contentsBox } ) ); 
	}
	
	public DPElement objectBoxWithFields(String title, DPElement fields[])
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		PrimitiveStyleSheet borderStyle = getObjectBorderStyleSheet();
		PrimitiveStyleSheet titleStyle = getObjectTitleStyleSheet();
		PrimitiveStyleSheet fieldListStyle = getObjectFieldListStyleSheet();
		double padding = getNonNull( "objectContentPadding", Double.class, defaultObjectContentPadding );
		
		DPElement titleElement = titleStyle.staticText( title );
		DPElement contentsBox = fieldListStyle.vbox( fields ).padX( padding );
		return borderStyle.border( primitive.vbox( new DPElement[] { titleElement, contentsBox } ) ); 
	}
	
	public DPElement horizontalObjectField(String title, DPElement value)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		PrimitiveStyleSheet fieldStyle = getObjectFieldStyleSheet();
		return fieldStyle.paragraph( new DPElement[] { fieldStyle.staticText( title ), primitive.staticText( " " ), primitive.lineBreak(), value } );
	}
	
	public DPElement verticalObjectField(String title, DPElement value)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		PrimitiveStyleSheet titleStyle = getObjectFieldStyleSheet();
		double indentation = getNonNull( "objectFieldIndentation", Double.class, defaultObjectFieldIndentation );
		return primitive.vbox( new DPElement[] { titleStyle.staticText( title ), primitive.layoutWrap( value ).padX( indentation, 0.0 ) } );
	}
	
	
	public List<DPElement> unescapedStringAsElementList(String x)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		PrimitiveStyleSheet stringContentStyle = getStringContentStyleSheet();
		PrimitiveStyleSheet escapeStyle = getStringEscapeStyleSheet();

		ArrayList<DPElement> elements = new ArrayList<DPElement>();
		// Break the string up into escaped and not escaped items
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < x.length(); i++)
		{
			char c = x.charAt( i );
			
			DPElement escapeItem = null;

			// Process a list of known escape sequences
			if ( c == '\r' )
			{
				escapeItem = escapeStyle.staticText( "\\r" );
			}
			else if ( c == '\t' )
			{
				escapeItem = escapeStyle.staticText( "\\t" );
			}
			else if ( c == '\n' )
			{
				escapeItem = escapeStyle.staticText( "\\n" );
			}
			
			if ( escapeItem != null )
			{
				// We have an escape sequence item
				// First, add any non-escaped content to the element list, then add the escape item
				if ( builder.length() > 0 )
				{
					elements.add( stringContentStyle.staticText( builder.toString() ) );
					elements.add( primitive.lineBreak() );
					builder = new StringBuilder();
				}
				elements.add( escapeItem );
				elements.add( primitive.lineBreak() );
			}
			else
			{
				// Non-escaped character - add to the string
				builder.append( c );
			}
		}
		
		if ( builder.length() > 0 )
		{
			// Non-escaped content remains - create a text element and add
			elements.add( stringContentStyle.staticText( builder.toString() ) );
		}
		
		return elements;
	}

	public DPElement unescapedStringAsHBox(String x)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return primitive.hbox( unescapedStringAsElementList( x ).toArray( new DPElement[0] ) );
	}

	public DPElement unescapedStringAsParagraph(String x)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return primitive.paragraph( unescapedStringAsElementList( x ).toArray( new DPElement[0] ) );
	}

	public DPElement unescapedStringAsSpan(String x)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return primitive.span( unescapedStringAsElementList( x ).toArray( new DPElement[0] ) );
	}
}
