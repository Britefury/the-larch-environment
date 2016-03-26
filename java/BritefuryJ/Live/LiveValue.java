//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Live;

import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;


public class LiveValue extends LiveInterface
{
	private IncrementalValueMonitor inc;
	private Object val;
	
	
	
	public LiveValue()
	{
		this( null );
	}
	
	public LiveValue(Object value)
	{
		super();
		inc = new IncrementalValueMonitor( this );
		this.val = value;
	}
	
	
	public void setLiteralValue(Object value)
	{
		this.val = value;
		inc.onChanged();
	}

	
	public Object getValue()
	{
		inc.onAccess();
		
		return val;
	}

	public Object getStaticValue()
	{
		return val;
	}


	
	public void addListener(IncrementalMonitorListener listener)
	{
		inc.addListener( listener );
	}

	public void removeListener(IncrementalMonitorListener listener)
	{
		inc.removeListener( listener );
	}
	
	public IncrementalMonitor getIncrementalMonitor()
	{
		return inc;
	}
}
