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
	
	
	@SuppressWarnings("unchecked")
	private MatchResult matchListContents(MatchState state, List<Object> input, int start, int stop)
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
			
			MatchResult result = subexps[i].processList( state, input, pos, stop );
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
					if ( result.isMergeable() )
					{
						value.addAll( (List<Object>)result.value );
					}
					else
					{
						value.add( result.value );
					}
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
	protected MatchResult evaluateNode(MatchState state, Object input)
	{
		if ( input instanceof List )
		{
			List<Object> node = (List<Object>)input;
			MatchResult res = matchListContents( state, node, 0, node.size() );
			if ( res.isValid() )
			{
				return res.withRange( 0, 1 );
			}
		}
		

		return MatchResult.failure( 0 );
	}

	@SuppressWarnings("unchecked")
	protected MatchResult evaluateList(MatchState state, List<Object> input, int start, int stop)
	{
		if ( stop > start )
		{
			Object x = input.get( start );
			if ( x instanceof List )
			{
				List<Object> node = (List<Object>)x;
				MatchResult res = matchListContents( state, node, 0, node.size() );
				if ( res.isValid() )
				{
					return res.withRange( start, start + 1 );
				}
			}
		}
		

		return MatchResult.failure( start );
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
