//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Utils;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WeakIdentityHashMap <Key, Value> implements Map<Key, Value>
{
	private static class WeakIdKey extends WeakReference<Object>
	{
		private int hashCode;
		
		
		public WeakIdKey(Object key)
		{
			super( key );
			this.hashCode = System.identityHashCode( key );
		}
		
		public WeakIdKey(Object key, ReferenceQueue<Object> queue)
		{
			super( key, queue );
			this.hashCode = System.identityHashCode( key );
		}
		
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			Object key = get();
			if ( x instanceof WeakIdKey )
			{
				WeakIdKey w = (WeakIdKey)x;
				return key == w.get();
			}
			else
			{
				return key == x;
			}
		}
		
		public int hashCode()
		{
			return hashCode;
		}
	}
	
	
	private class KeyIterator implements Iterator<Key>
	{
		private Iterator<WeakIdKey> iter;
		
		private KeyIterator(Iterator<WeakIdKey> iter)
		{
			this.iter = iter;
		}
		
		@Override
		public boolean hasNext()
		{
			cleanup();
			return iter.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Key next()
		{
			cleanup();
			return (Key)iter.next().get();
		}

		@Override
		public void remove()
		{
			cleanup();
			iter.remove();
		}
	}
	
	private class KeySet implements Set<Key>
	{
		private Set<WeakIdKey> set;
		
		private KeySet(Set<WeakIdKey> set)
		{
			this.set = set;
		}

		@Override
		public int size()
		{
			cleanup();
			return set.size();
		}

		@Override
		public boolean isEmpty()
		{
			cleanup();
			return set.isEmpty();
		}

		@Override
		public boolean contains(Object o)
		{
			cleanup();
			return set.contains( new WeakIdKey( o ) );
		}

		@Override
		public Iterator<Key> iterator()
		{
			cleanup();
			return new KeyIterator( set.iterator() );
		}

		@Override
		public Object[] toArray()
		{
			cleanup();
			WeakIdKey ws[] = set.toArray( new WeakIdKey[] {} );
			Object xs[] = new Object[ws.length];
			int i = 0;
			for (WeakIdKey w: ws)
			{
				xs[i++] = w.get();
			}
			return xs;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] a)
		{
			cleanup();
			WeakIdKey ws[] = set.toArray( new WeakIdKey[] {} );
			if ( a.length != ws.length )
			{
				a = (T[])new Object[ws.length];
			}
			int i = 0;
			for (WeakIdKey w: ws)
			{
				a[i++] = (T)w.get();
			}
			return a;
		}

		@Override
		public boolean add(Key e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o)
		{
			cleanup();
			return set.remove( new WeakIdKey( o ) );
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			cleanup();
			for (Object x: c)
			{
				if ( !set.contains( new WeakIdKey( x ) ) )
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends Key> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
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

		@Override
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

		@Override
		public void clear()
		{
			cleanup();
			set.clear();
		}
	}
	
	
	
	private static <Key, Value> Map.Entry<Key, Value> entry(Key k, Value v)
	{
		return new AbstractMap.SimpleImmutableEntry<Key, Value>( k, v );
	}
	
	private static <Key, Value> Map.Entry<WeakIdKey, Value> weakEntry(Key k, Value v)
	{
		return new AbstractMap.SimpleImmutableEntry<WeakIdKey, Value>( new WeakIdKey( k ), v );
	}
	
	
	private class EntryIterator implements Iterator<Map.Entry<Key, Value>>
	{
		private Iterator<Map.Entry<WeakIdKey, Value>> iter;
		
		private EntryIterator(Iterator<Map.Entry<WeakIdKey, Value>> iter)
		{
			this.iter = iter;
		}
		
		@Override
		public boolean hasNext()
		{
			cleanup();
			return iter.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map.Entry<Key, Value> next()
		{
			cleanup();
			Map.Entry<WeakIdKey, Value> e = iter.next();
			return entry( (Key)e.getKey().get(), e.getValue() );
		}

		@Override
		public void remove()
		{
			cleanup();
			iter.remove();
		}
	}
	
	private class EntrySet implements Set<Map.Entry<Key, Value>>
	{
		private Set<Map.Entry<WeakIdKey, Value>> set;
		
		private EntrySet(Set<Map.Entry<WeakIdKey, Value>> set)
		{
			this.set = set;
		}

		@Override
		public int size()
		{
			cleanup();
			return set.size();
		}

		@Override
		public boolean isEmpty()
		{
			cleanup();
			return set.isEmpty();
		}

		@Override
		public boolean contains(Object o)
		{
			cleanup();
			@SuppressWarnings("unchecked")
			Map.Entry<Key, Value> e = (Map.Entry<Key, Value>)o;
			return set.contains( weakEntry( e.getKey(), e.getValue()) );
		}

		@Override
		public Iterator<Map.Entry<Key, Value>> iterator()
		{
			cleanup();
			return new EntryIterator( set.iterator() );
		}

		@Override
		public Object[] toArray()
		{
			cleanup();
			@SuppressWarnings("unchecked")
			Map.Entry<WeakIdKey, Value> ws[] = set.toArray( new Map.Entry[] {} );
			Object xs[] = new Object[ws.length];
			int i = 0;
			for (Map.Entry<WeakIdKey, Value> w: ws)
			{
				xs[i++] = entry( w.getKey().get(), w.getValue() );
			}
			return xs;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] a)
		{
			cleanup();
			Map.Entry<WeakIdKey, Value> ws[] = set.toArray( new Map.Entry[] {} );
			if ( a.length != ws.length )
			{
				a = (T[])new Object[ws.length];
			}
			int i = 0;
			for (Map.Entry<WeakIdKey, Value> w: ws)
			{
				a[i++] = (T)entry( w.getKey().get(), w.getValue() );
			}
			return a;
		}

		@Override
		public boolean add(Map.Entry<Key, Value> e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o)
		{
			cleanup();
			@SuppressWarnings("unchecked")
			Map.Entry<Key, Value> e = (Map.Entry<Key, Value>)o;
			return set.remove( weakEntry( e.getKey(), e.getValue() ) );
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			cleanup();
			for (Object x: c)
			{
				@SuppressWarnings("unchecked")
				Map.Entry<Key, Value> e = (Map.Entry<Key, Value>)x;
				if ( !set.contains( weakEntry( e.getKey(), e.getValue() ) ) )
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends Map.Entry<Key, Value>> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			cleanup();
			ArrayList<Map.Entry<WeakIdKey, Value>> xs = new ArrayList<Map.Entry<WeakIdKey, Value>>(); 
			for (Object x: c)
			{
				@SuppressWarnings("unchecked")
				Map.Entry<Key, Value> e = (Map.Entry<Key, Value>)x;
				xs.add( weakEntry( e.getKey(), e.getValue() ));
			}
			return set.retainAll( xs );
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			cleanup();
			ArrayList<Map.Entry<WeakIdKey, Value>> xs = new ArrayList<Map.Entry<WeakIdKey, Value>>(); 
			for (Object x: c)
			{
				@SuppressWarnings("unchecked")
				Map.Entry<Key, Value> e = (Map.Entry<Key, Value>)x;
				xs.add( weakEntry( e.getKey(), e.getValue() ) );
			}
			return set.removeAll( xs );
		}

		@Override
		public void clear()
		{
			cleanup();
			set.clear();
		}
	}
	
	
	private ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
	private HashMap<WeakIdKey, Value> map = new HashMap<WeakIdKey, Value>();
	
	

	@Override
	public void clear()
	{
		cleanup();
		map.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		cleanup();
		return map.containsKey( new WeakIdKey( key ) );
	}

	@Override
	public boolean containsValue(Object value)
	{
		cleanup();
		return map.containsValue( value );
	}

	@Override
	public Set<Map.Entry<Key, Value>> entrySet()
	{
		cleanup();
		return new EntrySet( map.entrySet() );
	}

	@Override
	public Value get(Object key)
	{
		cleanup();
		return map.get( new WeakIdKey( key ) );
	}

	@Override
	public boolean isEmpty()
	{
		cleanup();
		return map.isEmpty();
	}

	@Override
	public Set<Key> keySet()
	{
		cleanup();
		return new KeySet( map.keySet() );
	}

	@Override
	public Value put(Key key, Value value)
	{
		cleanup();
		return map.put( new WeakIdKey( key, refQueue ), value );
	}

	@Override
	public void putAll(Map<? extends Key, ? extends Value> m)
	{
		cleanup();
		for (Map.Entry<? extends Key, ? extends Value> e: m.entrySet())
		{
			map.put( new WeakIdKey( e.getKey(), refQueue ), e.getValue() );
		}
	}

	@Override
	public Value remove(Object key)
	{
		cleanup();
		return map.remove( new WeakIdKey( key ) );
	}
	
	@Override
	public int size()
	{
		cleanup();
		return map.size();
	}

	@Override
	public Collection<Value> values()
	{
		cleanup();
		return map.values();
	}

	
	
	@Override
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
	
	@Override
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
