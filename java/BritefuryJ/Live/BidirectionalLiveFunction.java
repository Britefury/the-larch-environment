//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Live;

import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;



public class BidirectionalLiveFunction extends LiveInterface
{
	public interface Function
	{
		public Object apply(Object x);
	}


	private IncrementalFunctionMonitor inc;
	private LiveInterface live;
	private Function fn, inverseFn;
	private Object valueCache;




	public BidirectionalLiveFunction(LiveInterface live, Function fn, Function inverseFn)
	{
		inc = new IncrementalFunctionMonitor();
		this.live = live;
		this.fn = fn;
		this.inverseFn = inverseFn;
		valueCache = null;
	}


	public Function getFunction()
	{
		return fn;
	}

	public Function getInvertseFunction()
	{
		return inverseFn;
	}

	public void setFunctions(Function fn, Function inverseFn)
	{
		this.fn = fn;
		this.inverseFn = inverseFn;
		inc.onChanged();
	}



	public void setLiteralValue(Object value)
	{
		live.setLiteralValue(inverseFn.apply(value));
	}



	public Object getValue()
	{
		try
		{
			refreshValue();
		}
		finally
		{
			inc.onAccess();
		}

		return valueCache;
	}

	public Object getStaticValue()
	{
		refreshValue();

		return valueCache;
	}



	public void onChanged()
	{
		inc.onChanged();
	}



	private void refreshValue()
	{
		Object refreshState = inc.onRefreshBegin();
		try
		{
			if ( refreshState != null )
			{
				valueCache = fn.apply(live.getValue());
			}
		}
		finally
		{
			inc.onRefreshEnd( refreshState );
		}
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
