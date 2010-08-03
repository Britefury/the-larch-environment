//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import java.awt.Color;
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

class RichText
{
	public static final AttributeNamespace richTextNamespace = new AttributeNamespace( "richtext" );
	
	
	public static final InheritedAttributeNonNull pageSpacing = new InheritedAttributeNonNull( richTextNamespace, "pageSpacing", Double.class, 15.0 );
	public static final InheritedAttributeNonNull headSpacing = new InheritedAttributeNonNull( richTextNamespace, "headSpacing", Double.class, 1.0 );
	public static final InheritedAttributeNonNull bodySpacing = new InheritedAttributeNonNull( richTextNamespace, "bodySpacing", Double.class, 10.0 );
	public static final InheritedAttributeNonNull linkHeaderAttrs = new InheritedAttributeNonNull( richTextNamespace, "linkHeaderAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.hboxSpacing, 25.0 ).withAttr( Primitive.border, new FilledBorder( 5.0, 5.0, 5.0, 5.0, new Color( 184, 206, 203 ) ) ) );
	public static final InheritedAttributeNonNull linkHeaderPadding = new InheritedAttributeNonNull( richTextNamespace, "linkHeaderPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull titleTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "titleTextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.fontSize, 36 ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.textSmallCaps, true ) );
	public static final InheritedAttributeNonNull titlePadding = new InheritedAttributeNonNull( richTextNamespace, "titlePadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull titleBackground = new InheritedAttributeNonNull( richTextNamespace, "titleBackground", Paint.class, new Color( 232, 232, 232 ) );
	public static final InheritedAttributeNonNull titleBorderWidth = new InheritedAttributeNonNull( richTextNamespace, "titleBorderWidth", Double.class, 10.0 );
	public static final InheritedAttributeNonNull subtitleTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "titleTextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontFace, "Sans serif" ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.05f ) ) );
	public static final InheritedAttributeNonNull normalTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "normalTextAttrs", StyleSheet.class, StyleSheet.instance );
	public static final InheritedAttributeNonNull headingTextAttrs = new InheritedAttributeNonNull( richTextNamespace, "headingTextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontFace, "Serif" ) );
	public static final InheritedAttributeNonNull h1TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h1TextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontSize, 28 ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.foreground, new Color( 0.1f, 0.2f, 0.3f ) ) );
	public static final InheritedAttributeNonNull h2TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h2TextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontSize, 26 ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.foreground, new Color( 0.15f, 0.3f, 0.45f ) ) );
	public static final InheritedAttributeNonNull h3TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h3TextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontSize, 24 ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.foreground, new Color( 0.2f, 0.4f, 0.6f ) ) );
	public static final InheritedAttributeNonNull h4TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h4TextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontSize, 22 ).withAttr( Primitive.fontItalic, true ).withAttr( Primitive.foreground, new Color( 0.15f, 0.3f, 0.45f  ) ) );
	public static final InheritedAttributeNonNull h5TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h5TextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontSize, 18 ).withAttr( Primitive.fontItalic, true ).withAttr( Primitive.foreground, new Color( 0.2f, 0.4f, 0.6f ) ) );
	public static final InheritedAttributeNonNull h6TextAttrs = new InheritedAttributeNonNull( richTextNamespace, "h6TextAttrs", StyleSheet.class,
			StyleSheet.instance.withAttr( Primitive.fontSize, 16 ).withAttr( Primitive.fontItalic, true ).withAttr( Primitive.foreground, Color.black ) );
	public static final InheritedAttributeNonNull separatorPainter = new InheritedAttributeNonNull( richTextNamespace, "separatorPainter", Painter.class, new FillPainter( new Color( 32, 87, 147 ) ) );
	public static final InheritedAttributeNonNull separatorMajorPadding = new InheritedAttributeNonNull( richTextNamespace, "separatorMajorPadding", Double.class, 15.0 );
	public static final InheritedAttributeNonNull separatorMinorPadding = new InheritedAttributeNonNull( richTextNamespace, "separatorMinorPadding", Double.class, 3.0 );

	
	public static final InheritedAttributeNonNull appendNewlineToParagraphs = new InheritedAttributeNonNull( richTextNamespace, "appendNewlineToParagraphs", Boolean.class, false );
	
	
	public static StyleSheet pageStyle(StyleValues style)
	{
		return StyleSheet.instance.withAttrFrom( Primitive.vboxSpacing, style, pageSpacing );
	}

	public static StyleValues usePageAttrs(StyleValues style)
	{
		return style.useAttr( pageSpacing );
	}

	

	public static StyleSheet headStyle(StyleValues style)
	{
		return StyleSheet.instance.withAttrFrom( Primitive.vboxSpacing, style, headSpacing );
	}
	
	public static StyleValues useHeadAttrs(StyleValues style)
	{
		return style.useAttr( headSpacing );
	}

	

	public static StyleSheet bodyStyle(StyleValues style)
	{
		return StyleSheet.instance.withAttrFrom( Primitive.vboxSpacing, style, bodySpacing );
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
			return StyleSheet.instance.withAttrs( attribs.get( titleTextAttrs, StyleSheet.class ) ).withAttr( Primitive.border, new FilledBorder( pad, pad, pad, pad, background ) );
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
}
