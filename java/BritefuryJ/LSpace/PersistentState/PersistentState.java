//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.PersistentState;

import BritefuryJ.Incremental.IncrementalValueMonitor;

public class PersistentState
{
	private IncrementalValueMonitor incr;
	private Object value;
	
	
	public PersistentState()
	{
		incr = new IncrementalValueMonitor();
	}
	
	
	public Object getValue()
	{
		incr.onAccess();
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public <C> C getValueAsType(Class<C> cls)
	{
		incr.onAccess();
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
