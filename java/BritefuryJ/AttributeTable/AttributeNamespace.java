//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;

import java.util.ArrayList;

public class AttributeNamespace
{
	private String name;
	private int id = -1;
	
	
	
	public AttributeNamespace(String name)
	{
		this.name = name;
	}
	
	
	
	public String getName()
	{
		return name;
	}
	
	protected int getID()
	{
		if ( id == -1 )
		{
			id = AttributeTable2.registerNamespace( this );
		}
		return id;
	}




	//
	//
	// DERIVED VALUES
	//
	//

	private ArrayList<DerivedValueTable<? extends Object>> derivedValueTables = new ArrayList<DerivedValueTable<? extends Object>>();
	
	protected int registerDerivedValueTable(DerivedValueTable<? extends Object> derivedValueTable)
	{
		int id = derivedValueTables.size();
		derivedValueTables.add( derivedValueTable );
		return id;
	}
}
