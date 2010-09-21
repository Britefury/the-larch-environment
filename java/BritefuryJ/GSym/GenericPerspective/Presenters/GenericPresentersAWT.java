//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.Presenters;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.Table;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.PresCom.GenericStyle;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBorder;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectTitle;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresenterRegistry;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresenter;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GenericPresentersAWT extends GSymObjectPresenterRegistry
{
	public GenericPresentersAWT()
	{
		registerJavaObjectPresenter( AffineTransform.class,  presenter_AffineTransform );
		registerJavaObjectPresenter( Color.class,  presenter_Color );
		registerJavaObjectPresenter( Shape.class,  presenter_Shape );
		registerJavaObjectPresenter( BufferedImage.class,  presenter_BufferedImage );
	}


	public static final ObjectPresenter presenter_AffineTransform = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
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
		public Pres presentObject(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Color colour = (Color)x;
			
			Pres title = colourObjectBoxStyle.applyTo( new ObjectTitle( "java.awt.Color" ) );
			
			Pres red = colourRedStyle.applyTo( new StaticText( "R=" + String.valueOf( colour.getRed() ) ) );
			Pres green = colourGreenStyle.applyTo( new StaticText( "G=" + String.valueOf( colour.getGreen() ) ) );
			Pres blue = colourBlueStyle.applyTo( new StaticText( "B=" + String.valueOf( colour.getBlue() ) ) );
			Pres alpha = colourAlphaStyle.applyTo( new StaticText( "A=" + String.valueOf( colour.getAlpha() ) ) );
			
			Pres components = colourBoxStyle.applyTo( new Row( new Pres[] { red, green, blue, alpha } ) );
			
			Pres textBox = new Column( new Pres[] { title, components } );
			
			Pres swatch = staticStyle.withAttr( Primitive.shapePainter, new FillPainter( colour ) ).applyTo( new Box( 50.0, 20.0 ) ).alignVExpand();
			
			Pres contents = colourBoxStyle.applyTo( new Row( new Pres[] { textBox, swatch } ) );
			
			return colourObjectBoxStyle.applyTo( new ObjectBorder( contents ) );
		}
	};

	public static final ObjectPresenter presenter_Shape = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
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
			
			return new ObjectBox( x.getClass().getName(), new BritefuryJ.DocPresent.Combinators.Primitive.Shape( shape ) );
		}
	};

	public static final ObjectPresenter presenter_BufferedImage = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
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
			
			return new BritefuryJ.DocPresent.Combinators.Primitive.Image( image, width, height );
		}
	};


	private static final StyleSheet staticStyle = StyleSheet.instance.withAttr( Primitive.editable, false );

	private static final StyleSheet xformMatrixStyle = staticStyle.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.tableColumnSpacing, 10.0 ).withAttr( Primitive.tableRowSpacing, 10.0 );
	
	private static final StyleSheet colourObjectBoxStyle = staticStyle.withAttr( GenericStyle.objectBorderPaint, new Color( 0.0f, 0.1f, 0.4f ) ).withAttr(
			GenericStyle.objectTitlePaint, new Color( 0.0f, 0.1f, 0.4f ) );
	private static final StyleSheet colourRedStyle = staticStyle.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.75f, 0.0f, 0.0f ) );
	private static final StyleSheet colourGreenStyle = staticStyle.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.75f, 0.0f ) );
	private static final StyleSheet colourBlueStyle = staticStyle.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.75f ) );
	private static final StyleSheet colourAlphaStyle = staticStyle.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.3f, 0.3f, 0.3f ) );
	private static final StyleSheet colourBoxStyle = staticStyle.withAttr( Primitive.rowSpacing, 5.0 );
}
