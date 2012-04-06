//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Live;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;



public class LiveFunction extends LiveInterface
{
	public interface Function
	{
		public Object evaluate();
	}
	
	public static class PythonFunction implements Function
	{
		private PyObject func;
		
		
		public PythonFunction(PyObject func)
		{
			this.func = func;
		}


		@Override
		public Object evaluate()
		{
			return Py.tojava( func.__call__(), Object.class );
		}
	}
	
	protected static class ConstantFunction implements Function
	{
		private Object value;
		
		public ConstantFunction(Object value)
		{	
			this.value = value;
		}
		
		@Override
		public Object evaluate()
		{
			return value;
		}
		
		
		private static ConstantFunction nullInstance = new ConstantFunction( null );
	}
	
	
	private IncrementalFunctionMonitor inc;
	private Function fn;
	private Object valueCache;
	

	
	
	public LiveFunction()
	{
		this( ConstantFunction.nullInstance );
	}
	
	public LiveFunction(PyObject function)
	{
		this( new PythonFunction( function ) );
	}
	
	public LiveFunction(Function fn)
	{
		inc = new IncrementalFunctionMonitor();
		this.fn = fn;
		valueCache = null;
	}
	
	
	public Function getFunction()
	{
		return fn;
	}

	public void setFunction(PyObject function)
	{
		setFunction( new PythonFunction( function ) );
	}
	
	public void setFunction(Function fn)
	{
		this.fn = fn;
		inc.onChanged();
	}
	
	
	
	public void setLiteralValue(Object value)
	{
		setFunction( new ConstantFunction( value ) );
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
	
	
	


	private void refreshValue()
	{
		Object refreshState = inc.onRefreshBegin();
		try
		{
			if ( refreshState != null )
			{
				valueCache = fn.evaluate();
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
	
	
	
	public static LiveFunction value(Object value)
	{
		return new LiveFunction( new ConstantFunction( value ) );
	}
}
