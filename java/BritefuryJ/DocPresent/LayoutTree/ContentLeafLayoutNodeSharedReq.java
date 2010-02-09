//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.Math.Point2;

public abstract class ContentLeafLayoutNodeSharedReq extends LeafLayoutNodeSharedReq
{
	public ContentLeafLayoutNodeSharedReq(DPContentLeaf element, LReqBox reqBox)
	{
		super( element, reqBox );
	}

	
	public DPContentLeaf getContentLeafAbove(Point2 localPos, boolean bSkipWhitespace)
	{
		return getContentLeafAboveOrBelow( localPos, false, bSkipWhitespace );
	}
	
	public DPContentLeaf getContentLeafBelow(Point2 localPos, boolean bSkipWhitespace)
	{
		return getContentLeafAboveOrBelow( localPos, true, bSkipWhitespace );
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelow(Point2 localPos, boolean bBelow, boolean bSkipWhitespace)
	{
		DPWidget element = getElement();
		DPContainer parent = element.getParent();
		BranchLayoutNode branchLayout = parent != null  ?  (BranchLayoutNode)parent.getValidLayoutNodeOfClass( BranchLayoutNode.class )  :  null;
		
		if ( branchLayout != null )
		{
			return branchLayout.getContentLeafAboveOrBelowFromChild( element, bBelow, element.getLocalPointRelativeToAncestor( branchLayout.getElement(), localPos ), bSkipWhitespace );
		}
		else
		{
			return null;
		}
	}

	
	
	public DPContentLeaf getLeftContentLeaf()
	{
		return (DPContentLeaf)getElement();
	}

	public DPContentLeaf getRightContentLeaf()
	{
		return (DPContentLeaf)getElement();
	}

	public DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		DPContentLeaf element = (DPContentLeaf)getElement();
		if ( bSkipWhitespace && element.isWhitespace() )
		{
			return null;
		}
		else
		{
			return element;
		}
	}
	
	public DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		if ( filter == null  ||  filter.testElement( element ) )
		{
			return element;
		}
		else
		{
			return null;
		}
	}
}
