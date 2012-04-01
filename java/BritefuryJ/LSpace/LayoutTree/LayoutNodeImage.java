//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSImage;

public class LayoutNodeImage extends LayoutNodeBlank
{
	public LayoutNodeImage(LSImage element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		LSImage image = (LSImage)element;
		
		double w = image.getImageWidth();
		layoutReqBox.setRequisitionX( w, w );
	}

	protected void updateRequisitionY()
	{
		LSImage image = (LSImage)element;
		
		layoutReqBox.setRequisitionY( image.getImageHeight(), 0.0 );
	}
}
