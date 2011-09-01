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

import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.JythonInterface.JythonIndex;
import BritefuryJ.JythonInterface.JythonSlice;



abstract public class DPContainerSequence extends DPContainer
{
	public DPContainerSequence()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public DPContainerSequence(ContainerStyleParams styleParams)
	{
		super(styleParams);
	}


	
	//
	//
	// Child list access and modifications
	//
	//
	
	public void setChildren(DPElement items[])
	{
		setChildren( Arrays.asList( items ) );
	}
	
	public void setChildren(List<DPElement> items)
	{
		if ( registeredChildren.isEmpty() )
		{
			if ( items.size() > 0 )
			{
				// Set contents of @childEntries list
				registeredChildren.addAll( items );
		
				// Register added entries
				for (DPElement child: items)
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
				DPElement prevChild = registeredChildren.get( 0 );
				DPElement newChild = items.get( 0 );
				
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
			HashSet<DPElement> added, removed;
			
			added = new HashSet<DPElement>( items );
			removed = new HashSet<DPElement>( registeredChildren );
			added.removeAll( registeredChildren );
			removed.removeAll( items );

			
			// Unregister removed entries
			for (DPElement child: removed)
			{
				unregisterChild( child );
			}
			
			// Set contents of @childEntries list
			registeredChildren.clear();
			registeredChildren.addAll( items );
	
			// Register added entries
			for (DPElement child: added)
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
		for (DPElement child: registeredChildren)
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
	
	
	
	public int indexOf(DPElement child)
	{
		return registeredChildren.indexOf( child );
	}
	
	
	public DPElement get(int index)
	{
		return registeredChildren.get( index );
	}
	
	public DPElement __getitem__(int index)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement index out of range" );

		return get( index );
	}
	
	public DPElement[] __getitem__(PySlice slice)
	{
		DPElement[] in = new DPElement[registeredChildren.size()];
		
		for (int i = 0; i < in.length; i++)
		{
			in[i] = registeredChildren.get( i );
		}
		
		return (DPElement[])JythonSlice.arrayGetSlice( in, slice );
	}
	
	
	
	public void set(int index, DPElement child)
	{
		DPElement oldChild = registeredChildren.get( index );
		unregisterChild( oldChild );
		registeredChildren.set( index, child );
		registerChild( child );
		onChildListModified();
		queueResize();
	}
	
	public void __setitem__(int index, DPElement item)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement assignment index out of range" );

		set( index, item );
	}

	@SuppressWarnings("unchecked")
	public void __setitem__(PySlice slice, DPElement[] items)
	{
		HashSet<DPElement> oldEntrySet = new HashSet<DPElement>( registeredChildren );
		
		DPElement[] oldChildArray = registeredChildren.toArray( new DPElement[registeredChildren.size()] );
		DPElement[] newChildEntriesArray = (DPElement[])JythonSlice.arraySetSlice( oldChildArray, slice, items );
		
		HashSet<DPElement> newEntrySet = new HashSet<DPElement>( registeredChildren );
		
		
		HashSet<DPElement> removed = (HashSet<DPElement>)oldEntrySet.clone();
		removed.removeAll( newEntrySet );
		HashSet<DPElement> added = (HashSet<DPElement>)newEntrySet.clone();
		added.removeAll( oldEntrySet );
		
		
		for (DPElement child: removed)
		{
			unregisterChild( child );
		}

		registeredChildren.clear();
		registeredChildren.addAll( Arrays.asList( newChildEntriesArray ) );
		
		for (DPElement child: added)
		{
			registerChild( child );
		}
		
		
		onChildListModified();
		queueResize();
	}
	
	
	public void __delitem__(int index)
	{
		index = JythonIndex.pyIndexToJava( index, size(), "BranchElement assignment index out of range" );

		DPElement child = registeredChildren.get( index );
		unregisterChild( child );
		registeredChildren.remove( index );
		
		onChildListModified();
		queueResize();
	}
	
	public void __delitem__(PySlice slice)
	{
		DPElement[] in = registeredChildren.toArray( new DPElement[registeredChildren.size()] );
		
		DPElement[] removedArray = (DPElement[])JythonSlice.arrayGetSlice( in, slice );
		
		DPElement[] newChildEntriesArray = (DPElement[])JythonSlice.arrayDelSlice( in, slice );
		
		for (DPElement child: removedArray)
		{
			unregisterChild( child );
		}

		registeredChildren.clear();
		registeredChildren.addAll( Arrays.asList( newChildEntriesArray ) );
		
		onChildListModified();
		queueResize();
	}
	
	
	
	
	public void append(DPElement child)
	{
		assert !hasChild( child );
		
		registeredChildren.add( child );
		registerChild( child );
		onChildListModified();
		queueResize();
	}

	
	public void extend(List<DPElement> children)
	{
		for (DPElement child: children)
		{
			assert !hasChild( child );
		}
		
		int start = registeredChildren.size();
		registeredChildren.ensureCapacity( start + children.size() );
		for (int i = 0; i < children.size(); i++)
		{
			DPElement child = children.get( i );
			registeredChildren.add( child );
			registerChild( child );
		}

		onChildListModified();
		queueResize();
	}
	
	public void extend(DPElement[] children)
	{
		extend( Arrays.asList( children ) );
	}
	
	
	public void insert(int index, DPElement child)
	{
		assert !hasChild( child );
		
		registeredChildren.add( index, child );
		registerChild( child );
		onChildListModified();
		queueResize();
	}
	
	
	public void remove(DPElement child)
	{
		assert hasChild( child );
		
		unregisterChild( child );
		registeredChildren.remove( child );
		
		onChildListModified();
		queueResize();
	}
		


	protected void replaceChildWithEmpty(DPElement child)
	{
		int index = registeredChildren.indexOf( child );
		set( index, new DPHiddenContent() );
	}
		
	protected void replaceChild(DPElement child, DPElement replacement)
	{
		int index = registeredChildren.indexOf( child );
		set( index, replacement );
	}
	
	

	
	
	public List<DPElement> getChildren()
	{
		return registeredChildren;
	}





	protected double[] getChildrenAllocationX(List<DPElement> nodes)
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



	protected double[] getChildrenAllocationY(List<DPElement> nodes)
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



	protected LAllocV[] getChildrenAllocV(List<DPElement> nodes)
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
