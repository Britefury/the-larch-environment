//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeImage;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class LSImage extends LSBlank
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
	
	
	public LSImage(ElementStyleParams styleParams, BufferedImage image, BufferedImage hoverImage, double imageWidth, double imageHeight)
	{
		super( styleParams );
		
		initImage( img( image ), hoverImg( hoverImage ), imageWidth, imageHeight );
	}
	
	public LSImage(ElementStyleParams styleParams, BufferedImage image, BufferedImage hoverImage, double imageWidth)
	{
		super( styleParams );
		
		initImage( img( image ), hoverImg( hoverImage ), imageWidth );
	}
	
	public LSImage(ElementStyleParams styleParams, BufferedImage image, BufferedImage hoverImage)
	{
		super( styleParams );
		
		initImage( img( image ), hoverImg( hoverImage ) );
	}
	

	public LSImage(ElementStyleParams styleParams, SVGDiagram image, SVGDiagram hoverImage, double imageWidth, double imageHeight)
	{
		super( styleParams );
		
		initImage( img( image ), hoverImg( hoverImage ), imageWidth, imageHeight );
	}
	
	public LSImage(ElementStyleParams styleParams, SVGDiagram image, SVGDiagram hoverImage, double imageWidth)
	{
		super( styleParams );
		
		initImage( img( image ), hoverImg( hoverImage ), imageWidth );
	}
	
	public LSImage(ElementStyleParams styleParams, SVGDiagram image, SVGDiagram hoverImage)
	{
		super( styleParams );
		
		initImage( img( image ), hoverImg( hoverImage ) );
	}
	

	public LSImage(ElementStyleParams styleParams, File imageFile, File hoverImageFile, double imageWidth, double imageHeight)
	{
		super( styleParams );
		
		initImage( readImageFile( imageFile ), readImageFile( hoverImageFile ),  imageWidth, imageHeight );
	}
	
	public LSImage(ElementStyleParams styleParams, File imageFile, File hoverImageFile, double imageWidth)
	{
		super( styleParams );
		
		initImage( readImageFile( imageFile ), readImageFile( hoverImageFile ),  imageWidth );
	}
	
	public LSImage(ElementStyleParams styleParams, File imageFile, File hoverImageFile)
	{
		super( styleParams );
		
		initImage( readImageFile( imageFile ), readImageFile( hoverImageFile ) );
	}
	
	
	public LSImage(ElementStyleParams styleParams, String imageFilename, String hoverImageFilename, double imageWidth, double imageHeight)
	{
		this( styleParams, new File( imageFilename ), hoverImageFilename != null  ?  new File( hoverImageFilename )  :  null,  imageWidth, imageHeight );
	}
	
	public LSImage(ElementStyleParams styleParams, String imageFilename, String hoverImageFilename, double imageWidth)
	{
		this( styleParams, new File( imageFilename ), hoverImageFilename != null  ?  new File( hoverImageFilename )  :  null,  imageWidth );
	}
	
	public LSImage(ElementStyleParams styleParams, String imageFilename, String hoverImageFilename)
	{
		this( styleParams, new File( imageFilename ), hoverImageFilename != null  ?  new File( hoverImageFilename )  :  null );
	}
	
	
	public LSImage(ElementStyleParams styleParams, URL imageURL, URL hoverImageURL, double imageWidth, double imageHeight)
	{
		super( styleParams );
		
		initImage( readImageUrl( imageURL ), readImageUrl( hoverImageURL ),  imageWidth, imageHeight );
	}
	
	public LSImage(ElementStyleParams styleParams, URL imageURL, URL hoverImageURL, double imageWidth)
	{
		super( styleParams );
		
		initImage( readImageUrl( imageURL ), readImageUrl( hoverImageURL ),  imageWidth );
	}
	
	public LSImage(ElementStyleParams styleParams, URL imageURL, URL hoverImageURL)
	{
		super( styleParams );
		
		initImage( readImageUrl( imageURL ), readImageUrl( hoverImageURL ) );
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
	

	private static AbstractImg readImageFile(File file)
	{
		if ( file != null )
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
		else
		{
			return null;
		}
	}
	
	private static AbstractImg readImageUrl(URL url)
	{
		if ( url != null )
		{
			try
			{
				if ( url.getFile().toLowerCase().endsWith( ".svg" ) )
				{
					// Load as SVG
					SVGDiagram diagram = SVGCache.getSVGUniverse().getDiagram( url.toURI() );
					return img( diagram );
				}
				else
				{
					// Load as buffered image
					return img( ImageIO.read( url ) );
				}
			}
			catch (IOException e)
			{
				return img( getBadImage() );
			}
			catch (URISyntaxException e)
			{
				return img( getBadImage() );
			}
		}
		else
		{
			return null;
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
	
	
	
	private static AbstractImg img(BufferedImage img)
	{
		return img != null  ?  new BufferedImageImg( img )  :  new BufferedImageImg( getBadImage() );
	}
	
	private static AbstractImg img(SVGDiagram img)
	{
		return img != null  ?  new SVGImg( img )  :  new BufferedImageImg( getBadImage() );
	}
	
	
	private static AbstractImg hoverImg(BufferedImage img)
	{
		return img != null  ?  new BufferedImageImg( img )  :  null;
	}
	
	private static AbstractImg hoverImg(SVGDiagram img)
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
