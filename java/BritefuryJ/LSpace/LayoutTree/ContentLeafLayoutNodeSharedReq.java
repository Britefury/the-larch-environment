//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSContentLeaf;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LReqBox;
import BritefuryJ.Math.Point2;

public abstract class ContentLeafLayoutNodeSharedReq extends LeafLayoutNodeSharedReq implements ContentLeafLayoutNodeInterface
{
	public ContentLeafLayoutNodeSharedReq(LSContentLeaf element, LReqBox reqBox)
	{
		super( element, reqBox );
	}

	
	public LSContentLeafEditable getEditableContentLeafAbove(Point2 localPos)
	{
		return getEditableContentLeafAboveOrBelow( localPos, false );
	}
	
	public LSContentLeafEditable getEditableContentLeafBelow(Point2 localPos)
	{
		return getEditableContentLeafAboveOrBelow( localPos, true );
	}
	
	public LSContentLeafEditable getEditableContentLeafAboveOrBelow(Point2 localPos, boolean bBelow)
	{
		LSContainer parent = element.getParent();
		BranchLayoutNode branchLayout = parent != null  ?  (BranchLayoutNode)parent.getValidLayoutNodeOfClass( BranchLayoutNode.class )  :  null;
		
		if ( branchLayout != null )
		{
			return branchLayout.getEditableContentLeafAboveOrBelowFromChild( element, bBelow, element.getLocalPointRelativeToAncestor( branchLayout.element, localPos ) );
		}
		else
		{
			return null;
		}
	}

	
	
	public LSContentLeaf getLeftContentLeaf()
	{
		return (LSContentLeaf)element;
	}

	public LSContentLeaf getRightContentLeaf()
	{
		return (LSContentLeaf)element;
	}

	public LSContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		LSContentLeaf leafElement = (LSContentLeaf)element;
		if ( leafElement.isEditable() )
		{
			return (LSContentLeafEditable)leafElement;
		}
		else
		{
			return null;
		}
	}
	
	public LSElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
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
