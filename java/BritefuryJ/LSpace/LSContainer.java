//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.LayoutTree.BranchLayoutNode;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;




public abstract class LSContainer extends LSElement
{
	public static class CouldNotFindChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected final static int FLAGS_CONTAINER_END = FLAGS_ELEMENT_END;

	
	
	protected ArrayList<LSElement> registeredChildren = new ArrayList<LSElement>();			// Replace with array; operations like insert etc are hardly used at all
	
	
	
	
	//
	// Constructors
	//
	
	public LSContainer()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public LSContainer(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}
	
	
	
	//
	// Geometry methods
	//
	
	protected Xform2 getAllocationSpaceToLocalSpaceXform(LSElement child)
	{
		return Xform2.identity;
	}
	

	
	
	
	//
	// Child registration methods
	//
	
	protected LSElement registerChild(LSElement child)
	{
		child.unparent();
		
		child.setParent( this, rootElement );
		
		if ( isRealised() )
		{
			child.handleRealise();
		}
		
		return child;
	}
	
	protected void unregisterChild(LSElement child)
	{
		if ( isRealised() )
		{
			child.handleUnrealise( child );
		}
		
		child.setParent( null, null );
	}
	
	
	
	protected void onChildListModified()
	{
		onSubtreeStructureChanged();
		
		onDebugPresentationStateChanged();
		
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			branchLayout.onChildListModified();
		}
	}
	
	
	
	
	
	//
	// Tree structure methods
	//
	
	
	public int computeSubtreeSize()
	{
		int subtreeSize = 1;
		for (LSElement child: registeredChildren)
		{
			subtreeSize += child.computeSubtreeSize();
		}
		return subtreeSize;
	}
	
	
	protected abstract void replaceChildWithEmpty(LSElement child);
	
	protected abstract void replaceChild(LSElement child, LSElement replacement);
	
	public boolean hasChild(LSElement c)
	{
		for (LSElement child: registeredChildren)
		{
			if ( c == child )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	protected List<LSElement> getInternalChildren()
	{
		return registeredChildren;
	}
	
	public List<LSElement> getLayoutChildren()
	{
		return registeredChildren;
	}
	
	public abstract List<LSElement> getChildren();

	
	public boolean areChildrenInOrder(LSElement child0, LSElement child1)
	{
		List<LSElement> children = getInternalChildren();
		int index0 = children.indexOf( child0 );
		int index1 = children.indexOf( child1 );
		
		if ( index0 != -1  &&  index1 != -1 )
		{
			return index0 < index1;
		}
		else
		{
			throw new CouldNotFindChildException();
		}
	}
	
	
	public abstract boolean isSingleElementContainer();
	
	
	
	
	protected List<LSElement> getSearchChildren()
	{
		return getChildren();
	}

	
	
	
	
	protected void onSubtreeStructureChanged()
	{
		invalidateCachedValues();
		
		if ( parent != null )
		{
			parent.onSubtreeStructureChanged();
		}
	}
	
	
	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	public int getParagraphLinebreakCostModifier()
	{
		return 0;
	}
	
	
	
	
	//
	// Event handling methods
	//
	
	protected void onLeaveIntoChild(PointerMotionEvent event, LSElement child)
	{
	}
	
	protected void onEnterFromChild(PointerMotionEvent event, LSElement child)
	{
	}
	
	
	
	
	
	protected void childRedrawRequest(LSElement child, AABox2 childBox)
	{
		Xform2 childToContainer = child.getLocalToParentXform();
		AABox2 localBox = childToContainer.transform( childBox );
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			localBox = localBox.intersection( getLocalAABox() );
		}
		if ( !localBox.isEmpty() )
		{
			queueRedraw( localBox );
		}
	}
	
	
	
	protected LSElement getFirstChildAtLocalPoint(Point2 localPos)
	{
		for (LSElement child: registeredChildren)
		{
			if ( child.containsParentSpacePoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	
	protected LSElement getLastChildAtLocalPoint(Point2 localPos)
	{
		for (int i = registeredChildren.size() - 1; i >= 0; i--)
		{
			LSElement child = registeredChildren.get( i );
			if ( child.containsParentSpacePoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	

	protected LSElement getFirstLowestChildAtLocalPoint(Point2 localPos)
	{
		LSElement x = this;
		Point2 p = localPos;
		
		while ( true )
		{
			LSElement child = x.getFirstChildAtLocalPoint( p );
			if ( child != null )
			{
				p = child.getParentToLocalXform().transform( p );
				x = child;
			}
			else
			{
				break;
			}
		}
		
		return x;
	}
	
	protected LSElement getLastLowestChildAtLocalPoint(Point2 localPos)
	{
		LSElement x = this;
		Point2 p = localPos;
		
		while ( true )
		{
			LSElement child = x.getLastChildAtLocalPoint( p );
			if ( child != null )
			{
				p = child.getParentToLocalXform().transform( p );
				x = child;
			}
			else
			{
				break;
			}
		}
		
		return x;
	}

	
	
	
	
	
	
	//
	// Regular events
	//
	
	
	protected void handleRealise()
	{
		super.handleRealise();
		for (LSElement child: registeredChildren)
		{
			child.handleRealise();
		}
	}
	
	protected void handleUnrealise(LSElement unrealiseRoot)
	{
		for (LSElement child: registeredChildren)
		{
			if ( child == this )
			{
				throw new RuntimeException( "LSContainer.handleUnrealise: recursive container" );
			}
			child.handleUnrealise( unrealiseRoot );
		}
		super.handleUnrealise( unrealiseRoot );
	}
	
		
	
	
	protected void setRootElement(LSRootElement area)
	{
		super.setRootElement( area );
		
		for (LSElement child: registeredChildren)
		{
			child.setRootElement( area );
		}
	}


	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//

	public LSContentLeaf getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			for (LSElement child: getInternalChildren())
			{
				LSContentLeaf leaf = child.getFirstLeafInSubtree( branchFilter, leafFilter );
				if ( leaf != null )
				{
					return leaf;
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	public LSContentLeaf getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			List<LSElement> children = getInternalChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				LSContentLeaf leaf = children.get( i ).getLastLeafInSubtree( branchFilter, leafFilter );
				if ( leaf != null )
				{
					return leaf;
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}
	
	

	
	protected LSContentLeaf getPreviousLeafFromChild(LSElement child, ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<LSElement> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					LSElement e = children.get( i );
					LSContentLeaf l = e.getLastLeafInSubtree( branchFilter, leafFilter );
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			if ( parent != null )
			{
				return parent.getPreviousLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
			}
		}
		
		return null;
	}
	
	protected LSContentLeaf getNextLeafFromChild(LSElement child, ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<LSElement> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < children.size(); i++)
				{
					LSElement e = children.get( i );
					LSContentLeaf l = e.getFirstLeafInSubtree( branchFilter, leafFilter );
					if ( l != null )
					{
						return l;
					}
				}
			}
		
			if ( parent != null )
			{
				return parent.getNextLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
			}
		}

		return null;
	}
	

	
	//
	// CONTENT LEAF NAVIGATION METHODS
	//
	
	protected LSContentLeaf getContentLeafToLeftFromChild(LSElement child)
	{
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			return branchLayout.getContentLeafToLeftFromChild( child );
		}
		else
		{
			return null;
		}
	}
	
	protected LSContentLeaf getContentLeafToRightFromChild(LSElement child)
	{
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			return branchLayout.getContentLeafToRightFromChild( child );
		}
		else
		{
			return null;
		}
	}
	
	public LSContentLeafEditable getEditableContentLeafAboveOrBelowFromChild(LSElement child, boolean bBelow, Point2 localPos)
	{
		BranchLayoutNode branchLayout = (BranchLayoutNode)getValidLayoutNodeOfClass( BranchLayoutNode.class );
		if ( branchLayout != null )
		{
			return branchLayout.getEditableContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( branchLayout.getElement(), localPos ) );
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	//
	//
	// VALUE METHODS
	//
	//
	
	public Object getDefaultValue()
	{
		// Use getChildren() rather than getInternalChildren(), as these are the children that are expose to the outside world, as opposed to some extra elements that may
		// introduce unexpected objects into the value
		if ( isSingleElementContainer() )
		{
			List<LSElement> children = getChildren();
			if ( children.size() > 1 )
			{
				throw new RuntimeException( "Single element container should not have more than 1 child" );
			}
			else
			{
				if ( children.size() == 1 )
				{
					return children.get( 0 ).getValue();
				}
				else
				{
					return null;
				}
			}
		}
		else
		{
			ArrayList<Object> value = new ArrayList<Object>();
			for (LSElement child: getChildren())
			{
				value.add( child.getValue() );
			}
			return value;
		}
	}

	
	//
	//
	// SEQUENTIAL CONTENT METHODS
	//
	//
	
	public List<LSElement> getChildrenInSequentialOrder()
	{
		return getInternalChildren();
	}

	
	
	
	
	
	
	
	//
	// Meta-element
	//
	
	public Pres createMetaElement(FragmentView ctx, SimpleAttributeTable state)
	{
		ArrayList<Object> metaChildren = new ArrayList<Object>();
		for (LSElement child: getChildren())
		{
			if ( child != null )
			{
				Pres metaChild = new InnerFragment( child ); 
				metaChildren.add( metaChild );
			}
			else
			{
				System.out.println( "DPContainer.createMetaElement(): null child in " + getClass().getName() );
			}
		}
		Pres metaChildrenColumn = new Column( metaChildren.size() - 1, metaChildren );
		
		Pres indentMetaChildren = metaChildrenColumn.padX( 25.0, 0.0 );
		
		return new Column( new Pres[] { createDebugPresentationHeader(), indentMetaChildren } );
	}
}
