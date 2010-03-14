//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeLineBreak;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;

public class DPLineBreak extends DPBox
{
	public DPLineBreak()
	{
		super( );
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	public DPLineBreak(ContainerStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	
	
	public void initialiseLineBreakRequisition(LReqBoxInterface reqBox)
	{
		reqBox.setLineBreakCost( computeLineBreakCost() );
	}

	
	private int computeLineBreakCost()
	{
		int cost = 0;
		DPContainer w = getParent();
		
		while ( w != null  &&  !( w instanceof DPParagraph ) )
		{
			cost += w.getParagraphLinebreakCostModifier();
			w = w.getParent();
		}
		
		return cost;
	}
}
