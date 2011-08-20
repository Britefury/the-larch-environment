//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.LayoutTree.BranchLayoutNode;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;




public abstract class DPContainer extends DPElement
{
	public static class CouldNotFindChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected final static int FLAGS_CONTAINER_END = FLAGS_ELEMENT_END;

	
	
	protected ArrayList<DPElement> registeredChildren = new ArrayList<DPElement>();			// Replace with array; operations like insert etc are hardly used at all
	
	
	
	
	//
	// Constructors
	//
	
	public DPContainer()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public DPContainer(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}
	
	protected DPContainer(DPContainer element)
	{
		super( element );
	}
	
	
	
	//
	// Geometry methods
	//
	
	protected Xform2 getAllocationSpaceToLocalSpaceXform(DPElement child)
	{
		return Xform2.identity;
	}
	

	
	
	
	//
	// Child registration methods
	//
	
	protected DPElement registerChild(DPElement child)
	{
		child.unparent();
		
		child.setParent( this, rootElement );
		
		if ( isRealised() )
		{
			child.handleRealise();
		}
		
		return child;
	}
	
	protected void unregisterChild(DPElement child)
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
		for (DPElement child: registeredChildren)
		{
			subtreeSize += child.computeSubtreeSize();
		}
		return subtreeSize;
	}
	
	
	protected abstract void replaceChildWithEmpty(DPElement child);
	
	protected abstract void replaceChild(DPElement child, DPElement replacement);
	
	public boolean hasChild(DPElement c)
	{
		for (DPElement child: registeredChildren)
		{
			if ( c == child )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	protected List<DPElement> getInternalChildren()
	{
		return registeredChildren;
	}
	
	public List<DPElement> getLayoutChildren()
	{
		return registeredChildren;
	}
	
	public abstract List<DPElement> getChildren();

	
	public boolean areChildrenInOrder(DPElement child0, DPElement child1)
	{
		List<DPElement> children = getInternalChildren();
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
	
	
	
	
	protected List<DPElement> getSearchChildren()
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
	//
	// MARKER METHODS
	//
	//
	
	public Marker markerAtStart()
	{
		for (DPElement child: getChildren())
		{
			Marker m = child.markerAtStart();
			if ( m != null )
			{
				return m;
			}
		}
		
		return super.markerAtStart();
	}
	
	public Marker markerAtEnd()
	{
		List<DPElement> children = getChildren();
		for (int i = children.size() - 1; i >= 0; i--)
		{
			DPElement child = children.get( i );
			Marker m = child.markerAtEnd();
			if ( m != null )
			{
				return m;
			}
		}
		
		return super.markerAtEnd();
	}
	
	
	@Override
	public void moveMarkerToStart(Marker m)
	{
		m.moveTo( markerAtStart() );
	}
	
	@Override
	public void moveMarkerToEnd(Marker m)
	{
		m.moveTo( markerAtEnd() );
	}
	
	
	
	//
	// Event handling methods
	//
	
	protected void onLeaveIntoChild(PointerMotionEvent event, DPElement child)
	{
	}
	
	protected void onEnterFromChild(PointerMotionEvent event, DPElement child)
	{
	}
	
	
	
	
	
	protected void childRedrawRequest(DPElement child, AABox2 childBox)
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
	
	
	
	protected DPElement getFirstChildAtLocalPoint(Point2 localPos)
	{
		for (DPElement child: registeredChildren)
		{
			if ( child.containsParentSpacePoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	
	protected DPElement getLastChildAtLocalPoint(Point2 localPos)
	{
		for (int i = registeredChildren.size() - 1; i >= 0; i--)
		{
			DPElement child = registeredChildren.get( i );
			if ( child.containsParentSpacePoint( localPos ) )
			{
				return child;
			}
		}
		
		return null;
	}
	

	protected DPElement getFirstLowestChildAtLocalPoint(Point2 localPos)
	{
		DPElement x = this;
		Point2 p = localPos;
		
		while ( true )
		{
			DPElement child = x.getFirstChildAtLocalPoint( p );
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
	
	protected DPElement getLastLowestChildAtLocalPoint(Point2 localPos)
	{
		DPElement x = this;
		Point2 p = localPos;
		
		while ( true )
		{
			DPElement child = x.getLastChildAtLocalPoint( p );
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
		for (DPElement child: registeredChildren)
		{
			child.handleRealise();
		}
	}
	
	protected void handleUnrealise(DPElement unrealiseRoot)
	{
		for (DPElement child: registeredChildren)
		{
			child.handleUnrealise( unrealiseRoot );
		}
		super.handleUnrealise( unrealiseRoot );
	}
	
	
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		super.handleDrawBackground( graphics, areaBox );
		
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			for (DPElement child: registeredChildren)
			{
				if ( child.getAABoxInParentSpace().intersects( areaBox ) )
				{
					child.getLocalToParentXform().apply( graphics );
					child.handleDrawBackground( graphics, child.getParentToLocalXform().transform( areaBox ) );
					graphics.setTransform( currentTransform );
				}
			}
		}
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		super.handleDraw( graphics, areaBox );
		
		AABox2 clipBox = getLocalClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();
			for (DPElement child: registeredChildren)
			{
				if ( child.getAABoxInParentSpace().intersects( areaBox ) )
				{
					child.getLocalToParentXform().apply( graphics );
					child.handleDraw( graphics, child.getParentToLocalXform().transform( areaBox ) );
					graphics.setTransform( currentTransform );
				}
			}
		}
	}
	
	
	
	
	protected void setRootElement(PresentationComponent.RootElement area)
	{
		super.setRootElement( area );
		
		for (DPElement child: registeredChildren)
		{
			child.setRootElement( area );
		}
	}


	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//

	public DPContentLeaf getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			for (DPElement child: getInternalChildren())
			{
				DPContentLeaf leaf = child.getFirstLeafInSubtree( branchFilter, leafFilter );
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

	public DPContentLeaf getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( branchFilter == null  ||  branchFilter.testElement( this ) )
		{
			List<DPElement> children = getInternalChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				DPContentLeaf leaf = children.get( i ).getLastLeafInSubtree( branchFilter, leafFilter );
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
	
	

	
	protected DPContentLeaf getPreviousLeafFromChild(DPElement child, ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<DPElement> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					DPElement e = children.get( i );
					DPContentLeaf l = e.getLastLeafInSubtree( branchFilter, leafFilter );
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
	
	protected DPContentLeaf getNextLeafFromChild(DPElement child, ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( subtreeRootFilter == null  ||  subtreeRootFilter.testElement( this ) )
		{
			List<DPElement> children = getInternalChildren();
			int index = children.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < children.size(); i++)
				{
					DPElement e = children.get( i );
					DPContentLeaf l = e.getFirstLeafInSubtree( branchFilter, leafFilter );
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
	
	protected DPContentLeaf getContentLeafToLeftFromChild(DPElement child)
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
	
	protected DPContentLeaf getContentLeafToRightFromChild(DPElement child)
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
	
	public DPContentLeafEditable getEditableContentLeafAboveOrBelowFromChild(DPElement child, boolean bBelow, Point2 localPos)
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
			List<DPElement> children = getChildren();
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
			for (DPElement child: getChildren())
			{
				value.add( child.getValue() );
			}
			return value;
		}
	}

	
	//
	//
	// STREAM VALUE METHODS
	//
	//
	
	public List<DPElement> getChildrenInSequentialOrder()
	{
		return getInternalChildren();
	}

	
	
	
	
	
	
	
	//
	// Meta-element
	//
	
	public Pres createMetaElement(FragmentView ctx, SimpleAttributeTable state)
	{
		ArrayList<Object> metaChildren = new ArrayList<Object>();
		for (DPElement child: getChildren())
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
		Pres metaChildrenColumn = new Column( metaChildren, metaChildren.size() - 1 );
		
		Pres indentMetaChildren = metaChildrenColumn.padX( 25.0, 0.0 );
		
		return new Column( new Pres[] { createDebugPresentationHeader(), indentMetaChildren } );
	}
}
