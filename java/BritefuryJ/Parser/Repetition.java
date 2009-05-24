//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class Repetition extends UnaryBranchExpression
{
	protected int minRepetitions, maxRepetitions;
	protected boolean bNullIfZero;
	
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
	
	public Repetition(Object subexp, int minRepetitions, int maxRepetitions) throws ParserCoerceException
	{
		this( subexp, minRepetitions, maxRepetitions, false );
	}

	public Repetition(Object subexp, int minRepetitions, int maxRepetitions, boolean bNullIfZero) throws ParserCoerceException
	{
		super( subexp );
		
		this.minRepetitions = minRepetitions;
		this.maxRepetitions = maxRepetitions;
		this.bNullIfZero = bNullIfZero;
	}
	
	
	public int getMinRepetitions()
	{
		return minRepetitions;
	}
	
	public int getMaxRepetitions()
	{
		return maxRepetitions;
	}
	
	public boolean getNullIfZero()
	{
		return bNullIfZero;
	}
	

	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		
		int pos = start;
		int errorPos = start;
		int i = 0;
		
		while ( ( maxRepetitions == -1  ||  i < maxRepetitions ) )
		{
			ParseResult res = subexp.evaluateStream( state, input, pos );
			errorPos = res.end;
			
			if ( !res.isValid() )
			{
				break;
			}
			else
			{
				if ( !res.isSuppressed() )
				{
					values.add( res.value );
				}
				pos = res.end;
				i++;
			}
		}
		
		
		if ( ( i < minRepetitions)  ||  ( maxRepetitions != -1  &&  i > maxRepetitions ) )
		{
			return ParseResult.failure( errorPos );
		}
		else
		{
			if ( bNullIfZero  &&  i == 0 )
			{
				return new ParseResult( null, start, pos );
			}
			else
			{
				return new ParseResult( values, start, pos );
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
