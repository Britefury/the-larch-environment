//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Incremental;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class IncrementalMonitor
{
	protected enum IncrementalState { UNINITIALISED, REFRESH_REQUIRED, REFRESH_NOT_REQUIRED }
	
	protected static IncrementalFunctionMonitor currentComputation;
	
	
	
	protected IncrementalOwner owner;
	protected IncrementalState incrementalState;
	protected WeakHashMap<IncrementalFunctionMonitor, Object> outgoingDependencies;
	protected LinkedList<IncrementalMonitorListener> listeners;
	
	
	
	
	
	public IncrementalMonitor()
	{
		this( null );
	}
	
	public IncrementalMonitor(IncrementalOwner owner)
	{
		this.owner = owner;
		incrementalState = IncrementalState.UNINITIALISED;
		outgoingDependencies = null;
		listeners = null;
	}
	
	
	
	public void addListener(IncrementalMonitorListener listener)
	{
		if ( listeners == null )
		{
			listeners = new LinkedList<IncrementalMonitorListener>();
		}
		listeners.add( listener );
	}

	public void removeListener(IncrementalMonitorListener listener)
	{
		if ( listeners != null )
		{
			listeners.remove( listener );
			if ( listeners.isEmpty() )
			{
				listeners = null;
			}
		}
	}

	
	public IncrementalOwner getOwner()
	{
		return owner;
	}
	
	public Set<IncrementalFunctionMonitor> getOutgoingDependecies()
	{
		if ( outgoingDependencies == null )
		{
			return new HashSet<IncrementalFunctionMonitor>();
		}
		else
		{
			return outgoingDependencies.keySet();
		}
	}
	
	
	
	
	
	
	
	protected void onValueAccess()
	{
		if ( currentComputation != null )
		{
			currentComputation.onIncomingDependencyAccess( this );
		}
	}
	
	public void onChanged()
	{
		if ( incrementalState != IncrementalState.REFRESH_REQUIRED )
		{
			incrementalState = IncrementalState.REFRESH_REQUIRED;
			emitChanged();
			
			if ( outgoingDependencies != null )
			{
				for (IncrementalFunctionMonitor dep: outgoingDependencies.keySet())
				{
					dep.onChanged();
				}
			}
		}
	}
	
	protected void notifyRefreshed()
	{
		incrementalState = IncrementalState.REFRESH_NOT_REQUIRED;
	}
	
	
	
	
	protected void emitChanged()
	{
		if ( listeners != null )
		{
			for (IncrementalMonitorListener l: listeners)
			{
				l.onIncrementalMonitorChanged( this );
			}
		}
	}
	






	protected static IncrementalFunctionMonitor pushCurrentComputation(IncrementalFunctionMonitor newCurrentComputation)
	{
		IncrementalFunctionMonitor f = currentComputation;
		currentComputation = newCurrentComputation;
		return f;
	}
	
	protected static void popCurrentComputation(IncrementalFunctionMonitor prevCurrentComputation)
	{
		currentComputation = prevCurrentComputation;
	}
	

	public static IncrementalFunctionMonitor blockAccessTracking()
	{
		return pushCurrentComputation( null );
	}

	public static void unblockAccessTracking(IncrementalFunctionMonitor prevCurrentComputation)
	{
		popCurrentComputation( prevCurrentComputation );
	}
	
	
	
	
	
	protected void addOutgoingDependency(IncrementalFunctionMonitor dep)
	{
		if ( outgoingDependencies == null )
		{
			outgoingDependencies = new WeakHashMap<IncrementalFunctionMonitor, Object>();
		}
		outgoingDependencies.put( dep, null );
	}

	protected void removeOutgoingDependency(IncrementalFunctionMonitor dep)
	{
		outgoingDependencies.put( dep, null );
		if ( outgoingDependencies == null )
		{
			outgoingDependencies = new WeakHashMap<IncrementalFunctionMonitor, Object>();
		}
	}
}
