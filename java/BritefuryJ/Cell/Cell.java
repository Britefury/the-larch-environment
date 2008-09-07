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
	private CellOwner owner;
	

	
	
	public Cell()
	{
		evaluator = new CellEvaluatorLiteral( null );
		valueCache = null;
		dependencies = null;
		cycleLock = false;
		owner = null;
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
	
	
	
	public boolean isValid()
	{
		return true;
	}
	


	private void setEval(CellEvaluator eval)
	{
		CellEvaluator oldEval = evaluator;
		evaluator = eval;
		emitEvaluator( oldEval, evaluator );
		if ( owner != null )
		{
			owner.onCellEvaluator( this, oldEval, evaluator );
		}
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
