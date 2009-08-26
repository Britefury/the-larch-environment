//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.python.core.PySlice;

import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.JythonInterface.JythonIndex;
import BritefuryJ.JythonInterface.JythonSlice;



abstract public class DPContainerSequence extends DPContainer
{
	public DPContainerSequence()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public DPContainerSequence(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
	}

	
	
	public void setChildren(List<DPWidget> items)
	{
		if ( registeredChildren.isEmpty() )
		{
			if ( items.size() > 0 )
			{
				// Set contents of @childEntries list
				registeredChildren.addAll( items );
		
				// Register added entries
				for (DPWidget child: items)
				{
					registerChild( child, null );
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
				onChildListModified();
				queueResize();
			}
			else if ( items.size() == 1 )
			{
				DPWidget prevChild = registeredChildren.get( 0 );
				DPWidget newChild = items.get( 0 );
				
				if ( newChild != prevChild )
				{
					unregisterChild( prevChild );
					registeredChildren.set( 0, newChild );
					registerChild( newChild, null );
					onChildListModified();
					queueResize();
				}
			}
		}
		else
		{
			HashSet<DPWidget> added, removed;
			
			added = new HashSet<DPWidget>( items );
			removed = new HashSet<DPWidget>( registeredChildren );
			added.removeAll( registeredChildren );
			removed.removeAll( items );

			
			// Unregister removed entries
			for (DPWidget child: removed)
			{
				unregisterChild( child );
			}
			
			// Set contents of @childEntries list
			registeredChildren.clear();
			registeredChildren.addAll( items );
	
			// Register added entries
			for (DPWidget child: added)
			{
				registerChild( child, null );
			}

			onChildListModified();
			queueResize();
		}
	}
	
	
	public void clear()
	{
		// Unregister removed entries
		for (DPWidget child: registeredChildren)
		{
			unregisterChild( child );
		}
		
		// Set contents of @childEntries list
		registeredChildren.clear();

		onChildListModified();
		queueResize();
	}
	
	

	
	
	public int size()
	{
		return registeredChildren.size();
	}
	
	public int __len__()
	{
		return size();
	}
	
	
	public DPWidget get(int index)
	{
		return registeredChildren.get( index );
	}
	
	public DPWidget __getitem__(int index)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement index out of range" );

		return get( index );
	}
	
	public DPWidget[] __getitem__(PySlice slice)
	{
		DPWidget[] in = new DPWidget[registeredChildren.size()];
		
		for (int i = 0; i < in.length; i++)
		{
			in[i] = registeredChildren.get( i );
		}
		
		return (DPWidget[])JythonSlice.arrayGetSlice( in, slice );
	}
	
	
	
	public void set(int index, DPWidget child)
	{
		DPWidget oldChild = registeredChildren.get( index );
		unregisterChild( oldChild );
		registeredChildren.set( index, child );
		registerChild( child, null );
		onChildListModified();
		queueResize();
	}
	
	public void __setitem__(int index, DPWidget item)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement assignment index out of range" );

