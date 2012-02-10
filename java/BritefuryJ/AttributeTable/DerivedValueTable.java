//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;


public abstract class DerivedValueTable <V>
{
	private AttributeNamespace namespace;
	
	
	public DerivedValueTable(AttributeNamespace namespace)
	{
		this.namespace = namespace;
	}
	
	
	public AttributeNamespace getNamespace()
	{
		return namespace;
	}
	
	
	public V get(AttributeTable attribs)
	{
		return attribs.getDerivedValuesForTable( this );
	}
	
	
	protected abstract V evaluate(AttributeTable attribs);
}
