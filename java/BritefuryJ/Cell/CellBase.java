package BritefuryJ.Cell;


import java.util.WeakHashMap;
import java.util.LinkedList;
import java.util.Set;



public abstract class CellBase implements CellInterface {
	protected enum RefreshState { UNINITIALISED, REFRESH_REQUIRED, REFRESH_NOT_REQUIRED }
	
	protected static WeakHashMap<CellInterface, Object> cellDependencies = null;
	
	protected RefreshState refreshState;
	protected WeakHashMap<CellInterface, Object> dependents;
	protected LinkedList<CellListener> listeners;
	

	protected void changed()
	{
		if ( refreshState != RefreshState.REFRESH_REQUIRED )
		{
			refreshState = RefreshState.REFRESH_REQUIRED;
			emitChanged();
		}
	}
	
	protected Set<CellInterface> getDependents()
	{
		return dependents.keySet();
	}
	
	
	protected void emitChanged()
	{
		for (CellListener l: listeners)
		{
			l.onCellChanged( this );
		}
	}
	
	protected void emitEvaluator(CellEvaluator oldEval, CellEvaluator newEval)
	{
		for (CellListener l: listeners)
		{
			l.onCellEvaluator( this, oldEval, newEval );
		}
	}

	protected void emitValidity()
	{
		for (CellListener l: listeners)
		{
			l.onCellValidity( this );
		}
	}
	
}
