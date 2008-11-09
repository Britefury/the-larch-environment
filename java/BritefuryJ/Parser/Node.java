//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;
import java.util.List;

public class Node extends BranchExpression
{
	public Node(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public Node(Object[] subexps)
	{
		super( subexps );
	}
	
	public Node(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		return ParseResult.failure( start );
	}
	
	
	private ParseResult parseNodeContents(ParserState state, List<Object> input, int start, int stop)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			if ( pos > stop )
			{
				return ParseResult.failure( pos );
			}
			
			ParseResult result = subexps[i].evaluateNode(  state, input, pos, stop );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				if ( !result.isSuppressed() )
				{
					value.add( result.value );
				}
			}
		}
		
		if ( pos == stop )
		{
			return new ParseResult( value, start, pos );
		}
		else
		{
			return ParseResult.failure( pos );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
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
					ParseResult res = parseNodeContents( state, node, 0, node.size() );
					if ( res.isValid() )
					{
						return new ParseResult( res.getValue(), start, start + 1 );
					}
				}
			}
		}
		

		return ParseResult.failure( start );
	}

	@SuppressWarnings("unchecked")
	protected ParseResult parseRootNode(ParserState state, Object input, int start, int stop)
	{
		if ( input instanceof List )
		{
			List<Object> node = (List<Object>)input;
			return parseNodeContents( state, node, start, stop );
		}

	
		return ParseResult.failure( start );
	}

	
	public ParserExpression __add__(ParserExpression x)
	{
		return new Sequence( appendToSubexps( x ) );
	}

	public ParserExpression __add__(Object x)
	{
		return new Sequence( appendToSubexps( coerce( x ) ) );
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
