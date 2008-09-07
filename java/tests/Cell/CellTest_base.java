package tests.Cell;

import java.util.HashMap;

import BritefuryJ.Cell.CellEvaluator;
import BritefuryJ.Cell.CellInterface;
import BritefuryJ.Cell.CellListener;
import junit.framework.TestCase;

public class CellTest_base extends TestCase
{
	private HashMap<String, Integer> sigs;
	
	
	protected void setUp()
	{
		sigs = new HashMap<String, Integer>();
	}
		
	protected void tearDown()
	{
		sigs = null;
	}
	
	protected void onSignal(String name)
	{
		Integer value = sigs.get( name );
		
		if ( value == null )
		{
			value = new Integer( 1 );
		}
		else
		{
			value = new Integer( value.intValue() + 1 );
		}
		
		sigs.put( name, value );
	}
	
	protected int getSignalCount(String name)
	{
		Integer value = sigs.get( name );

		if ( value != null )
		{
			return value.intValue();
		}
		else
		{
			return 0;
		}
	}
		

	
	protected CellListener makeListener(final String prefix)
	{
		final CellTest_base tester = this;

		CellListener listener = new CellListener()
		{
			public void onCellChanged(CellInterface cell)
			{
				tester.onSignal( prefix + "changed" );
			}

			public void onCellEvaluator(CellInterface cell, CellEvaluator oldEval, CellEvaluator newEval)
			{
				tester.onSignal( prefix + "evaluator" );
			}

			public void onCellValidity(CellInterface cell)
			{
				tester.onSignal( prefix + "validity" );
			}
		};
		
		return listener;
	}
}
