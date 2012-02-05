//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;

public abstract class DPContainerNonOverlayed extends DPContainer
{
	//
	// Constructors
	//
	
	public DPContainerNonOverlayed()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public DPContainerNonOverlayed(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}

	
	
	@Override
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawSelfBackground( graphics, areaBox );
		
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			// Visibility culling can be expensive - only perform it if there are more than a certain number of child elements
			for (DPElement child: registeredChildren)
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
		
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			for (DPElement child: registeredChildren)
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
