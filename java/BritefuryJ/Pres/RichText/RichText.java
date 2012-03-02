//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.RichText;

import java.awt.Color;
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.FilledBorder;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class RichText
{
	// Blue theme
	private static final Color blueH1Colour = new Color( 0.1f, 0.2f, 0.3f );
	private static final Color blueH2Colour = new Color( 0.15f, 0.3f, 0.45f );
	private static final Color blueH3Colour = new Color( 0.2f, 0.4f, 0.6f );
	private static final Color blueH4Colour = new Color( 0.15f, 0.3f, 0.45f );
	private static final Color blueH5Colour = new Color( 0.2f, 0.4f, 0.6f );
	private static final Color blueH6Colour = Color.BLACK;
	
	// Green theme
	private static final Color greenH1Colour = new Color( 109, 140, 91 );
	private static final Color greenH2Colour = new Color( 85, 115, 69 );
	private static final Color greenH3Colour = new Color( 64, 89, 49 );
	private static final Color greenH4Colour = new Color( 99, 128, 83 );
	private static final Color greenH5Colour = new Color( 76, 102, 61 );
	private static final Color greenH6Colour = new Color( 55, 76, 42 );
	
	// Burgundy theme
	private static final Color burgundyH1Colour = new Color( 115, 42, 96 );
	private static final Color burgundyH2Colour = new Color( 89, 29, 73 );
	private static final Color burgundyH3Colour = new Color( 64, 17, 52 );
	private static final Color burgundyH4Colour = new Color( 102, 37, 85 );
	private static final Color burgundyH5Colour = new Color( 76, 24, 63 );
	private static final Color burgundyH6Colour = new Color( 51, 14, 41 );
	
	
	public static final AttributeNamespace richTextNamespace = new AttributeNamespace( "richtext" );
	
	public static final InheritedAttributeNonNull ruleThickness = new InheritedAttributeNonNull( richTextNamespace, "ruleThickness", Double.class, 2.0 ); 
	public static final InheritedAttributeNonNull ruleInset = new InheritedAttributeNonNull( richTextNamespace, "ruleInset", Double.class, 15.0 ); 
	public static final InheritedAttributeNonNull rulePadding = new InheritedAttributeNonNull( richTextNamespace, "rulePadding", Double.class, 3.0 ); 
	public static final InheritedAttributeNonNull rulePaint = new InheritedAttributeNonNull( richTextNamespace, "rulePaint", Paint.class, new Color( 0.5f, 0.5f, 0.5f ) ); 

	public static final InheritedAttributeNonNull pageSpacing = new InheritedAttributeNonNull( richTextNamespace, "pageSpacing", Double.class, 15.0 );
	public static final InheritedAttributeNonNull headSpacing = new InheritedAttributeNonNull( richTextNamespace, "headSpacing", Double.class, 1.0 );
	public static final InheritedAttributeNonNull bodySpacing = new InheritedAttributeNonNull( richTextNamespace, "bodySpacing", Double.class, 10.0 );
	public static final InheritedAttributeNonNull linkHeaderAttrs = new InheritedAttributeNonNull( richTextNamespace, "linkHeaderAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.rowSpacing.as( 25.0 ), ruleInset.as( 0.0 ), rulePaint.as( new Color( 0.65f, 0.65f, 0.65f ) ), ruleThickness.as( 1.0 ), rulePadding.as( 0.0 ),
				    Primitive.background.as( new FillPainter( new Color( 237, 237, 237 ) ) ) ) );
	public static final InheritedAttributeNonNull linkHeaderPadding = new InheritedAttributeNonNull( richTextNamespace, "linkHeaderPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull titleTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "titleTextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontFace.as( "Lucida Bright; Serif" ), Primitive.fontSize.as( 36 ) ) );
	public static final InheritedAttributeNonNull titlePadding = new InheritedAttributeNonNull( richTextNamespace, "titlePadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull titleBackground = new InheritedAttributeNonNull( richTextNamespace, "titleBackground", Paint.class, new Color( 232, 232, 232 ) );
	public static final InheritedAttributeNonNull titleBorderWidth = new InheritedAttributeNonNull( richTextNamespace, "titleBorderWidth", Double.class, 10.0 );
	public static final InheritedAttributeNonNull subtitleTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "subtitleTextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontFace.as( "DejaVu Sans; SansSerif" ), Primitive.fontSize.as( 14 ), Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.05f ) ) ) );
	public static final InheritedAttributeNonNull normalTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "normalTextAttrs", StyleSheet.class, StyleSheet.instance );
	public static final InheritedAttributeNonNull captionTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "captionTextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 10 ), Primitive.fontBold.as( true ), Primitive.foreground.as( new Color( 0.25f, 0.5f, 0.75f ) ) ) );
	public static final InheritedAttributeNonNull headingTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "headingTextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontFace.as( "Perpetua; Serif" ) ) );
	public static final InheritedAttributeNonNull h1TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h1TextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 28 ), Primitive.fontBold.as( true ), Primitive.foreground.as( blueH1Colour ) ) );
	public static final InheritedAttributeNonNull h2TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h2TextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 26 ), Primitive.fontBold.as( true ), Primitive.foreground.as( blueH2Colour ) ) );
	public static final InheritedAttributeNonNull h3TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h3TextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 24 ), Primitive.fontBold.as( true ), Primitive.foreground.as( blueH3Colour) ) );
	public static final InheritedAttributeNonNull h4TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h4TextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 22 ), Primitive.fontItalic.as( true ), Primitive.foreground.as( blueH4Colour ) ) );
	public static final InheritedAttributeNonNull h5TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h5TextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 18 ), Primitive.fontItalic.as( true ), Primitive.foreground.as( blueH5Colour ) ) );
	public static final InheritedAttributeNonNull h6TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h6TextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 16 ), Primitive.fontItalic.as( true ), Primitive.foreground.as( blueH6Colour ) ) );
	public static final InheritedAttributeNonNull separatorPainter = new InheritedAttributeNonNull( richTextNamespace, "separatorPainter", Painter.class, new FillPainter( new Color( 32, 87, 147 ) ) );
	public static final InheritedAttributeNonNull separatorMajorPadding = new InheritedAttributeNonNull( richTextNamespace, "separatorMajorPadding", Double.class, 15.0 );
	public static final InheritedAttributeNonNull separatorMinorPadding = new InheritedAttributeNonNull( richTextNamespace, "separatorMinorPadding", Double.class, 3.0 );
	public static final InheritedAttribute figureBorder = new InheritedAttribute( richTextNamespace, "figureBorder", AbstractBorder.class, new SolidBorder( 1.0, 2.0, Color.black, null ) );
	
	public static final InheritedAttributeNonNull appendNewlineToParagraphs = new InheritedAttributeNonNull( richTextNamespace, "appendNewlineToParagraphs", Boolean.class, false );
	
	
	public static StyleSheet pageStyle(StyleValues style)
	{
		return StyleSheet.instance.withAttrFrom( Primitive.columnSpacing, style, pageSpacing );
	}

	public static StyleValues usePageAttrs(StyleValues style)
	{
		return style.useAttr( pageSpacing );
	}

	

	public static StyleSheet headStyle(StyleValues style)
	{
		return StyleSheet.instance.withAttrFrom( Primitive.columnSpacing, style, headSpacing );
	}
	
	public static StyleValues useHeadAttrs(StyleValues style)
	{
		return style.useAttr( headSpacing );
	}

	

	public static StyleSheet bodyStyle(StyleValues style)
	{
		return StyleSheet.instance.withAttrFrom( Primitive.columnSpacing, style, bodySpacing );
	}
	
	public static StyleValues useBodyAttrs(StyleValues style)
	{
		return style.useAttr( bodySpacing );
	}

	

	public static StyleSheet linkHeaderStyle(StyleValues style)
	{
		return style.get( linkHeaderAttrs, StyleSheet.class );
	}
	
	public static StyleValues useLinkHeaderAttrs(StyleValues style)
	{
		return style.useAttr( linkHeaderAttrs );
	}

	

	
	protected static DerivedValueTable<StyleSheet> titleStyle = new DerivedValueTable<StyleSheet>( richTextNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			double pad = attribs.get( titlePadding, Double.class );
			Paint background = attribs.get( titleBackground, Paint.class );
			return StyleSheet.instance.withAttrs( attribs.get( titleTextAttrs, StyleSheet.class ) ).withValues( Primitive.border.as( new FilledBorder( pad, pad, pad, pad, background ) ) );
		}
	};
	
	public static StyleValues useTitleAttrs(StyleValues style)
	{
		return style.useAttr( titleTextAttrs );
	}

	

	public static StyleSheet titleTextStyle(StyleValues style)
	{
		return style.get( titleTextAttrs, StyleSheet.class );
	}
	
	public static StyleValues useTitleTextAttrs(StyleValues style)
	{
		return style.useAttr( titleTextAttrs );
	}

	

	public static StyleSheet subtitleTextStyle(StyleValues style)
	{
		return style.get( subtitleTextAttrs, StyleSheet.class );
	}
	
	public static StyleValues useSubtitleTextAttrs(StyleValues style)
	{
		return style.useAttr( subtitleTextAttrs );
	}

	

	public static StyleSheet normalTextStyle(StyleValues style)
	{
		return style.get( normalTextAttrs, StyleSheet.class );
	}
	
	public static StyleValues useNormalTextAttrs(StyleValues style)
	{
		return style.useAttr( normalTextAttrs );
	}

	

	public static StyleSheet captionTextStyle(StyleValues style)
	{
		return style.get( captionTextAttrs, StyleSheet.class );
	}
	
	public static StyleValues useCaptionTextAttrs(StyleValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( captionTextAttrs );
	}

	

	public static StyleSheet h1TextStyle(StyleValues style)
	{
		return style.get( headingTextAttrs, StyleSheet.class ).withAttrs( style.get( h1TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH1TextAttrs(StyleValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h1TextAttrs );
	}

	

	public static StyleSheet h2TextStyle(StyleValues style)
	{
		return style.get( headingTextAttrs, StyleSheet.class ).withAttrs( style.get( h2TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH2TextAttrs(StyleValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h2TextAttrs );
	}

	

	public static StyleSheet h3TextStyle(StyleValues style)
	{
		return style.get( headingTextAttrs, StyleSheet.class ).withAttrs( style.get( h3TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH3TextAttrs(StyleValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h3TextAttrs );
	}

	

	public static StyleSheet h4TextStyle(StyleValues style)
	{
		return style.get( headingTextAttrs, StyleSheet.class ).withAttrs( style.get( h4TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH4TextAttrs(StyleValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h4TextAttrs );
	}

	

	public static StyleSheet h5TextStyle(StyleValues style)
	{
		return style.get( headingTextAttrs, StyleSheet.class ).withAttrs( style.get( h5TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH5TextAttrs(StyleValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h5TextAttrs );
	}

	

	public static StyleSheet h6TextStyle(StyleValues style)
	{
		return style.get( headingTextAttrs, StyleSheet.class ).withAttrs( style.get( h6TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH6TextAttrs(StyleValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h6TextAttrs );
	}



	public static StyleSheet separatorStyle(StyleValues style)
	{
		return StyleSheet.instance.withAttrFrom( Primitive.shapePainter, style, separatorPainter );
	}
	
	public static StyleValues useSeparatorAttrs(StyleValues style)
	{
		return style.useAttr( separatorPainter );
	}



	public static StyleSheet figureStyle(StyleValues style)
	{
		return StyleSheet.instance.withAttrFrom( Primitive.border, style, figureBorder );
	}
	
	public static StyleValues useFigureAttrs(StyleValues style)
	{
		return style.useAttr( figureBorder );
	}
}
