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
	
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return ParseResult.failure( 0 );
	}
	
	@SuppressWarnings("unchecked")
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			ParseResult result = subexps[i].handleStringChars( state, input, pos );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, result.bindings );

				if ( !result.isSuppressed() )
				{
					if ( result.isMergeable() )
					{
						value.addAll( (List<Object>)result.value );
					}
					else
					{
						value.add( result.value );
					}
				}
			}
		}
		
		return new ParseResult( value, start, pos, bindings );
	}

	@SuppressWarnings("unchecked")
	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			ParseResult result = subexps[i].handleRichStringItems( state, input, pos );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, result.bindings );

				if ( !result.isSuppressed() )
				{
					if ( result.isMergeable() )
					{
						value.addAll( (List<Object>)result.value );
					}
					else
					{
						value.add( result.value );
					}
				}
			}
		}
		
		return new ParseResult( value, start, pos, bindings );
	}

	@SuppressWarnings("unchecked")
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			ParseResult result = subexps[i].handleListItems( state, input, pos );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, result.bindings );

				if ( !result.isSuppressed() )
				{
					if ( result.isMergeable() )
					{
						value.addAll( (List<Object>)result.value );
					}
					else
					{
						value.add( result.value );
					}
				}
			}
		}
		
		return new ParseResult( value, start, pos, bindings );
	}

	
	
	
	public ParserExpression __add__(ParserExpression x)
	{
		return new Sequence( appendToSubexps( x ) );
	}

	public ParserExpression __add__(Object x) throws ParserCoerceException
	{
		return new Sequence( appendToSubexps( coerce( x ) ) );
	}


	public String toString()
	{
		return "Sequence( " + subexpsToString() + " )";
	}
}
