//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import java.awt.Color;
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeTable2;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

class RichText
{
	public static final InheritedAttributeNonNull pageSpacing = new InheritedAttributeNonNull( "richtext", "pageSpacing", Double.class, 15.0 );
	public static final InheritedAttributeNonNull bodySpacing = new InheritedAttributeNonNull( "richtext", "bodySpacing", Double.class, 10.0 );
	public static final InheritedAttributeNonNull linkHeaderAttrs = new InheritedAttributeNonNull( "richtext", "linkHeaderAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.hboxSpacing, 25.0 ).withAttr( Primitive.border, new FilledBorder( 10.0, 10.0, 5.0, 1.0, null ) ) );
	public static final InheritedAttributeNonNull titleTextAttrs = new InheritedAttributeNonNull( "richtext", "titleTextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.fontSize, 36 ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.textSmallCaps, true ) );
	public static final InheritedAttributeNonNull titlePadding = new InheritedAttributeNonNull( "richtext", "titlePadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull titleBackground = new InheritedAttributeNonNull( "richtext", "titleBackground", Paint.class, new Color( 232, 232, 232 ) );
	public static final InheritedAttributeNonNull titleBorderWidth = new InheritedAttributeNonNull( "richtext", "titleBorderWidth", Double.class, 10.0 );
	public static final InheritedAttributeNonNull subtitleTextAttrs = new InheritedAttributeNonNull( "richtext", "titleTextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontFace, "Sans serif" ).withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.05f ) ) );
	public static final InheritedAttributeNonNull normalTextAttrs = new InheritedAttributeNonNull( "richtext", "normalTextAttrs", StyleSheet2.class, StyleSheet2.instance );
	public static final InheritedAttributeNonNull headingTextAttrs = new InheritedAttributeNonNull( "richtext", "headingTextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontFace, "Serif" ) );
	public static final InheritedAttributeNonNull h1TextAttrs = new InheritedAttributeNonNull( "richtext", "h1TextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontSize, 28 ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.foreground, new Color( 0.1f, 0.2f, 0.3f ) ) );
	public static final InheritedAttributeNonNull h2TextAttrs = new InheritedAttributeNonNull( "richtext", "h2TextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontSize, 26 ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.foreground, new Color( 0.15f, 0.3f, 0.45f ) ) );
	public static final InheritedAttributeNonNull h3TextAttrs = new InheritedAttributeNonNull( "richtext", "h3TextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontSize, 24 ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.foreground, new Color( 0.2f, 0.4f, 0.6f ) ) );
	public static final InheritedAttributeNonNull h4TextAttrs = new InheritedAttributeNonNull( "richtext", "h4TextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontSize, 22 ).withAttr( Primitive.fontItalic, true ).withAttr( Primitive.foreground, new Color( 0.15f, 0.3f, 0.45f  ) ) );
	public static final InheritedAttributeNonNull h5TextAttrs = new InheritedAttributeNonNull( "richtext", "h5TextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontSize, 18 ).withAttr( Primitive.fontItalic, true ).withAttr( Primitive.foreground, new Color( 0.2f, 0.4f, 0.6f ) ) );
	public static final InheritedAttributeNonNull h6TextAttrs = new InheritedAttributeNonNull( "richtext", "h6TextAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontSize, 16 ).withAttr( Primitive.fontItalic, true ).withAttr( Primitive.foreground, Color.black ) );
	public static final InheritedAttributeNonNull separatorPainter = new InheritedAttributeNonNull( "richtext", "separatorPainter", Painter.class, new FillPainter( new Color( 32, 87, 147 ) ) );
	public static final InheritedAttributeNonNull separatorMajorPadding = new InheritedAttributeNonNull( "richtext", "separatorMajorPadding", Double.class, 15.0 );
	public static final InheritedAttributeNonNull separatorMinorPadding = new InheritedAttributeNonNull( "richtext", "separatorMinorPadding", Double.class, 3.0 );

	
	public static final InheritedAttributeNonNull appendNewlineToParagraphs = new InheritedAttributeNonNull( "richtext", "appendNewlineToParagraphs", Boolean.class, false );
	
	
	public static StyleSheetValues pageStyle(StyleSheetValues style)
	{
		return style.remapAttr( Primitive.vboxSpacing, pageSpacing );
	}

	public static StyleSheetValues usePageAttrs(StyleSheetValues style)
	{
		return style.useAttr( pageSpacing );
	}

	

	public static StyleSheetValues bodyStyle(StyleSheetValues style)
	{
		return style.remapAttr( Primitive.vboxSpacing, bodySpacing );
	}
	
	public static StyleSheetValues useBodyAttrs(StyleSheetValues style)
	{
		return style.useAttr( bodySpacing );
	}

	

	public static StyleSheetValues linkHeaderStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( linkHeaderAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useLinkHeaderAttrs(StyleSheetValues style)
	{
		return style.useAttr( linkHeaderAttrs );
	}

	

	
	protected static DerivedValueTable<StyleSheetValues> titleStyle = new DerivedValueTable<StyleSheetValues>()
	{
		protected StyleSheetValues evaluate(AttributeTable2 attribs)
		{
			double pad = attribs.get( titlePadding, Double.class );
			Paint background = attribs.get( titleBackground, Paint.class );
			return (StyleSheetValues)attribs.withAttrs( attribs.get( titleTextAttrs, StyleSheet2.class ) ).withAttr( Primitive.border, new FilledBorder( pad, pad, pad, pad, background ) );
		}
	};
	
	public static StyleSheetValues useTitleAttrs(StyleSheetValues style)
	{
		return style.useAttr( titleTextAttrs );
	}

	

	public static StyleSheetValues titleTextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( titleTextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useTitleTextAttrs(StyleSheetValues style)
	{
		return style.useAttr( titleTextAttrs );
	}

	

	public static StyleSheetValues subtitleTextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( subtitleTextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useSubtitleTextAttrs(StyleSheetValues style)
	{
		return style.useAttr( subtitleTextAttrs );
	}

	

	public static StyleSheetValues normalTextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( normalTextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useNormalTextAttrs(StyleSheetValues style)
	{
		return style.useAttr( normalTextAttrs );
	}

	

	public static StyleSheetValues h1TextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( headingTextAttrs, StyleSheet2.class ) ).withAttrs( style.get( h1TextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useH1TextAttrs(StyleSheetValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h1TextAttrs );
	}

	

	public static StyleSheetValues h2TextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( headingTextAttrs, StyleSheet2.class ) ).withAttrs( style.get( h2TextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useH2TextAttrs(StyleSheetValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h2TextAttrs );
	}

	

	public static StyleSheetValues h3TextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( headingTextAttrs, StyleSheet2.class ) ).withAttrs( style.get( h3TextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useH3TextAttrs(StyleSheetValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h3TextAttrs );
	}

	

	public static StyleSheetValues h4TextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( headingTextAttrs, StyleSheet2.class ) ).withAttrs( style.get( h4TextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useH4TextAttrs(StyleSheetValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h4TextAttrs );
	}

	

	public static StyleSheetValues h5TextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( headingTextAttrs, StyleSheet2.class ) ).withAttrs( style.get( h5TextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useH5TextAttrs(StyleSheetValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h5TextAttrs );
	}

	

	public static StyleSheetValues h6TextStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( headingTextAttrs, StyleSheet2.class ) ).withAttrs( style.get( h6TextAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useH6TextAttrs(StyleSheetValues style)
	{
		return style.useAttr( normalTextAttrs ).useAttr( h6TextAttrs );
	}



	public static StyleSheetValues separatorStyle(StyleSheetValues style)
	{
		return style.remapAttr( Primitive.shapePainter, separatorPainter );
	}
	
	public static StyleSheetValues useSeparatorAttrs(StyleSheetValues style)
	{
		return style.useAttr( separatorPainter );
	}
}
