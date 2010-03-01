//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeImage;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;

public class DPImage extends DPContentLeaf
{
	private double imageScaleX, imageScaleY;
	private BufferedImage image;
	
	
	public DPImage(ContentLeafStyleParams styleParams, String textRepresentation, BufferedImage image, double imageWidth, double imageHeight)
	{
		super( styleParams, textRepresentation );
		
		initImage( image, imageWidth, imageHeight );
	}
	
	public DPImage(ContentLeafStyleParams styleParams, String textRepresentation, BufferedImage image)
	{
		super( styleParams, textRepresentation );
		
		initImage( image );
	}
	
	public DPImage(ContentLeafStyleParams styleParams, String textRepresentation, File imageFile, double imageWidth, double imageHeight)
	{
		super( styleParams, textRepresentation );
		
		initImage( readImageFile( imageFile ),  imageWidth, imageHeight );
	}
	
	public DPImage(ContentLeafStyleParams styleParams, String textRepresentation, File imageFile)
	{
		super( styleParams, textRepresentation );
		
		initImage( readImageFile( imageFile ) );
	}
	
	public DPImage(ContentLeafStyleParams styleParams, String textRepresentation, String imageFilename, double imageWidth, double imageHeight)
	{
		this( styleParams, textRepresentation, new File( imageFilename ), imageWidth, imageHeight );
	}
	
	public DPImage(ContentLeafStyleParams styleParams, String textRepresentation, String imageFilename)
	{
		this( styleParams, textRepresentation, new File( imageFilename ) );
	}
	
	
	
	private void initImage(BufferedImage image, double imageWidth, double imageHeight)
	{
		this.image = image;
		imageScaleX = imageWidth / (double)image.getWidth();
		imageScaleY = imageHeight / (double)image.getHeight();
		
		layoutNode = new LayoutNodeImage( this );
	}
	
	private void initImage(BufferedImage image)
	{
		this.image = image;
		imageScaleX = imageScaleY = 1.0;
		
		layoutNode = new LayoutNodeImage( this );
	}
	
	private BufferedImage readImageFile(File file)
	{
		try
		{
			return ImageIO.read( file );
		}
		catch (IOException e)
		{
			return getBadImage();
		}
	}
	
	
	
	public double getScaleX()
	{
		return imageScaleX;
	}
	
	public double getScaleY()
	{
		return imageScaleY;
	}
	
	public double getImageWidth()
	{
		return image.getWidth() * imageScaleX;
	}
	
	public double getImageHeight()
	{
		return image.getHeight() * imageScaleY;
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		DPPresentationArea presArea = getPresentationArea();
		
		if ( presArea != null )
		{
			if ( imageScaleX == 1.0  &&  imageScaleY == 1.0 )
			{
				graphics.drawImage( image, 0, 0, presArea.getImageObserver() );
			}
			else
			{
				AffineTransform xform = new AffineTransform();
				xform.setToScale( imageScaleX, imageScaleY );
				graphics.drawImage( image, xform, presArea.getImageObserver() );
			}
		}
	}
	
	
	
	
	private static BufferedImage getBadImage()
	{
		if ( !bBadImageReady )
		{
			Graphics2D graphics = (Graphics2D)badImage.getGraphics();
			graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			graphics.setPaint( new Color( 0.8f, 0.8f, 0.8f ) );
			graphics.fill( new Rectangle2D.Double( 1.5, 1.5, 22.0, 22.0 ) );
			graphics.setColor( Color.red );
			graphics.setStroke( new BasicStroke( 2.0f ) );
			graphics.draw( new Line2D.Double( 4.0, 4.0, 20.0, 20.0 ) );
			graphics.draw( new Line2D.Double( 4.0, 20.0, 20.0, 4.0 ) );
			bBadImageReady = true;
		}
		return badImage;
	}
	
	static final BufferedImage badImage = new BufferedImage( 24, 24, BufferedImage.TYPE_4BYTE_ABGR );
	static boolean bBadImageReady = false;
}
