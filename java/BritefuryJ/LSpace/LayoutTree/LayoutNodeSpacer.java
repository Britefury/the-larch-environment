//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSSpacer;

public class LayoutNodeSpacer extends LayoutNodeBlank
{
	public LayoutNodeSpacer(LSSpacer element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		LSSpacer spacer = (LSSpacer)element;
		
		double w = spacer.getMinWidth();
		layoutReqBox.setRequisitionX( w, w );
	}

	protected void updateRequisitionY()
	{
		LSSpacer spacer = (LSSpacer)element;
		
		layoutReqBox.setRequisitionY( spacer.getMinHeight(), 0.0 );
	}
}
