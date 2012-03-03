//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.util.WeakHashMap;

class ElementValueCacheManager
{
	private WeakHashMap<ElementValueCache<?>, Object> caches = new WeakHashMap<ElementValueCache<?>, Object>();
	
	
	public ElementValueCacheManager(PresentationComponent.RootElement rootElement)
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
