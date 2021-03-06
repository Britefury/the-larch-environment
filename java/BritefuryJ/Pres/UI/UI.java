//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.UI;

import java.awt.Color;

import BritefuryJ.AttributeTable.AttributeNamespace;
import BritefuryJ.AttributeTable.AttributeNonNull;
import BritefuryJ.AttributeTable.AttributeUsageSet;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class UI
{
	public static final AttributeNamespace uiNamespace = new AttributeNamespace( "ui" );
	
	
	public static final InheritedAttributeNonNull bubblePopupBorderWidth = new InheritedAttributeNonNull( uiNamespace, "bubblePopupBorderWidth", Double.class, 4.0 );
	public static final InheritedAttributeNonNull bubblePopupCornerRadius = new InheritedAttributeNonNull( uiNamespace, "bubblePopupCornerRadius", Double.class, 8.0 );
	public static final InheritedAttributeNonNull bubblePopupArrowLength = new InheritedAttributeNonNull( uiNamespace, "bubblePopupArrowLength", Double.class, 12.0 );
	public static final InheritedAttributeNonNull bubblePopupArrowWidth = new InheritedAttributeNonNull( uiNamespace, "bubblePopupArrowWidth", Double.class, 12.0 );



	private static final Color titleColour = new Color( 0.15f, 0.15f, 0.15f );
	private static final Color h1Colour = new Color( 0.2f, 0.2f, 0.2f );
	private static final Color h2Colour = new Color( 0.25f, 0.25f, 0.25f );
	private static final Color h3Colour = new Color( 0.3f, 0.3f, 0.3f );


	public static final InheritedAttributeNonNull uiTextAttrs = new InheritedAttributeNonNull( uiNamespace, "uiTextAttrs", StyleSheet.class,
			StyleSheet.style( Primitive.editable.as( false ), Primitive.selectable.as( false ), Primitive.fontFace.as( Primitive.lightFontName) ) );

	public static final InheritedAttributeNonNull normalTextAttrs = new InheritedAttributeNonNull( uiNamespace, "normalTextAttrs", StyleSheet.class,
			StyleSheet.style( Primitive.foreground.as( Color.BLACK )) );

	public static final InheritedAttributeNonNull titleTextAttrs = new InheritedAttributeNonNull( uiNamespace, "titleTextAttrs", StyleSheet.class,
			    StyleSheet.style( Primitive.fontSize.as( 40 ), Primitive.foreground.as( titleColour ) ) );

	public static final InheritedAttributeNonNull notesTextAttrs = new InheritedAttributeNonNull( uiNamespace, "notesTextAttrs", StyleSheet.class,
			    StyleSheet.style( Primitive.fontSize.as( 11 ), Primitive.foreground.as( new Color( 0.3f, 0.3f, 0.3f ) ) ) );

	public static final InheritedAttributeNonNull h1TextAttrs = new InheritedAttributeNonNull( uiNamespace, "h1TextAttrs", StyleSheet.class,
			    StyleSheet.style( Primitive.fontSize.as( 22 ), Primitive.foreground.as( h1Colour ) ) );
	public static final InheritedAttributeNonNull h2TextAttrs = new InheritedAttributeNonNull( uiNamespace, "h2TextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 18 ), Primitive.foreground.as( h2Colour ) ) );
	public static final InheritedAttributeNonNull h3TextAttrs = new InheritedAttributeNonNull( uiNamespace, "h3TextAttrs", StyleSheet.class,
		    StyleSheet.style( Primitive.fontSize.as( 14 ), Primitive.foreground.as( h3Colour) ) );
	
	
	public static final AttributeNonNull sectionColumnStyle = new AttributeNonNull( uiNamespace, "sectionColumnStyle", StyleSheet.class,
			StyleSheet.style( Primitive.columnSpacing.as( 5.0 ) ) );
	public static final AttributeNonNull sectionPadding = new AttributeNonNull( uiNamespace, "sectionPadding", Double.class, 10.0 );
	
	public static final AttributeNonNull controlsRowStyle = new AttributeNonNull( uiNamespace, "controlsRowStyle", StyleSheet.class,
			    StyleSheet.style( Primitive.rowSpacing.as( 10.0 ) ) );
		
		
		
	
	public static final AttributeNonNull formTitleSpacing = new AttributeNonNull( uiNamespace, "formTitleSpacing", Double.class, 10.0 );
	public static final AttributeNonNull formNotesSpacing = new AttributeNonNull( uiNamespace, "formNotesSpacing", Double.class, 5.0 );
	public static final AttributeNonNull formTableStyle = new AttributeNonNull( uiNamespace, "formTableStyle", StyleSheet.class,
			StyleSheet.style( Primitive.tableRowSpacing.as( 10.0 ), Primitive.tableColumnSpacing.as( 20.0 ) ) );
	public static final AttributeNonNull formHPadding = new AttributeNonNull( uiNamespace, "formHPadding", Double.class, 10.0 );
	public static final AttributeNonNull formVPadding = new AttributeNonNull( uiNamespace, "formVPadding", Double.class, 5.0 );
	




	public static StyleSheet normalTextStyle(StyleValues style)
	{
		return style.get( uiTextAttrs, StyleSheet.class ).withAttrs( style.get( normalTextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useNormalTextAttrs(StyleValues style)
	{
		return style.useAttr( uiTextAttrs ).useAttr( normalTextAttrs );
	}

	
	
	public static StyleSheet titleTextStyle(StyleValues style)
	{
		return style.get( uiTextAttrs, StyleSheet.class ).withAttrs( style.get( titleTextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useTitleTextAttrs(StyleValues style)
	{
		return style.useAttr( uiTextAttrs ).useAttr( titleTextAttrs );
	}

	
	
	public static StyleSheet h1TextStyle(StyleValues style)
	{
		return style.get( uiTextAttrs, StyleSheet.class ).withAttrs( style.get( h1TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH1TextAttrs(StyleValues style)
	{
		return style.useAttr( uiTextAttrs ).useAttr( h1TextAttrs );
	}

	

	public static StyleSheet h2TextStyle(StyleValues style)
	{
		return style.get( uiTextAttrs, StyleSheet.class ).withAttrs( style.get( h2TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH2TextAttrs(StyleValues style)
	{
		return style.useAttr( uiTextAttrs ).useAttr( h2TextAttrs );
	}

	

	public static StyleSheet h3TextStyle(StyleValues style)
	{
		return style.get( uiTextAttrs, StyleSheet.class ).withAttrs( style.get( h3TextAttrs, StyleSheet.class ) );
	}
	
	public static StyleValues useH3TextAttrs(StyleValues style)
	{
		return style.useAttr( uiTextAttrs ).useAttr( h3TextAttrs );
	}

	

	public static StyleSheet notesTextStyle(StyleValues style)
	{
		return style.get( uiTextAttrs, StyleSheet.class ).withAttrs( style.get( notesTextAttrs, StyleSheet.class ) );
	}

	
	
	public static StyleValues useFormAttrs(StyleValues style)
	{
		return style.useAttr( formTitleSpacing ).useAttr( formTableStyle ).useAttr( formHPadding ).useAttr( formVPadding );
	}
	
	public static StyleValues useFormSectionAttrs(StyleValues style)
	{
		return style.useAttr( formNotesSpacing );
	}


	protected static final AttributeUsageSet controlsRowUsage = new AttributeUsageSet( controlsRowStyle );
}
