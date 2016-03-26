//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.util.IdentityHashMap;

public class ElementValueCache <ValueType extends Object>
{
	private IdentityHashMap<LSElement, ValueType> cache = new IdentityHashMap<LSElement, ValueType>();
	private ElementValueCacheManager manager;
	
	
	
	
	private void updateManager(LSElement element)
	{
		if ( manager == null )
		{
			manager = element.getRootElement().getElementValueCacheManager();
			manager.registerCache( this );
		}
	}
	
	
	public void put(LSElement element, ValueType value)
	{
		updateManager( element );
		manager.elementValueAdded( element );
		cache.put( element, value );
	}
	
	public void remove(LSElement element)
	{
		updateManager( element );
		manager.elementValueRemoved( element );
		cache.remove( element );
	}
	
	public ValueType get(LSElement element)
	{
		return cache.get( element );
	}
	
	public boolean containsKey(LSElement element)
	{
		return cache.containsKey( element );
	}
	
	public int size()
	{
		return cache.size();
	}
}
