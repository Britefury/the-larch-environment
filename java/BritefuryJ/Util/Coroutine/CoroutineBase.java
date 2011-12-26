//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Util.Coroutine;

import java.util.LinkedList;
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
	
	
	protected static LinkedList<CoroutineBase> coStack = new LinkedList<CoroutineBase>();
	
	private Semaphore lock;
	private Object value;
	protected String name;
	protected RuntimeException throwOnResume;

	
	
	
	public CoroutineBase(String name)
	{
		lock = new Semaphore( 0 );
		this.name = name;
	}

	
	
	public static Object yieldToParent()
	{
		return yieldToParent( null );
	}
	
	public static Object yieldToParent(Object x)
	{
		CoroutineBase source = getCurrent();

		if ( !source.isRoot() )
		{
			// CONTEXT:
			// source = the source coroutine  -  THE ONE WHICH WE ARE RUNNING RIGHT NOW
			// target = the target coroutine
			
			CoroutineBase target = null;
			synchronized( coStack )
			{
				coStack.removeLast();
				if ( coStack.isEmpty() )
				{
					target = RootCoroutine.forThread( Thread.currentThread() );
				}
				else
				{
					target = coStack.peekLast();
				}
			}
			
			// Send the value @x to the target coroutine
			target.setValue( x );
			
			// Resume the target coroutine
			target.resume();
			
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
		else
		{
			throw new RuntimeException( "Cannot yield to parent coroutine when current coroutine is root" );
		}
	}

	
	public Object yieldTo()
	{
		return yieldTo( null );
	}
	
	public Object yieldTo(Object x)
	{
		// This method is invoked on the target coroutine (this), from within the source coroutine
		
		// Ensure that @this is not already on the coroutine stack
		if ( isLive() )
		{
			throw new RecursiveYieldException( "Attempting to yield to live coroutine" );
		}
		
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
			
			// Push the target thread on the coroutine stack. If the target thread is a root coroutine, clear the stack.
			synchronized( coStack )
			{
				coStack.add( this );
			}
			
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
		
		// Keep popping co-routines off the stack until we find one that is not finished yet
		CoroutineBase newCurrent = null;
		synchronized( coStack )
		{
			coStack.removeLast();
				
			if ( coStack.isEmpty() )
			{
				newCurrent = RootCoroutine.forThread( Thread.currentThread() );
			}
			else
			{
				newCurrent = coStack.peekLast();
			}
		}
			
		// Resume it - this thread will continue to run, and terminate
		newCurrent.setValue( null );
		newCurrent.throwOnResume( caughtException );
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
	
	
	
	
	
	protected abstract void initialise();
	protected abstract Thread getThread();
	protected abstract boolean isRoot();
	
	public abstract boolean hasStarted();
	public abstract boolean isFinished();
	
	
	public boolean isLive()
	{
		return this == RootCoroutine.root  ||  coStack.contains( this );
	}
	
	
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

		synchronized( coStack )
		{
			if ( coStack.isEmpty() )
			{
				return RootCoroutine.forThread( t );
			}
			else
			{
				CoroutineBase current = coStack.peekLast();
				if ( current.getThread() != t )
				{
					throw new RuntimeException( "Current coroutine thread is not the current system thread" );
				}
		
				return current;
			}
		}
	}
}