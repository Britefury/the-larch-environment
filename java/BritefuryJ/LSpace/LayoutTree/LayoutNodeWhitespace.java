//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.HashMap;

import BritefuryJ.LSpace.LSWhitespace;
import BritefuryJ.LSpace.Layout.LReqBox;

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




	public LayoutNodeWhitespace(LSWhitespace element)
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
