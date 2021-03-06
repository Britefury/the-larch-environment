//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import BritefuryJ.Util.RichString.RichStringAccessor;

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
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return ParseResult.failure( 0 );
	}
	
	@SuppressWarnings("unchecked")
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		
		int pos = start;
		int errorPos = start;
		int i = 0;
		
		while ( ( maxRepetitions == -1  ||  i < maxRepetitions ) )
		{
			ParseResult res = subexp.handleStringChars( state, input, pos );
			errorPos = res.end;
			
			if ( !res.isValid() )
			{
				break;
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, res.bindings );

				if ( !res.isSuppressed() )
				{
					if ( res.isMergeable() )
					{
						values.addAll( (List<Object>)res.value );
					}
					else
					{
						values.add( res.value );
					}
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
				return new ParseResult( null, start, pos, bindings );
			}
			else
			{
				return new ParseResult( values, start, pos, bindings );
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		
		int pos = start;
		int errorPos = start;
		int i = 0;
		
		while ( ( maxRepetitions == -1  ||  i < maxRepetitions ) )
		{
			ParseResult res = subexp.handleRichStringItems( state, input, pos );
			errorPos = res.end;
			
			if ( !res.isValid() )
			{
				break;
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, res.bindings );

				if ( !res.isSuppressed() )
				{
					if ( res.isMergeable() )
					{
						values.addAll( (List<Object>)res.value );
					}
					else
					{
						values.add( res.value );
					}
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
				return new ParseResult( null, start, pos, bindings );
			}
			else
			{
				return new ParseResult( values, start, pos, bindings );
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		
		int pos = start;
		int errorPos = start;
		int i = 0;
		
		while ( ( maxRepetitions == -1  ||  i < maxRepetitions ) )
		{
			ParseResult res = subexp.handleListItems( state, input, pos );
			errorPos = res.end;
			
			if ( !res.isValid() )
			{
				break;
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, res.bindings );

				if ( !res.isSuppressed() )
				{
					if ( res.isMergeable() )
					{
						values.addAll( (List<Object>)res.value );
					}
					else
					{
						values.add( res.value );
					}
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
				return new ParseResult( null, start, pos, bindings );
			}
			else
			{
				return new ParseResult( values, start, pos, bindings );
			}
		}
	}

	
	
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof Repetition )
		{
			Repetition rx = (Repetition)x;
			return super.isEquivalentTo( x )  &&  minRepetitions == rx.minRepetitions  &&  maxRepetitions == rx.maxRepetitions  &&  bNullIfZero == rx.bNullIfZero;  
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
