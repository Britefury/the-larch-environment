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
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;

public class DPLineBreak extends DPEmpty
{
	public DPLineBreak()
	{
		super( );
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	public DPLineBreak(ElementStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	protected DPLineBreak(DPLineBreak element)
	{
		super( element );
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPLineBreak clone = new DPLineBreak( this );
		clone.clonePostConstuct( this );
		return clone;
	}
	
	
	
	
	//
	//
	// Line break
	//
	//
	
	public void initialiseLineBreakRequisition(LReqBoxInterface reqBox)
	{
		reqBox.setLineBreakCost( computeLineBreakCost() );
	}

	
	private int computeLineBreakCost()
	{
		int cost = 0;
		DPContainer w = getParent();
		
		// Accumulate line break costs, until we encounter a paragraph element		
		while ( w != null  &&  !( w instanceof DPParagraph ) )
		{
			cost += w.getParagraphLinebreakCostModifier();
			w = w.getParent();
		}
		
		return cost;
	}
}
