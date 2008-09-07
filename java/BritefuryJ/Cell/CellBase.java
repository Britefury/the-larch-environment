package BritefuryJ.Cell;


import java.util.WeakHashMap;
import java.util.LinkedList;
import java.util.Set;



public abstract class CellBase implements CellInterface
{
	protected enum RefreshState { UNINITIALISED, REFRESH_REQUIRED, REFRESH_NOT_REQUIRED }
	
	protected static WeakHashMap<CellInterface, Object> cellAccessList = null;
	
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
	






	protected static WeakHashMap<CellInterface, Object> pushNewAccessList()
	{
		// Starts tracking cell accesses;
		// Creates a new global cell access list that records all cells that are accessed through getValue() / getImmutableValue()
		// Returns the current access list
		// Call popAccessList() with the value returned here, to finish tracking cell accesses
		
		// Save the existing/old global access list
		WeakHashMap<CellInterface, Object> oldAccesses = cellAccessList;
		// Create a new access list
		cellAccessList = new WeakHashMap<CellInterface, Object>();
		// Return the existing access list
		return oldAccesses;
	}

	
	protected static WeakHashMap<CellInterface, Object> popAccessList(WeakHashMap<CellInterface, Object> oldAccesses)
	{
		// Stops tracking cell accesses;
		// Restores the old cell access list (which was returned by pushNewAccessList())
		// Returns a WeakHashMap where the keys are the cells that were access between pushNewAccessList() and popAccessList()
		
		// Get the current access list
		WeakHashMap<CellInterface, Object> accesses = cellAccessList;
		// Restore the existing/old global access list
		cellAccessList = oldAccesses;
		// Return the current list
		return accesses;
	}


	protected static WeakHashMap<CellInterface, Object> blockAccessTracking()
	{
		// Blocks tracking of cell accesses
		// Returns the current access list; this MUST be passed to unblockAccessTracking()

		// Save the existing/old global access list
		WeakHashMap<CellInterface, Object> oldAccesses = cellAccessList;
		// Clear access list
		cellAccessList = null;
		// Return the existing list
		return oldAccesses;
	}

	protected static void unblockAccessTracking(WeakHashMap<CellInterface, Object> oldAccesses)
	{
		// Unblocks cell access tracking
		// Pass the object returned by blockAccessTracking()

		// Restore the existing/old global access list
		cellAccessList = oldAccesses;
	}
}
