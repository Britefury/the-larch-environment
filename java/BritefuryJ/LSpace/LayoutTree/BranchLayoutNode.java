//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.List;

import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSContentLeaf;
import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;


public abstract class BranchLayoutNode extends LayoutNode
{
	public BranchLayoutNode(LSElement element)
	{
		super( element );
	}
	
	
	
	public void onChildListModified()
	{
	}
	


	
	public LSContentLeaf getLeftContentLeaf()
	{
		// Check the child nodes
		List<LSElement> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (LSElement w: navList)
			{
				LSContentLeaf l = w.getLayoutNode().getLeftContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}
	
	public LSContentLeaf getRightContentLeaf()
	{
		// Check the child nodes
		List<LSElement> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (int i = navList.size() - 1; i >= 0; i--)
			{
				LSElement w = navList.get( i );
				LSContentLeaf l = w.getLayoutNode().getRightContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}

	public LSContentLeafEditable getLeftEditableContentLeaf()
	{
		// Check the child nodes
		List<LSElement> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (LSElement w: navList)
			{
				LSContentLeafEditable l = w.getLayoutNode().getLeftEditableContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}
	
	public LSContentLeafEditable getRightEditableContentLeaf()
	{
		// Check the child nodes
		List<LSElement> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (int i = navList.size() - 1; i >= 0; i--)
			{
				LSElement w = navList.get( i );
				LSContentLeafEditable l = w.getLayoutNode().getRightEditableContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}

	public LSContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		List<LSElement> navList = verticalNavigationList();
		if ( navList != null )
		{
			if ( bBottom )
			{
				for (int i = navList.size() - 1; i >= 0; i--)
				{
					LSElement w = navList.get( i );
					LSContentLeafEditable l = w.getLayoutNode().getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
					if ( l != null )
					{
						return l;
					}
				}
			}
			else
			{
				for (LSElement w: navList)
				{
					LSContentLeafEditable l = w.getLayoutNode().getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
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
				LSContentLeafEditable closestNode = null;
				for (LSElement item: navList)
				{
					AABox2 bounds = item.getLocalAABox();
					double lower = item.getLocalPointRelativeToRoot( bounds.getLower() ).x;
					double upper = item.getLocalPointRelativeToRoot( bounds.getUpper() ).x;
					if ( cursorPosInRootSpace.x >=  lower  &&  cursorPosInRootSpace.x <= upper )
					{
						LSContentLeafEditable l = item.getLayoutNode().getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
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
							LSContentLeafEditable l = item.getLayoutNode().getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
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
	
	
	public LSContentLeaf getContentLeafToLeftFromChild(LSElement child)
	{
		List<LSElement> navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					LSElement w = navList.get( i );
					LSContentLeaf l = w.getLayoutNode().getRightContentLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		LSContainer parent = element.getParent();
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
	
	public LSContentLeaf getContentLeafToRightFromChild(LSElement child)
	{
		List<LSElement> navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < navList.size(); i++)
				{
					LSElement w = navList.get( i );
					LSContentLeaf l = w.getLayoutNode().getLeftContentLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		LSContainer parent = element.getParent();
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
	
	public LSContentLeafEditable getEditableContentLeafAboveOrBelowFromChild(LSElement child, boolean bBelow, Point2 localPos)
	{
		List<LSElement> navList = verticalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				Point2 posInRootSpace = element.getLocalPointRelativeToRoot( localPos );
				if ( bBelow )
				{
					for (int i = index + 1; i < navList.size(); i++)
					{
						LSElement w = navList.get( i );
						LSContentLeafEditable l = w.getLayoutNode().getTopOrBottomEditableContentLeaf( false, posInRootSpace );
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
						LSElement w = navList.get( i );
						LSContentLeafEditable l = w.getLayoutNode().getTopOrBottomEditableContentLeaf( true, posInRootSpace );
						if ( l != null )
						{
							return l;
						}
					}
				}
			}
		}
		
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

}
