//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.util.WeakHashMap;

class ElementValueCacheManager
{
	private WeakHashMap<ElementValueCache<?>, Object> caches = new WeakHashMap<ElementValueCache<?>, Object>();
	
	
	public ElementValueCacheManager(LSRootElement rootElement)
	{
	}


	
	protected void elementValueAdded(LSElement element)
	{
		element.setHasCachedValues();
	}

	protected void elementValueRemoved(LSElement element)
	{
		for (ElementValueCache<?> cache: caches.keySet())
		{
			if ( cache.containsKey( element ) )
			{
				return;
			}
		}

		// No longer have a value cached for @element
		element.clearHasCachedValues();
	}
	
	
	protected void registerCache(ElementValueCache<?> cache)
	{
		caches.put( cache, null );
	}
	
	
	protected void invalidateCachedValuesFor(LSElement element)
	{
		for (ElementValueCache<?> cache: caches.keySet())
		{
			cache.remove( element );
		}

		// No longer have a value cached for @element
		element.clearHasCachedValues();
	}
}
