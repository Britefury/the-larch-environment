//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StructuralRepresentation;

import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public class StructuralRepresentation
{
	private StructuralValue prefixValue, mainValue, suffixValue;
	
	
	
	public StructuralRepresentation()
	{
	}
	
	
	
	public void setPrefixValue(StructuralValue value)
	{
		prefixValue = value;
	}
	
	public void setMainValue(StructuralValue value)
	{
		mainValue = value;
	}
	
	public void setSuffixValue(StructuralValue value)
	{
		suffixValue = value;
	}



	public void clearPrefixValue()
	{
		prefixValue = null;
	}
	
	public void clearMainValue()
	{
		mainValue = null;
	}
	
	public void clearSuffixValue()
	{
		suffixValue = null;
	}



	public void addPrefixToStream(ItemStreamBuilder builder)
	{
		if ( prefixValue != null )
		{
			prefixValue.addToStream( builder );
		}
	}

	public void addMainToStream(ItemStreamBuilder builder)
	{
		if ( mainValue != null )
		{
			mainValue.addToStream( builder );
		}
	}

	public void addSuffixToStream(ItemStreamBuilder builder)
	{
		if ( suffixValue != null )
		{
			suffixValue.addToStream( builder );
		}
	}
	
	
	public StructuralValue getPrefixValue()
	{
		return prefixValue;
	}

	public StructuralValue getMainValue()
	{
		return mainValue;
	}

	public StructuralValue getSuffixValue()
	{
		return suffixValue;
	}
}
