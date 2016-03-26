//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSShape;

public class LayoutNodeShape extends LayoutNodeBlank
{
	public LayoutNodeShape(LSShape element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		LSShape shape = (LSShape)element;
		
		double w = shape.getShapeBounds().getUpperX();
		layoutReqBox.setRequisitionX( w, w );
	}

	protected void updateRequisitionY()
	{
		LSShape shape = (LSShape)element;
		
		layoutReqBox.setRequisitionY( shape.getShapeBounds().getUpperY(), 0.0, 0.0 );
	}
}
