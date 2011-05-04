//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.IncrementalUnit;

import org.python.core.PyObject;

import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;



public class Unit extends UnitInterface
{
	private IncrementalFunctionMonitor inc;
	private UnitEvaluator evaluator;
	private Object valueCache;
	

	
	
	public Unit()
	{
		this( new UnitEvaluatorLiteral( null ) );
	}
	
	public Unit(UnitEvaluator evaluator)
	{
		inc = new IncrementalFunctionMonitor();
		this.evaluator = evaluator;
		valueCache = null;
	}
	
	public Unit(PyObject function)
	{
		this( new UnitEvaluatorPythonFunction( function ) );
	}
	
	
	public UnitEvaluator getEvaluator()
	{
		return evaluator;
	}

	public void setEvaluator(UnitEvaluator eval)
	{
		setEval( eval );
	}
	
	
	
	public Object getLiteralValue()
	{
		if ( evaluator.isLiteral() )
		{
			return evaluator.evaluate();
		}
		else
		{
			return null;
		}
	}

	public void setLiteralValue(Object value)
	{
		setEval( new UnitEvaluatorLiteral( value ) );
	}

	public boolean isLiteral()
	{
		return evaluator.isLiteral();
	}
	
	
	
	public void setFunction(PyObject function)
	{
		setEval( new UnitEvaluatorPythonFunction( function ) );
	}
	
	
	
	public Object getValue()
	{
		refreshValue();
		
		inc.onAccess();
		
		return valueCache;
	}
	
	
	


	private void setEval(UnitEvaluator eval)
	{
		evaluator = eval;
		inc.onChanged();
	}
	
	
	
	private void refreshValue()
	{
		Object refreshState = inc.onRefreshBegin();
		try
		{
			if ( refreshState != null )
			{
				valueCache = evaluator.evaluate();
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
	
	
	
	public static Unit functionUnit(PyObject function)
	{
		return new Unit( function );
	}
	
	public static Unit valueUnit(Object value)
	{
		return new Unit( new UnitEvaluatorLiteral( value ) );
	}
}
