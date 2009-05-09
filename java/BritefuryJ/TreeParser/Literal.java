//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

public class Literal extends StringTerminal
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
	
	
	
	protected TreeParseResult matchString(TreeParserState state, String input)
	{
		if ( matchString.equals( input ) )
		{
			return new TreeParseResult( input, 0, input.length() );
		}
		
		return TreeParseResult.failure( 0 );
	}
	
	
	public boolean compareTo(TreeParserExpression x)
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
