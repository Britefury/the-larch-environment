//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPImage;

public class LayoutNodeImage extends ContentLeafLayoutNode
{
	public LayoutNodeImage(DPImage element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		DPImage image = (DPImage)element;
		
		double w = image.getImageWidth();
		layoutReqBox.setRequisitionX( w, w );
	}

	protected void updateRequisitionY()
	{
		DPImage image = (DPImage)element;
		
		layoutReqBox.setRequisitionY( image.getImageHeight(), 0.0 );
	}
}
