//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.Presenters;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectPresStyle;
import BritefuryJ.Pres.ObjectPres.ObjectBorder;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.ObjectPres.ObjectTitle;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.StyleSheet.StyleSheet;

public class PresentersAWT extends ObjectPresenterRegistry
{
	public PresentersAWT()
	{
		registerJavaObjectPresenter( AffineTransform.class,  presenter_AffineTransform );
		registerJavaObjectPresenter( Color.class,  presenter_Color );
		registerJavaObjectPresenter( Shape.class,  presenter_Shape );
		registerJavaObjectPresenter( BufferedImage.class,  presenter_BufferedImage );
	}


	public static final ObjectPresenter presenter_AffineTransform = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			AffineTransform xform = (AffineTransform)x;
			
			double matrix[] = new double[6];
			xform.getMatrix( matrix );
			
			Pres m00 = new Label( String.valueOf( matrix[0] ) );	
			Pres m10 = new Label( String.valueOf( matrix[1] ) );	
			Pres m01 = new Label( String.valueOf( matrix[2] ) );	
			Pres m11 = new Label( String.valueOf( matrix[3] ) );	
			Pres m02 = new Label( String.valueOf( matrix[4] ) );	
			Pres m12 = new Label( String.valueOf( matrix[5] ) );
			
			Pres m = xformMatrixStyle.applyTo(
					new Table( new Object[][]
					                        { new Object[] { m00, m01, m02 },
					                        	new Object[] { m10, m11, m12 },
					                        	new Object[] { new Label( "0.0" ), new Label( "0.0" ), new Label( "1.0" ) } } ) );
			
			return new ObjectBox( "java.awt.geom.AffineTransform", m );
		}
	};


	
	public static final ObjectPresenter presenter_Color = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Color colour = (Color)x;
			
			Pres title = colourObjectBoxStyle.applyTo( new ObjectTitle( "java.awt.Color" ) );
			
			Pres red = colourRedStyle.applyTo( new Label( "R=" + String.valueOf( colour.getRed() ) ) );
			Pres green = colourGreenStyle.applyTo( new Label( "G=" + String.valueOf( colour.getGreen() ) ) );
			Pres blue = colourBlueStyle.applyTo( new Label( "B=" + String.valueOf( colour.getBlue() ) ) );
			Pres alpha = colourAlphaStyle.applyTo( new Label( "A=" + String.valueOf( colour.getAlpha() ) ) );
			
			Pres components = colourBoxStyle.applyTo( new Row( new Pres[] { red, green, blue, alpha } ) );
			
			Pres textBox = new Column( new Pres[] { title, components } );
			
			Pres swatch = staticStyle.withValues( Primitive.shapePainter.as( new FillPainter( colour ) ) ).applyTo( new Box( 50.0, 20.0 ) ).alignVExpand();
			
			Pres contents = colourBoxStyle.applyTo( new Row( new Pres[] { textBox, swatch } ) );
			
			return colourObjectBoxStyle.applyTo( new ObjectBorder( contents ) );
		}
	};

	public static final ObjectPresenter presenter_Shape = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Shape shape = (Shape)x;
//			Rectangle2D bounds = shape.getBounds2D();
//			double offsetX = -bounds.getMinX(), offsetY = -bounds.getMinY();
//			double width = bounds.getWidth(), height = bounds.getHeight();
//			
//			double scale = 1.0;
//			if ( width > height  &&  width > 96.0 )
//			{
//				scale = 96.0 / width;
//			}
//			else if ( height > width  &&  height > 96.0 )
//			{
//				scale = 96.0 / height;
//			}
			
			return new ObjectBox( x.getClass().getName(), new BritefuryJ.Pres.Primitive.Shape( shape ) );
		}
	};

	public static final ObjectPresenter presenter_BufferedImage = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			BufferedImage image = (BufferedImage)x;
			double width = (double)image.getWidth();
			double height = (double)image.getHeight();
			
			if ( width > height  &&  width > 96.0 )
			{
				height *= ( 96.0 / width );
				width = 96.0;
			}
			else if ( height > width  &&  height > 96.0 )
			{
				width *= ( 96.0 / height );
				height = 96.0;
			}
			
			return new BritefuryJ.Pres.Primitive.Image( image, width, height );
		}
	};


	private static final StyleSheet staticStyle = StyleSheet.style( Primitive.editable.as( false ) );

	private static final StyleSheet xformMatrixStyle = staticStyle.withValues( Primitive.fontFace.as( "Serif" ), Primitive.tableColumnSpacing.as( 10.0 ), Primitive.tableRowSpacing.as( 10.0 ) );

	private static final StyleSheet colourObjectBoxStyle = staticStyle.withValues( ObjectPresStyle.objectBorderPaint.as( new Color( 0.0f, 0.1f, 0.4f ) ), ObjectPresStyle.objectTitlePaint.as( new Color( 0.0f, 0.1f, 0.4f ) ) );
	private static final StyleSheet colourRedStyle = staticStyle.withValues( Primitive.fontSize.as( 12 ), Primitive.foreground.as( new Color( 0.75f, 0.0f, 0.0f ) ) );
	private static final StyleSheet colourGreenStyle = staticStyle.withValues( Primitive.fontSize.as( 12 ), Primitive.foreground.as( new Color( 0.0f, 0.75f, 0.0f ) ) );
	private static final StyleSheet colourBlueStyle = staticStyle.withValues( Primitive.fontSize.as( 12 ), Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.75f ) ) );
	private static final StyleSheet colourAlphaStyle = staticStyle.withValues( Primitive.fontSize.as( 12 ), Primitive.foreground.as( new Color( 0.3f, 0.3f, 0.3f ) ) );
	private static final StyleSheet colourBoxStyle = staticStyle.withValues( Primitive.rowSpacing.as( 5.0 ) );
}
