//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;

import BritefuryJ.Incremental.IncrementalValue;
import BritefuryJ.Incremental.IncrementalValueListener;


public class LiteralCell extends CellInterface
{
	private IncrementalValue inc;
	private CellEvaluatorLiteral evaluator;
	
	
	
	public LiteralCell()
	{
		this( null );
	}
	
	public LiteralCell(Object value)
	{
		super();
		evaluator = new CellEvaluatorLiteral( value );
		inc = new IncrementalValue( this );
	}
	
	
	public CellEvaluator getEvaluator()
	{
		return evaluator;
	}

	public void setEvaluator(CellEvaluator eval)
	{
		evaluator = (CellEvaluatorLiteral)eval;
		inc.onChanged();
	}


	public Object getLiteralValue()
	{
		return evaluator.evaluate();
	}

	public void setLiteralValue(Object value)
	{
		setEvaluator( new CellEvaluatorLiteral( value ) );
	}

	public boolean isLiteral()
	{
		return true;
	}

	
	public Object getValue()
	{
		Object refreshState = inc.onRefreshBegin();
		inc.onRefreshEnd( refreshState );
		
		inc.onAccess();
		
		return evaluator.evaluate();
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
