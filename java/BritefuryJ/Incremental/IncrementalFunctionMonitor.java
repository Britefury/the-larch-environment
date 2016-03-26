//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Incremental;

import java.util.HashSet;
import java.util.Set;



public class IncrementalFunctionMonitor extends IncrementalMonitor
{
	public static class IncrementalEvaluationCycleException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected final static int FLAG_CYCLE_LOCK = 0x1;
	protected final static int FLAG_BLOCK_INCOMING_DEPENDENCIES = 0x2;
	
	private HashSet<IncrementalMonitor> incomingDependencies = null;
	private int flags = 0;
	
	
	
	
	public IncrementalFunctionMonitor()
	{
		this( null );
	}

	public IncrementalFunctionMonitor(Object owner)
	{
		super( owner );
	}
	
	
	
	public Set<IncrementalMonitor> getIncomingDependencies()
	{
		if ( incomingDependencies == null )
		{
			return new HashSet<IncrementalMonitor>();
		}
		else
		{
			return incomingDependencies;
		}
	}


	public void onAccess()
	{
		onValueAccess();
	}
	
	public void onChanged()
	{
		notifyChanged();
	}
	
	public void blockAndClearIncomingDependencies()
	{
		setFlag( FLAG_BLOCK_INCOMING_DEPENDENCIES );
		incomingDependencies = null;
	}
	
	public Object onRefreshBegin()
	{
		if ( testFlag( FLAG_CYCLE_LOCK ) )
		{
			throw new IncrementalEvaluationCycleException();
		}
		
		clearFlag( FLAG_BLOCK_INCOMING_DEPENDENCIES );
		setFlag( FLAG_CYCLE_LOCK );

		if ( incrementalState != IncrementalState.REFRESH_NOT_REQUIRED )
		{
			// Push current computation
			IncrementalFunctionMonitor oldComputation = pushCurrentComputation( this );
			
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
			IncrementalFunctionMonitor oldCurrentComputation = (IncrementalFunctionMonitor)refreshStateArray[0];
			HashSet<IncrementalMonitor> prevIncomingDependencies = (HashSet<IncrementalMonitor>)refreshStateArray[1];
			
			// Restore the current computation
			popCurrentComputation( oldCurrentComputation );
			
			// Disconnect the dependencies that are being removed
			if ( prevIncomingDependencies != null )
			{
				for (IncrementalMonitor inc: prevIncomingDependencies)
				{
					if ( incomingDependencies == null  ||  !incomingDependencies.contains( inc ) )
					{
						inc.removeOutgoingDependency( this );
					}
				}
			}
			
			// Connect new dependencies
			if ( incomingDependencies != null )
			{
				for (IncrementalMonitor inc: incomingDependencies)
				{
					if ( prevIncomingDependencies == null  ||  !prevIncomingDependencies.contains( inc ) )
					{
						inc.addOutgoingDependency( this );
					}
				}
			}
				

			incrementalState = IncrementalState.REFRESH_NOT_REQUIRED;
		}

		clearFlag( FLAG_CYCLE_LOCK );
	}
	
	
	protected void onIncomingDependencyAccess(IncrementalMonitor inc)
	{
		addIncomingDependency( inc );
	}


	protected void addIncomingDependency(IncrementalMonitor dep)
	{
		if ( !testFlag( FLAG_BLOCK_INCOMING_DEPENDENCIES ) )
		{
			if ( incomingDependencies == null )
			{
				incomingDependencies = new HashSet<IncrementalMonitor>();
			}
			incomingDependencies.add( dep );
		}
	}

	protected void removeIncomingDependency(IncrementalMonitor dep)
	{
		incomingDependencies.remove( dep );
		if ( incomingDependencies.isEmpty() )
		{
			incomingDependencies = null;
		}
	}


	//
	//
	// Flag methods
	//
	//
	
	protected void clearFlag(int flag)
	{
		flags &= ~flag;
	}
	
	protected void setFlag(int flag)
	{
		flags |= flag;
	}
	
	protected void setFlagValue(int flag, boolean value)
	{
		if ( value )
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
	}
	
	protected boolean testFlag(int flag)
	{
		return ( flags & flag )  !=  0;
	}
}
