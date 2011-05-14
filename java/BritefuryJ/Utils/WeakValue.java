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

class WeakValue <Value, Key> extends WeakReference<Value>
{
	protected Key key;
	
	
	public WeakValue(Value val)
	{
		super( val );
		this.key = null;
	}
	
	public WeakValue(Value val, Key key)
	{
		super( val );
		this.key = key;
	}
	
	public WeakValue(Value val, ReferenceQueue<Value> refQueue, Key key)
	{
		super( val, refQueue );
		this.key = key;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		else if ( x instanceof Reference )
		{
			Value v = get();
			return v.equals( ((Reference<Value>)x).get() );
		}
		else
		{
			Value v = get();
			return v.equals( x );
		}
	}
	
	@Override
	public int hashCode()
	{
		Value v = get();
		return v != null  ?  v.hashCode()  :  0;
	}
}