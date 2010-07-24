//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;

import java.util.WeakHashMap;

public abstract class DerivedValueTable <V extends Object>
{
	private WeakHashMap<AttributeTable2, V> values = new WeakHashMap<AttributeTable2, V>();
	
	
	public DerivedValueTable()
	{
	}
	
	
	public V get(AttributeTable2 attribs)
	{
		if ( values.containsKey( attribs ) )
		{
			return values.get( attribs );
		}
		else
		{
			V value = evaluate( attribs );
			values.put( attribs, value );
			return value;
		}
	}
	
	
	protected abstract V evaluate(AttributeTable2 attribs);
}
