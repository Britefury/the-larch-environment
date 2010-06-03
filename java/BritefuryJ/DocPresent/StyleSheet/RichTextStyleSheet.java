//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeValues;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.FilledBorder;


public class RichTextStyleSheet extends StyleSheet
{
	private static final double defaultPageSpacing = 10.0;
	private static final AttributeValues defaultLinkHeaderAttrs = new AttributeValues( new String[] { "hboxSpacing", "border" }, new Object[] { 25.0, new FilledBorder( 10.0, 10.0, 5.0, 1.0, null ) } );
	private static final AttributeValues defaultTitleTextAttrs = new AttributeValues( new String[] { "fontFace", "fontSize", "fontBold" }, new Object[] { "Serif", 36, true } );
	private static final Paint defaultTitleBackground = new Color( 232, 232, 232 );
	private static final double defaultTitlePadding = 5.0;
	private static final double defaultTitleBorderWidth = 5.0;
	private static final AttributeValues defaultSubtitleTextAttrs = new AttributeValues( new String[] { "fontFace", "fontSize", "foreground" }, new Object[] { "Sans serif", 14, new Color( 0.0f, 0.5f, 0.05f ) } );
	private static final AttributeValues defaultParagraphAttrs = new AttributeValues();
	private static final AttributeValues defaultHeaderAttrs = new AttributeValues( new String[] { "fontFace" }, new Object[] { "Serif" } );
	private static final AttributeValues defaultH1Attrs = new AttributeValues( new String[] { "fontSize", "foreground", "fontBold", "textSmallCaps" }, new Object[] { 28, new Color( 0.1f, 0.2f, 0.3f ), true, true } );
	private static final AttributeValues defaultH2Attrs = new AttributeValues( new String[] { "fontSize", "foreground", "fontBold" }, new Object[] { 26, new Color( 0.15f, 0.3f, 0.45f ), true } );
	private static final AttributeValues defaultH3Attrs = new AttributeValues( new String[] { "fontSize", "foreground", "fontBold" }, new Object[] { 24, new Color( 0.2f, 0.4f, 0.6f ), true } );
	private static final AttributeValues defaultH4Attrs = new AttributeValues( new String[] { "fontSize", "foreground", "fontItalic" }, new Object[] { 22, new Color( 0.15f, 0.3f, 0.45f  ), true } );
	private static final AttributeValues defaultH5Attrs = new AttributeValues( new String[] { "fontSize", "foreground", "fontItalic" }, new Object[] { 18, new Color( 0.2f, 0.4f, 0.6f ), true } );
	private static final AttributeValues defaultH6Attrs = new AttributeValues( new String[] { "fontSize", "foreground", "fontItalic" }, new Object[] { 16, Color.black, true } );

	
	public static final RichTextStyleSheet instance = new RichTextStyleSheet();
	
	
	
	public RichTextStyleSheet()
	{
		super();
		
		initAttr( "primitiveStyleSheet", PrimitiveStyleSheet.instance.withNonEditable() );
		
		
		initAttr( "pageSpacing", defaultPageSpacing );
		initAttr( "linkHeaderAttrs", defaultLinkHeaderAttrs );
		initAttr( "titleTextAttrs", defaultTitleTextAttrs );
		initAttr( "titleBackground", defaultTitleBackground );
		initAttr( "titlePadding", defaultTitlePadding );
		initAttr( "titleBorderWidth", defaultTitleBorderWidth );
		initAttr( "subtitleTextAttrs", defaultSubtitleTextAttrs );
		initAttr( "paragraphAttrs", defaultParagraphAttrs );
		initAttr( "headerAttrs", defaultHeaderAttrs );
		initAttr( "h1Attrs", defaultH1Attrs );
		initAttr( "h2Attrs", defaultH2Attrs );
		initAttr( "h3Attrs", defaultH3Attrs );
		initAttr( "h4Attrs", defaultH4Attrs );
		initAttr( "h5Attrs", defaultH5Attrs );
		initAttr( "h6Attrs", defaultH6Attrs );
	}



	protected StyleSheet newInstance()
	{
		return new RichTextStyleSheet();
	}
	

	
	public RichTextStyleSheet withPrimitiveStyleSheet(PrimitiveStyleSheet styleSheet)
	{
		return (RichTextStyleSheet)withAttr( "primitiveStyleSheet", styleSheet );
	}



