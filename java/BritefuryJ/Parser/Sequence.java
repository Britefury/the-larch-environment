//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class Sequence extends BranchExpression
{
	public Sequence(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public Sequence(Object[] subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	public Sequence(List<Object> subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	
	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		Vector<Object> value = new Vector<Object>();
		HashMap<String, Object> bindings = new HashMap<String, Object>();
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			if ( pos > stop )
			{
				return new ParseResult( pos );
			}
			
			ParseResult result = subexps[i].evaluate(  state, input, pos, stop );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return new ParseResult( pos );
			}
			else
			{
				if ( !result.isSuppressed() )
				{
					bindings.putAll( result.bindings );
					value.add( result.value );
				}
			}
		}
		
		return new ParseResult( value, start, pos, bindings );
	}
	
	

	public ParserExpression __add__(ParserExpression x)
	{
		return new Sequence( joinSubexp( x ) );
	}

	public ParserExpression __add__(String x)
	{
		return new Sequence( joinSubexp( coerce( x ) ) );
	}

	public ParserExpression __add__(List<Object> x) throws ParserCoerceException
	{
		return new Sequence( joinSubexp( coerce( x ) ) );
	}


	public String toString()
	{
		return "Sequence( " + subexpsToString() + " )";
	}
}
