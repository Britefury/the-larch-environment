//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.PresCom;

import java.awt.Color;
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeTable2;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class GenericStyle
{
	public static final InheritedAttributeNonNull objectBorderThickness = new InheritedAttributeNonNull( "genericPerspective", "objectBorderThickness", Double.class, 1.0 );
	public static final InheritedAttributeNonNull objectBorderInset = new InheritedAttributeNonNull( "genericPerspective", "objectBorderInset", Double.class, 3.0 );
	public static final InheritedAttributeNonNull objectBorderRounding = new InheritedAttributeNonNull( "genericPerspective", "objectBorderRounding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull objectBorderPaint = new InheritedAttributeNonNull( "genericPerspective", "objectBorderPaint", Paint.class, new Color( 0.35f, 0.35f, 0.35f ) );
	public static final InheritedAttribute objectBorderBackground = new InheritedAttribute( "genericPerspective", "objectBorderBackground", Paint.class, null );
	public static final InheritedAttributeNonNull objectTitleAttrs = new InheritedAttributeNonNull( "genericPerspective", "objectTitleAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.fontFace, "Sans serif" ).withAttr( Primitive.fontSize, 10 ).withAttr( Primitive.foreground, new Color( 0.35f, 0.35f, 0.35f ) ) );
	public static final InheritedAttributeNonNull objectContentPadding = new InheritedAttributeNonNull( "genericPerspective", "objectContentPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull objectFieldSpacing = new InheritedAttributeNonNull( "genericPerspective", "objectFieldSpacing", Double.class, 2.0 );
	public static final InheritedAttributeNonNull objectFieldIndentation = new InheritedAttributeNonNull( "genericPerspective", "objectFieldIndentation", Double.class, 5.0 );
	public static final InheritedAttributeNonNull objectFieldStyle = new InheritedAttributeNonNull( "genericPerspective", "objectFieldStyle", StyleSheet2.class, 
			StyleSheet2.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.5f ) ).withAttr( Primitive.paragraphIndentation, objectFieldIndentation.getDefaultValue() ) );
	public static final InheritedAttributeNonNull errorBorderStyle = new InheritedAttributeNonNull( "genericPerspective", "errorBorderStyle", StyleSheet2.class, 
			StyleSheet2.instance.withAttr( Primitive.border, new SolidBorder( 1.0, 3.0, 10.0, 10.0, new Color( 0.8f, 0.0f, 0.0f ), new Color( 1.0f, 0.9f, 0.9f ) ) ) );
	public static final InheritedAttributeNonNull stringContentStyle = new InheritedAttributeNonNull( "genericPerspective", "stringContentStyle", StyleSheet2.class, 
			StyleSheet2.instance );
	public static final InheritedAttributeNonNull stringEscapeStyle = new InheritedAttributeNonNull( "genericPerspective", "stringContentStyle", StyleSheet2.class, 
			StyleSheet2.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.15f, 0.35f ) ).withAttr( Primitive.background, new FillPainter( new Color( 0.8f, 0.8f, 1.0f ) ) ) );


	
	protected static DerivedValueTable<StyleSheet2> objectBorderStyle = new DerivedValueTable<StyleSheet2>()
	{
		protected StyleSheet2 evaluate(AttributeTable2 attribs)
		{
			double thickness = attribs.get( objectBorderThickness, Double.class );
			double inset = attribs.get( objectBorderInset, Double.class );
			double rounding = attribs.get( objectBorderRounding, Double.class );
			Paint paint = attribs.get( objectBorderPaint, Paint.class );
			Paint background = attribs.get( objectBorderBackground, Paint.class );
			return StyleSheet2.instance.withAttr( Primitive.border, new SolidBorder( thickness, inset, rounding, rounding, paint, background ) );
		}
	};
	
	public static StyleValues useObjectBorderAttrs(StyleValues values)
	{
		return values.useAttr( objectBorderThickness ).useAttr( objectBorderInset ).useAttr( objectBorderRounding ).useAttr( objectBorderPaint ).useAttr( objectBorderBackground );
	}

	public static PresentationContext useObjectBorderAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useObjectBorderAttrs( ctx.getStyle() ) );
	}

	
	
	
	public static StyleValues useObjectBoxAttrs(StyleValues values)
	{
		return values.useAttr( objectContentPadding );
	}
	
	public static PresentationContext useObjectBoxAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useObjectBoxAttrs( ctx.getStyle() ) );
	}

	
	
	protected static DerivedValueTable<StyleSheet2> objectBoxFieldListStyle = new DerivedValueTable<StyleSheet2>()
	{
		protected StyleSheet2 evaluate(AttributeTable2 attribs)
		{
			double spacing = attribs.get( objectFieldSpacing, Double.class );
			return StyleSheet2.instance.withAttr( Primitive.vboxSpacing, spacing );
		}
	};
	
	public static StyleValues useObjectFieldListAttrs(StyleValues values)
	{
		return values.useAttr( objectFieldSpacing );
	}
	
	public static PresentationContext useObjectFieldListAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useObjectFieldListAttrs( ctx.getStyle() ) );
	}
	
	
	
	public static StyleValues useObjectFieldAttrs(StyleValues values)
	{
		return values.useAttr( objectFieldStyle );
	}
	
	public static PresentationContext useObjectFieldAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useObjectFieldAttrs( ctx.getStyle() ) );
	}



	
	public static StyleValues useErrorBorderAttrs(StyleValues values)
	{
		return values.useAttr( errorBorderStyle );
	}

	public static PresentationContext useErrorBorderAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useErrorBorderAttrs( ctx.getStyle() ) );
	}

	
	
	
	public static StyleValues useErrorBoxAttrs(StyleValues values)
	{
		return values.useAttr( objectContentPadding );
	}
	
	public static PresentationContext useErrorBoxAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useErrorBoxAttrs( ctx.getStyle() ) );
	}
}
