//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import BritefuryJ.Math.Point2;
import org.python.core.PySlice;

import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Util.Jython.JythonIndex;
import BritefuryJ.Util.Jython.JythonSlice;



abstract public class LSContainerSequence extends LSContainerNonOverlayed
{
	protected static final int VISIBILITY_CULLING_THRESHHOLD = 16;
	
	
	public LSContainerSequence(ContainerStyleParams styleParams, LSElement[] items)
	{
		super( styleParams );

		if ( items != null  &&  items.length > 0 )
		{
			// Set contents of @childEntries list
			registeredChildren.addAll( Arrays.asList( items ) );
	
			// Register added entries
			for (LSElement child: items)
			{
				registerChild( child );
			}
		}
	}


	
	//
	//
	// Child list access and modifications
	//
	//
	
	public void setChildren(LSElement items[])
	{
		setChildren( Arrays.asList( items ) );
	}
	
	public void setChildren(List<LSElement> items)
	{
		if ( registeredChildren.isEmpty() )
		{
			if ( items.size() > 0 )
			{
				// Set contents of @childEntries list
				registeredChildren.addAll( items );
		
				// Register added entries
				for (LSElement child: items)
				{
					registerChild( child );
				}
	
				onChildListModified();
				queueResize();
			}
		}
		else if ( registeredChildren.size() == 1  &&  items.size() <= 1 )
		{
			// Special case for when there is only 1 child
			if ( items.size() == 0 )
			{
				unregisterChild( registeredChildren.get( 0 ) );
				registeredChildren.clear();
				onChildListModified();
				queueResize();
			}
			else if ( items.size() == 1 )
			{
				LSElement prevChild = registeredChildren.get( 0 );
				LSElement newChild = items.get( 0 );
				
				if ( newChild != prevChild )
				{
					unregisterChild( prevChild );
					registeredChildren.set( 0, newChild );
					registerChild( newChild );
					onChildListModified();
					queueResize();
				}
			}
		}
		else
		{
			HashSet<LSElement> added, removed;
			
			added = new HashSet<LSElement>( items );
			removed = new HashSet<LSElement>( registeredChildren );
			added.removeAll( registeredChildren );
			removed.removeAll( items );

			
			// Unregister removed entries
			for (LSElement child: removed)
			{
				unregisterChild( child );
			}
			
			// Set contents of @childEntries list
			registeredChildren.clear();
			registeredChildren.addAll( items );
	
			// Register added entries
			for (LSElement child: added)
			{
				registerChild( child );
			}

			onChildListModified();
			queueResize();
		}
	}
	
	
	public void clear()
	{
		// Unregister removed entries
		for (LSElement child: registeredChildren)
		{
			unregisterChild( child );
		}
		
		// Set contents of @childEntries list
		registeredChildren.clear();

		onChildListModified();
		queueResize();
	}

	
	public boolean isSingleElementContainer()
	{
		return false;
	}
	
	

	
	
	public int size()
	{
		return registeredChildren.size();
	}
	
	public int __len__()
	{
		return size();
	}
	
	
	
	public int indexOf(LSElement child)
	{
		return registeredChildren.indexOf( child );
	}
	
	
	public LSElement get(int index)
	{
		return registeredChildren.get( index );
	}
	
	public LSElement __getitem__(int index)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement index out of range" );

