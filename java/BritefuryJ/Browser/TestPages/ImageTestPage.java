//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Image;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.NormalText;

public class ImageTestPage extends SystemPage
{
	protected ImageTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Image test";
	}

	protected String getDescription()
	{
		return "The image element displays an image (in the form of a java.awt.image.BufferedImage), or an image file.";
	}
	
	
	
	protected Pres createContents()
	{
		BufferedImage ellipseImage = new BufferedImage( 64, 32, BufferedImage.TYPE_4BYTE_ABGR );
		Graphics2D graphics = (Graphics2D)ellipseImage.getGraphics();
		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		graphics.setPaint( new Color( 0.0f, 0.5f, 0.0f ) );
		graphics.fillOval( 4, 4, 56, 24 );
		
		BufferedImage blueEllipseImage = new BufferedImage( 64, 32, BufferedImage.TYPE_4BYTE_ABGR );
		graphics = (Graphics2D)blueEllipseImage.getGraphics();
		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		graphics.setPaint( new Color( 0.1f, 0.25f, 0.5f ) );
		graphics.fillOval( 4, 4, 56, 24 );
		
		return new Body( new Pres[] {
				new NormalText( "A 64x32 image with a green oval" ),
				new Image( ellipseImage ),
				new NormalText( "A 64x32 image with a green oval, scaled to 256x128" ),
				new Image( ellipseImage, 256.0, 128.0 ),
				new NormalText( "Image from 'back arrow' system image" ),
				Image.systemImage( "back arrow.png" ),
				new NormalText( "Image from 'forward arrow' system image, passed as URL" ),
				new Image( getClass().getResource( "/images/forward arrow.png" ) ),
				new NormalText( "Invalid image filename" ),
				new Image( "" ),
				new NormalText( "A 64x32 image with a green oval, with a blue oval displayed on hover" ),
				new Image( ellipseImage, blueEllipseImage ),
				new NormalText( "Image from 'back arrow' image file, as URL, with 'forward arrow' image URL on hover" ),
				new Image( getClass().getResource( "/images/back arrow.png" ), getClass().getResource( "/images/forward arrow.png" ) ) } );
	}
}
