//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StructuralRepresentation;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public class StructuralValueSequence extends StructuralValue
{
	private List<Object> values;
	
	
	public StructuralValueSequence(List<Object> values)
	{
		this.values = new ArrayList<Object>();
		this.values.addAll( values );
	}
	
	
	public void addToStream(ItemStreamBuilder builder)
	{
		for (Object value: values)
		{
			builder.appendStructuralValue( value );
		}
	}


	public Object getValue()
	{
		return values;
	}
}
