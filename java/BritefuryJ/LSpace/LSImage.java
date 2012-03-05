//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

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

import BritefuryJ.LSpace.LayoutTree.LayoutNodeImage;
import BritefuryJ.LSpace.StyleParams.ContentLeafStyleParams;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class LSImage extends LSContentLeaf
{
	private static abstract class AbstractImg
	{
		public abstract double getWidth();
		public abstract double getHeight();
		public abstract void draw(Graphics2D graphics, double scaleX, double scaleY, LSRootElement root); 
	}
	
	
	private static class BufferedImageImg extends AbstractImg
	{
		private BufferedImage image;
		
		public BufferedImageImg(BufferedImage image)
		{
			this.image = image;
		}
		
		public double getWidth()
		{
			return image.getWidth();
		}
		
		public double getHeight()
		{
			return image.getHeight();
		}
		
		public void draw(Graphics2D graphics, double scaleX, double scaleY, LSRootElement root)
		{
			if ( scaleX == 1.0  &&  scaleY == 1.0 )
			{
				graphics.drawImage( image, 0, 0, root.getImageObserver() );
			}
			else
			{
				AffineTransform xform = new AffineTransform();
				xform.setToScale( scaleX, scaleY );
				graphics.drawImage( image, xform, root.getImageObserver() );
			}
		}
	}
	
	
	private static class SVGImg extends AbstractImg
	{
		private SVGDiagram svg;
		
		public SVGImg(SVGDiagram svg)
		{
			this.svg = svg;
		}
		
		public double getWidth()
		{
			return svg.getWidth();
		}
		
		public double getHeight()
		{
			return svg.getHeight();
		}
		
		public void draw(Graphics2D graphics, double scaleX, double scaleY, LSRootElement root)
		{
			AffineTransform prevXform = graphics.getTransform();
			
			graphics.scale( scaleX, scaleY );
			
			try
			{
				svg.render( graphics );
			}
			catch (SVGException e)
			{
				throw new RuntimeException( e );
			}
			
			graphics.setTransform( prevXform );
		}
	}
	
	
	
	
	private double imageScaleX, imageScaleY;
	private double hoverImageScaleX, hoverImageScaleY;
	private AbstractImg image, hoverImage;
	
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, BufferedImage image, BufferedImage hoverImage, double imageWidth, double imageHeight)
	{
		super( styleParams, textRepresentation );
		
		initImage( img( image ), img( hoverImage ), imageWidth, imageHeight );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, BufferedImage image, BufferedImage hoverImage, double imageWidth)
	{
		super( styleParams, textRepresentation );
		
		initImage( img( image ), img( hoverImage ), imageWidth );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, BufferedImage image, BufferedImage hoverImage)
	{
		super( styleParams, textRepresentation );
		
		initImage( img( image ), img( hoverImage ) );
	}
	

	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, SVGDiagram image, SVGDiagram hoverImage, double imageWidth, double imageHeight)
	{
		super( styleParams, textRepresentation );
		
		initImage( img( image ), img( hoverImage ), imageWidth, imageHeight );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, SVGDiagram image, SVGDiagram hoverImage, double imageWidth)
	{
		super( styleParams, textRepresentation );
		
		initImage( img( image ), img( hoverImage ), imageWidth );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, SVGDiagram image, SVGDiagram hoverImage)
	{
		super( styleParams, textRepresentation );
		
		initImage( img( image ), img( hoverImage ) );
	}
	

	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, File imageFile, File hoverImageFile, double imageWidth, double imageHeight)
	{
		super( styleParams, textRepresentation );
		
		initImage( readImageFile( imageFile ), hoverImageFile != null  ?  readImageFile( hoverImageFile )  :  null,  imageWidth, imageHeight );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, File imageFile, File hoverImageFile, double imageWidth)
	{
		super( styleParams, textRepresentation );
		
		initImage( readImageFile( imageFile ), hoverImageFile != null  ?  readImageFile( hoverImageFile )  :  null,  imageWidth );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, File imageFile, File hoverImageFile)
	{
		super( styleParams, textRepresentation );
		
		initImage( readImageFile( imageFile ), hoverImageFile != null  ?  readImageFile( hoverImageFile )  :  null );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, String imageFilename, String hoverImageFilename, double imageWidth, double imageHeight)
	{
		this( styleParams, textRepresentation, new File( imageFilename ), hoverImageFilename != null  ?  new File( hoverImageFilename )  :  null,  imageWidth, imageHeight );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, String imageFilename, String hoverImageFilename, double imageWidth)
	{
		this( styleParams, textRepresentation, new File( imageFilename ), hoverImageFilename != null  ?  new File( hoverImageFilename )  :  null,  imageWidth );
	}
	
	public LSImage(ContentLeafStyleParams styleParams, String textRepresentation, String imageFilename, String hoverImageFilename)
	{
		this( styleParams, textRepresentation, new File( imageFilename ), hoverImageFilename != null  ?  new File( hoverImageFilename )  :  null );
	}
	
	
	//
	//
	// Element initialisation
	//
	//
	
	private void initImage(AbstractImg image, AbstractImg hoverImage, double imageWidth, double imageHeight)
	{
		this.image = image;
		imageScaleX = imageWidth / (double)image.getWidth();
		imageScaleY = imageHeight / (double)image.getHeight();
		
		this.hoverImage = hoverImage;
		if ( hoverImage != null )
		{
			hoverImageScaleX = imageWidth / (double)hoverImage.getWidth();
			hoverImageScaleY = imageHeight / (double)hoverImage.getHeight();
		}
		
		layoutNode = new LayoutNodeImage( this );
	}
	
	private void initImage(AbstractImg image, AbstractImg hoverImage, double imageWidth)
	{
		this.image = image;
		imageScaleX = imageScaleY = imageWidth / (double)image.getWidth();
		
		this.hoverImage = hoverImage;
		if ( hoverImage != null )
		{
			hoverImageScaleX = hoverImageScaleY = imageWidth / (double)hoverImage.getWidth();
		}
		
		layoutNode = new LayoutNodeImage( this );
	}
	
	private void initImage(AbstractImg image, AbstractImg hoverImage)
	{
		this.image = image;
		imageScaleX = imageScaleY = 1.0;
		
		this.hoverImage = hoverImage;
		if ( hoverImage != null )
		{
			hoverImageScaleX = (double)image.getWidth() / (double)hoverImage.getWidth();
			hoverImageScaleY = (double)image.getHeight() / (double)hoverImage.getHeight();
		}

		layoutNode = new LayoutNodeImage( this );
	}
	

	private AbstractImg readImageFile(File file)
	{
		try
		{
			if ( file.getName().toLowerCase().endsWith( ".svg" ) )
			{
				// Load as SVG
				SVGDiagram diagram = SVGCache.getSVGUniverse().getDiagram( file.toURI() );
				return img( diagram );
			}
			else
			{
				// Load as buffered image
				return img( ImageIO.read( file ) );
			}
		}
		catch (IOException e)
		{
			return img( getBadImage() );
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
	
	
	
	public boolean isRedrawRequiredOnHover()
	{
		return super.isRedrawRequiredOnHover()  ||  hoverImage != null;
	}
	

	protected void draw(Graphics2D graphics)
	{
		LSRootElement root = getRootElement();
		
		if ( root != null )
		{
			boolean bUseHover = isHoverActive() && hoverImage != null;
			
			AbstractImg img = bUseHover  ?  hoverImage  :  image;
			double sX = bUseHover  ?  hoverImageScaleX  :  imageScaleX;
			double sY = bUseHover  ?  hoverImageScaleY  :  imageScaleY;

			img.draw( graphics, sX, sY, root );
		}
	}
	
	
	
	private static BufferedImageImg img(BufferedImage img)
	{
		return img != null  ?  new BufferedImageImg( img )  :  null;
	}
	
	private static SVGImg img(SVGDiagram img)
	{
		return img != null  ?  new SVGImg( img )  :  null;
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
