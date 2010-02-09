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
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class DPLineBreak extends DPBin
{
	public DPLineBreak(ElementContext context)
	{
		super( context );
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	public DPLineBreak(ElementContext context, ContainerStyleSheet styleSheet)
	{
		super( context, styleSheet );
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	
	
	public void initialiseLineBreakRequisition(LReqBoxInterface reqBox)
	{
		reqBox.setLineBreakCost( computeLineBreakCost() );
	}

	
	private int computeLineBreakCost()
	{
		int cost = 0;
		DPWidget w = this;
		
		while ( w != null  &&  !( w instanceof DPParagraph ) )
		{
			w = w.parent;
			cost++;
		}
		
		return cost;
	}
}
