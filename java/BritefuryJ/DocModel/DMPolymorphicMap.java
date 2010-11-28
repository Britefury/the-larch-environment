//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel;

import java.io.Serializable;
import java.util.HashMap;


public class DMPolymorphicMap <ValueType extends Object> implements Serializable
{
	private static final long serialVersionUID = 1L;


	private static class Entry <ValueType extends Object>
	{
		private ValueType value;
		
		public Entry(ValueType value)
		{
			this.value = value;
		}
	}
	
	private HashMap<DMNodeClass, Entry<ValueType>> registeredValues = new HashMap<DMNodeClass, Entry<ValueType>>();
	private HashMap<DMNodeClass, Entry<ValueType>> cachedValues = new HashMap<DMNodeClass, Entry<ValueType>>();
	private Entry<ValueType> nullEntry = new Entry<ValueType>( null );
	
	
	public DMPolymorphicMap()
	{
	}


	
	public void put(DMNodeClass type, ValueType value)
	{
		registeredValues.put( type, new Entry<ValueType>( value ) );
		cachedValues.clear();
	}
	
	public void remove(DMNodeClass type)
	{
		registeredValues.remove( type );
		cachedValues.clear();
	}
	
	public void update(DMPolymorphicMap<ValueType> values)
	{
		registeredValues.putAll( values.registeredValues );
		cachedValues.clear();
	}
	
	public ValueType get(DMObject x)
	{
		return get( x.getDMObjectClass() );
	}
	
	public ValueType get(DMNodeClass type)
	{
		if ( registeredValues.isEmpty() )
		{
			return null;
		}
		
		// If the list of cached values is empty, but the registered list is not, then copy
		if ( cachedValues.isEmpty()  &&  !registeredValues.isEmpty() )
		{
			cachedValues.putAll( registeredValues );
		}

		
		// See if we have a value
		Entry<ValueType> entry = cachedValues.get( type );
		if ( entry != null )
		{
			return entry.value;
		}
		
		// No, we don't
		// The class of x is a subclass of Object
		DMNodeClass superClass = type.getSuperclass();
		
		while ( superClass != null )
		{
			// See if we can get a value for this superclass
			entry = cachedValues.get( superClass );
			if ( entry != null )
			{
				// Yes - cache it for future queries
				cachedValues.put( type, entry );
				return entry.value;
			}
			
			// Try the next class up the hierarchy
			superClass = superClass.getSuperclass();
		}
		
		cachedValues.put( type, nullEntry );
		return null;
	}
}
