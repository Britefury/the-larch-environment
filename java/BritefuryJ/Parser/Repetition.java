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

public class Repetition extends UnaryBranchExpression
{
	protected int minRepetitions, maxRepetitions;
	protected boolean bNullIfZero;
	
	public Repetition(String subexp, int minRepetitions, int maxRepetitions)
	{
		this( subexp, minRepetitions, maxRepetitions, false );
	}

	public Repetition(String subexp, int minRepetitions, int maxRepetitions, boolean bNullIfZero)
	{
		super( subexp );
		
		this.minRepetitions = minRepetitions;
		this.maxRepetitions = maxRepetitions;
		this.bNullIfZero = bNullIfZero;
	}
	
	public Repetition(List<Object> subexp, int minRepetitions, int maxRepetitions) throws ParserCoerceException
	{
		this( subexp, minRepetitions, maxRepetitions, false );
	}

	public Repetition(List<Object> subexp, int minRepetitions, int maxRepetitions, boolean bNullIfZero) throws ParserCoerceException
	{
		super( subexp );
		
		this.minRepetitions = minRepetitions;
		this.maxRepetitions = maxRepetitions;
		this.bNullIfZero = bNullIfZero;
	}
		
	public Repetition(ParserExpression subexp, int minRepetitions, int maxRepetitions)
	{
		this( subexp, minRepetitions, maxRepetitions, false );
	}

	public Repetition(ParserExpression subexp, int minRepetitions, int maxRepetitions, boolean bNullIfZero)
	{
		super( subexp );
		
		this.minRepetitions = minRepetitions;
		this.maxRepetitions = maxRepetitions;
		this.bNullIfZero = bNullIfZero;
	}
	

	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		Vector<Object> values = new Vector<Object>();
		HashMap<String, Object> bindings = new HashMap<String, Object>();
		
		int pos = start;
		int errorPos = start;
		int i = 0;
		
		while ( pos <= stop  &&  ( maxRepetitions == -1  ||  i < maxRepetitions ) )
		{
			ParseResult res = subexp.evaluate( state, input, pos, stop );
			errorPos = res.end;
			
			if ( !res.isValid() )
			{
				break;
			}
			else
			{
				if ( !res.isSuppressed() )
				{
					bindings.putAll( res.bindings );
					values.add( res.value );
				}
				pos = res.end;
				i++;
			}
		}
		
		
		if ( ( i < minRepetitions)  ||  ( maxRepetitions != -1  &&  i > maxRepetitions ) )
		{
			return new ParseResult( errorPos );
		}
		else
		{
			if ( bNullIfZero  &&  i == 0 )
			{
				return new ParseResult( null, start, pos, bindings );
			}
			else
			{
				return new ParseResult( values, start, pos, bindings );
			}
		}

	}
	

	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Repetition )
		{
			Repetition rx = (Repetition)x;
			return super.compareTo( x )  &&  minRepetitions == rx.minRepetitions  &&  maxRepetitions == rx.maxRepetitions  &&  bNullIfZero == rx.bNullIfZero;  
		}
		else
		{
			return false;
		}
	}

	
	public String toString()
	{
		String nullIfZeroStr = bNullIfZero  ?  ", null-if-zero"  :  "";
		return "Repetition( " + subexp.toString() + ", " + String.valueOf( minRepetitions ) + ":" + String.valueOf( maxRepetitions ) + nullIfZeroStr + " )";
	}
}
