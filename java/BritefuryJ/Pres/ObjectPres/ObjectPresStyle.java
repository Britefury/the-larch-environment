//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.ObjectPres;

import java.awt.Color;
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class ObjectPresStyle
{
	public static final AttributeNamespace genericPerspectiveNamespace = new AttributeNamespace( "genericPerspective" );
	
	
	public static final InheritedAttributeNonNull objectBorderThickness = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectBorderThickness", Double.class, 1.0 );
	public static final InheritedAttributeNonNull objectBorderInset = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectBorderInset", Double.class, 3.0 );
	public static final InheritedAttributeNonNull objectBorderRounding = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectBorderRounding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull objectBorderPaint = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectBorderPaint", Paint.class, new Color( 0.35f, 0.35f, 0.35f ) );
	public static final InheritedAttribute objectBorderBackground = new InheritedAttribute( genericPerspectiveNamespace, "objectBorderBackground", Paint.class, null );
	public static final InheritedAttributeNonNull objectTitlePaint = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectTitlePaint", Paint.class, new Color( 0.35f, 0.35f, 0.35f ) );
	public static final InheritedAttributeNonNull objectTitleAttrs = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectTitleAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontFace.as( "Sans serif" ), Primitive.fontSize.as( 10 ) ) );
	public static final InheritedAttributeNonNull objectContentPadding = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectContentPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull objectFieldSpacing = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectFieldSpacing", Double.class, 2.0 );
	public static final InheritedAttributeNonNull objectFieldIndentation = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectFieldIndentation", Double.class, 5.0 );
	public static final InheritedAttributeNonNull objectFieldStyle = new InheritedAttributeNonNull( genericPerspectiveNamespace, "objectFieldStyle", StyleSheet.class,
		    StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.25f, 0.5f ) ), Primitive.paragraphIndentation.as( objectFieldIndentation.getDefaultValue() ) ) );
	public static final InheritedAttributeNonNull errorBorderStyle = new InheritedAttributeNonNull( genericPerspectiveNamespace, "errorBorderStyle", StyleSheet.class,
		    StyleSheet.style( Primitive.border.as( new SolidBorder( 1.0, 3.0, 10.0, 10.0, new Color( 0.8f, 0.0f, 0.0f ), new Color( 1.0f, 0.9f, 0.9f ) ) ) ) );
	public static final InheritedAttributeNonNull stringContentStyle = new InheritedAttributeNonNull( genericPerspectiveNamespace, "stringContentStyle", StyleSheet.class, 
			StyleSheet.instance );
	public static final InheritedAttributeNonNull stringEscapeStyle = new InheritedAttributeNonNull( genericPerspectiveNamespace, "stringEscapeStyle", StyleSheet.class,
		    StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.15f, 0.35f ) ), Primitive.background.as( new FillPainter( new Color( 0.8f, 0.8f, 1.0f ) ) ) ) );


	
	protected static DerivedValueTable<StyleSheet> objectTitleStyle = new DerivedValueTable<StyleSheet>( genericPerspectiveNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			StyleSheet attrs = attribs.get( objectTitleAttrs, StyleSheet.class );
			Paint paint = attribs.get( objectTitlePaint, Paint.class );
			return StyleSheet.instance.withAttrs( attrs ).withValues( Primitive.foreground.as( paint ) );
		}
	};
	
	protected static DerivedValueTable<StyleSheet> objectBorderStyle = new DerivedValueTable<StyleSheet>( genericPerspectiveNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			double thickness = attribs.get( objectBorderThickness, Double.class );
			double inset = attribs.get( objectBorderInset, Double.class );
			double rounding = attribs.get( objectBorderRounding, Double.class );
			Paint paint = attribs.get( objectBorderPaint, Paint.class );
			Paint background = attribs.get( objectBorderBackground, Paint.class );
			return StyleSheet.style( Primitive.border.as( new SolidBorder( thickness, inset, rounding, rounding, paint, background ) ) );
		}
	};
	
	public static StyleValues useObjectBorderAttrs(StyleValues values)
	{
		return values.useAttr( objectBorderThickness ).useAttr( objectBorderInset ).useAttr( objectBorderRounding ).useAttr( objectBorderPaint ).useAttr( objectBorderBackground );
	}

	
	
	
	public static StyleValues useObjectBoxAttrs(StyleValues values)
	{
		return values.useAttr( objectContentPadding );
	}

	
	
	protected static DerivedValueTable<StyleSheet> objectBoxFieldListStyle = new DerivedValueTable<StyleSheet>( genericPerspectiveNamespace )
	{
		protected StyleSheet evaluate(AttributeTable attribs)
		{
			double spacing = attribs.get( objectFieldSpacing, Double.class );
			return StyleSheet.style( Primitive.columnSpacing.as( spacing ) );
		}
	};
	
	public static StyleValues useObjectFieldListAttrs(StyleValues values)
	{
		return values.useAttr( objectFieldSpacing );
	}
	
	
	
	public static StyleValues useObjectFieldAttrs(StyleValues values)
	{
		return values.useAttr( objectFieldStyle );
	}



	
	public static StyleValues useErrorBorderAttrs(StyleValues values)
	{
		return values.useAttr( errorBorderStyle );
	}

	
	
	
	public static StyleValues useErrorBoxAttrs(StyleValues values)
	{
		return values.useAttr( objectContentPadding );
	}
}
