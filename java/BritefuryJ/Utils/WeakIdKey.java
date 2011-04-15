//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Utils;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

class WeakIdKey extends WeakReference<Object>
{
	private int hashCode;
	
	
	public WeakIdKey(Object key)
	{
		super( key );
		this.hashCode = System.identityHashCode( key );
	}
	
	public WeakIdKey(Object key, ReferenceQueue<Object> queue)
	{
		super( key, queue );
		this.hashCode = System.identityHashCode( key );
	}
	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof Reference )
		{
			Reference<?> r = (Reference<?>)x;
			return get() == r.get();
		}
		else
		{
			return get() == x;
		}
	}
	
	public int hashCode()
	{
		return hashCode;
	}
}