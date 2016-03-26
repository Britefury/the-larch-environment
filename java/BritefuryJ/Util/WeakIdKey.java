//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util;

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