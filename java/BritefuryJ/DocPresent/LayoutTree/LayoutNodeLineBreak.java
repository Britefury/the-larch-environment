//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;

public class LayoutNodeLineBreak extends LayoutNodeBox
{
	public LayoutNodeLineBreak(DPLineBreak element)
	{
		super( element );
	}



	protected void updateRequisitionX()
	{
		super.updateRequisitionX();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPLineBreak lineBreak = (DPLineBreak)element;
		
		lineBreak.initialiseLineBreakRequisition( layoutReqBox );
	}
}
