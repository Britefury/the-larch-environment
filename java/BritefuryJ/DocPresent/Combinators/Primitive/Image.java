//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.awt.image.BufferedImage;
import java.io.File;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPImage;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;

public class Image extends Pres
{
	private static abstract class ImageFactory
	{
		public abstract DPImage create(ContentLeafStyleParams styleParams);
	}
	
	private ImageFactory imageFactory;
	
	
	
	
	public Image(final BufferedImage image, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", image, null, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final BufferedImage image, final BufferedImage hoverImage, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", image, hoverImage, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final BufferedImage image, final BufferedImage hoverImage, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", image, hoverImage, imageWidth );
			}
		};
	}
	
	public Image(final BufferedImage image)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", image, null );
			}
		};
	}
	
	public Image(final BufferedImage image, final BufferedImage hoverImage)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", image, hoverImage );
			}
		};
	}
	
	public Image(final File imageFile, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFile, null, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final File imageFile, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFile, null, imageWidth );
			}
		};
	}
	
	public Image(final File imageFile, final File hoverImageFile, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFile, hoverImageFile, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final File imageFile, final File hoverImageFile, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFile, hoverImageFile, imageWidth );
			}
		};
	}
	
	public Image(final File imageFile)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFile, null );
			}
		};
	}
	
	public Image(final File imageFile, final File hoverImageFile)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFile, hoverImageFile );
			}
		};
	}
	
	public Image(final String imageFilename, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFilename, null, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final String imageFilename, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFilename, null, imageWidth );
			}
		};
	}
	
	public Image(final String imageFilename, final String hoverImageFilename, final double imageWidth, final double imageHeight)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFilename, hoverImageFilename, imageWidth, imageHeight );
			}
		};
	}
	
	public Image(final String imageFilename, final String hoverImageFilename, final double imageWidth)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFilename, hoverImageFilename, imageWidth );
			}
		};
	}
	
	public Image(final String imageFilename)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFilename, null );
			}
		};
	}
	
	public Image(final String imageFilename, final String hoverImageFilename)
	{
		imageFactory = new ImageFactory()
		{
			public DPImage create(ContentLeafStyleParams styleParams)
			{
				return new DPImage( styleParams, "", imageFilename, hoverImageFilename );
			}
		};
	}
	
	public static Image systemIcon(String iconName)
	{
		return new Image( "icons/" + iconName + ".png" );
	}

	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		return imageFactory.create( Primitive.contentLeafParams.get( ctx.getStyle() ) );
	}
}
