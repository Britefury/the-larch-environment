//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class ListNode extends BranchExpression
{
	public ListNode(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	
	@SuppressWarnings("unchecked")
	private ParseResult matchListContents(ParserState state, List<Object> input, int start, int stop)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		HashMap<String, Object> bindings = null;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			if ( pos > stop )
			{
				return ParseResult.failure( pos );
			}
			
			ParseResult result = subexps[i].handleListItems( state, input, pos );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, result.getBindings() );
				
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
			return new ParseResult( value, start, pos, bindings );
		}
		else
		{
			return ParseResult.failure( pos );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( input instanceof List )
		{
			List<Object> node = (List<Object>)input;
			ParseResult res = matchListContents( state, node, 0, node.size() );
			if ( res.isValid() )
			{
				return res.withRange( 0, 1 );
			}
		}
		

		return ParseResult.failure( 0 );
	}
	
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return ParseResult.failure( start );
	}
	
	@SuppressWarnings("unchecked")
	protected ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		if ( start < input.length() )
		{
			Object valueArray[] = input.matchStructuralNode( start );
			
			if ( valueArray != null )
			{
				if ( valueArray[0] instanceof List )
				{
					List<Object> xs = (List<Object>)valueArray[0];
					ParseResult res = matchListContents( state, xs, 0, xs.size() );
					if ( res.isValid() )
					{
						return res.withRange( start, start + 1 );
					}
				}
			}
		}
		
		return ParseResult.failure( start );
	}
	
	@SuppressWarnings("unchecked")
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		if ( start < input.size() )
		{
			Object x = input.get( start );
			if ( x instanceof List )
			{
				List<Object> xs = (List<Object>)x;
				ParseResult res = matchListContents( state, xs, 0, xs.size() );
				if ( res.isValid() )
				{
					return res.withRange( start, start + 1 );
				}
			}
		}
		

		return ParseResult.failure( start );
	}


	
	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof ListNode )
		{
			return super.compareTo( x );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "ListNode( " + subexpsToString() + " )";
	}
}
