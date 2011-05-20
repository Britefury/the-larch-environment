//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Utils;

import java.lang.ref.ReferenceQueue;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class WeakValueHashMap <Key, Value> implements Map<Key, Value>
{
	private static <Key, Value> Map.Entry<Key, WeakValue<Value, Key>> weakEntry(Map.Entry<Key, Value> e)
	{
		return new AbstractMap.SimpleImmutableEntry<Key, WeakValue<Value, Key>>( e.getKey(), new WeakValue<Value, Key>( e.getValue(), e.getKey() ) );
	}
	
	
	private static <Key, Value> WeakValue<Value, Key> weakValue(Value v)
	{
		return new WeakValue<Value, Key>( v );
	}
	
	
	
	
	private class ValueIterator implements Iterator<Value>
	{
		private Iterator<Map.Entry<Key, Value>> iter;
		
		private ValueIterator(Iterator<Map.Entry<Key, Value>> iter)
		{
			this.iter = iter;
		}
		
		
		public boolean hasNext()
		{
			return iter.hasNext();
		}

		
		public Value next()
		{
			return iter.next().getValue();
		}

		
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private class ValueCollection implements Collection<Value>
	{
		private Collection<WeakValue<Value, Key>> col;
		
		private ValueCollection(Collection<WeakValue<Value, Key>> col)
		{
			this.col = col;
		}

		
		public int size()
		{
			cleanup();
			return WeakValueHashMap.this.size();
		}

		
		public boolean isEmpty()
		{
			cleanup();
			return WeakValueHashMap.this.isEmpty();
		}

		
		public boolean contains(Object o)
		{
			cleanup();
			return col.contains( weakValue( o ) );
		}

		
		public Iterator<Value> iterator()
		{
			cleanup();
			return new ValueIterator( WeakValueHashMap.this.entrySet().iterator() );
		}

		
		public Object[] toArray()
		{
			cleanup();
			ArrayList<Object> values = new ArrayList<Object>();
			for (Map.Entry<Key, Value> e: WeakValueHashMap.this.entrySet())
			{
				values.add( e.getValue() );
			}
			return values.toArray();
		}

		@SuppressWarnings("unchecked")
		
		public <T> T[] toArray(T[] a)
		{
			cleanup();
			ArrayList<T> values = new ArrayList<T>();
			for (Map.Entry<Key, Value> e: WeakValueHashMap.this.entrySet())
			{
				values.add( (T)e.getValue() );
			}
			return values.toArray( a );
		}

		
		public boolean add(Value v)
		{
			throw new UnsupportedOperationException();
		}

		
		public boolean remove(Object o)
		{
			cleanup();
			return col.remove( weakValue( o ) );
		}

		
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

		
		public boolean addAll(Collection<? extends Value> c)
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		
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

		
		public void clear()
		{
			cleanup();
			WeakValueHashMap.this.clear();
		}
	}
	
	
	
	private static class Entry <Key, Value> implements Map.Entry<Key, Value>
	{
		private Map.Entry<Key, WeakValue<Value, Key>> e;
		private Value v;
		
		public Entry(Map.Entry<Key, WeakValue<Value, Key>> e)
		{
			this.e = e;
			this.v = e.getValue().get();
		}

		
		public Key getKey()
		{
			return e.getKey();
		}

		
		public Value getValue()
		{
			return v;
		}

		
		public Value setValue(Value value)
		{
			this.v = value;
			WeakValue<Value, Key> w = new WeakValue<Value, Key>( value );
			w = e.setValue( w );
			return w.get();
		}
	}
	
	private class EntryIterator implements Iterator<Map.Entry<Key, Value>>
	{
		private Iterator<Map.Entry<Key, WeakValue<Value, Key>>> iter;
		private Entry<Key, Value> next = null;
		
		private EntryIterator(Iterator<Map.Entry<Key, WeakValue<Value, Key>>> iter)
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
				Map.Entry<Key, WeakValue<Value, Key>> e = iter.next();
				Value v = e.getValue().get();
				if ( v != null )
				{
					return new Entry<Key, Value>( e );
				}
			}
			return null;
		}
	}
	
	private class EntrySet implements Set<Map.Entry<Key, Value>>
	{
		private Set<Map.Entry<Key, WeakValue<Value, Key>>> set;
		
		private EntrySet(Set<Map.Entry<Key, WeakValue<Value, Key>>> set)
		{
			this.set = set;
		}

		
		public int size()
		{
			cleanup();
			return WeakValueHashMap.this.size();
		}

		
		public boolean isEmpty()
		{
			cleanup();
			return WeakValueHashMap.this.isEmpty();
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
			ArrayList<Map.Entry<Key, WeakValue<Value, Key>>> xs = new ArrayList<Map.Entry<Key, WeakValue<Value, Key>>>(); 
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
			ArrayList<Map.Entry<Key, WeakValue<Value, Key>>> xs = new ArrayList<Map.Entry<Key, WeakValue<Value, Key>>>(); 
			for (Object x: c)
			{
				xs.add( weakEntry( (Map.Entry<Key, Value>)x ));
			}
			return set.removeAll( xs );
		}

		
		public void clear()
		{
			cleanup();
			WeakValueHashMap.this.clear();
		}
	}
	
	
	private ReferenceQueue<Value> refQueue = new ReferenceQueue<Value>();
	private HashMap<Key, WeakValue<Value, Key>> map = new HashMap<Key, WeakValue<Value, Key>>();
	
	

	
	public void clear()
	{
		cleanup();
		map.clear();
	}

	
	public boolean containsKey(Object key)
	{
		cleanup();
		return map.containsKey( key );
	}

	
	public boolean containsValue(Object value)
	{
		cleanup();
		return map.containsValue( weakValue( value ) );
	}

	
	public Set<Map.Entry<Key, Value>> entrySet()
	{
		cleanup();
		return new EntrySet( map.entrySet() );
	}

	
	public Value get(Object key)
	{
		cleanup();
		WeakValue<Value, Key> w = map.get( key );
		return w != null  ?  w.get()  :  null;
	}

	
	public boolean isEmpty()
	{
		cleanup();
		return map.isEmpty();
	}

	
	public Set<Key> keySet()
	{
		cleanup();
		return map.keySet();
	}

	
	public Value put(Key key, Value value)
	{
		cleanup();
		WeakValue<Value, Key> w = map.put( key, new WeakValue<Value, Key>( value, refQueue, key ) );
		return w != null  ?  w.get()  :  null;
	}

	
	public void putAll(Map<? extends Key, ? extends Value> m)
	{
		cleanup();
		for (Map.Entry<? extends Key, ? extends Value> e: m.entrySet())
		{
			Key key = e.getKey();
			map.put( key, new WeakValue<Value, Key>( e.getValue(), refQueue, key ) );
		}
	}

	
	public Value remove(Object key)
	{
		cleanup();
		WeakValue<Value, Key> w = map.remove( key );
		return w != null  ?  w.get()  :  null;
	}
	
	
	public int size()
	{
		cleanup();
		return map.size();
	}

	
	public Collection<Value> values()
	{
		cleanup();
		return new ValueCollection( map.values() );
	}

	
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		else if ( x instanceof WeakValueHashMap )
		{
			@SuppressWarnings("unchecked")
			WeakValueHashMap<Key, Value> w = (WeakValueHashMap<Key, Value>)x;
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

	
	
	@SuppressWarnings("unchecked")
	private void cleanup()
	{
		WeakValue<Value, Key> r;
		
		r = (WeakValue<Value, Key>)refQueue.poll();
		while ( r != null )
		{
			remove( r.key );
			r = (WeakValue<Value, Key>)refQueue.poll();
		}
	}
}
