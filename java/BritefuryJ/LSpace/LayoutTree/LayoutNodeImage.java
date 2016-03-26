//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
