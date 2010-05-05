//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;

import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;


public class LiteralCell extends CellInterface
{
	private IncrementalValueMonitor inc;
	private Object value;
	
	
	
	public LiteralCell()
	{
		this( null );
	}
	
	public LiteralCell(Object value)
	{
		super();
		inc = new IncrementalValueMonitor( this );
		this.value = value;
	}
	
	
	public Object getLiteralValue()
	{
		return value;
	}

	public void setLiteralValue(Object value)
	{
		this.value = value;
		inc.onChanged();
	}

	public boolean isLiteral()
	{
		return true;
	}

	
	public Object getValue()
	{
		inc.onAccess();
		
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
