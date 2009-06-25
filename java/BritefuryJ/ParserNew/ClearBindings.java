//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

/*
 * ClearBindings
 * 
 * ClearBindings:node( input )			->  clearBindings( Bind.subexp:node( input ) )
 * ClearBindings:string( input, start )		->  clearBindings( Bind.subexp:string( input, start ) )
 * ClearBindings:stream( input, start )		->  clearBindings( Bind.subexp:stream( input, start ) )
 * ClearBindings:list( input, start )		->  clearBindings( Bind.subexp:list( input, start ) )
 */
public class ClearBindings extends UnaryBranchExpression
{
	public ClearBindings(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public ClearBindings(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleStringChars( state, input, start );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		ParseResult res = subexp.handleStreamItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleListItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}
	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof ClearBindings )
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
		return "ClearBindings( " + subexp.toString() + " )";
	}
}