		set( index, item );
	}

	@SuppressWarnings("unchecked")
	public void __setitem__(PySlice slice, DPWidget[] items)
	{
		HashSet<DPWidget> oldEntrySet = new HashSet<DPWidget>( registeredChildren );
		
		DPWidget[] oldChildArray = (DPWidget[])registeredChildren.toArray();
		DPWidget[] newChildEntriesArray = (DPWidget[])JythonSlice.arraySetSlice( oldChildArray, slice, items );
		
		HashSet<DPWidget> newEntrySet = new HashSet<DPWidget>( registeredChildren );
		
		
		HashSet<DPWidget> removed = (HashSet<DPWidget>)oldEntrySet.clone();
		removed.removeAll( newEntrySet );
		HashSet<DPWidget> added = (HashSet<DPWidget>)newEntrySet.clone();
		added.removeAll( oldEntrySet );
		
		
		for (DPWidget child: removed)
		{
			unregisterChild( child );
		}

		registeredChildren.clear();
		registeredChildren.addAll( Arrays.asList( newChildEntriesArray ) );
		
		for (DPWidget child: added)
		{
			registerChild( child, null );
		}
		
		
		onChildListModified();
		queueResize();
	}
	
	
	public void __delitem__(int index)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement assignment index out of range" );

		DPWidget child = registeredChildren.get( index );
		unregisterChild( child );
		registeredChildren.remove( index );
		
		onChildListModified();
		queueResize();
	}
	
	public void __delitem__(PySlice slice)
	{
		DPWidget[] in = (DPWidget[])registeredChildren.toArray();
		
		DPWidget[] removedArray = (DPWidget[])JythonSlice.arrayGetSlice( in, slice );
		
		DPWidget[] newChildEntriesArray = (DPWidget[])JythonSlice.arrayDelSlice( in, slice );
		
		for (DPWidget child: removedArray)
		{
			unregisterChild( child );
		}

		registeredChildren.clear();
		registeredChildren.addAll( Arrays.asList( newChildEntriesArray ) );
		
		onChildListModified();
		queueResize();
	}
	
	
	
	
	public void append(DPWidget child)
	{
		assert !hasChild( child );
		
		registeredChildren.add( child );
		registerChild( child, null );
		onChildListModified();
		queueResize();
	}

	
	public void extend(List<DPWidget> children)
	{
		for (DPWidget child: children)
		{
			assert !hasChild( child );
		}
		
		int start = registeredChildren.size();
		registeredChildren.ensureCapacity( start + children.size() );
		for (int i = 0; i < children.size(); i++)
		{
			DPWidget child = children.get( i );
			registeredChildren.add( child );
			registerChild( child, null );
		}

		onChildListModified();
		queueResize();
	}
	
	public void extend(DPWidget[] children)
	{
		extend( Arrays.asList( children ) );
	}
	
	
	protected void insert(int index, DPWidget child)
	{
		assert !hasChild( child );
		
		registeredChildren.add( index, child );
		registerChild( child, null );
		onChildListModified();
		queueResize();
	}
	
	
	protected void remove(DPWidget child)
	{
		assert hasChild( child );
		
		unregisterChild( child );
		registeredChildren.remove( child );
		
		onChildListModified();
		queueResize();
	}
		


	protected void replaceChildWithEmpty(DPWidget child)
	{
		int index = registeredChildren.indexOf( child );
		set( index, new DPEmpty() );
	}
		
	

	
	
	public List<DPWidget> getChildren()
	{
		return registeredChildren;
	}





	protected LReqBox[] getChildrenRefreshedRequistionXBoxes(List<DPWidget> nodes)
	{
		LReqBox[] boxes = new LReqBox[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).refreshRequisitionX();
		}
		return boxes;
	}

	protected LReqBox[] getChildrenRefreshedRequistionXBoxes()
	{
		return getChildrenRefreshedRequistionXBoxes( registeredChildren );
	}


	protected LReqBox[] getChildrenRefreshedRequistionYBoxes(List<DPWidget> nodes)
	{
		LReqBox[] boxes = new LReqBox[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).refreshRequisitionY();
		}
		return boxes;
	}

	protected LReqBox[] getChildrenRefreshedRequistionYBoxes()
	{
		return getChildrenRefreshedRequistionYBoxes( registeredChildren );
	}
	
	
	
	
	protected LReqBox[] getChildrenRequisitionBoxes(List<DPWidget> nodes)
	{
		LReqBox[] boxes = new LReqBox[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).layoutReqBox;
		}
		return boxes;
	}

	protected LReqBox[] getChildrenRequisitionBoxes()
	{
		return getChildrenRequisitionBoxes( registeredChildren );
	}
	
	
	
	protected LAllocBox[] getChildrenAllocationBoxes(List<DPWidget> nodes)
	{
		LAllocBox[] boxes = new LAllocBox[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).layoutAllocBox;
		}
		return boxes;
	}

	protected LAllocBox[] getChildrenAllocationBoxes()
	{
		return getChildrenAllocationBoxes( registeredChildren );
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	protected <T extends PackingParams> T[] getChildrenPackingParams(List<DPWidget> nodes, T packingParams[])
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			packingParams[i] = (T)nodes.get( i ).getParentPacking();
		}
		return packingParams;
	}

	protected <T extends PackingParams> T[] getChildrenPackingParams(T packingParams[])
	{
		return getChildrenPackingParams( registeredChildren, packingParams );
	}
	
	
	
	
	protected double[] getChildrenAllocationX(List<DPWidget> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocation().x;
		}
		return values;
	}

	protected double[] getChildrenAllocationX()
	{
		return getChildrenAllocationX( registeredChildren );
	}



	protected double[] getChildrenAllocationY(List<DPWidget> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocation().y;
		}
		return values;
	}

	protected double[] getChildrenAllocationY()
	{
		return getChildrenAllocationY( registeredChildren );
	}



	protected LAllocV[] getChildrenAllocV(List<DPWidget> nodes)
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
}
