//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser;

import java.util.Stack;

import BritefuryJ.Projection.Subject;

class BrowserHistory
{
	private Stack<BrowserState> past, future;
	private BrowserState currentState;
	
	
	
	public BrowserHistory(Subject subject)
	{
		past = new Stack<BrowserState>();
		future = new Stack<BrowserState>();
		currentState = new BrowserState( subject.path() );
	}
	
	
	public BrowserState getCurrentState()
	{
		return currentState;
	}
	
	
	public void visit(Subject subject)
	{
		past.push( currentState );
		currentState = new BrowserState( subject.path() );
		future.clear();
	}
	
	
	public boolean canGoBack()
	{
		return past.size() > 0;
	}
	
	public void back()
	{
		if ( past.size() > 0 )
		{
			future.push( currentState );
			currentState = past.pop();
		}
	}


	public boolean canGoForward()
	{
		return future.size() > 0;
	}
	
	public void forward()
	{
		if ( future.size() > 0 )
		{
			past.push( currentState );
			currentState = future.pop();
		}
	}
	
	
	public void clear()
	{
		past.clear();
		future.clear();
	}
}
