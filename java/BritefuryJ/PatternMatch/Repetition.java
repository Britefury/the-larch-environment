//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.ArrayList;
import java.util.HashMap;

import BritefuryJ.PatternMatch.MatchResult.NameAlreadyBoundException;

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
	
	public Repetition(MatchExpression subexp, int minRepetitions, int maxRepetitions)
	{
		this( subexp, minRepetitions, maxRepetitions, false );
	}

	public Repetition(MatchExpression subexp, int minRepetitions, int maxRepetitions, boolean bNullIfZero)
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
	

	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		HashMap<String, Object> bindings = null;
		
		int pos = start;
		int errorPos = start;
		int i = 0;
		
		while ( pos <= stop  &&  ( maxRepetitions == -1  ||  i < maxRepetitions ) )
		{
			MatchResult result = subexp.evaluateNode( state, input, pos, stop );
			errorPos = result.end;
			
			if ( !result.isValid() )
			{
				break;
			}
			else
			{
				try
				{
					bindings = result.addBindingsTo( bindings );
				}
				catch (NameAlreadyBoundException e)
				{
					break;
				}

				if ( !result.isSuppressed() )
				{
					values.add( result.value );
				}
				pos = result.end;
				i++;
			}
		}
		
		
		if ( ( i < minRepetitions)  ||  ( maxRepetitions != -1  &&  i > maxRepetitions ) )
		{
			return MatchResult.failure( errorPos );
		}
		else
		{
			if ( bNullIfZero  &&  i == 0 )
			{
				return new MatchResult( null, start, pos, bindings );
			}
			else
			{
				return new MatchResult( values, start, pos, bindings );
			}
		}

	}
	

	public boolean compareTo(MatchExpression x)
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
