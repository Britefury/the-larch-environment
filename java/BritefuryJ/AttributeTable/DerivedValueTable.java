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
	private AttributeNamespace namespace;
	private WeakHashMap<AttributeTable, Object[]> values = new WeakHashMap<AttributeTable, Object[]>();
	
	
	public DerivedValueTable(AttributeNamespace namespace)
	{
		this.namespace = namespace;
	}
	
	
	public AttributeNamespace getNamespace()
	{
		return namespace;
	}
	
	
	@SuppressWarnings("unchecked")
	public V get(AttributeTable attribs)
	{
		Object valueHolder[] = values.get( attribs );
		if ( valueHolder != null )
		{
			return (V)valueHolder[0];
		}
		else
		{
			V value = evaluate( attribs );
			values.put( attribs, new Object[] { value } );
			return value;
		}
	}
	
	
	protected abstract V evaluate(AttributeTable attribs);
}
