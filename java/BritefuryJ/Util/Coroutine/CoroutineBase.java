//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.Coroutine;

import java.util.concurrent.Semaphore;


public abstract class CoroutineBase
{
	public static class TerminateException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	public static class RecursiveYieldException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public RecursiveYieldException(String message)
		{
			super( message );
		}
	}
	
	
	private Semaphore lock;
	private Object value;
	private CoroutineBase parent;
	private static CoroutineBase current;
	protected String name;
	protected RuntimeException throwOnResume;

	
	
	
	public CoroutineBase(String name, CoroutineBase parent)
	{
		lock = new Semaphore( 0 );
		this.name = name;
		this.parent = parent;
	}

	
	
	public Object yieldToParent()
	{
		return yieldToParent( null );
	}
	
	public Object yieldToParent(Object x)
	{
		if ( parent != null )
		{
			return parent.yieldTo( x );
		}
		else
		{
			throw new RuntimeException( "Cannot yield to parent - no parent co-routine" );
		}
	}
	
	
	public Object yieldTo()
	{
		return yieldTo( null );
	}
	
	public Object yieldTo(Object x)
	{
		// This method is invoked on the target coroutine (this), from within the source coroutine
		
		// Get the source coroutine
		CoroutineBase source = getCurrent();
		
		if ( isFinished() )
		{
			return null;
		}
		else
		{
			// CONTEXT:
			// this = the target coroutine
			// source = the source coroutine  -  THE ONE WHICH WE ARE RUNNING RIGHT NOW
			
			// Ensure that the target coroutine has been initialised / started
			initialise();
			
			// Send the value @x to the target coroutine
			setValue( x );
			
			// Set the current coroutine
			current = this;
			
			// Resume the target coroutine
			resume();
			
			// Halt the source coroutine
			source.halt();
			
			// CONTEXT
			// source.halt() has returned :-
			// The source coroutine has resumed, and now has control			
			
			// The halt() method has returned - the source coroutine has been resumed - check if it needs to be terminated
			if ( source.isTerminated() )
			{
				// Throw a terminate exception, which is caught by the run-method
				throw new TerminateException();
			}
			
			// Retrieve any exception that is to be passed up
			RuntimeException t = source.consumeExceptionToThrowOnResume();
			if ( t != null )
			{
				throw t;
			}
			
			// Return the value within the source co-routine
			return source.getValue();
		}
	}
	
	
	protected static void coFinish(CoroutineBase c, RuntimeException caughtException)
	{
		if ( !c.isCurrent() )
		{
			throw new RuntimeException( "Finishing non-current co-routine" );
		}
		
		// Switch to the parent co-routine
		CoroutineBase newCurrent = c.parent;
		if ( newCurrent == null )
		{
			throw new RuntimeException( "coFinish: @c has not parent" );
		}
			
		// Resume it - this thread will continue to run, and terminate
		newCurrent.setValue( null );
		newCurrent.throwOnResume( caughtException );
		current = newCurrent;
		newCurrent.resume();
	}
	
	
	protected void throwOnResume(RuntimeException t)
	{
		throwOnResume = t;
	}
	
	protected RuntimeException consumeExceptionToThrowOnResume()
	{
		RuntimeException t = throwOnResume;
		throwOnResume = null;
		return t;
	}
	
	
	
	
	public CoroutineBase getParent()
	{
		return parent;
	}
	
	
	protected abstract void initialise();
	protected abstract Thread getThread();
	protected abstract boolean isRoot();
	
	public abstract boolean hasStarted();
	public abstract boolean isFinished();
	
	
	protected abstract boolean isTerminated();
	
	
	public boolean isCurrent()
	{
		return this == getCurrent();
	}
	
	
	protected void halt()
	{
		try
		{
			lock.acquire();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException( "InterruptedException received by thread during acquire() in halt" );
		}
	}
	
	protected void resume()
	{
		lock.release();
	}
	
	protected synchronized Object getValue()
	{
		return value;
	}
	
	protected synchronized void setValue(Object x)
	{
		value = x;
	}

	
	
	public static CoroutineBase getCurrent()
	{
		Thread t = Thread.currentThread();

		if ( current == null )
		{
			current = RootCoroutine.forThread( t );
		}
		
		return current;
	}
}