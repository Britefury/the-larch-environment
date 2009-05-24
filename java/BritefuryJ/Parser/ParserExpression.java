//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;
import org.python.core.PyObject;

import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParserExpressionInterface;

public abstract class ParserExpression implements ParserExpressionInterface
{
	public static class ParserCoerceException extends Exception
	{
		private static final long serialVersionUID = 1L;
	};
	
	
	
	public ParseResult parseString(String input)
	{
		return parseStream( new ItemStream( input ) );
	}

	public ParseResult parseString(String input, String junkRegex)
	{
		return parseStream( new ItemStream( input ), junkRegex );
	}

	public ParseResult parseString(String input, int start)
	{
		return parseStream( new ItemStream( input ), start );
	}
	
	public ParseResult parseString(String input, int start, String junkRegex)
	{
		return parseStream( new ItemStream( input ), start, junkRegex );
	}
	
	
	public DebugParseResult debugParseString(String input)
	{
		return debugParseStream( new ItemStream( input ) );
	}

	public DebugParseResult debugParseString(String input, String junkRegex)
	{
		return debugParseStream( new ItemStream( input ), junkRegex );
	}

	public DebugParseResult debugParseString(String input, int start)
	{
		return debugParseStream( new ItemStream( input ), start );
	}
	
	public DebugParseResult debugParseString(String input, int start, String junkRegex)
	{
		return debugParseStream( new ItemStream( input ), start, junkRegex );
	}
	
	
	
	
	public ParseResult parseStream(ItemStream input)
	{
		return parseStream( input, 0 );
	}

	public ParseResult parseStream(ItemStream input, String junkRegex)
	{
		return parseStream( input, 0, junkRegex );
	}

	public ParseResult parseStream(ItemStream input, int start)
	{
		return parseStream( input, start, "[ ]*" );
	}
	
	public ParseResult parseStream(ItemStream input, int start, String junkRegex)
	{
		return parseStream( input.accessor(), start, junkRegex );
	}

	public ParseResult parseStream(ItemStreamAccessor input, int start, String junkRegex)
	{
		ParserState state = new ParserState( junkRegex );
		ParseResult result = evaluateStream( state, input, start );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}
	
	
	public DebugParseResult debugParseStream(ItemStream input)
	{
		return debugParseStream( input, 0 );
	}

	public DebugParseResult debugParseStream(ItemStream input, String junkRegex)
	{
		return debugParseStream( input, 0, junkRegex );
	}

	public DebugParseResult debugParseStream(ItemStream input, int start)
	{
		return debugParseStream( input, start, "[ ]*" );
	}
	
	public DebugParseResult debugParseStream(ItemStream input, int start, String junkRegex)
	{
		return debugParseStream( input.accessor(), start, junkRegex );
	}

	public DebugParseResult debugParseStream(ItemStreamAccessor input, int start, String junkRegex)
	{
		ParserState state = new ParserState( junkRegex );
		state.enableDebugging();
		DebugParseResult result = (DebugParseResult)evaluateStream( state, input, start );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}
	
	
	
	
	protected ParseResult evaluateStream(ParserState state, ItemStreamAccessor input, int start)
	{
		if ( state.bDebuggingEnabled )
		{
			// Get the current top of the debug stack (outer call)
			DebugNode prev = state.debugStack;
			// Create the debug info node
			DebugNode node = new DebugNode( prev, this, input );

			// Push @node onto the debug stack
			state.debugStack = node;
			
			// Get the parse result
			ParseResult result = parseStream( state, input, start );
			node.setResult( result );
			
			// If @prev is valid, add @node as a call-child of @prev
			if ( prev != null )
			{
				prev.addCallChild( node );
			}
			
			// Pop @node off the debug stack
			state.debugStack = prev;
			
			
			if ( result instanceof DebugParseResult )
			{
				DebugParseResult debugResult = (DebugParseResult)result;
				
				DebugNode fromNode = node;
				DebugNode toNode = debugResult.debugNode;
				
				if ( !fromNode.getCallChildren().contains( toNode ) )
				{
					fromNode.addMemoChild( toNode );
				}
			}
			
			
			return result.debug( node );
		}
		else
		{
			return parseStream( state, input, start );
		}
	}
	
	protected abstract ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start);
	
	
	
	public String getExpressionName()
	{
		return null;
	}
	
	
	
	protected ParserExpression[] withSibling(ParserExpression sibling)
	{
		ParserExpression[] exprs = { this, sibling };
		return exprs;
	}


	
	

	public ParserExpression __add__(ParserExpression x)
	{
		return new Sequence( withSibling( x ) );
	}

	public ParserExpression __add__(Object x) throws ParserCoerceException
	{
		return new Sequence( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __sub__(ParserExpression x)
	{
		return new Combine( withSibling( x ) );
	}

	public ParserExpression __sub__(Object x) throws ParserCoerceException
	{
		return new Combine( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __or__(ParserExpression x)
	{
		return new Choice( withSibling( x ) );
	}

	public ParserExpression __or__(Object x) throws ParserCoerceException
	{
		return new Choice( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __xor__(ParserExpression x)
	{
		return new BestChoice( withSibling( x ) );
	}

	public ParserExpression __xor__(Object x) throws ParserCoerceException
	{
		return new BestChoice( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __and__(ParseCondition cond)
	{
		return condition( cond );
	}

	public ParserExpression __and__(PyObject cond)
	{
		return condition( cond );
	}



	public ParserExpression action(ParseAction a)
	{
		return new Action( this, a );
	}

	public ParserExpression action(PyObject a)
	{
		return new Action( this, a );
	}
	
	public ParserExpression condition(ParseCondition cond)
	{
		return new Condition( this, cond );
	}
	
	public ParserExpression condition(PyObject cond)
	{
		return new Condition( this, cond );
	}
	
	public ParserExpression suppress()
	{
		return new Suppress( this );
	}
	
	public ParserExpression optional()
	{
		return new Optional( this );
	}
	
	public ParserExpression zeroOrMore()
	{
		return new ZeroOrMore( this );
	}
	
	public ParserExpression oneOrMore()
	{
		return new OneOrMore( this );
	}

	
	
	public static ParserExpression coerce(Object x) throws ParserCoerceException
	{
		if ( x instanceof ParserExpression )
		{
			return (ParserExpression)x;
		}
		else if ( x instanceof String )
		{
			return new Literal( (String)x );
		}
		else
		{
			throw new ParserCoerceException();
		}
	}

	public static ParserExpression coerce(String x)
	{
		return new Literal( x );
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
	
	public static boolean compareExpressions(ParserExpression x, ParserExpression y)
	{
		if ( x == null  &&  y == null )
		{
			return true;
		}
		else if ( x != null  &&  y != null )
		{
			return x.compareTo( y );
		}
		else
		{
			return false;
		}
	}
	
	
	
	public String toString()
	{
		return "ParserExpression()";
	}
}
