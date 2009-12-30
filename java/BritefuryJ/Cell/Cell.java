//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;

import org.python.core.PyObject;

import BritefuryJ.Incremental.IncrementalFunction;
import BritefuryJ.Incremental.IncrementalValueListener;



public class Cell extends CellInterface
{
	private IncrementalFunction inc;
	private CellEvaluator evaluator;
	private Object valueCache;
	

	
	
	public Cell()
	{
		inc = new IncrementalFunction();
		evaluator = new CellEvaluatorLiteral( null );
		valueCache = null;
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
		if ( refreshState != null )
		{
			valueCache = evaluator.evaluate();
		}
		inc.onRefreshEnd( refreshState );
	}
	
	
	
	public void addListener(IncrementalValueListener listener)
	{
		inc.addListener( listener );
	}

	public void removeListener(IncrementalValueListener listener)
	{
		inc.removeListener( listener );
	}
}
