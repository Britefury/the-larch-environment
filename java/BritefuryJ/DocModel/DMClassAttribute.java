//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.IdentityHashMap;
import java.util.Map;

import org.python.core.Py;

public class DMClassAttribute
{
	private static class Entry
	{
		public Object value;
		public boolean bValid;
		
		
		public Entry(Object value, boolean bValid)
		{
			this.value = value;
			this.bValid = bValid;
		}
	}
	
	private IdentityHashMap<DMNodeClass, Entry> values = new IdentityHashMap<DMNodeClass, Entry>();
	private String name;
	
	
	public DMClassAttribute(String name, Map<DMNodeClass, Object> vals)
	{
		this.name = name;
		
		for (Map.Entry<DMNodeClass, Object> entry: vals.entrySet())
		{
			values.put( entry.getKey(), new Entry( entry.getValue(), true ) );
		}
	}
	
	
	public boolean contains(DMNodeClass key)
	{
		return getEntry( key ).bValid;
	}
	
	public Object get(DMNodeClass key)
	{
		return getEntry( key ).value;
	}
	
	public Object __getitem__(DMNodeClass key)
	{
		Entry e = getEntry( key );
		if ( e.bValid )
		{
			return e.value;
		}
		else
		{
			throw Py.KeyError( "No value for attribute '" + name + "' + for node class '" + key.getName() + "'" );
		}
	}
	
	
	private Entry getEntry(DMNodeClass key)
	{
		Entry e = values.get( key );
		
		if ( e == null )
		{
			DMNodeClass superClass = key.getSuperclass();
			
			if ( superClass != null )
			{
				e = getEntry( superClass );
				e = new Entry( e.value, false );
			}
			else
			{
				e = new Entry( null, false );
			}
			
			values.put( key, e );
		}
		
		return e;
	}
}
