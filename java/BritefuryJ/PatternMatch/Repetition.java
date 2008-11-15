//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import BritefuryJ.PatternMatch.MatchResult.NameAlreadyBoundException;

public class Repetition extends UnaryBranchExpression
{
	protected int minRepetitions, maxRepetitions;
	
	public Repetition(Object subexp, int minRepetitions, int maxRepetitions)
	{
		super( subexp );
		
		this.minRepetitions = minRepetitions;
		this.maxRepetitions = maxRepetitions;
	}
	
	public Repetition(MatchExpression subexp, int minRepetitions, int maxRepetitions)
	{
		super( subexp );
		
		this.minRepetitions = minRepetitions;
		this.maxRepetitions = maxRepetitions;
	}
	
	
	public int getMinRepetitions()
	{
		return minRepetitions;
	}
	
	public int getMaxRepetitions()
	{
		return maxRepetitions;
	}
	

	@SuppressWarnings("unchecked")
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
					if ( result.isMergeable() )
					{
						values.addAll( (List<Object>)result.value );
					}
					else
					{
						values.add( result.value );
					}
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
			return MatchResult.mergeableValue( values, start, pos, bindings );
		}

	}
	

	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof Repetition )
		{
			Repetition rx = (Repetition)x;
			return super.compareTo( x )  &&  minRepetitions == rx.minRepetitions  &&  maxRepetitions == rx.maxRepetitions;  
		}
		else
		{
			return false;
		}
	}

	
	public String toString()
	{
		return "Repetition( " + subexp.toString() + ", " + String.valueOf( minRepetitions ) + ":" + String.valueOf( maxRepetitions ) + " )";
	}
}
