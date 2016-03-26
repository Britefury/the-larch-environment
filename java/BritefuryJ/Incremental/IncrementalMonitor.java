//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Incremental;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import BritefuryJ.Util.WeakListenerList;

public abstract class IncrementalMonitor
{
	protected enum IncrementalState { UNINITIALISED, REFRESH_REQUIRED, REFRESH_NOT_REQUIRED }
	
	protected static IncrementalFunctionMonitor currentComputation;
	
	
	
	protected Object owner;
	protected IncrementalState incrementalState;
	protected WeakHashMap<IncrementalFunctionMonitor, Object> outgoingDependencies;
	protected WeakListenerList<IncrementalMonitorListener> listeners;
	
	
	
	
	
	public IncrementalMonitor()
	{
		this( null );
	}
	
	public IncrementalMonitor(Object owner)
	{
		this.owner = owner;
		incrementalState = IncrementalState.UNINITIALISED;
		outgoingDependencies = null;
		listeners = null;
	}
	
	
	
	public void addListener(IncrementalMonitorListener listener)
	{
		listeners = WeakListenerList.addListener( listeners, listener );
	}

	public void removeListener(IncrementalMonitorListener listener)
	{
		listeners = WeakListenerList.removeListener( listeners, listener );
	}
	
	public boolean hasListeners()
	{
		return listeners != null && !listeners.isEmpty();
	}

	
	public Object getOwner()
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
			for (IncrementalMonitorListener listener: listeners)
			{
				listener.onIncrementalMonitorChanged( this );
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
