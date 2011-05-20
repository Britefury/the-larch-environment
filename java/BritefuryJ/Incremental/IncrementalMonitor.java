//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Incremental;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class IncrementalMonitor
{
	protected enum IncrementalState { UNINITIALISED, REFRESH_REQUIRED, REFRESH_NOT_REQUIRED }
	
	protected static IncrementalFunctionMonitor currentComputation;
	
	
	
	protected IncrementalOwner owner;
	protected IncrementalState incrementalState;
	protected WeakHashMap<IncrementalFunctionMonitor, Object> outgoingDependencies;
	protected ArrayList<WeakReference<IncrementalMonitorListener>> listeners;
	
	
	
	
	
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
			listeners = new ArrayList<WeakReference<IncrementalMonitorListener>>();
		}
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			WeakReference<IncrementalMonitorListener> ref = listeners.get( i );
			IncrementalMonitorListener l = ref.get();
			if ( l == listener )
			{
				return;
			}
			else if ( l == null )
			{
				listeners.remove( i );
			}
		}
		listeners.add( new WeakReference<IncrementalMonitorListener>( listener ) );
	}

	public void removeListener(IncrementalMonitorListener listener)
	{
		if ( listeners != null )
		{
			for (int i = listeners.size() - 1; i >= 0; i--)
			{
				WeakReference<IncrementalMonitorListener> ref = listeners.get( i );
				IncrementalMonitorListener l = ref.get();
				if ( l == listener )
				{
					listeners.remove( i );
					if ( listeners.isEmpty() )
					{
						listeners = null;
					}
					return;
				}
				else if ( l == null )
				{
					listeners.remove( i );
				}
			}
		}
	}
	
	public boolean hasListeners()
	{
		return listeners != null && !listeners.isEmpty();
	}

	
	public IncrementalOwner getOwner()
	{
		return owner;
	}
	
	public Set<IncrementalFunctionMonitor> getOutgoingDependencies()
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
	
	public boolean hasOutgoingDependencies()
	{
		return outgoingDependencies != null && !outgoingDependencies.isEmpty();
	}
	
	
	
	
	
	
	
	protected void onValueAccess()
	{
		if ( currentComputation != null )
		{
			currentComputation.onIncomingDependencyAccess( this );
		}
	}
	
	protected void notifyChanged()
	{
		if ( incrementalState != IncrementalState.REFRESH_REQUIRED )
		{
			incrementalState = IncrementalState.REFRESH_REQUIRED;
			emitChanged();
			
			if ( outgoingDependencies != null )
			{
				for (IncrementalFunctionMonitor dep: outgoingDependencies.keySet())
				{
					dep.notifyChanged();
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
			for (int i = listeners.size() - 1; i >= 0; i--)
			{
				WeakReference<IncrementalMonitorListener> ref = listeners.get( i );
				IncrementalMonitorListener l = ref.get();
				if ( l == null )
				{
					listeners.remove( i );
				}
				else
				{
					l.onIncrementalMonitorChanged( this );
				}
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
		if ( outgoingDependencies != null )
		{
			outgoingDependencies.remove( dep );
			if ( outgoingDependencies.isEmpty() )
			{
				outgoingDependencies = null;
			}
		}
	}
}
