//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.PersistentState;

import BritefuryJ.Incremental.IncrementalValue;

public class PersistentState
{
	private IncrementalValue incr;
	private Object value;
	
	
	public PersistentState()
	{
		incr = new IncrementalValue();
	}
	
	
	public Object getValue()
	{
		incr.onLiteralAccess();
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public <C> C getValueAsType(Class<C> cls)
	{
		incr.onLiteralAccess();
		if ( cls.isInstance( value ) )
		{
			return (C)value;
		}
		else
		{
			return null;
		}
	}
	
	public void setValue(Object value)
	{
		this.value = value;
		incr.onChanged();
	}
}
