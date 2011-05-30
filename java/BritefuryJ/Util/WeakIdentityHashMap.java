//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class WeakIdentityHashMap <Key, Value> implements Map<Key, Value>
{
	private static <Key, Value> Map.Entry<WeakIdKey, Value> weakEntry(Map.Entry<Key, Value> e)
	{
		return new AbstractMap.SimpleImmutableEntry<WeakIdKey, Value>( new WeakIdKey( e.getKey() ), e.getValue() );
	}
	
	
	private class KeyIterator implements Iterator<Key>
	{
		private Iterator<Map.Entry<Key, Value>> iter;
		
		private KeyIterator(Iterator<Map.Entry<Key, Value>> iter)
		{
			this.iter = iter;
		}
		
		
		public boolean hasNext()
		{
			return iter.hasNext();
		}

		
		public Key next()
		{
			Map.Entry<Key, Value> e = iter.next();
			return e.getKey();
		}

		
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private class KeySet implements Set<Key>
	{
		private Set<WeakIdKey> set;
		
		private KeySet(Set<WeakIdKey> set)
		{
			this.set = set;
		}

		
		public int size()
		{
			cleanup();
			return WeakIdentityHashMap.this.size();
		}

		
		public boolean isEmpty()
		{
			cleanup();
			return WeakIdentityHashMap.this.isEmpty();
		}

		
		public boolean contains(Object o)
		{
			cleanup();
			return WeakIdentityHashMap.this.containsKey( o );
		}

		
		public Iterator<Key> iterator()
		{
			cleanup();
			return new KeyIterator( WeakIdentityHashMap.this.entrySet().iterator() );
		}

		
		public Object[] toArray()
		{
			cleanup();
			ArrayList<Object> keys = new ArrayList<Object>();
			for (Map.Entry<Key, Value> e: WeakIdentityHashMap.this.entrySet())
			{
				keys.add( e.getKey() );
			}
			return keys.toArray();
		}

		@SuppressWarnings("unchecked")
		
		public <T> T[] toArray(T[] a)
		{
			cleanup();
			ArrayList<T> keys = new ArrayList<T>();
			for (Map.Entry<Key, Value> e: WeakIdentityHashMap.this.entrySet())
			{
				keys.add( (T)e.getKey() );
			}
			return keys.toArray( a );
		}

		
		public boolean add(Key e)
		{
			throw new UnsupportedOperationException();
		}

		
		public boolean remove(Object o)
		{
			cleanup();
			return set.remove( new WeakIdKey( o ) );
		}

		
		public boolean containsAll(Collection<?> c)
		{
			cleanup();
			for (Object x: c)
			{
				if ( !contains( x ) )
				{
					return false;
				}
			}
			return true;
		}

		
		public boolean addAll(Collection<? extends Key> c)
		{
			throw new UnsupportedOperationException();
		}

		
		public boolean retainAll(Collection<?> c)
		{
			cleanup();
			ArrayList<WeakIdKey> xs = new ArrayList<WeakIdKey>(); 
			for (Object x: c)
			{
				xs.add( new WeakIdKey( x ) );
			}
			return set.retainAll( xs );
		}

		
		public boolean removeAll(Collection<?> c)
		{
			cleanup();
			ArrayList<WeakIdKey> xs = new ArrayList<WeakIdKey>(); 
			for (Object x: c)
			{
				xs.add( new WeakIdKey( x ) );
			}
			return set.removeAll( xs );
		}

		
		public void clear()
		{
			cleanup();
			WeakIdentityHashMap.this.clear();
		}
	}
	
	
	
	private static class Entry <Key, Value> implements Map.Entry<Key, Value>
	{
		private Map.Entry<WeakIdKey, Value> e;
		private Key k;
		
		@SuppressWarnings("unchecked")
		public Entry(Map.Entry<WeakIdKey, Value> e)
		{
			this.e = e;
			this.k = (Key)e.getKey().get();
		}

		
		public Key getKey()
		{
			return k;
		}

		
		public Value getValue()
		{
			return e.getValue();
		}

		
		public Value setValue(Value value)
		{
			return e.setValue( value );
		}
	}
	
	private class EntryIterator implements Iterator<Map.Entry<Key, Value>>
	{
		private Iterator<Map.Entry<WeakIdKey, Value>> iter;
		private Entry<Key, Value> next = null;
		
		private EntryIterator(Iterator<Map.Entry<WeakIdKey, Value>> iter)
		{
			this.iter = iter;
			next = fetchNext();
		}
		
		
		public boolean hasNext()
		{
			if ( next == null )
			{
				next = fetchNext();
			}
			return next != null;
		}

		
		public Map.Entry<Key, Value> next()
		{
			if ( next != null )
			{
				Entry<Key, Value> e = next;
				next = fetchNext();
				if ( e.getKey() == null )
				{
					throw new RuntimeException( "WeakIdentityHashMap.EntryIterator.next(): e.getKey() is null" );
				}
				return e;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	
		private Entry<Key, Value> fetchNext()
		{
			while ( iter.hasNext() )
			{
				Map.Entry<WeakIdKey, Value> e = iter.next();
				@SuppressWarnings("unchecked")
				Key k = (Key)e.getKey().get();
				if ( k != null )
				{
					return new Entry<Key, Value>( e );
				}
			}
			return null;
		}
	}
	
	private class EntrySet implements Set<Map.Entry<Key, Value>>
	{
		private Set<Map.Entry<WeakIdKey, Value>> set;
		
		private EntrySet(Set<Map.Entry<WeakIdKey, Value>> set)
		{
			this.set = set;
		}

		
		public int size()
		{
			cleanup();
			return WeakIdentityHashMap.this.size();
		}

		
		public boolean isEmpty()
		{
			cleanup();
			return WeakIdentityHashMap.this.isEmpty();
		}

		@SuppressWarnings("unchecked")
		
		public boolean contains(Object o)
		{
			cleanup();
			return set.contains( weakEntry( (Map.Entry<Key, Value>)o ) );
		}

		
		public Iterator<Map.Entry<Key, Value>> iterator()
		{
			cleanup();
			return new EntryIterator( set.iterator() );
		}

		
		public Object[] toArray()
		{
			cleanup();
			ArrayList<Object> arr = new ArrayList<Object>();
			for (Map.Entry<Key, Value> e: this)
			{
				arr.add( e );
			}
			return arr.toArray();
		}

		@SuppressWarnings("unchecked")
		
		public <T> T[] toArray(T[] a)
		{
			cleanup();
			ArrayList<T> arr = new ArrayList<T>();
			for (Map.Entry<Key, Value> e: this)
			{
				arr.add( (T)e );
			}
			return arr.toArray( a );
		}

		
		public boolean add(Map.Entry<Key, Value> e)
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		
		public boolean remove(Object o)
		{
			cleanup();
			return set.remove( weakEntry( (Map.Entry<Key, Value>)o ) );
		}

		@SuppressWarnings("unchecked")
		
		public boolean containsAll(Collection<?> c)
		{
			cleanup();
			for (Object x: c)
			{
				if ( !set.contains( weakEntry( (Map.Entry<Key, Value>)x ) ) )
				{
					return false;
				}
			}
			return true;
		}

		
		public boolean addAll(Collection<? extends Map.Entry<Key, Value>> c)
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		
		public boolean retainAll(Collection<?> c)
		{
			cleanup();
			ArrayList<Map.Entry<WeakIdKey, Value>> xs = new ArrayList<Map.Entry<WeakIdKey, Value>>(); 
			for (Object x: c)
			{
				xs.add( weakEntry( (Map.Entry<Key, Value>)x ));
			}
			return set.retainAll( xs );
		}

		@SuppressWarnings("unchecked")
		
		public boolean removeAll(Collection<?> c)
		{
			cleanup();
			ArrayList<Map.Entry<WeakIdKey, Value>> xs = new ArrayList<Map.Entry<WeakIdKey, Value>>(); 
			for (Object x: c)
			{
				xs.add( weakEntry( (Map.Entry<Key, Value>)x ));
			}
			return set.removeAll( xs );
		}

		
		public void clear()
		{
			cleanup();
			WeakIdentityHashMap.this.clear();
		}
	}
	
	
	private ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
	private HashMap<WeakIdKey, Value> map = new HashMap<WeakIdKey, Value>();
	
	

	
	public void clear()
	{
		cleanup();
		map.clear();
	}

	
	public boolean containsKey(Object key)
	{
		cleanup();
		return map.containsKey( new WeakIdKey( key ) );
	}

	
	public boolean containsValue(Object value)
	{
		cleanup();
		return map.containsValue( value );
	}

	
	public Set<Map.Entry<Key, Value>> entrySet()
	{
		cleanup();
		return new EntrySet( map.entrySet() );
	}

	
	public Value get(Object key)
	{
		cleanup();
		return map.get( new WeakIdKey( key ) );
	}

	
	public boolean isEmpty()
	{
		cleanup();
		return map.isEmpty();
	}

	
	public Set<Key> keySet()
	{
		cleanup();
		return new KeySet( map.keySet() );
	}

	
	public Value put(Key key, Value value)
	{
		cleanup();
		return map.put( new WeakIdKey( key, refQueue ), value );
	}

	
	public void putAll(Map<? extends Key, ? extends Value> m)
	{
		cleanup();
		for (Map.Entry<? extends Key, ? extends Value> e: m.entrySet())
		{
			map.put( new WeakIdKey( e.getKey(), refQueue ), e.getValue() );
		}
	}

	
	public Value remove(Object key)
	{
		cleanup();
		return map.remove( new WeakIdKey( key ) );
	}
	
	
	public int size()
	{
		cleanup();
		return map.size();
	}

	
	public Collection<Value> values()
	{
		cleanup();
		return map.values();
	}

	
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		else if ( x instanceof WeakIdentityHashMap )
		{
			@SuppressWarnings("unchecked")
			WeakIdentityHashMap<Key, Value> w = (WeakIdentityHashMap<Key, Value>)x;
			return map.equals( w.map );
		}
		else
		{
			return false;
		}
	}
	
	
	public int hashCode()
	{
		return map.hashCode();
	}

	
	
	private void cleanup()
	{
		Reference<?> k;
		
		k = refQueue.poll();
		while ( k != null )
		{
			map.remove( k );
			k = refQueue.poll();
		}
	}
}
