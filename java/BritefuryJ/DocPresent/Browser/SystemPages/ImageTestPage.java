//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class ImageTestPage extends SystemPage
{
	protected ImageTestPage()
	{
		register( "tests.image" );
	}
	
	
	public String getTitle()
	{
		return "Image test";
	}

	protected String getDescription()
	{
		return "The image element displays an image (in the form of a java.awt.image.BufferedImage), or an image file.";
	}
	
	
	
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;

	
	
	protected DPElement createContents()
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
		
		return styleSheet.vbox( new DPElement[] {
				styleSheet.staticText( "A 64x32 image with a green oval" ),
				styleSheet.image( ellipseImage ),
				styleSheet.staticText( "A 64x32 image with a green oval, scaled to 256x128" ),
				styleSheet.image( ellipseImage, 256.0, 128.0 ),
				styleSheet.staticText( "Image from 'back arrow' image file, as file object" ),
				styleSheet.image( new File( "icons/back arrow.png" ) ),
				styleSheet.staticText( "Image from 'forward arrow' image file, as file name string" ),
				styleSheet.image( "icons/forward arrow.png" ),
				styleSheet.staticText( "Invalid image filename" ),
				styleSheet.image( "" ),
				styleSheet.staticText( "A 64x32 image with a green oval, with a blue oval displayed on hover" ),
				styleSheet.image( ellipseImage, blueEllipseImage ),
				styleSheet.staticText( "Image from 'back arrow' image file, as file object, with 'forward arrow' image file on hover" ),
				styleSheet.image( new File( "icons/back arrow.png" ), new File( "icons/forward arrow.png" ) ) } );
	}
}
