//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.Math.Point2;

public abstract class ContentLeafLayoutNode extends LeafLayoutNode implements ContentLeafLayoutNodeInterface
{
	public ContentLeafLayoutNode(DPContentLeaf element)
	{
		super( element );
	}

	
	public DPContentLeafEditable getEditableContentLeafAbove(Point2 localPos)
	{
		return getEditableContentLeafAboveOrBelow( localPos, false );
	}
	
	public DPContentLeafEditable getEditableContentLeafBelow(Point2 localPos)
	{
		return getEditableContentLeafAboveOrBelow( localPos, true );
	}
	
	public DPContentLeafEditable getEditableContentLeafAboveOrBelow(Point2 localPos, boolean bBelow)
	{
		DPWidget element = getElement();
		DPContainer parent = element.getParent();
		BranchLayoutNode branchLayout = parent != null  ?  (BranchLayoutNode)parent.getValidLayoutNodeOfClass( BranchLayoutNode.class )  :  null;
		
		if ( branchLayout != null )
		{
			return branchLayout.getEditableContentLeafAboveOrBelowFromChild( element, bBelow, element.getLocalPointRelativeToAncestor( branchLayout.getElement(), localPos ) );
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

	public DPContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		DPContentLeaf element = (DPContentLeaf)getElement();
		if ( element.isEditable() )
		{
			return (DPContentLeafEditable)element;
		}
		else
		{
			return null;
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
