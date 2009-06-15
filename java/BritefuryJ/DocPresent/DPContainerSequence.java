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
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
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
			// Set contents of @childEntries list
			registeredChildren.addAll( items );
	
			// Register added entries
			for (DPWidget child: items)
			{
				registerChild( child, null );
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
		}
		
		
		childListModified();
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
		childListModified();
		queueResize();
	}
	
	public void __setitem__(int index, DPWidget item)
	{
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
		
		
		childListModified();
		queueResize();
	}
	
	
	public void __delitem__(int index)
	{
		DPWidget child = registeredChildren.get( index );
		unregisterChild( child );
		registeredChildren.remove( index );
		
		childListModified();
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
		
		childListModified();
		queueResize();
	}
	
	
	
	
	public void append(DPWidget child)
	{
		assert !hasChild( child );
		
		registeredChildren.add( child );
		registerChild( child, null );
		childListModified();
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

		childListModified();
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
		childListModified();
		queueResize();
	}
	
	
	protected void remove(DPWidget child)
	{
		assert hasChild( child );
		
		unregisterChild( child );
		registeredChildren.remove( child );
		
		childListModified();
		queueResize();
	}
		


	protected void replaceChildWithEmpty(DPWidget child)
	{
		int index = registeredChildren.indexOf( child );
		__setitem__( index, new DPEmpty() );
	}
		
	

	
	
	protected List<DPWidget> getChildren()
	{
		return registeredChildren;
	}





	LReqBox[] getChildrenRefreshedRequistionXBoxes(List<DPWidget> nodes)
	{
		LReqBox[] boxes = new LReqBox[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).refreshRequisitionX();
		}
		return boxes;
	}

	LReqBox[] getChildrenRefreshedRequistionXBoxes()
	{
		return getChildrenRefreshedRequistionXBoxes( registeredChildren );
	}


	LReqBox[] getChildrenRefreshedRequistionYBoxes(List<DPWidget> nodes)
	{
		LReqBox[] boxes = new LReqBox[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).refreshRequisitionY();
		}
		return boxes;
	}

	LReqBox[] getChildrenRefreshedRequistionYBoxes()
	{
		return getChildrenRefreshedRequistionYBoxes( registeredChildren );
	}
	
	
	
	
	LReqBox[] getChildrenRequisitionBoxes(List<DPWidget> nodes)
	{
		LReqBox[] boxes = new LReqBox[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).layoutReqBox;
		}
		return boxes;
	}

	LReqBox[] getChildrenRequisitionBoxes()
	{
		return getChildrenRequisitionBoxes( registeredChildren );
	}
	
	
	
	LAllocBox[] getChildrenAllocationBoxes(List<DPWidget> nodes)
	{
		LAllocBox[] boxes = new LAllocBox[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).layoutAllocBox;
		}
		return boxes;
	}

	LAllocBox[] getChildrenAllocationBoxes()
	{
		return getChildrenAllocationBoxes( registeredChildren );
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	<T extends PackingParams> T[] getChildrenPackingParams(List<DPWidget> nodes, T packingParams[])
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			packingParams[i] = (T)nodes.get( i ).getParentPacking();
		}
		return packingParams;
	}

	<T extends PackingParams> T[] getChildrenPackingParams(T packingParams[])
	{
		return getChildrenPackingParams( registeredChildren, packingParams );
	}
	
	
	
	
	double[] getChildrenAllocationX(List<DPWidget> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocation().x;
		}
		return values;
	}

	double[] getChildrenAllocationX()
	{
		return getChildrenAllocationX( registeredChildren );
	}



	double[] getChildrenAllocationY(List<DPWidget> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocation().y;
		}
		return values;
	}

	double[] getChildrenAllocationY()
	{
		return getChildrenAllocationY( registeredChildren );
	}
}
