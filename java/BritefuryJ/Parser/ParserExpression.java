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
	public static class ParserCoerceException extends Exception
	{
		private static final long serialVersionUID = 1L;
	};
	
	
	
	protected String debugName = "";
	
	
	
	
	public ParseResult parseString(String input)
	{
		return parseString( input, 0, input.length() );
	}

	public ParseResult parseString(String input, String junkRegex)
	{
		return parseString( input, 0, input.length(), junkRegex );
	}

	public ParseResult parseString(String input, int start, int stop)
	{
		return parseString( input, start, stop, "[ \t\n]*" );
	}
	
	public ParseResult parseString(String input, int start, int stop, String junkRegex)
	{
		if ( stop == -1 )
		{
			stop = input.length();
		}
		
		ParserState state = new ParserState( junkRegex );
		ParseResult result = evaluate( state, input, start, stop );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end, stop );
		}
		
		return result;
	}
	
	
	protected abstract ParseResult evaluate(ParserState state, String input, int start, int stop);
	
	
	
	public void debug(String debugName)
	{
		this.debugName = debugName;
	}
	
	public String getDebugName()
	{
		return debugName;
	}
	
	
	
	protected List<Object> withSibling(ParserExpression sibling)
	{
		Object[] exprs = { this, sibling };
		return Arrays.asList( exprs );
	}


	
	

	public ParserExpression __add__(ParserExpression x) throws ParserCoerceException
	{
		return new Sequence( withSibling( x ) );
	}

	public ParserExpression __add__(String x) throws ParserCoerceException
	{
		return new Sequence( withSibling( coerce( x ) ) );
	}

	public ParserExpression __add__(List<Object> x) throws ParserCoerceException
	{
		return new Sequence( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __sub__(ParserExpression x) throws ParserCoerceException
	{
		return new Combine( withSibling( x ) );
	}

	public ParserExpression __sub__(String x) throws ParserCoerceException
	{
		return new Combine( withSibling( coerce( x ) ) );
	}

	public ParserExpression __sub__(List<Object> x) throws ParserCoerceException
	{
		return new Combine( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __or__(ParserExpression x) throws ParserCoerceException
	{
		return new Choice( withSibling( x ) );
	}

	public ParserExpression __or__(String x) throws ParserCoerceException
	{
		return new Choice( withSibling( coerce( x ) ) );
	}

	public ParserExpression __or__(List<Object> x) throws ParserCoerceException
	{
		return new Choice( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __xor__(ParserExpression x) throws ParserCoerceException
	{
		return new BestChoice( withSibling( x ) );
	}

	public ParserExpression __xor__(String x) throws ParserCoerceException
	{
		return new BestChoice( withSibling( coerce( x ) ) );
	}

	public ParserExpression __xor__(List<Object> x) throws ParserCoerceException
	{
		return new BestChoice( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __pow__(String name)
	{
		return new Bind( this, name );
	}

	
	public ParserExpression __and__(ParseCondition cond)
	{
		return new Condition( this, cond );
	}



	public ParserExpression action(ParseAction a)
	{
		return new Action( this, a );
	}

	public ParserExpression bindTo(String name)
	{
		return new Bind( this, name );
	}

	public ParserExpression condition(ParseCondition cond)
	{
		return new Condition( this, cond );
	}
	
	public ParserExpression suppress()
	{
		return new Suppress( this );
	}
	
	
	
	public static ParserExpression coerce(ParserExpression x)
	{
		return x;
	}

	public static ParserExpression coerce(String x)
	{
		return new Literal( x );
	}

	public static ParserExpression coerce(List<Object> xs) throws ParserCoerceException
	{
		return new Sequence( xs );
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
