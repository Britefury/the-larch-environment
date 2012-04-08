//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.WeakHashMap;

public class WeakListenerList <ListenerType> implements Iterable<ListenerType>
{
	private class ListenerIterator implements Iterator<ListenerType>
	{
		private ListenerType next;
		private int index;
		
		
		private ListenerIterator()
		{
			index = nextIndex( -1 );
			next = getListenerAt( index );
			if ( next != null )
			{
				addActiveIterator( this );
			}
		}
		

		@Override
		public boolean hasNext()
		{
			return next != null;
		}

		@Override
		public ListenerType next()
		{
			ListenerType result = next;
			
			index = nextIndex( index );
			next = getListenerAt( index );
			
			if ( next == null )
			{
				removeActiveIterator( this );
			}

			return result;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
		
		
		private int nextIndex(int index)
		{
			if ( listeners != null )
			{
				int start = index == -1  ?  listeners.size() - 1  :  index - 1;
				if ( start >= 0 )
				{
					for (int i = start; i >= 0; i--)
					{
						WeakReference<ListenerType> ref = listeners.get( i );
						ListenerType l = ref.get();
						if ( l != null )
						{
							return i;
						}
					}
				}
			}
			
			return -1;
		}
		
		private ListenerType getListenerAt(int index)
		{
			if ( index != -1 )
			{
				WeakReference<ListenerType> ref = listeners.get( index );
				return ref.get();
			}
			else
			{
				return null;
			}
		}
	}
	
	
	
	
	private ArrayList<WeakReference<ListenerType>> listeners = null;
	private WeakHashMap<ListenerIterator, Object> activeIterators;
	private ReferenceQueue<ListenerType> refQueue = null;
	
	
	
	private WeakListenerList()
	{
	}
	
	
	public static <ListenerType> WeakListenerList<ListenerType> addListener(WeakListenerList<ListenerType> listeners, ListenerType listener)
	{
		if ( listeners == null )
		{
			listeners = new WeakListenerList<ListenerType>();
		}
		listeners.add( listener );
		return listeners;
	}

	public static <ListenerType> WeakListenerList<ListenerType> removeListener(WeakListenerList<ListenerType> listeners, ListenerType listener)
	{
		if ( listeners != null )
		{
			listeners.remove( listener );
			if ( listeners.isEmpty() )
			{
				listeners = null;
			}
		}
		return listeners;
	}
	
	
	
	@Override
	public Iterator<ListenerType> iterator()
	{
		if ( !hasActiveIterators() )
		{
			// cleanUp() mutates the listener list, so we should only clean up if there are no active iterators
			cleanUp();
		}
		return new ListenerIterator();
	}
	
	
	public boolean isEmpty()
	{
		return listeners == null || listeners.isEmpty();
	}

	
	private void add(ListenerType listener)
	{
		if ( hasActiveIterators() )
		{
			throw new RuntimeException( "Cannot add to listener list during iteration" );
		}
		if ( listeners == null )
		{
			listeners = new ArrayList<WeakReference<ListenerType>>();
			refQueue = new ReferenceQueue<ListenerType>();
			listeners.add( new WeakReference<ListenerType>( listener, refQueue ) );
		}
		else
		{
			for (WeakReference<ListenerType> ref: listeners)
			{
				if ( ref.get() == listener )
				{
					return;
				}
			}
			listeners.add( new WeakReference<ListenerType>( listener, refQueue ) );
			cleanUp();
		}
	}

	private void remove(ListenerType listener)
	{
		if ( hasActiveIterators() )
		{
			throw new RuntimeException( "Cannot remove from listener list during iteration" );
		}
		if ( listeners != null )
		{
			for (int i = listeners.size() - 1; i >= 0; i--)
			{
				WeakReference<ListenerType> ref = listeners.get( i );
				ListenerType l = ref.get();
				if ( l == listener )
				{
					listeners.remove( i );
					break;
				}
			}
			cleanUp();
		}
	}
	
	private void cleanUp()
	{
		if ( refQueue != null )
		{
			if ( refQueue.poll() != null )
			{
				for (int i = listeners.size() - 1; i >= 0; i--)
				{
					WeakReference<ListenerType> ref = listeners.get( i );
					ListenerType l = ref.get();
					if ( l == null )
					{
						listeners.remove( i );
					}
				}
				
				if ( listeners.isEmpty() )
				{
					listeners = null;
					refQueue = null;
				}
			}
		}
	}
	
	
	private void addActiveIterator(ListenerIterator iter)
	{
		if ( activeIterators == null )
		{
			activeIterators = new WeakHashMap<ListenerIterator, Object>();
		}
		activeIterators.put( iter, null );
	}

	private void removeActiveIterator(ListenerIterator iter)
	{
		if ( activeIterators != null )
		{
			activeIterators.remove( iter );
			
			if ( activeIterators.isEmpty() )
			{
				activeIterators = null;
				
				// No more active iterators - clean up
				cleanUp();
			}
		}
	}
	
	private boolean hasActiveIterators()
	{
		return activeIterators != null  &&  !activeIterators.isEmpty();
	}
}
