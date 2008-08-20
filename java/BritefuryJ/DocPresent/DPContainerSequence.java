package BritefuryJ.DocPresent;

import java.awt.Color;
import java.util.HashSet;

import org.python.core.PySlice;

import BritefuryJ.JythonInterface.JythonSlice;



abstract public class DPContainerSequence extends DPContainer
{
	public DPContainerSequence()
	{
		super( null );
	}
	
	public DPContainerSequence(Color backgroundColour)
	{
		super( backgroundColour );
	}

	
	
	public int size()
	{
		return childEntries.size();
	}
	
	public int __len__()
	{
		return size();
	}
	
	
	public DPWidget __getitem__(int index)
	{
		return childEntries.get( index ).child;
	}
	
	public DPWidget[] __getitem__(PySlice slice)
	{
		DPWidget[] in = new DPWidget[childEntries.size()];
		
		for (int i = 0; i < in.length; i++)
		{
			in[i] = childEntries.get( i ).child;
		}
		
		return (DPWidget[])JythonSlice.arrayGetSlice( in, slice );
	}
	
	
	
	public void __setitem__(int index, DPWidget item)
	{
		ChildEntry newEntry = createChildEntryForChild( item );
		ChildEntry oldEntry = childEntries.get( index );
		unregisterChildEntry( oldEntry );
		childEntries.set( index, newEntry );
		registerChildEntry( newEntry );
		childListModified();
		queueResize();
	}
	
	@SuppressWarnings("unchecked")
	public void __setitem__(PySlice slice, DPWidget[] items)
	{
		HashSet<ChildEntry> oldEntrySet = new HashSet<ChildEntry>( childEntries );
		
		ChildEntry[] itemEntriesArray = new ChildEntry[items.length];
		for (int i = 0; i < items.length; i++)
		{
			itemEntriesArray[i] = createChildEntryForChild( items[i] );
		}
		ChildEntry[] oldChildEntriesArray = (ChildEntry[])childEntries.toArray();
		ChildEntry[] newChildEntriesArray = (ChildEntry[])JythonSlice.arraySetSlice( oldChildEntriesArray, slice, itemEntriesArray );
		
		childEntries.setSize( newChildEntriesArray.length );
		for (int i = 0; i < newChildEntriesArray.length; i++)
		{
			childEntries.set( i, newChildEntriesArray[i] );
		}
		
		HashSet<ChildEntry> newEntrySet = new HashSet<ChildEntry>( childEntries );
		
		
		HashSet<ChildEntry> removed = (HashSet<ChildEntry>)oldEntrySet.clone();
		removed.removeAll( newEntrySet );
		HashSet<ChildEntry> added = (HashSet<ChildEntry>)newEntrySet.clone();
		added.removeAll( oldEntrySet );
		
		
		for (ChildEntry entry: removed)
		{
			unregisterChildEntry( entry );
		}

		for (ChildEntry entry: added)
		{
			registerChildEntry( entry );
		}
		
		
		childListModified();
		queueResize();
	}
	
	
	public void __delitem__(int index)
	{
		ChildEntry entry = childEntries.get( index );
		childEntries.remove( index );
		unregisterChildEntry( entry );
		
		childListModified();
		queueResize();
	}
	
	public void __delitem__(PySlice slice)
	{
		ChildEntry[] in = (ChildEntry[])childEntries.toArray();
		
		ChildEntry[] removedArray = (ChildEntry[])JythonSlice.arrayGetSlice( in, slice );
		
		ChildEntry[] newChildEntriesArray = (ChildEntry[])JythonSlice.arrayDelSlice( in, slice );
		
		childEntries.setSize( newChildEntriesArray.length );
		for (int i = 0; i < newChildEntriesArray.length; i++)
		{
			childEntries.set( i, newChildEntriesArray[i] );
		}
		
		for (ChildEntry entry: removedArray)
		{
			unregisterChildEntry( entry );
		}

		childListModified();
		queueResize();
	}
	
	
	
	
	protected void appendChildEntry(ChildEntry entry)
	{
		assert !hasChild( entry.child );
		
		childEntries.add( entry );
		registerChildEntry( entry );
		childListModified();
		queueResize();
	}

	
	protected void extendChildEntries(ChildEntry[] entries)
	{
		for (ChildEntry entry: entries)
		{
			assert !hasChild( entry.child );
		}
		
		int start = childEntries.size();
		childEntries.setSize( start + entries.length );
		for (int i = 0; i < entries.length; i++)
		{
			ChildEntry entry = entries[i];
			childEntries.set( start + i, entry );
			registerChildEntry( entry );
		}

		childListModified();
		queueResize();
	}
	
	
	protected void insertChildEntry(int index, ChildEntry entry)
	{
		assert !hasChild( entry.child );
		
		childEntries.insertElementAt( entry, index );
		registerChildEntry( entry );
		childListModified();
		queueResize();
	}
	
	
	protected void removeChildEntry(ChildEntry entry)
	{
		assert hasChild( entry.child );
		
		childEntries.remove( entry );
		unregisterChildEntry( entry );
		
		childListModified();
		queueResize();
	}
		


	protected void removeChild(DPWidget child)
	{
		ChildEntry entry = childToEntry.get( child );
		int index = childEntries.indexOf( entry );
		__setitem__( index, new DPEmpty() );
	}
		
	
	abstract protected void childListModified();
}
