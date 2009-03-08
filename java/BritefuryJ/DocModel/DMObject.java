//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.CommandTrackerFactory;
import BritefuryJ.CommandHistory.Trackable;

public class DMObject implements DMObjectInterface, Trackable
{
	public static class InvalidFieldNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected static class SetViewEntry implements Entry<String, Object>
	{
		private DMObject obj;
		private int index;
		
		
		public SetViewEntry(DMObject obj, int index)
		{
			this.obj = obj;
			this.index = index;
		}
		
		
		public String getKey()
		{
			return obj.getDMClass().getField( index ).getName();
		}

		public Object getValue()
		{
			return obj.fieldData[index];
		}

		public Object setValue(Object value)
		{
			Object old = obj.fieldData[index];
			obj.fieldData[index] = value;
			return old;
		}
	}
	
	
	protected static class SetView implements Set<Entry<String, Object>>
	{
		private DMObject obj;
		private Entry<String, Object> entries[];
		
		
		public SetView(DMObject obj)
		{
			this.obj = obj;
		}


		
		public boolean add(Entry<String, Object> arg0)
		{
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends Entry<String, Object>> arg0)
		{
			throw new UnsupportedOperationException();
		}

		public void clear()
		{
			obj.clear();
		}


		public boolean contains(Object x)
		{
			for (int i = 0; i < entries.length; i++)
			{
				if ( x == entries[i] )
				{
					return true;
				}
			}
			
			return false;
		}

		public boolean containsAll(Collection<?> xs)
		{
			for (Object x: xs)
			{
				if ( !contains( x ) )
				{
					return false;
				}
			}
			return true;
		}


		public boolean isEmpty()
		{
			return entries.length == 0;
		}


		public Iterator<Entry<String, Object>> iterator()
		{
			return Arrays.asList( entries ).iterator();
		}


		public boolean remove(Object x)
		{
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> arg0)
		{
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection<?> arg0)
		{
			throw new UnsupportedOperationException();
		}


		public int size()
		{
			return entries.length;
		}


		public Object[] toArray()
		{
			SetViewEntry xs[] = new SetViewEntry[entries.length];
			System.arraycopy( entries, 0, xs, 0, entries.length );
			return xs;
		}

		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] arg0)
		{
			SetViewEntry xs[] = new SetViewEntry[entries.length];
			System.arraycopy( entries, 0, xs, 0, entries.length );
			return (T[])xs;
		}
	}
	
	
	
	
	
	private DMObjectClass objClass;
	private Object fieldData[];
	
	
	
	
	public DMObject(DMObjectClass objClass)
	{
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
	}
	
	public DMObject(DMObjectClass objClass, Object data[])
	{
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
		int numToCopy = Math.min( data.length, fieldData.length );
		System.arraycopy( data, 0, fieldData, 0, numToCopy );
		if ( numToCopy < fieldData.length )
		{
			for (int i = numToCopy; i < fieldData.length; i++)
			{
				fieldData[i] = null;
			}
		}
	}
	
	public DMObject(DMObjectClass objClass, Map<String, Object> data)
	{
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
		for (int i = 0; i < objClass.getNumFields(); i++)
		{
			DMObjectField f = objClass.getField( i );
			String name = f.getName();
			fieldData[i] = data.get( name );
		}
	}
	
	
	
	public DMObjectClass getDMClass()
	{
		return objClass;
	}
	
	public Object getFieldValue(int value)
	{
		return fieldData[value];
	}
	
	
	
	public void clear()
	{
		throw new UnsupportedOperationException();
	}
	
	public boolean containsKey(Object key)
	{
		return objClass.hasField( (String)key );
	}

	public boolean containsValue(Object value)
	{
		for (Object v: fieldData)
		{
			if ( v == value )
			{
				return true;
			}
		}
		
		return false;
	}

	public Set<Entry<String, Object>> entrySet()
	{
		return new SetView( this );
	}
	
	public boolean equals(Object x)
	{
		if ( x instanceof DMObjectInterface )
		{
			DMObjectInterface xx = (DMObjectInterface)x;
			
			if ( xx.getDMClass()  ==  objClass )
			{
				for (int i = 0; i < objClass.getNumFields(); i++)
				{
					if ( !fieldData[i].equals( xx.getFieldValue( i ) ) )
					{
						return false;
					}
				}
				return true;
			}
		}
		
		return false;
	}

	public Object get(Object key)
	{
		int index = objClass.getFieldIndex( (String)key );
		if ( index != -1 )
		{
			return fieldData[index];
		}
		else
		{
			return null;
		}
	}
	
	public boolean isEmpty()
	{
		return objClass.isEmpty();
	}
	
	public Set<String> keySet()
	{
		return objClass.fieldNameSet();
	}
	
	
	public Object put(String key, Object value)
	{
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
			throw new InvalidFieldNameException();
		}
		else
		{
			Object old = fieldData[index];
			fieldData[index] = value;
			return old;
		}
	}

	public void putAll(Map<? extends String, ? extends Object> xs)
	{
		for (Entry<? extends String, ? extends Object> e: xs.entrySet())
		{
			put( e.getKey(), e.getValue() );
		}
	}

	
	
	public Object remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public int size()
	{
		return objClass.getNumFields();
	}


	public Collection<Object> values()
	{
		return Arrays.asList( fieldData );
	}

	
	
	
	public Object copy()
	{
		return new DMObject( objClass, fieldData );
	}

	public Object get(String key, Object defaultValue)
	{
		int index = objClass.getFieldIndex( (String)key );
		if ( index != -1 )
		{
			return fieldData[index];
		}
		else
		{
			return defaultValue;
		}
	}

	
	@SuppressWarnings("unchecked")
	public List<List<Object>> items()
	{
		ArrayList<List<Object>> xs = new ArrayList();
		xs.ensureCapacity( objClass.getNumFields() );
		
		for (int i = 0; i < objClass.getNumFields(); i++)
		{
			ArrayList<Object> pair = new ArrayList();
			pair.ensureCapacity( 2 );
			pair.add( objClass.getField( i ).getName() );
			pair.add( fieldData[i] );
			xs.add( pair );
		}
		
		return xs;
	}
	
	public Iterator<List<Object>> iteritems()
	{
		return items().iterator();
	}
	
	
	public Set<String> keys()
	{
		return keySet();
	}
	
	public Iterator<String> iterkeys()
	{
		return keys().iterator();
	}

	
	public Iterator<Object> itervalues()
	{
		return values().iterator();
	}

	
	public Object pop(String key)
	{
		throw new UnsupportedOperationException();
	}

	public Object pop(String key, Object defaultValue)
	{
		throw new UnsupportedOperationException();
	}

	public Object popitem()
	{
		throw new UnsupportedOperationException();
	}

	
	public Object setdefault(String key)
	{
		return setdefault( key, null );
	}

	public Object setdefault(String key, Object defaultValue)
	{
		return get( key );
	}

	public void update(Map<String, Object> table)
	{
		for (Map.Entry<String, Object> e: table.entrySet())
		{
			put( e.getKey(), e.getValue() );
		}
	}


	@Override
	public CommandTrackerFactory getTrackerFactory()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTracker(CommandTracker tracker)
	{
		// TODO Auto-generated method stub
		
	}
}
