//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.HashMap;

import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.Layout.LReqBox;

public class LayoutNodeWhitespace extends ContentLeafLayoutNodeSharedReq
{
	private static HashMap<Double, LReqBox> reqBoxes = new HashMap<Double, LReqBox>();
	
	
	private static LReqBox getWhitespaceReqBox(double width)
	{
		LReqBox box = reqBoxes.get( width );
		
		if ( box == null )
		{
			box = new LReqBox();
			box.setRequisitionX( width, width );
			reqBoxes.put( width, box );
		}

		return box;
	}




	public LayoutNodeWhitespace(DPWhitespace element)
	{
		super( element, getWhitespaceReqBox( element.getWhitespaceWidth() ) );
	}

	

	protected void updateRequisitionX()
	{
	}

	protected void updateRequisitionY()
	{
	}
}
