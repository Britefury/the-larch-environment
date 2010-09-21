//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;

import org.python.core.PyObject;

import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;



public class Cell extends CellInterface
{
	private IncrementalFunctionMonitor inc;
	private CellEvaluator evaluator;
	private Object valueCache;
	

	
	
	public Cell()
	{
		this( new CellEvaluatorLiteral( null ) );
	}
	
	public Cell(CellEvaluator evaluator)
	{
		inc = new IncrementalFunctionMonitor();
		this.evaluator = evaluator;
		valueCache = null;
	}
	
	public Cell(PyObject function)
	{
		this( new CellEvaluatorPythonFunction( function ) );
	}
	
	
	public CellEvaluator getEvaluator()
	{
		return evaluator;
	}

	public void setEvaluator(CellEvaluator eval)
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
		setEval( new CellEvaluatorLiteral( value ) );
	}

	public boolean isLiteral()
	{
		return evaluator.isLiteral();
	}
	
	
	
	public void setFunction(PyObject function)
	{
		setEval( new CellEvaluatorPythonFunction( function ) );
	}
	
	
	
	public Object getValue()
	{
		refreshValue();
		
		inc.onAccess();
		
		return valueCache;
	}
	
	
	


	private void setEval(CellEvaluator eval)
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
	
	
	
	public static Cell functionCell(PyObject function)
	{
		return new Cell( function );
	}
	
	public static Cell valueCell(Object value)
	{
		return new Cell( new CellEvaluatorLiteral( value ) );
	}
}
