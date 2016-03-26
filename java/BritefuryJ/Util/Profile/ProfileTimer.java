//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.Profile;

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
