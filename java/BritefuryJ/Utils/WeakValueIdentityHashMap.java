//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WeakValueIdentityHashMap <Key, Value> implements Map<Key, Value>
{
	private static <Key, Value> Map.Entry<Key, Value> entry(Map.Entry<Key, WeakValue<Value, Key>> e)
	{
		return new AbstractMap.SimpleImmutableEntry<Key, Value>( e.getKey(), e.getValue().get() );
	}
	
	private static <Key, Value> Map.Entry<Key, WeakValue<Value, Key>> weakEntry(Map.Entry<Key, Value> e)
	{
		return new AbstractMap.SimpleImmutableEntry<Key, WeakValue<Value, Key>>( e.getKey(), new WeakValue<Value, Key>( e.getValue(), e.getKey() ) );
	}
	
	
	private static <Key, Value> WeakValue<Value, Key> weakValue(Value v)
	{
		return new WeakValue<Value, Key>( v );
	}
	
	
	
	
	private static class WeakValue <Value, Key> extends WeakReference<Value>
	{
		private Key key;
		
		
		public WeakValue(Value val)
		{
			super( val );
			this.key = null;
		}
		
		public WeakValue(Value val, Key key)
		{
			super( val );
			this.key = key;
		}
		
		public WeakValue(Value val, ReferenceQueue<Value> refQueue, Key key)
		{
			super( val, refQueue );
			this.key = key;
		}
		
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			else if ( x instanceof WeakValue )
			{
				Value v = get();
				return v.equals( ((WeakValue<Value, Key>)x).get() );
			}
			else
			{
				Value v = get();
				return v.equals( x );
			}
		}
		
		@Override
		public int hashCode()
		{
			Value v = get();
			return v != null  ?  v.hashCode()  :  0;
		}
	}
	
	
	
	private class ValueIterator implements Iterator<Value>
	{
		private Iterator<WeakValue<Value, Key>> iter;
		
		private ValueIterator(Iterator<WeakValue<Value, Key>> iter)
		{
			this.iter = iter;
		}
		
		@Override
		public boolean hasNext()
		{
			cleanup();
			return iter.hasNext();
		}

		@Override
		public Value next()
		{
			cleanup();
			return iter.next().get();
		}

		@Override
		public void remove()
		{
			cleanup();
			iter.remove();
		}
	}
	
	private class ValueCollection implements Collection<Value>
	{
		private Collection<WeakValue<Value, Key>> col;
		
		private ValueCollection(Collection<WeakValue<Value, Key>> col)
		{
			this.col = col;
		}

		@Override
		public int size()
		{
			cleanup();
			return col.size();
		}

		@Override
		public boolean isEmpty()
		{
			cleanup();
			return col.isEmpty();
		}

		@Override
		public boolean contains(Object o)
		{
			cleanup();
			return col.contains( weakValue( o ) );
		}

		@Override
		public Iterator<Value> iterator()
		{
			cleanup();
			return new ValueIterator( col.iterator() );
		}

		@Override
		public Object[] toArray()
		{
			cleanup();
			@SuppressWarnings("unchecked")
			WeakValue<Value, Key> ws[] = col.toArray( new WeakValue[] {} );
			Object xs[] = new Object[ws.length];
			int i = 0;
			for (WeakValue<Value, Key> w: ws)
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
			WeakValue<Value, Key> ws[] = col.toArray( new WeakValue[] {} );
			if ( a.length != ws.length )
			{
				a = (T[])new Object[ws.length];
			}
			int i = 0;
			for (WeakValue<Value, Key> w: ws)
			{
				a[i++] = (T)w.get();
			}
			return a;
		}

		@Override
		public boolean add(Value v)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o)
		{
			cleanup();
			return col.remove( weakValue( o ) );
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			cleanup();
			for (Object x: c)
			{
				if ( !col.contains( weakValue( x ) ) )
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends Value> c)
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean retainAll(Collection<?> c)
		{
			cleanup();
			ArrayList<WeakValue<Value, Key>> xs = new ArrayList<WeakValue<Value, Key>>(); 
			for (Object x: c)
			{
				xs.add( (WeakValue<Value, Key>)weakValue( x ) );
			}
			return col.retainAll( xs );
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean removeAll(Collection<?> c)
		{
			cleanup();
			ArrayList<WeakValue<Value, Key>> xs = new ArrayList<WeakValue<Value, Key>>(); 
			for (Object x: c)
			{
				xs.add( (WeakValue<Value, Key>)weakValue( x ) );
			}
			return col.removeAll( xs );
		}

		@Override
		public void clear()
		{
			cleanup();
			col.clear();
		}
	}
	
	
	
	private class EntryIterator implements Iterator<Map.Entry<Key, Value>>
	{
		private Iterator<Map.Entry<Key, WeakValue<Value, Key>>> iter;
		
		private EntryIterator(Iterator<Map.Entry<Key, WeakValue<Value, Key>>> iter)
		{
			this.iter = iter;
		}
		
		@Override
		public boolean hasNext()
		{
			cleanup();
			return iter.hasNext();
		}

		@Override
		public Map.Entry<Key, Value> next()
		{
			cleanup();
			return entry( iter.next() );
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
		private Set<Map.Entry<Key, WeakValue<Value, Key>>> set;
		
		private EntrySet(Set<Map.Entry<Key, WeakValue<Value, Key>>> set)
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

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object o)
		{
			cleanup();
			return set.contains( weakEntry( (Map.Entry<Key, Value>)o ) );
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
			Map.Entry<Key, WeakValue<Value, Key>> ws[] = set.toArray( new Map.Entry[] {} );
			Object xs[] = new Object[ws.length];
			int i = 0;
			for (Map.Entry<Key, WeakValue<Value, Key>> w: ws)
			{
				xs[i++] = entry( w );
			}
			return xs;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] a)
		{
			cleanup();
			Map.Entry<Key, WeakValue<Value, Key>> ws[] = set.toArray( new Map.Entry[] {} );
			if ( a.length != ws.length )
			{
				a = (T[])new Object[ws.length];
			}
			int i = 0;
			for (Map.Entry<Key, WeakValue<Value, Key>> w: ws)
			{
				a[i++] = (T)entry( w );
			}
			return a;
		}

		@Override
		public boolean add(Map.Entry<Key, Value> e)
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object o)
		{
			cleanup();
			return set.remove( weakEntry( (Map.Entry<Key, Value>)o ) );
		}

		@SuppressWarnings("unchecked")
		@Override
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

		@Override
		public boolean addAll(Collection<? extends Map.Entry<Key, Value>> c)
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean retainAll(Collection<?> c)
		{
			cleanup();
			ArrayList<Map.Entry<Key, WeakValue<Value, Key>>> xs = new ArrayList<Map.Entry<Key, WeakValue<Value, Key>>>(); 
			for (Object x: c)
			{
				xs.add( weakEntry( (Map.Entry<Key, Value>)x ));
			}
			return set.retainAll( xs );
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean removeAll(Collection<?> c)
		{
			cleanup();
			ArrayList<Map.Entry<Key, WeakValue<Value, Key>>> xs = new ArrayList<Map.Entry<Key, WeakValue<Value, Key>>>(); 
			for (Object x: c)
			{
				xs.add( weakEntry( (Map.Entry<Key, Value>)x ));
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
	
	
	private ReferenceQueue<Value> refQueue = new ReferenceQueue<Value>();
	private HashMap<Key, WeakValue<Value, Key>> map = new HashMap<Key, WeakValue<Value, Key>>();
	
	

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
		return map.containsKey( key );
	}

	@Override
	public boolean containsValue(Object value)
	{
		cleanup();
		return map.containsValue( weakValue( value ) );
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
		return map.get( key ).get();
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
		return map.keySet();
	}

	@Override
	public Value put(Key key, Value value)
	{
		cleanup();
		return map.put( key, new WeakValue<Value, Key>( value, refQueue, key ) ).get();
	}

	@Override
	public void putAll(Map<? extends Key, ? extends Value> m)
	{
		cleanup();
		for (Map.Entry<? extends Key, ? extends Value> e: m.entrySet())
		{
			Key key = e.getKey();
			map.put( key, new WeakValue<Value, Key>( e.getValue(), refQueue, key ) );
		}
	}

	@Override
	public Value remove(Object key)
	{
		cleanup();
		return map.remove( key ).get();
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
		return new ValueCollection( map.values() );
	}

	
	
	@Override
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		else if ( x instanceof WeakValueIdentityHashMap )
		{
			@SuppressWarnings("unchecked")
			WeakValueIdentityHashMap<Key, Value> w = (WeakValueIdentityHashMap<Key, Value>)x;
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

	
	
	@SuppressWarnings("unchecked")
	private void cleanup()
	{
		WeakValue<Value, Key> r;
		
		r = (WeakValue<Value, Key>)refQueue.poll();
		while ( r != null )
		{
			map.remove( r.key );
			r = (WeakValue<Value, Key>)refQueue.poll();
		}
	}
}
