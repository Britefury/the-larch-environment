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

public class IncrementalValue
{
	protected enum IncrementalState { UNINITIALISED, REFRESH_REQUIRED, REFRESH_NOT_REQUIRED }
	
	protected static IncrementalFunction currentComputation;
	
	
	
	protected IncrementalOwner owner;
	protected IncrementalState incrementalState;
	protected WeakHashMap<IncrementalFunction, Object> outgoingDependencies;
	protected LinkedList<IncrementalValueListener> listeners;
	
	
	
	
	
	public IncrementalValue()
	{
		this( null );
	}
	
	public IncrementalValue(IncrementalOwner owner)
	{
		this.owner = owner;
		incrementalState = IncrementalState.UNINITIALISED;
		outgoingDependencies = null;
		listeners = null;
	}
	
	
	
	public void addListener(IncrementalValueListener listener)
	{
		if ( listeners == null )
		{
			listeners = new LinkedList<IncrementalValueListener>();
		}
		listeners.add( listener );
	}

	public void removeListener(IncrementalValueListener listener)
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
	
	public Set<IncrementalFunction> getOutgoingDependecies()
	{
		if ( outgoingDependencies == null )
		{
			return new HashSet<IncrementalFunction>();
		}
		else
		{
			return outgoingDependencies.keySet();
		}
	}
	
	
	
	
	
	
	
	public void onAccess()
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
				for (IncrementalFunction dep: outgoingDependencies.keySet())
				{
					dep.onChanged();
				}
			}
		}
	}
	
	public Object onRefreshBegin()
	{
		return null;
	}
	
	public void onRefreshEnd(Object refreshState)
	{
		incrementalState = IncrementalState.REFRESH_NOT_REQUIRED;
	}
	
	
	
	
	protected void emitChanged()
	{
		if ( listeners != null )
		{
			for (IncrementalValueListener l: listeners)
			{
				l.onIncrementalValueChanged( this );
			}
		}
	}
	






	protected static IncrementalFunction pushCurrentComputation(IncrementalFunction newCurrentComputation)
	{
		IncrementalFunction f = currentComputation;
		currentComputation = newCurrentComputation;
		return f;
	}
	
	protected static void popCurrentComputation(IncrementalFunction prevCurrentComputation)
	{
		currentComputation = prevCurrentComputation;
	}
	

	public static IncrementalFunction blockAccessTracking()
	{
		return pushCurrentComputation( null );
	}

	public static void unblockAccessTracking(IncrementalFunction prevCurrentComputation)
	{
		popCurrentComputation( prevCurrentComputation );
	}
	
	
	
	
	
	protected void addOutgoingDependency(IncrementalFunction dep)
	{
		if ( outgoingDependencies == null )
		{
			outgoingDependencies = new WeakHashMap<IncrementalFunction, Object>();
		}
		outgoingDependencies.put( dep, null );
	}

	protected void removeOutgoingDependency(IncrementalFunction dep)
	{
		outgoingDependencies.put( dep, null );
		if ( outgoingDependencies == null )
		{
			outgoingDependencies = new WeakHashMap<IncrementalFunction, Object>();
		}
	}
}
