//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * Literal
 * 
 * Literal:node( input )			->  input == Literal.matchString  ?  input  :  fail
 * Literal:string( input, start )		->  input[start:start+Literal.matchString.length()] == Literal.matchString  ?  input[start:start+Literal.matchString.length()] : fail
 * Literal:richStr( input, start )		->  input[start:start+Literal.matchString.length()] == Literal.matchString  ?  input[start:start+Literal.matchString.length()] : fail
 * Literal:list( input, start )			->  input[start] == Literal.matchString  ?  input[start]  :  fail
 */
public class Literal extends TerminalString
{
	protected final String matchString;
	
	
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
	
	protected ParseResult consumeRichString(RichStringAccessor input, int start)
	{
		int end = input.consumeString( start, matchString );
		
		if ( end != -1 )
		{
			return new ParseResult( matchString, start, end );
		}
		
		return ParseResult.failure( start );
	}



	public boolean isEquivalentTo(ParserExpression x)
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
