//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.HashMap;
import java.util.List;

public class PatternMatcher
{
	public static class MatchFailureException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	private Guard[] guards;
	
	
	public PatternMatcher(Guard[] gs)
	{
		this.guards = gs;
	}
	
	public PatternMatcher(List<Guard> gs)
	{
		this.guards = new Guard[gs.size()];
		this.guards = gs.toArray( this.guards );
	}
	
	
	public Object match(Object x) throws MatchFailureException
	{
		return match( x, null );
	}

	public Object match(Object x, Object arg) throws MatchFailureException
	{
		HashMap<String, Object> bindings = new HashMap<String, Object>();
		
		for (Guard g: guards)
		{
			if ( g.testPattern( x, bindings ) )
			{
				return g.invokeAction( x, bindings, arg );
			}
			bindings.clear();
		}
		
		throw new MatchFailureException();
	}
}
