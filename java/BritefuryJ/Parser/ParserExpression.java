//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

public abstract class ParserExpression
{
	protected String debugName = "";
	
	
	
	
	public ParseResult parseString(String input, int start, int stop)
	{
		return parseString( input, start, stop, " \t" );
	}
	
	public ParseResult parseString(String input, int start, int stop, String ignoreCharsRegex)
	{
		if ( stop == -1 )
		{
			stop = input.length();
		}
		
		ParserState state = new ParserState( ignoreCharsRegex );
		ParseResult result = evaluate( state, input, start, stop );
		if ( result.isValid() )
		{
			result.end = state.skipIgnoredChars( input, result.end, stop );
		}
		
		return result;
	}
	
	
	protected abstract ParseResult evaluate(ParserState state, String input, int start, int stop);
	
	
	
	public void debug(String debugName)
	{
		this.debugName = debugName;
	}
	
	
	
	protected List<ParserExpression> withSibling(ParserExpression sibling)
	{
		ParserExpression[] exprs = { this, sibling };
		return Arrays.asList( exprs );
	}



	protected ParserExpression __add__(ParserExpression x)
	{
		return new Sequence( withSibling( x ) );
	}

	protected ParserExpression __sub__(ParserExpression x)
	{
		return new Combine( withSibling( x ) );
	}

	protected ParserExpression __or__(ParserExpression x)
	{
		return new Choice( withSibling( x ) );
	}

	protected ParserExpression __xor__(ParserExpression x)
	{
		return new BestChoice( withSibling( x ) );
	}

	protected ParserExpression __pow__(String name)
	{
		return new Bind( this, name );
	}

	protected ParserExpression __and__(ParseCondition cond)
	{
		return new Condition( this, cond );
	}



	protected ParserExpression action(ParseAction a)
	{
		return new Action( this, a );
	}

	protected ParserExpression condition(ParseCondition cond)
	{
		return new Condition( this, cond );
	}





	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = {};
		return Arrays.asList( children );
	}
	
	
	public boolean compareTo(ParserExpression x)
	{
		return false;
	}
	
	
	public String toString()
	{
		return "ParserExpression()";
	}
}
