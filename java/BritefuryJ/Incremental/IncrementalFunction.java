//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Incremental;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;



public class IncrementalFunction extends IncrementalValue
{
	public static class IncrementalEvaluationCycleException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	private WeakHashMap<IncrementalValue, Object> incomingDependencies;
	private boolean cycleLock;
	
	
	
	
	public IncrementalFunction()
	{
		this( null );
	}

	public IncrementalFunction(IncrementalOwner owner)
	{
		super( owner );
		
		incomingDependencies = null;
		cycleLock = false;
	}
	
	
	
	public Set<IncrementalValue> getIncomingDependencies()
	{
		if ( incomingDependencies == null )
		{
			return new HashSet<IncrementalValue>();
		}
		else
		{
			return incomingDependencies.keySet();
		}
	}

	

	public Object onRefreshBegin()
	{
		if ( cycleLock )
		{
			throw new IncrementalEvaluationCycleException();
		}
		
		cycleLock = true;

		if ( incrementalState != IncrementalState.REFRESH_NOT_REQUIRED )
		{
			// Push current computation
			IncrementalFunction oldComputation = pushCurrentComputation( this );
			
			Object refreshState[] = new Object[] { oldComputation, incomingDependencies };
			
			incomingDependencies = null;
			
			return refreshState;
		}
		else
		{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void onRefreshEnd(Object refreshState)
	{
		if ( incrementalState != IncrementalState.REFRESH_NOT_REQUIRED )
		{
			Object refreshStateArray[] = (Object[])refreshState;
			IncrementalFunction oldCurrentComputation = (IncrementalFunction)refreshStateArray[0];
			WeakHashMap<IncrementalValue, Object> prevIncomingDependencies = (WeakHashMap<IncrementalValue, Object>)refreshStateArray[1];
			
			// Restore the current computation
			popCurrentComputation( oldCurrentComputation );
			
			// Disconnect the dependencies that are being removed
			if ( prevIncomingDependencies != null )
			{
				for (IncrementalValue inc: prevIncomingDependencies.keySet())
				{
					if ( incomingDependencies == null  ||  !incomingDependencies.containsKey( inc ) )
					{
						inc.removeOutgoingDependency( this );
					}
				}
			}
			
			// Connect new dependencies
			if ( incomingDependencies != null )
			{
				for (IncrementalValue inc: incomingDependencies.keySet())
				{
					if ( prevIncomingDependencies == null  ||  !prevIncomingDependencies.containsKey( inc ) )
					{
						inc.addOutgoingDependency( this );
					}
				}
			}
				

			incrementalState = IncrementalState.REFRESH_NOT_REQUIRED;
		}

		cycleLock = false;
	}

	
	protected void onIncomingDependencyAccess(IncrementalValue inc)
	{
		addIncomingDependency( inc );
	}


	protected void addIncomingDependency(IncrementalValue dep)
	{
		if ( incomingDependencies == null )
		{
			incomingDependencies = new WeakHashMap<IncrementalValue, Object>();
		}
		incomingDependencies.put( dep, null );
	}

	protected void removeIncomingDependency(IncrementalValue dep)
	{
		incomingDependencies.put( dep, null );
		if ( incomingDependencies == null )
		{
			incomingDependencies = new WeakHashMap<IncrementalValue, Object>();
		}
	}
}
