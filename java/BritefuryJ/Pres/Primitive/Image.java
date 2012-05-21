//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringReader;
import java.net.URI;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSImage;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;

public class Image extends Pres
{
	private static abstract class ImageFactory
	{
		public abstract LSImage create(ElementStyleParams styleParams);
	}
	
	private ImageFactory imageFactory;
	
	
	
	
	public Image(final BufferedImage image, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, null, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final BufferedImage image, final BufferedImage hoverImage, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, hoverImage, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final BufferedImage image, final BufferedImage hoverImage, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, hoverImage, imageWidth );
			}
		};
	}
	
	public Image(final BufferedImage image)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams,  image, null );
			}
		};
	}
	
	public Image(final BufferedImage image, final BufferedImage hoverImage)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, hoverImage );
			}
		};
	}
	

	
	public Image(final SVGDiagram image, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, null, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final SVGDiagram image, final SVGDiagram hoverImage, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, hoverImage, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final SVGDiagram image, final SVGDiagram hoverImage, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, hoverImage, imageWidth );
			}
		};
	}
	
	public Image(final SVGDiagram image)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, null );
			}
		};
	}
	
	public Image(final SVGDiagram image, final SVGDiagram hoverImage)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, image, hoverImage );
			}
		};
	}
	

	
	public Image(final File imageFile, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFile, null, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final File imageFile, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFile, null, imageWidth );
			}
		};
	}
	
	public Image(final File imageFile, final File hoverImageFile, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFile, hoverImageFile, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final File imageFile, final File hoverImageFile, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFile, hoverImageFile, imageWidth );
			}
		};
	}
	
	public Image(final File imageFile)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFile, null );
			}
		};
	}
	
	public Image(final File imageFile, final File hoverImageFile)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFile, hoverImageFile );
			}
		};
	}
	
	public Image(final String imageFilename, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFilename, null, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final String imageFilename, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFilename, null, imageWidth );
			}
		};
	}
	
	public Image(final String imageFilename, final String hoverImageFilename, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFilename, hoverImageFilename, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final String imageFilename, final String hoverImageFilename, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFilename, hoverImageFilename, imageWidth );
			}
		};
	}
	
	public Image(final String imageFilename)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFilename, null );
			}
		};
	}
	
	public Image(final String imageFilename, final String hoverImageFilename)
	{
		imageFactory = new ImageFactory()
		{
			public LSImage create(ElementStyleParams styleParams)
			{
				return new LSImage( styleParams, imageFilename, hoverImageFilename );
			}
		};
	}
	
	public static Image systemImage(String iconName)
	{
		return new Image( "images/" + iconName + ".png" );
	}


	
	public static Image svgFromSource(final String image, final double imageWidth, final double imageHeight)
	{
		return new Image( srcToSVG( image ), imageWidth, imageHeight );
	}
	
	public static Image svgFromSource(final String image, final String hoverImage, final double imageWidth, final double imageHeight)
	{
		return new Image( srcToSVG( image ), srcToSVG( hoverImage ), imageWidth, imageHeight );
	}
	
	public static Image svgFromSource(final String image, final String hoverImage, final double imageWidth)
	{
		return new Image( srcToSVG( image ), srcToSVG( hoverImage ), imageWidth );
	}
	
	public static Image svgFromSource(final String image)
	{
		return new Image( srcToSVG( image ) );
	}
	
	public static Image svgFromSource(final String image, final String hoverImage)
	{
		return new Image( srcToSVG( image ), srcToSVG( hoverImage ) );
	}
	
	
	private static SVGDiagram srcToSVG(String source)
	{
		StringReader reader = new StringReader( source );
		URI uri = SVGCache.getSVGUniverse().loadSVG( reader, "svgImage", true );
		return SVGCache.getSVGUniverse().getDiagram( uri );
	}
	

	
	
	
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return imageFactory.create( Primitive.contentLeafParams.get( style ) );
	}
}
