//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeLineBreak;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;

public class LSLineBreak extends LSElement
{
	public LSLineBreak()
	{
		super( );
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	public LSLineBreak(ElementStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeLineBreak( this );
	}
	
	
	//
	//
	// Line break
	//
	//
	
	public int computeLineBreakCost()
	{
		int cost = 0;
		LSContainer w = getParent();
		
		// Accumulate line break costs, until we encounter a paragraph element		
		while ( w != null  &&  !( w instanceof LSParagraph ) )
		{
			cost += w.getParagraphLinebreakCostModifier();
			w = w.getParent();
		}
		
		return cost;
	}



    //
    //
    // TEXT REPRESENTATION METHODS
    //
    //

    @Override
    public String getLeafTextRepresentation()
    {
        return "";
    }


    //
    //
    // VALUE METHODS
    //
    //

    @Override
    public Object getDefaultValue()
    {
        return null;
    }
}