	public RichTextStyleSheet withPageSpacing(double spacing)
	{
		return (RichTextStyleSheet)withAttr( "pageSpacing", spacing );
	}

	public RichTextStyleSheet withLinkHeaderAttrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "linkHeaderAttrs", attrs );
	}

	public RichTextStyleSheet withTitleTextAttrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "titleTextAttrs", attrs );
	}

	public RichTextStyleSheet withTitleBackground(Paint background)
	{
		return (RichTextStyleSheet)withAttr( "titleBackground", background );
	}

	public RichTextStyleSheet withTitlePadding(double padding)
	{
		return (RichTextStyleSheet)withAttr( "titlePadding", padding );
	}

	public RichTextStyleSheet withTitleBorderWidth(double width)
	{
		return (RichTextStyleSheet)withAttr( "titleBorderWidth", width );
	}

	public RichTextStyleSheet withSubtitleTextAttrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "subtitleTextAttrs", attrs );
	}

	public RichTextStyleSheet withHeaderAttrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "headerAttrs", attrs );
	}

	public RichTextStyleSheet withH1Attrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "h1Attrs", attrs );
	}

	public RichTextStyleSheet withH2Attrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "h2Attrs", attrs );
	}

	public RichTextStyleSheet withH3Attrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "h3Attrs", attrs );
	}

	public RichTextStyleSheet withH4Attrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "h4Attrs", attrs );
	}

	public RichTextStyleSheet withH5Attrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "h5Attrs", attrs );
	}

	public RichTextStyleSheet withH6Attrs(AttributeValues attrs)
	{
		return (RichTextStyleSheet)withAttr( "h6Attrs", attrs );
	}
	
	
	
	public RichTextStyleSheet withEditable()
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return withPrimitiveStyleSheet( primitive.withEditable() );
	}

	public RichTextStyleSheet withNonEditable()
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return withPrimitiveStyleSheet( primitive.withNonEditable() );
	}

	public RichTextStyleSheet withEditability(boolean bEditable)
	{
		PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
		return withPrimitiveStyleSheet( primitive.withEditability( bEditable ) );
	}




	private PrimitiveStyleSheet pageStyleSheet = null;

	private PrimitiveStyleSheet getPageStyleSheet()
	{
		if ( pageStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			double spacing = getNonNull( "pageSpacing", Double.class, defaultPageSpacing );
			pageStyleSheet = (PrimitiveStyleSheet)primitive.withVBoxSpacing( spacing );
		}
		return pageStyleSheet;
	}

	
	
	
	private PrimitiveStyleSheet linkHeaderStyleSheet = null;

	private PrimitiveStyleSheet getLinkHeaderStyleSheet()
	{
		if ( linkHeaderStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues linkHeaderAttrs = getNonNull( "linkHeaderAttrs", AttributeValues.class, defaultLinkHeaderAttrs );
			linkHeaderStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( linkHeaderAttrs );
		}
		return linkHeaderStyleSheet;
	}

	
	
	
	private PrimitiveStyleSheet titleStyleSheet = null;

	private PrimitiveStyleSheet getTitleStyleSheet()
	{
		if ( titleStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues titleTextAttrs = getNonNull( "titleTextAttrs", AttributeValues.class, defaultTitleTextAttrs );
			double pad = getNonNull( "titlePadding", Double.class, defaultTitlePadding );
			Paint titleBackground = get( "titleBackground", Paint.class, defaultTitleBackground );
			titleStyleSheet = (PrimitiveStyleSheet)((PrimitiveStyleSheet)primitive.withAttrValues( titleTextAttrs )).withBorder( new FilledBorder( pad, pad, pad, pad, titleBackground ) );
		}
		return titleStyleSheet;
	}

	
	
	
	private PrimitiveStyleSheet subtitleStyleSheet = null;

	private PrimitiveStyleSheet getSubtitleStyleSheet()
	{
		if ( subtitleStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues subtitleTextAttrs = getNonNull( "subtitleTextAttrs", AttributeValues.class, defaultSubtitleTextAttrs );
			subtitleStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( subtitleTextAttrs );
		}
		return subtitleStyleSheet;
	}

	
	
	
	private PrimitiveStyleSheet paragraphStyleSheet = null;

	private PrimitiveStyleSheet getParagraphStyleSheet()
	{
		if ( paragraphStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues parargraphAttrs = getNonNull( "paragraphAttrs", AttributeValues.class, defaultParagraphAttrs );
			paragraphStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( parargraphAttrs );
		}
		return paragraphStyleSheet;
	}

	
	
	
	private PrimitiveStyleSheet headerStyleSheet = null;

	private PrimitiveStyleSheet getHeaderStyleSheet()
	{
		if ( headerStyleSheet == null )
		{
			PrimitiveStyleSheet primitive = getNonNull( "primitiveStyleSheet", PrimitiveStyleSheet.class, PrimitiveStyleSheet.instance );
			AttributeValues headerAttrs = getNonNull( "headerAttrs", AttributeValues.class, defaultHeaderAttrs );
			headerStyleSheet = (PrimitiveStyleSheet)primitive.withAttrValues( headerAttrs );
		}
		return headerStyleSheet;
	}


	
	private PrimitiveStyleSheet h1StyleSheet = null;

	private PrimitiveStyleSheet getH1StyleSheet()
	{
		if ( h1StyleSheet == null )
		{
			PrimitiveStyleSheet header = getHeaderStyleSheet();
			AttributeValues h1Attrs = getNonNull( "h1Attrs", AttributeValues.class, defaultH1Attrs );
			h1StyleSheet = (PrimitiveStyleSheet)header.withAttrValues( h1Attrs );
		}
		return h1StyleSheet;
	}

	
	
	private PrimitiveStyleSheet h2StyleSheet = null;

	private PrimitiveStyleSheet getH2StyleSheet()
	{
		if ( h2StyleSheet == null )
		{
			PrimitiveStyleSheet header = getHeaderStyleSheet();
			AttributeValues h2Attrs = getNonNull( "h2Attrs", AttributeValues.class, defaultH2Attrs );
			h2StyleSheet = (PrimitiveStyleSheet)header.withAttrValues( h2Attrs );
		}
		return h2StyleSheet;
	}

	
	
	private PrimitiveStyleSheet h3StyleSheet = null;

	private PrimitiveStyleSheet getH3StyleSheet()
	{
		if ( h3StyleSheet == null )
		{
			PrimitiveStyleSheet header = getHeaderStyleSheet();
			AttributeValues h3Attrs = getNonNull( "h3Attrs", AttributeValues.class, defaultH3Attrs );
			h3StyleSheet = (PrimitiveStyleSheet)header.withAttrValues( h3Attrs );
		}
		return h3StyleSheet;
	}

	
	
	private PrimitiveStyleSheet h4StyleSheet = null;

	private PrimitiveStyleSheet getH4StyleSheet()
	{
		if ( h4StyleSheet == null )
		{
			PrimitiveStyleSheet header = getHeaderStyleSheet();
			AttributeValues h4Attrs = getNonNull( "h4Attrs", AttributeValues.class, defaultH4Attrs );
			h4StyleSheet = (PrimitiveStyleSheet)header.withAttrValues( h4Attrs );
		}
		return h4StyleSheet;
	}

	
	
	private PrimitiveStyleSheet h5StyleSheet = null;

	private PrimitiveStyleSheet getH5StyleSheet()
	{
		if ( h5StyleSheet == null )
		{
			PrimitiveStyleSheet header = getHeaderStyleSheet();
			AttributeValues h5Attrs = getNonNull( "h5Attrs", AttributeValues.class, defaultH5Attrs );
			h5StyleSheet = (PrimitiveStyleSheet)header.withAttrValues( h5Attrs );
		}
		return h5StyleSheet;
	}

	
	
	private PrimitiveStyleSheet h6StyleSheet = null;

	private PrimitiveStyleSheet getH6StyleSheet()
	{
		if ( h6StyleSheet == null )
		{
			PrimitiveStyleSheet header = getHeaderStyleSheet();
			AttributeValues h6Attrs = getNonNull( "h6Attrs", AttributeValues.class, defaultH6Attrs );
			h6StyleSheet = (PrimitiveStyleSheet)header.withAttrValues( h6Attrs );
		}
		return h6StyleSheet;
	}

	
	
	
	
	private ArrayList<DPElement> textToWordsAndLineBreaks(PrimitiveStyleSheet primitive, String text)
	{
		ArrayList<DPElement> elements = new ArrayList<DPElement>();

		boolean bGotChars = false, bGotTrailingSpace = false;
		int wordStartIndex = 0;
		
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt( i );
			if ( c == ' ' )
			{
				if ( bGotChars )
				{
					bGotTrailingSpace = true;
				}
			}
			else
			{
				if ( bGotTrailingSpace )
				{
					// Make text element for word
					String word = text.substring( wordStartIndex, i );
					elements.add( primitive.text( word ) );
					elements.add( primitive.lineBreak() );
					
					// Begin new word
					bGotChars = bGotTrailingSpace = false;
					wordStartIndex = i;
				}
				else
				{
					bGotChars = true;
				}
			}
		}
		
		if ( wordStartIndex < text.length() )
		{
			String word = text.substring( wordStartIndex );
			elements.add( primitive.text( word ) );
			elements.add( primitive.lineBreak() );
		}
		return elements;
	}
	
	private DPElement textParagraph(PrimitiveStyleSheet primitive, String text)
	{
		if ( text.equals(  "" ) )
		{
			return primitive.paragraph( new DPElement[] { primitive.text( "" ) } );
		}
		else
		{
			return primitive.paragraph( textToWordsAndLineBreaks( primitive, text ) );
		}
	}
	
	
	
	public DPElement page(DPElement contents[])
	{
		return page( Arrays.asList( contents ) );
	}
	
	public DPElement page(List<DPElement> contents)
	{
		return getPageStyleSheet().vbox( contents ).alignHExpand();
	}
	
	
	
	public DPElement linkHeaderBar(DPElement links[])
	{
		return linkHeaderBar( Arrays.asList( links ) );
	}
	
	public DPElement linkHeaderBar(List<DPElement> links)
	{
		PrimitiveStyleSheet linkHeaderStyle = getLinkHeaderStyleSheet();
		return linkHeaderStyle.border( linkHeaderStyle.hbox( links ).alignHRight() ).alignHExpand();
	}
	
	
	
	public DPElement title(String text)
	{
		return textParagraph( getTitleStyleSheet(), text );
	}

	public DPElement titleBar(String text)
	{
		PrimitiveStyleSheet titleStyle = getTitleStyleSheet();
		double borderWidth = getNonNull( "titleBorderWidth", Double.class, defaultTitleBorderWidth );
		DPElement title = textParagraph( titleStyle, text );
		DPElement titleBackground = titleStyle.border( title.alignHCentre() );
		return titleBackground.alignHExpand().pad( borderWidth, borderWidth ).alignHExpand();
	}

	public DPElement titleBarWithSubtitle(String text, String subtitleText)
	{
		PrimitiveStyleSheet titleStyle = getTitleStyleSheet();
		PrimitiveStyleSheet subtitleStyle = getSubtitleStyleSheet();
		double borderWidth = getNonNull( "titleBorderWidth", Double.class, defaultTitleBorderWidth );
		DPElement title = textParagraph( titleStyle, text );
		DPElement subtitle = textParagraph( subtitleStyle, subtitleText );
		DPElement titleVBox = titleStyle.vbox( new DPElement[] { title.alignHCentre(), subtitle.alignHCentre() } );
		DPElement titleBackground = titleStyle.border( titleVBox.alignHCentre() );
		return titleBackground.alignHExpand().pad( borderWidth, borderWidth ).alignHExpand();
	}

	public DPElement paragraph(String text)
	{
		return textParagraph( getParagraphStyleSheet(), text );
	}

	public DPElement h1(String text)
	{
		return textParagraph( getH1StyleSheet(), text );
	}

	public DPElement h2(String text)
	{
		return textParagraph( getH2StyleSheet(), text );
	}

	public DPElement h3(String text)
	{
		return textParagraph( getH3StyleSheet(), text );
	}

	public DPElement h4(String text)
	{
		return textParagraph( getH4StyleSheet(), text );
	}

	public DPElement h5(String text)
	{
		return textParagraph( getH5StyleSheet(), text );
	}

	public DPElement h6(String text)
	{
		return textParagraph( getH6StyleSheet(), text );
	}
}
