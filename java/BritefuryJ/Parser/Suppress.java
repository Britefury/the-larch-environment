//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * Suppress
 * 
 * Suppress:node( input )			->  Suppress.subexp:node( input ).suppress()
 * Suppress:string( input, start )		->  Suppress.subexp:string( input, start ).suppress()
 * Suppress:richStr( input, start )	->  Suppress.subexp:richStr( input, start ).suppress()
 * Suppress:list( input, start )		->  Suppress.subexp:list( input, start ).suppress()
 */
public class Suppress extends UnaryBranchExpression
{
	public Suppress(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public Suppress(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return subexp.handleNode( state, input ).suppressed();
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return subexp.handleStringChars( state, input, start ).suppressed();
	}

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		return subexp.handleRichStringItems( state, input, start ).suppressed();
	}
	
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		return subexp.handleListItems( state, input, start ).suppressed();
	}


	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof Suppress )
		{
			return super.isEquivalentTo( x );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Suppress( " + subexp.toString() + " )";
	}
}
