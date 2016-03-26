//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
