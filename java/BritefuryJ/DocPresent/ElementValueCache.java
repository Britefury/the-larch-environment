//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent;

import java.util.IdentityHashMap;

public class ElementValueCache <ValueType extends Object>
{
	private IdentityHashMap<DPElement, ValueType> cache = new IdentityHashMap<DPElement, ValueType>();
	private ElementValueCacheManager manager;
	
	
	
	
	private void updateManager(DPElement element)
	{
		if ( manager == null )
		{
			manager = element.getRootElement().getElementValueCacheManager();
			manager.registerCache( this );
		}
	}
	
	
	public void put(DPElement element, ValueType value)
	{
		updateManager( element );
		manager.elementValueAdded( element );
		cache.put( element, value );
	}
	
	public void remove(DPElement element)
	{
		updateManager( element );
		manager.elementValueRemoved( element );
		cache.remove( element );
	}
	
	public ValueType get(DPElement element)
	{
		return cache.get( element );
	}
	
	public boolean containsKey(DPElement element)
	{
		return cache.containsKey( element );
	}
	
	public int size()
	{
		return cache.size();
	}
}
