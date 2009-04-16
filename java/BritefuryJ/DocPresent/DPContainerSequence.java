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

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
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

	
	
	@SuppressWarnings("unchecked")
	public void setChildren(List<DPWidget> items)
	{
		HashSet<DPWidget> oldEntrySet = new HashSet<DPWidget>( registeredChildren );

		// Set of added entries
		HashSet<DPWidget> added = new HashSet<DPWidget>();
		
		// Build an array containing the child entry list, *after* modification
		for (int i = 0; i < items.size(); i++)
		{
			if ( !oldEntrySet.contains( items.get( i ) ) )
			{
				// @item is not already present in the list of children; it is new, so @entry will be added to the list
				added.add( items.get( i ) );

				// We cannot simply assume that the (existing) child at position @i is removed, since this child may have been moved to a different
				// position in the sequence, and another child removed. We have to determine what is removed later on
			}
		}
		
		HashSet<DPWidget> newEntrySet = new HashSet<DPWidget>( items );
		
		
		// Compute set of removed entries
		HashSet<DPWidget> removed = (HashSet<DPWidget>)oldEntrySet.clone();
		removed.removeAll( newEntrySet );
		
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
			registerChild( child );
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
	
	
	
	public void set(int index, DPWidget item)
	{
		DPWidget oldChild = registeredChildren.get( index );
		unregisterChild( oldChild );
		registeredChildren.set( index, item );
		registerChild( item );
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
			registerChild( child );
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
		registerChild( child );
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
			registerChild( child );
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
		registerChild( child );
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
		
	
	abstract protected void childListModified();

	
	
	
	protected List<DPWidget> getChildren()
	{
		return registeredChildren;
	}








	HMetrics[] getChildrenRefreshedMinimumHMetrics(List<DPWidget> nodes)
	{
		HMetrics[] chm = new HMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).refreshMinimumHMetrics();
		}
		return chm;
	}

	HMetrics[] getChildrenRefreshedMinimumHMetrics()
	{
		return getChildrenRefreshedMinimumHMetrics( registeredChildren );
	}

	
	HMetrics[] getChildrenRefreshedPreferredHMetrics(List<DPWidget> nodes)
	{
		HMetrics[] chm = new HMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).refreshPreferredHMetrics();
		}
		return chm;
	}
	
	HMetrics[] getChildrenRefreshedPreferredHMetrics()
	{
		return getChildrenRefreshedPreferredHMetrics( registeredChildren );
	}
	
	
	
	VMetrics[] getChildrenRefreshedMinimumVMetrics(List<DPWidget> nodes)
	{
		VMetrics[] chm = new VMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).refreshMinimumVMetrics();
		}
		return chm;
	}

	VMetrics[] getChildrenRefreshedMinimumVMetrics()
	{
		return getChildrenRefreshedMinimumVMetrics( registeredChildren );
	}

	
	VMetrics[] getChildrenRefreshedPreferredVMetrics(List<DPWidget> nodes)
	{
		VMetrics[] chm = new VMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).refreshPreferredVMetrics();
		}
		return chm;
	}

	VMetrics[] getChildrenRefreshedPreferredVMetrics()
	{
		return getChildrenRefreshedPreferredVMetrics( registeredChildren );
	}


	
	
	
	static HMetrics[] getChildrenMinimumHMetrics(List<DPWidget> nodes)
	{
		HMetrics[] chm = new HMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).minH;
		}
		return chm;
	}

	HMetrics[] getChildrenMinimumHMetrics()
	{
		return getChildrenMinimumHMetrics( registeredChildren );
	}

	
	static HMetrics[] getChildrenPreferredHMetrics(List<DPWidget> nodes)
	{
		HMetrics[] chm = new HMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).prefH;
		}
		return chm;
	}
	
	HMetrics[] getChildrenPreferredHMetrics()
	{
		return getChildrenPreferredHMetrics( registeredChildren );
	}
	
	
	
	static VMetrics[] getChildrenMinimumVMetrics(List<DPWidget> nodes)
	{
		VMetrics[] chm = new VMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).minV;
		}
		return chm;
	}

	VMetrics[] getChildrenMinimumVMetrics()
	{
		return getChildrenMinimumVMetrics( registeredChildren );
	}

	
	static VMetrics[] getChildrenPreferredVMetrics(List<DPWidget> nodes)
	{
		VMetrics[] chm = new VMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).prefV;
		}
		return chm;
	}

	VMetrics[] getChildrenPreferredVMetrics()
	{
		return getChildrenPreferredVMetrics( registeredChildren );
	}
}
