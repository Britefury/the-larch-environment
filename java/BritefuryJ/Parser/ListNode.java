//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import BritefuryJ.DocPresent.StreamValue.StreamValueAccessor;

/*
 * ListNode
 * 
 * ListNode:node( input )				->  input instanceof List  ?  ListNode.matchListContents( input )  :  fail
 * ListNode:string( input, start )			->  fail
 * ListNode:stream( input, start )		->  item = input.consumeStructuralItem(); item instanceof List  ?  ListNode.matchListContents( input )  :  fail
 * ListNode:list( input, start )			->  input[start] instanceof List  ?  ListNode.matchListContents( input[start], 0 )  :  fail
 * 
 * ListNode.matchListContents( input )	->  start = 0; [ start, result = s:list( input, start )   for s in ListNode.subexps ]
 */
public class ListNode extends BranchExpression
{
	public ListNode(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public ListNode(Object[] subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	
	@SuppressWarnings("unchecked")
	private ParseResult matchListContents(ParserState state, List<Object> input)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		
		int pos = 0;
		for (int i = 0; i < subexps.length; i++)
		{
			if ( pos > input.size() )
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
		
		if ( pos == input.size() )
		{
			return new ParseResult( value, 0, pos, bindings );
		}
		else
		{
			return ParseResult.failure( pos );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( input instanceof List<?> )
		{
			List<Object> node = (List<Object>)input;
			ParseResult res = matchListContents( state, node );
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
	protected ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start)
	{
		if ( start < input.length() )
		{
			start = state.skipJunkChars( input, start );
			
			Object valueArray[] = input.matchStructuralNode( start );
			
			if ( valueArray != null )
			{
				if ( valueArray[0] instanceof List )
				{
					List<Object> xs = (List<Object>)valueArray[0];
					ParseResult res = matchListContents( state, xs );
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
				ParseResult res = matchListContents( state, xs );
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