		return get( index );
	}
	
	public LSElement[] __getitem__(PySlice slice)
	{
		LSElement[] in = new LSElement[registeredChildren.size()];
		
		for (int i = 0; i < in.length; i++)
		{
			in[i] = registeredChildren.get( i );
		}
		
		return (LSElement[])JythonSlice.arrayGetSlice( in, slice );
	}
	
	
	
	public void set(int index, LSElement child)
	{
		LSElement oldChild = registeredChildren.get( index );
		unregisterChild( oldChild );
		registeredChildren.set( index, child );
		registerChild( child );
		onChildListModified();
		queueResize();
	}
	
	public void __setitem__(int index, LSElement item)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement assignment index out of range" );

		set( index, item );
	}

	@SuppressWarnings("unchecked")
	public void __setitem__(PySlice slice, LSElement[] items)
	{
		HashSet<LSElement> oldEntrySet = new HashSet<LSElement>( registeredChildren );
		
		LSElement[] oldChildArray = registeredChildren.toArray( new LSElement[registeredChildren.size()] );
		LSElement[] newChildEntriesArray = (LSElement[])JythonSlice.arraySetSlice( oldChildArray, slice, items );
		
		HashSet<LSElement> newEntrySet = new HashSet<LSElement>( registeredChildren );
		
		
		HashSet<LSElement> removed = (HashSet<LSElement>)oldEntrySet.clone();
		removed.removeAll( newEntrySet );
		HashSet<LSElement> added = (HashSet<LSElement>)newEntrySet.clone();
		added.removeAll( oldEntrySet );
		
		
		for (LSElement child: removed)
		{
			unregisterChild( child );
		}

		registeredChildren.clear();
		registeredChildren.addAll( Arrays.asList( newChildEntriesArray ) );
		
		for (LSElement child: added)
		{
			registerChild( child );
		}
		
		
		onChildListModified();
		queueResize();
	}
	
	
	public void __delitem__(int index)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement assignment index out of range" );

		LSElement child = registeredChildren.get( index );
		unregisterChild( child );
		registeredChildren.remove( index );
		
		onChildListModified();
		queueResize();
	}
	
	public void __delitem__(PySlice slice)
	{
		LSElement[] in = registeredChildren.toArray( new LSElement[registeredChildren.size()] );
		
		LSElement[] removedArray = (LSElement[])JythonSlice.arrayGetSlice( in, slice );
		
		LSElement[] newChildEntriesArray = (LSElement[])JythonSlice.arrayDelSlice( in, slice );
		
		for (LSElement child: removedArray)
		{
			unregisterChild( child );
		}

		registeredChildren.clear();
		registeredChildren.addAll( Arrays.asList( newChildEntriesArray ) );
		
		onChildListModified();
		queueResize();
	}
	
	
	
	
	public void append(LSElement child)
	{
		assert !hasChild( child );
		
		registeredChildren.add( child );
		registerChild( child );
		onChildListModified();
		queueResize();
	}

	
	public void extend(List<LSElement> children)
	{
		for (LSElement child: children)
		{
			assert !hasChild( child );
		}
		
		int start = registeredChildren.size();
		registeredChildren.ensureCapacity( start + children.size() );
		for (int i = 0; i < children.size(); i++)
		{
			LSElement child = children.get( i );
			registeredChildren.add( child );
			registerChild( child );
		}

		onChildListModified();
		queueResize();
	}
	
	public void extend(LSElement[] children)
	{
		extend( Arrays.asList( children ) );
	}
	
	
	public void insert(int index, LSElement child)
	{
		assert !hasChild( child );
		
		registeredChildren.add( index, child );
		registerChild( child );
		onChildListModified();
		queueResize();
	}
	
	
	public void remove(LSElement child)
	{
		assert hasChild( child );
		
		unregisterChild( child );
		registeredChildren.remove( child );
		
		onChildListModified();
		queueResize();
	}
		


	protected void replaceChildWithEmpty(LSElement child)
	{
		int index = registeredChildren.indexOf( child );
		set( index, new LSBlank() );
	}
		
	protected void replaceChild(LSElement child, LSElement replacement)
	{
		int index = registeredChildren.indexOf( child );
		set( index, replacement );
	}
	
	

	
	
	public List<LSElement> getChildren()
	{
		return registeredChildren;
	}




	public InsertionPoint getInsertionPointClosestToLocalPoint(Point2 localPoint)
	{
		ArrangedSequenceLayoutNode l = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
		return l.getInsertionPointClosestToLocalPoint( this, localPoint );
	}





	protected double[] getChildrenAllocationX(List<LSElement> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocWidth();
		}
		return values;
	}

	protected double[] getChildrenAllocationX()
	{
		return getChildrenAllocationX( registeredChildren );
	}



	protected double[] getChildrenAllocationY(List<LSElement> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocHeight();
		}
		return values;
	}

	protected double[] getChildrenAllocationY()
	{
		return getChildrenAllocationY( registeredChildren );
	}



	protected LAllocV[] getChildrenAllocV(List<LSElement> nodes)
	{
		LAllocV[] values = new LAllocV[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocV();
		}
		return values;
	}

	protected LAllocV[] getChildrenAllocV()
	{
		return getChildrenAllocV( registeredChildren );
	}
	
	
	
	//
	//
	// DRAWING
	//
	//
	
	@SuppressWarnings("unchecked")
	@Override
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawSelfBackground( graphics, areaBox );
		
		AABox2 clipBox = getLocalVisibleBoundsClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();

			ArrangedSequenceLayoutNode seqLayout = (ArrangedSequenceLayoutNode)layoutNode;
			
			// Draw branches
			if ( seqLayout == null )
			{
				for (LSElement child: registeredChildren)
				{
					if ( child.getAABoxInParentSpace().intersects( areaBox ) )
					{
						child.getLocalToParentXform().apply( graphics );
						child.handleDrawBackground( graphics, child.getParentToLocalXform().transform( areaBox ) );
						graphics.setTransform( currentTransform );
					}
				}
			}
			else
			{
				// Visibility culling can be expensive - only perform it if there are more than a certain number of child elements
				Iterable<LSContainer> culledBranches;
				Iterable<LSElement> culledLeaves;
				
				if ( seqLayout.getNumLeaves() <= VISIBILITY_CULLING_THRESHHOLD )
				{
					culledBranches = seqLayout.getBranches();
					culledLeaves = seqLayout.getLeaves();
				}
				else
				{
					Object culledBranchesAndLeaves[] = seqLayout.getVisibilityCulledBranchAndLeafLists( areaBox );
					culledBranches = (Iterable<LSContainer>)culledBranchesAndLeaves[0];
					culledLeaves = (Iterable<LSElement>)culledBranchesAndLeaves[1];
				}
				for (LSElement child: culledBranches)
				{
					if ( child.getAABoxInParentSpace().intersects( areaBox ) )
					{
						child.getLocalToParentXform().apply( graphics );
						child.handleDrawSelfBackground( graphics, child.getParentToLocalXform().transform( areaBox ) );
						graphics.setTransform( currentTransform );
					}
				}
				for (LSElement child: culledLeaves)
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
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawSelf( graphics, areaBox );
		
		AABox2 clipBox = getLocalVisibleBoundsClipBox();
		if ( clipBox != null )
		{
			areaBox = areaBox.intersection( clipBox );
		}
		
		if ( !areaBox.isEmpty() )
		{
			AffineTransform currentTransform = graphics.getTransform();

			ArrangedSequenceLayoutNode seqLayout = (ArrangedSequenceLayoutNode)layoutNode;
			
			if ( seqLayout == null )
			{
				for (LSElement child: registeredChildren)
				{
					if ( child.getAABoxInParentSpace().intersects( areaBox ) )
					{
						child.getLocalToParentXform().apply( graphics );
						child.handleDraw( graphics, child.getParentToLocalXform().transform( areaBox ) );
						graphics.setTransform( currentTransform );
					}
				}
			}
			else
			{
				// Visibility culling can be expensive - only perform it if there are more than a certain number of child elements
				Iterable<LSContainer> culledBranches;
				Iterable<LSElement> culledLeaves;
				
				// Draw branches
				if ( seqLayout.getNumLeaves() <= VISIBILITY_CULLING_THRESHHOLD )
				{
					culledBranches = seqLayout.getBranches();
					culledLeaves = seqLayout.getLeaves();
				}
				else
				{
					Object culledBranchesAndLeaves[] = seqLayout.getVisibilityCulledBranchAndLeafLists( areaBox );
					culledBranches = (Iterable<LSContainer>)culledBranchesAndLeaves[0];
					culledLeaves = (Iterable<LSElement>)culledBranchesAndLeaves[1];
				}
				for (LSElement child: culledBranches)
				{
					if ( child.getAABoxInParentSpace().intersects( areaBox ) )
					{
						child.getLocalToParentXform().apply( graphics );
						child.handleDrawSelf( graphics, child.getParentToLocalXform().transform( areaBox ) );
						graphics.setTransform( currentTransform );
					}
				}
				for (LSElement child: culledLeaves)
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
	}
}
