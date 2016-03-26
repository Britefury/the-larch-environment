//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSBox;

public class LayoutNodeBox extends LayoutNodeBlank
{
	public LayoutNodeBox(LSBox element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		LSBox rectangle = (LSBox)element;
		
		double w = rectangle.getMinWidth();
		layoutReqBox.setRequisitionX( w, w );
	}

	protected void updateRequisitionY()
	{
		LSBox rectangle = (LSBox)element;
		
		layoutReqBox.setRequisitionY( rectangle.getMinHeight(), 0.0 );
	}
}
