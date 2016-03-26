//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
