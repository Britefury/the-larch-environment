//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;

public abstract class LSContainerNonOverlayed extends LSContainer
{
	protected final static int FLAGS_CONTAINERNONOVERLAYED_END = FLAGS_CONTAINER_END;

	//
	// Constructors
	//
	
	public LSContainerNonOverlayed()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public LSContainerNonOverlayed(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}

	
	
	@Override
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawSelfBackground( graphics, areaBox );
		
		AABox2 clipBox = getLocalVisibleBoundsClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			// Visibility culling can be expensive - only perform it if there are more than a certain number of child elements
			for (LSElement child: registeredChildren)
			{
				if ( child.getAABoxInParentSpace().intersects( areaBox ) )
				{
					child.getLocalToParentXform().apply( graphics );
					child.handleDrawBackground( graphics, child.getParentToLocalXform().transform( areaBox ) );
					graphics.setTransform( currentTransform );
				}
			}
		}
	}
	
	@Override
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawSelf( graphics, areaBox );
		
		AABox2 clipBox = getLocalVisibleBoundsClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			for (LSElement child: registeredChildren)
			{
				if ( child.getAABoxInParentSpace().intersects( areaBox ) )
				{
					child.getLocalToParentXform().apply( graphics );
					child.handleDraw( graphics, child.getParentToLocalXform().transform( areaBox ) );
					graphics.setTransform( currentTransform );
				}
			}
		}
	}
}
