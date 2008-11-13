//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import BritefuryJ.PatternMatch.MatchResult.NameAlreadyBoundException;

public class ListNode extends BranchExpression
{
	public ListNode(MatchExpression[] subexps)
	{
		super( subexps );
	}
	
	public ListNode(Object[] subexps)
	{
		super( subexps );
	}
	
	public ListNode(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	protected MatchResult parseString(MatchState state, String input, int start, int stop)
	{
		return MatchResult.failure( start );
	}
	
	
	private MatchResult parseNodeContents(MatchState state, List<Object> input, int start, int stop)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		HashMap<String, Object> bindings = null;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			if ( pos > stop )
			{
				return MatchResult.failure( pos );
			}
			
			MatchResult result = subexps[i].evaluateNode(  state, input, pos, stop );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return MatchResult.failure( pos );
			}
			else
			{
				try
				{
					bindings = result.addBindingsTo( bindings );
				}
				catch (NameAlreadyBoundException e)
				{
					return MatchResult.failure( pos );
				}
				
				if ( !result.isSuppressed() )
				{
					value.add( result.value );
				}
			}
		}
		
		if ( pos == stop )
		{
			return new MatchResult( value, start, pos, bindings );
		}
		else
		{
			return MatchResult.failure( pos );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		if ( input instanceof List )
		{
			List<Object> xs = (List<Object>)input;
			if ( stop > start )
			{
				Object x = xs.get( start );
				if ( x instanceof List )
				{
					List<Object> node = (List<Object>)x;
					MatchResult res = parseNodeContents( state, node, 0, node.size() );
					if ( res.isValid() )
					{
						return res.withRange( start, start + 1 );
					}
				}
			}
		}
		

		return MatchResult.failure( start );
	}


	
	public MatchExpression __add__(MatchExpression x)
	{
		return new Sequence( appendToSubexps( x ) );
	}

	public MatchExpression __add__(Object x)
	{
		return new Sequence( appendToSubexps( toParserExpression( x ) ) );
	}


	public String toString()
	{
		return "Sequence( " + subexpsToString() + " )";
	}

	
	protected boolean isSequence()
	{
		return true;
	}
}
