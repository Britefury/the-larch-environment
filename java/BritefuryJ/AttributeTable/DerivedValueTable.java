//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;


public abstract class DerivedValueTable <V extends Object>
{
	private AttributeNamespace namespace;
	private int idWithinNamespace;
	
	
	public DerivedValueTable(AttributeNamespace namespace)
	{
		this.namespace = namespace;
	}
	
	
	@SuppressWarnings("unchecked")
	public V get(AttributeTable2 attribs)
	{
		return (V)attribs.getDerivedValue( namespace, this );
	}
	
	
	public AttributeNamespace getNamespace()
	{
		return namespace;
	}
	
	
	protected int getIDWithinNamespace()
	{
		if ( idWithinNamespace == -1 )
		{
			idWithinNamespace = namespace.registerDerivedValueTable( this );
		}
		return idWithinNamespace;
	}

	
	protected abstract V evaluate(AttributeTable2 attribs);
}
