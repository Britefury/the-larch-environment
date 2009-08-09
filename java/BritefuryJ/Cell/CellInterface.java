//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;


import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;



public abstract class CellInterface
{
	protected enum RefreshState { UNINITIALISED, REFRESH_REQUIRED, REFRESH_NOT_REQUIRED }
	
	protected static WeakHashMap<CellInterface, Object> cellAccessList = null;
	
	protected RefreshState refreshState;
	protected WeakHashMap<CellInterface, Object> dependents;
	protected LinkedList<CellListener> listeners;
	

	
	
	public CellInterface()
	{
		refreshState = RefreshState.UNINITIALISED;
		dependents = new WeakHashMap<CellInterface, Object>();
		listeners = new LinkedList<CellListener>();
	}
	
	
	public void addListener(CellListener listener)
	{
		listeners.add( listener );
	}
	
	
	
	public abstract CellEvaluator getEvaluator();
	public abstract void setEvaluator(CellEvaluator eval);
	
	public abstract Object getLiteralValue();
	public abstract void setLiteralValue(Object value);
	public abstract boolean isLiteral();
	
	public abstract Object getValue();
	public abstract boolean isValid();
	
	
	
	protected void onChanged()
	{
		if ( refreshState != RefreshState.REFRESH_REQUIRED )
		{
			refreshState = RefreshState.REFRESH_REQUIRED;
			emitChanged();
			
			for (CellInterface dep: dependents.keySet())
			{
				dep.onChanged();
			}
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


	public static WeakHashMap<CellInterface, Object> blockAccessTracking()
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

	public static void unblockAccessTracking(WeakHashMap<CellInterface, Object> oldAccesses)
	{
		// Unblocks cell access tracking
		// Pass the object returned by blockAccessTracking()

		// Restore the existing/old global access list
		cellAccessList = oldAccesses;
	}
	
	
	
	protected void onAccess()
	{
		if ( cellAccessList != null )
		{
			cellAccessList.put( this, null );
		}
	}
}
