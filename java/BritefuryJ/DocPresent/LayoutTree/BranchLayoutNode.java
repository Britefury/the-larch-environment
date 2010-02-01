//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;


public abstract class BranchLayoutNode extends LayoutNode
{
	public void onChildListModified()
	{
	}
	


	
	public DPContentLeaf getLeftContentLeaf()
	{
		// Check the child nodes
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (DPWidget w: navList)
			{
				DPContentLeaf l = w.getLayoutNode().getLeftContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}
	
	public DPContentLeaf getRightContentLeaf()
	{
		// Check the child nodes
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (int i = navList.size() - 1; i >= 0; i--)
			{
				DPWidget w = navList.get( i );
				DPContentLeaf l = w.getLayoutNode().getRightContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}

	public DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		List<DPWidget> navList = verticalNavigationList();
		if ( navList != null )
		{
			if ( bBottom )
			{
				for (int i = navList.size() - 1; i >= 0; i--)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getLayoutNode().getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
					if ( l != null )
					{
						return l;
					}
				}
			}
			else
			{
				for (DPWidget w: navList)
				{
					DPContentLeaf l = w.getLayoutNode().getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			return null;
		}
		else
		{
			navList = horizontalNavigationList();
			if ( navList != null )
			{
				double closestDistance = 0.0;
				DPContentLeaf closestNode = null;
				for (DPWidget item: navList)
				{
					AABox2 bounds = item.getLocalAABox();
					double lower = item.getLocalPointRelativeToRoot( bounds.getLower() ).x;
					double upper = item.getLocalPointRelativeToRoot( bounds.getUpper() ).x;
					if ( cursorPosInRootSpace.x >=  lower  &&  cursorPosInRootSpace.x <= upper )
					{
						DPContentLeaf l = item.getLayoutNode().getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
					else
					{
						double distance;
						if ( cursorPosInRootSpace.x < lower )
						{
							// Cursor to the left of the box
							distance = lower - cursorPosInRootSpace.x;
						}
						else // cursorPosInRootSpace.x > upper
						{
							// Cursor to the right of the box
							distance = cursorPosInRootSpace.x - upper;
						}
						
						if ( closestNode == null  ||  distance < closestDistance )
						{
							DPContentLeaf l = item.getLayoutNode().getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
							if ( l != null )
							{
								closestDistance = distance;
								closestNode = l;
							}
						}
					}
				}
				
				if ( closestNode != null )
				{
					return closestNode;
				}
			}
			
			return null;
		}
	}
	
	
	public DPContentLeaf getContentLeafToLeftFromChild(DPWidget child)
	{
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getLayoutNode().getRightContentLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		DPWidget element = getElement();
		DPContainer parent = element.getParent();
		BranchLayoutNode parentBranchLayout = parent != null  ?  (BranchLayoutNode)parent.getValidLayoutNodeOfClass( BranchLayoutNode.class )  :  null;
		
		if ( parentBranchLayout != null )
		{
			return parentBranchLayout.getContentLeafToLeftFromChild( element );
		}
		else
		{
			return null;
		}
	}
	
	public DPContentLeaf getContentLeafToRightFromChild(DPWidget child)
	{
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < navList.size(); i++)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getLayoutNode().getLeftContentLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		DPWidget element = getElement();
		DPContainer parent = element.getParent();
		BranchLayoutNode parentBranchLayout = parent != null  ?  (BranchLayoutNode)parent.getValidLayoutNodeOfClass( BranchLayoutNode.class )  :  null;
		
		if ( parentBranchLayout != null )
		{
			return parentBranchLayout.getContentLeafToRightFromChild( element );
		}
		else
		{
			return null;
		}
	}
	
	public DPContentLeaf getContentLeafAboveOrBelowFromChild(DPWidget child, boolean bBelow, Point2 localPos, boolean bSkipWhitespace)
	{
		List<DPWidget> navList = verticalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				Point2 posInRootSpace = getElement().getLocalPointRelativeToRoot( localPos );
				if ( bBelow )
				{
					for (int i = index + 1; i < navList.size(); i++)
					{
						DPWidget w = navList.get( i );
						DPContentLeaf l = w.getLayoutNode().getTopOrBottomContentLeaf( false, posInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
				}
				else
				{
					for (int i = index - 1; i >= 0; i--)
					{
						DPWidget w = navList.get( i );
						DPContentLeaf l = w.getLayoutNode().getTopOrBottomContentLeaf( true, posInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
				}
			}
		}
		
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

}
