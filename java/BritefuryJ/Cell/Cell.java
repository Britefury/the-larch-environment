//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.python.core.PyObject;



public class Cell extends CellInterface
{
	public static class CellEvaluationCycleException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	private CellEvaluator evaluator;
	private Object valueCache;
	private WeakHashMap<CellInterface, Object> dependencies;
	private boolean cycleLock;
	

	
	
	public Cell()
	{
		evaluator = new CellEvaluatorLiteral( null );
		valueCache = null;
		dependencies = null;
		cycleLock = false;
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
		
		return valueCache;
	}
	
	
	


	private void setEval(CellEvaluator eval)
	{
		evaluator = eval;
		onChanged();
	}
	
	
	
	private void refreshValue()
	{
		if ( cycleLock )
		{
			throw new CellEvaluationCycleException();
		}
		
		cycleLock = true;
		
		try
		{
			// Log the access
			onAccess();
			
			if ( refreshState != RefreshState.REFRESH_NOT_REQUIRED )
			{
				refreshState = RefreshState.REFRESH_NOT_REQUIRED;
				
				if ( evaluator.isLiteral() )
				{
					valueCache = evaluator.evaluate();
					
					// Literal value; clear any dependencies
					if ( dependencies != null )
					{
						for (CellInterface cell: dependencies.keySet())
						{
							cell.dependents.remove( this );
						}
						
						dependencies.clear();
					}
				}
				else
				{
					// Push a new cell access list
					WeakHashMap<CellInterface, Object> oldCellAccesses = pushNewAccessList();
					
					// Compute the cell value
					valueCache = evaluator.evaluate();
					
					// Restore the existing cell access list
					WeakHashMap<CellInterface, Object> deps = popAccessList( oldCellAccesses );
					
					if ( dependencies == null )
					{
						dependencies = deps;
						
						// Register this as a dependent of the dependencies
						for (CellInterface cell: deps.keySet())
						{
							cell.dependents.put( this, null );
						}
					}
					else
					{
						WeakHashMap<CellInterface, Object> oldDeps = dependencies;
						
						// Disconnect the dependencies that are being removed
						for (CellInterface cell: oldDeps.keySet())
						{
							if ( !deps.containsKey( cell ) )
							{
								cell.dependents.remove( this );
							}
						}
						
						// Connect new dependencies
						for (CellInterface cell: deps.keySet())
						{
							if ( !oldDeps.containsKey( cell ) )
							{
								cell.dependents.put( this, null );
							}
						}
						
						// Set deps list
						dependencies = deps;
					}
				}

				refreshState = RefreshState.REFRESH_NOT_REQUIRED;
			}
		}
		finally
		{
			cycleLock = false;
		}
	}
	
	
	public boolean dependsOn(CellInterface cell)
	{
		if ( dependencies != null )
		{
			return dependencies.containsKey( cell );
		}
		else
		{
			return false;
		}
	}
	
	
	public Set<CellInterface> getDependencies()
	{
		if ( dependencies != null )
		{
			return dependencies.keySet();
		}
		else
		{
			return new HashSet<CellInterface>();
		}
	}
}
