//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.IdentityHashMap;

import org.python.core.Py;

public class DMClassAttribute
{
	private static class Entry
	{
		public Object value;
		public boolean bInherited, bValid;
		
		
		public Entry(Object value, boolean bInherited, boolean bValid)
		{
			this.value = value;
			this.bInherited = bInherited;
			this.bValid = bValid;
		}
	}
	
	private IdentityHashMap<DMNodeClass, Entry> values = new IdentityHashMap<DMNodeClass, Entry>();
	private String name;
	private DMNodeClass rootClass;
	private boolean bCommitted;
	
	
	public DMClassAttribute(String name, DMNodeClass rootClass)
	{
		this.name = name;
		this.rootClass = rootClass;
		this.bCommitted = false;
		
		rootClass.registerClassAttribute( this );
	}
	
	
	public String getName()
	{
		return name;
	}
	
	
	public DMNodeClass getRootClass()
	{
		return rootClass;
	}
	
	
	
	public void set(DMNodeClass key, Object value)
	{
		if ( bCommitted )
		{
			throw new RuntimeException( "Cannot add set class attribute values after committing" );
		}
		
		if ( !key.isSubclassOf( rootClass ) )
		{
			throw new RuntimeException( "Class attributes can only be applied to subclasses of the root class (" + rootClass.getName() + ")" );
		}
		
		values.put( key, new Entry( value, false, true ) );
	}
	
	public void __setitem__(DMNodeClass key, Object value)
	{
		if ( bCommitted )
		{
			throw Py.ValueError( "Cannot add set class attribute values after committing" );
		}
		
		if ( !key.isSubclassOf( rootClass ) )
		{
			throw Py.KeyError( "Class attributes can only be applied to subclasses of the root class (" + rootClass.getName() + ")" );
		}
		
		values.put( key, new Entry( value, false, true ) );
	}
	
	public void commit()
	{
		if ( bCommitted )
		{
			throw new RuntimeException( "Cannot commit twice" );
		}
		bCommitted = true;
	}
	
	
	public boolean contains(DMNodeClass key)
	{
		Entry entry = getEntry( key );
		return entry != null  &&  entry.bValid  &&  !entry.bInherited;
	}
	
	public boolean hasValueFor(DMNodeClass key)
	{
		Entry entry = getEntry( key );
		return entry != null  &&  entry.bValid;
	}
	
	public Object get(DMNodeClass key)
	{
		Entry e = getEntry( key );
		return e != null  ?  e.value  :  null;
	}
	
	public Object get(DMNode node)
	{
		return get( node.getDMNodeClass() );
	}
	
	public Object __getitem__(DMNodeClass key)
	{
		Entry e = getEntry( key );
		if ( e != null  &&  e.bValid )
		{
			return e.value;
		}
		else
		{
			throw Py.KeyError( "No value for attribute '" + name + "' + for node class '" + key.getName() + "'" );
		}
	}
	
	public Object __getitem__(DMNode node)
	{
		return __getitem__( node.getDMNodeClass() );
	}
	
	
	private Entry getEntry(DMNodeClass key)
	{
		if ( !bCommitted )
		{
			throw new RuntimeException( "Class attributes must be committed before being accessed. Call commit() after inserting all values." );
		}
		Entry e = values.get( key );
		
		if ( e == null )
		{
			if ( !key.isSubclassOf( rootClass ) )
			{
				return null;
			}
			
			DMNodeClass superClass = key.getSuperclass();
			
			if ( superClass != null )
			{
				e = getEntry( superClass );
				e = new Entry( e.value, true, true );
			}
			else
			{
				e = new Entry( null, false, false );
			}
			
			values.put( key, e );
		}
		
		return e;
	}
}
