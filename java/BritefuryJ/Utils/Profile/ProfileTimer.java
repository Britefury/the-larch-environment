//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Utils.Profile;

import java.util.Stack;

public class ProfileTimer
{
	private long startTime;
	private double timeAccum;
	
	private static Stack<ProfileTimer> timerStack = null;
	
	
	
	
	public static void initProfiling()
	{
		timerStack = new Stack<ProfileTimer>();
	}
	
	public static void shutdownProfiling()
	{
		assert timerStack.empty();
		timerStack = null;
	}
	
	
	
	
	public ProfileTimer()
	{
		startTime = -1;
		timeAccum = 0.0;
	}
	
	
	public void reset()
	{
		startTime = -1;
		timeAccum = 0.0;
	}
	
	
	public void start()
	{
		long currentTime = System.nanoTime();
		
		if ( !timerStack.empty() )
		{
			ProfileTimer current = timerStack.peek();
			
			current._stop( currentTime );
		}
		
		timerStack.push( this );
		_start( currentTime );
	}
	
	public void stop()
	{
		long currentTime = System.nanoTime();
		
		assert !timerStack.empty();
		assert timerStack.peek() == this;
		_stop( currentTime );
		timerStack.pop();
		
		if ( !timerStack.empty() )
		{
			ProfileTimer prev = timerStack.peek();
			
			prev._start( currentTime );
		}
	}
	
	public double getTime()
	{
		return timeAccum;
	}
	
	
	private void _start(long currentTime)
	{
		assert startTime == -1;

		startTime = currentTime;
	}

	private void _stop(long currentTime)
	{
		assert startTime != -1;
		
		timeAccum += (double)( currentTime - startTime ) * 1.0e-9;
		
		startTime = -1;
	}
}
