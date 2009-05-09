//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import BritefuryJ.TreeParser.TreeParseResult.NameAlreadyBoundException;

public class ListMatch extends BranchExpression
{
	public ListMatch(TreeParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public ListMatch(Object[] subexps)
	{
		super( subexps );
	}
	
	public ListMatch(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	@SuppressWarnings("unchecked")
	private TreeParseResult matchListContents(TreeParserState state, List<Object> input, int start, int stop)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		HashMap<String, Object> bindings = null;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			if ( pos > stop )
			{
				return TreeParseResult.failure( pos );
			}
			
			TreeParseResult result = subexps[i].processList( state, input, pos, stop );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return TreeParseResult.failure( pos );
			}
			else
			{
				try
				{
					bindings = result.addBindingsTo( bindings );
				}
				catch (NameAlreadyBoundException e)
				{
					return TreeParseResult.failure( pos );
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
			return new TreeParseResult( value, start, pos, bindings );
		}
		else
		{
			return TreeParseResult.failure( pos );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		if ( input instanceof List )
		{
			List<Object> node = (List<Object>)input;
			TreeParseResult res = matchListContents( state, node, 0, node.size() );
			if ( res.isValid() )
			{
				return res.withRange( 0, 1 );
			}
		}
		

		return TreeParseResult.failure( 0 );
	}

	@SuppressWarnings("unchecked")
	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		if ( stop > start )
		{
			Object x = input.get( start );
			if ( x instanceof List )
			{
				List<Object> node = (List<Object>)x;
				TreeParseResult res = matchListContents( state, node, 0, node.size() );
				if ( res.isValid() )
				{
					return res.withRange( start, start + 1 );
				}
			}
		}
		

		return TreeParseResult.failure( start );
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
