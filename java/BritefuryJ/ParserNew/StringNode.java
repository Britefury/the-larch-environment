//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class StringNode extends UnaryBranchExpression
{
	public StringNode(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public StringNode(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	
	private ParseResult matchNode(ParserState state, Object input, int start)
	{
		if ( input instanceof String )
		{
			String s = (String)input;
			ParseResult res = subexp.handleStringChars( state, s, 0 );
			if ( res.getEnd() == s.length() )
			{
				return res.withRange( start, start + 1 );
			}
		}
		else if ( input instanceof ItemStreamAccessor )
		{
			ItemStreamAccessor s = (ItemStreamAccessor)input;
			ParseResult res = subexp.handleStreamItems( state, s, 0 );
			if ( res.getEnd() == s.length() )
			{
				return res.withRange( start, start + 1 );
			}
		}

		return ParseResult.failure( start );
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return matchNode( state, input, 0 );
	}
	
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		if ( start < input.length() )
		{
			Object structural[] = input.matchStructuralNode( start );
			
			if ( structural != null )
			{
				return matchNode( state, structural[0], start );
			}
		}
		
		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		if ( start < input.size() )
		{
			return matchNode( state, input.get( start ), start );
		}
		

		return ParseResult.failure( start );
	}


	
	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof StringNode )
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
		return "StringNode( " + subexp.toString() + " )";
	}
}
