//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Live;

import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;


public class LiveValue extends LiveInterface
{
	private IncrementalValueMonitor inc;
	private Object value;
	
	
	
	public LiveValue()
	{
		this( null );
	}
	
	public LiveValue(Object value)
	{
		super();
		inc = new IncrementalValueMonitor( this );
		this.value = value;
	}
	
	
	public void setLiteralValue(Object value)
	{
		this.value = value;
		inc.onChanged();
	}

	
	public Object getValue()
	{
		inc.onAccess();
		
		return value;
	}

	public Object getStaticValue()
	{
		return value;
	}


	
	public void addListener(IncrementalMonitorListener listener)
	{
		inc.addListener( listener );
	}

	public void removeListener(IncrementalMonitorListener listener)
	{
		inc.removeListener( listener );
	}
}
