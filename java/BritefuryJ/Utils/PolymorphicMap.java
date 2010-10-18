//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

public class PolymorphicMap <ValueType extends Object> implements Serializable
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
	
	private HashMap<Class<?>, Entry<ValueType>> registeredJavaValues = new HashMap<Class<?>, Entry<ValueType>>();
	private HashMap<Class<?>, Entry<ValueType>> cachedJavaValues = new HashMap<Class<?>, Entry<ValueType>>();
	private HashMap<PyType, Entry<ValueType>> registeredPythonValues = new HashMap<PyType, Entry<ValueType>>();
	private HashMap<PyType, Entry<ValueType>> cachedPythonValues = new HashMap<PyType, Entry<ValueType>>();
	private Entry<ValueType> nullEntry = new Entry<ValueType>( null );
	
	
	public PolymorphicMap()
	{
	}


	
	public void put(Class<?> type, ValueType value)
	{
		registeredJavaValues.put( type, new Entry<ValueType>( value ) );
		cachedJavaValues.clear();
	}
	
	public void put(PyType type, ValueType value)
	{
		registeredPythonValues.put( type, new Entry<ValueType>( value ) );
		cachedPythonValues.clear();
	}
	
	public void remove(Class<?> type)
	{
		registeredJavaValues.remove( type );
		cachedJavaValues.clear();
	}
	
	public void remove(PyType type)
	{
		registeredPythonValues.remove( type );
		cachedPythonValues.clear();
	}
	
	public void copyFrom(PolymorphicMap<ValueType> values)
	{
		registeredJavaValues.putAll( values.registeredJavaValues );
		cachedJavaValues.clear();
		registeredPythonValues.putAll( values.registeredPythonValues );
		cachedPythonValues.clear();
	}
	
	public ValueType getForInstance(Object x)
	{
		if ( x instanceof PyObject )
		{
			PyObject pyX = (PyObject)x;
			return get( pyX.getType() );
		}
		else
		{
			return get( x.getClass() );
		}
	}
	
	public ValueType get(Class<?> type)
	{
		if ( registeredJavaValues.isEmpty() )
		{
			return null;
		}
		
		// If the list of cached values is empty, but the registered list is not, then copy
		if ( cachedJavaValues.isEmpty()  &&  !registeredJavaValues.isEmpty() )
		{
			cachedJavaValues.putAll( registeredJavaValues );
		}

		
		// See if we have a value
		Entry<ValueType> entry = cachedJavaValues.get( type );
		if ( entry != null )
		{
			return entry.value;
		}
		
		// No, we don't
		if ( type != Object.class )
		{
			// The class of x is a subclass of Object
			Class<?> superClass = type.getSuperclass();
			
			while ( superClass != Object.class )
			{
				// See if we can get a value for this superclass
				entry = cachedJavaValues.get( superClass );
				if ( entry != null )
				{
					// Yes - cache it for future queries
					cachedJavaValues.put( type, entry );
					return entry.value;
				}
				
				// Try the next class up the hierarchy
				superClass = superClass.getSuperclass();
			}
		}
		
		// Now check the interfaces
		
		// First, build a list of all interfaces implemented by x, and its superclasses.
		Class<?> c = type;
		HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
		Stack<Class<?>> interfaceStack = new Stack<Class<?>>();
		while ( c != Object.class )
		{
			for (Class<?> iface: c.getInterfaces())
			{
				// See if we can get a value for this interface
				entry = cachedJavaValues.get( iface );
				if ( entry != null )
				{
					// Yes - cache it for future queries
					cachedJavaValues.put( type, entry );
					return entry.value;
				}
				if ( !interfaces.contains( iface ) )
				{
					interfaces.add( iface );
					interfaceStack.add( iface );
				}
			}
			c = c.getSuperclass();
		}
		
		// Now check the super interfaces
		while ( !interfaceStack.empty() )
		{
			Class<?> interfaceFromStack = interfaceStack.pop();
			for (Class<?> superInterface: interfaceFromStack.getInterfaces())
			{
				// See if we can get a value for this super-interface
				entry = cachedJavaValues.get( superInterface );
				if ( entry != null )
				{
					// Yes - cache it for future queries
					cachedJavaValues.put( type, entry );
					return entry.value;
				}
				if ( !interfaces.contains( superInterface ) )
				{
					interfaces.add( superInterface );
					interfaceStack.add( superInterface );
				}
			}
		}
		
		cachedJavaValues.put( type, nullEntry );
		return null;
	}
	
	public ValueType get(PyType type)
	{
		if ( registeredPythonValues.isEmpty() )
		{
			return null;
		}
		
		// If the list of python values is empty, but the registered list is not, then copy
		if ( cachedPythonValues.isEmpty()  &&  !registeredPythonValues.isEmpty() )
		{
			cachedPythonValues.putAll( registeredPythonValues );
		}

		// See if we have a presenter
		Entry<ValueType> entry = cachedPythonValues.get( type );
		if ( entry != null )
		{
			return entry.value;
		}
		
		// No, we don't
		PyTuple mro = type.getMro();
		
		for (PyObject t: mro.getArray())
		{
			PyType superType = (PyType)t;
			
			// See if we can get a presenter for this superclass
			entry = cachedPythonValues.get( superType );
			if ( entry != null )
			{
				// Yes - cache it for future tests
				cachedPythonValues.put( type, entry );
				return entry.value;
			}
		}
		
		cachedPythonValues.put( type, nullEntry );
		return null;
	}
}
