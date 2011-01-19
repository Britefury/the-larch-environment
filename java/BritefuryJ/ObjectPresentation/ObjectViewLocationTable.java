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

public class ObjectViewLocationTable
{
	private WeakHashMap<Object, Integer> objectToLocation = new WeakHashMap<Object, Integer>();
	private HashMap<Integer, WeakReference<Object>> locationToObject = new HashMap<Integer, WeakReference<Object>>();
	private int objectCount = 1;
	
	
	
	public ObjectViewLocationTable()
	{
	}
	
	
	public String getRelativeLocationForObject(Object x)
	{
		Integer key = objectToLocation.get( x );
		if ( key == null )
		{
			key = objectCount++;
			objectToLocation.put( x, key );
			locationToObject.put( key, new WeakReference<Object>( x ) );
		}
		String location = "[" + key + "]";
		return location;
	}
	
	public Object __getitem__(int key)
	{
		WeakReference<Object> ref = locationToObject.get( key );
		if ( ref != null )
		{
			Object x = ref.get();
			
			if ( x == null )
			{
				locationToObject.remove( key );
			}
			
			return x;
		}
		
		throw Py.KeyError( "Object at " + key + " does not exist" );
	}
}
