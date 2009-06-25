//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

/*
 * Literal
 * 
 * Literal:node( input )			->  input == Literal.matchString  ?  input  :  fail
 * Literal:string( input, start )		->  input[start:start+Literal.matchString.length()] == Literal.matchString  ?  input[start:start+Literal.matchString.length()] : fail
 * Literal:stream( input, start )		->  input[start:start+Literal.matchString.length()] == Literal.matchString  ?  input[start:start+Literal.matchString.length()] : fail
 * Literal:list( input, start )			->  input[start] == Literal.matchString  ?  input[start]  :  fail
 */
public class Literal extends TerminalString
{
	protected String matchString;
	
	
	public Literal(String matchString)
	{
		this.matchString = matchString;
	}
	
	
	public String getMatchString()
	{
		return matchString;
	}
	
	
	
	protected ParseResult consumeString(String input, int start)
	{
		int end = start + matchString.length();
		if ( end <= input.length()  &&  input.subSequence( start, end ).equals( matchString ) )
		{
			return new ParseResult( matchString, start, end );
		}

		return ParseResult.failure( start );
	}
	
	protected ParseResult consumeStream(ItemStreamAccessor input, int start)
	{
		int end = input.consumeString( start, matchString );
		
		if ( end != -1 )
		{
			return new ParseResult( matchString, start, end );
		}
		
		return ParseResult.failure( start );
	}



	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Literal )
		{
			Literal xl = (Literal)x;
			return matchString.equals( xl.matchString );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Literal( \"" + matchString + "\" )";
	}
}
