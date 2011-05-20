//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ObjectPresentation;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.python.core.Py;
import org.python.core.PyString;

public class ObjectViewLocationTable
{
	private WeakHashMap<Object, String> objectToLocation = new WeakHashMap<Object, String>();
	private HashMap<String, WeakReference<Object>> locationToObject = new HashMap<String, WeakReference<Object>>();
	private int objectCount = 1;
	
	
	
	public ObjectViewLocationTable()
	{
	}
	
	
	public String getRelativeLocationForObject(Object x)
	{
		String key = objectToLocation.get( x );
		if ( key == null )
		{
			int index = objectCount++;
			key = "o" + index;
			objectToLocation.put( x, key );
			locationToObject.put( key, new WeakReference<Object>( x ) );
		}
		return "." + key;
	}
	
	public Object __resolve__(PyString key)
	{
		String keyString = key.asString();
		WeakReference<Object> ref = locationToObject.get( keyString );
		if ( ref != null )
		{
			Object x = ref.get();
			
			if ( x == null )
			{
				locationToObject.remove( keyString );
			}
			
			return x;
		}
		
		throw Py.KeyError( "Object at " + keyString + " does not exist" );
	}
}
