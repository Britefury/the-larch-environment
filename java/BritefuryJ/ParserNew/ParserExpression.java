//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

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
	
	
	

	
	protected enum Mode
	{
		NODE,
		STRING,
		STREAM,
		LIST
	}





	protected String debugName = "";
	
	
	

	public ParseResult parseNode(Object input)
	{
		return parseNode( input, "[ ]*", (ParseAction)null );
	}

	public ParseResult parseNode(Object input, String junkRegex)
	{
		return parseNode( input, junkRegex, (ParseAction)null );
	}

	public ParseResult parseNode(Object input, ParseAction delegateAction)
	{
		return parseNode( input, "[ ]*", delegateAction );
	}

	public ParseResult parseNode(Object input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleNode( state, input );
		
		return result;
	}

	public ParseResult parseNode(Object input, PyObject delegateAction)
	{
		return parseNode( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseNode(Object input, String junkRegex, PyObject delegateAction)
	{
		return parseNode( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	public DebugParseResult debugParseNode(Object input)
	{
		return debugParseNode( input, "[ ]*", (ParseAction)null );
	}

	public DebugParseResult debugParseNode(Object input, String junkRegex)
	{
		return debugParseNode( input, junkRegex, (ParseAction)null );
	}

	public DebugParseResult debugParseNode(Object input, ParseAction delegateAction)
	{
		return debugParseNode( input, "[ ]*", delegateAction );
	}

	public DebugParseResult debugParseNode(Object input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableDebugging();
		DebugParseResult result = (DebugParseResult)handleNode( state, input );
		
		return result;
	}

	public DebugParseResult debugParseNode(Object input, PyObject delegateAction)
	{
		return debugParseNode( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public DebugParseResult debugParseNode(Object input, String junkRegex, PyObject delegateAction)
	{
		return debugParseNode( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	
	
	

	
	public ParseResult parseStringChars(String input)
	{
		return parseStringChars( input, "[ ]*", (ParseAction)null );
	}

	public ParseResult parseStringChars(String input, String junkRegex)
	{
		return parseStringChars( input, junkRegex, (ParseAction)null );
	}

	public ParseResult parseStringChars(String input, ParseAction delegateAction)
	{
		return parseStringChars( input, "[ ]*", delegateAction );
	}

	public ParseResult parseStringChars(String input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleStringChars( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}

	public ParseResult parseStringChars(String input, PyObject delegateAction)
	{
		return parseStringChars( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseStringChars(String input, String junkRegex, PyObject delegateAction)
	{
		return parseStringChars( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	public DebugParseResult debugParseStringChars(String input)
	{
		return debugParseStringChars( input, "[ ]*", (ParseAction)null );
	}

	public DebugParseResult debugParseStringChars(String input, String junkRegex)
	{
		return debugParseStringChars( input, junkRegex, (ParseAction)null );
	}

	public DebugParseResult debugParseStringChars(String input, ParseAction delegateAction)
	{
		return debugParseStringChars( input, "[ ]*", delegateAction );
	}

	public DebugParseResult debugParseStringChars(String input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableDebugging();
		DebugParseResult result = (DebugParseResult)evaluateStringChars( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}

	public DebugParseResult debugParseStringChars(String input, PyObject delegateAction)
	{
		return debugParseStringChars( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public DebugParseResult debugParseStringChars(String input, String junkRegex, PyObject delegateAction)
	{
		return debugParseStringChars( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	

	
	public ParseResult parseStreamItems(ItemStream input)
	{
		return parseStreamItems( input.accessor(), "[ ]*", null );
	}

	public ParseResult parseStreamItems(ItemStream input, String junkRegex)
	{
		return parseStreamItems( input.accessor(), junkRegex, null );
	}

	public ParseResult parseStreamItems(ItemStream input, ParseAction delegateAction)
	{
		return parseStreamItems( input.accessor(), "[ ]*", delegateAction );
	}

	public ParseResult parseStreamItems(ItemStream input, String junkRegex, ParseAction delegateAction)
	{
		return parseStreamItems( input.accessor(), junkRegex, delegateAction );
	}

	public ParseResult parseStreamItems(ItemStream input, PyObject delegateAction)
	{
		return parseStreamItems( input.accessor(), "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseStreamItems(ItemStream input, String junkRegex, PyObject delegateAction)
	{
		return parseStreamItems( input.accessor(), junkRegex, new Action.PyAction( delegateAction ) );
	}

	private ParseResult parseStreamItems(ItemStreamAccessor input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleStreamItems( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}
	
	
	public DebugParseResult debugParseStreamItems(ItemStream input)
	{
		return debugParseStreamItems( input.accessor(), "[ ]*", null );
	}

	public DebugParseResult debugParseStreamItems(ItemStream input, String junkRegex)
	{
		return debugParseStreamItems( input.accessor(), junkRegex, null );
	}

	public DebugParseResult debugParseStreamItems(ItemStream input, ParseAction delegateAction)
	{
		return debugParseStreamItems( input.accessor(), "[ ]*", delegateAction );
	}

	public DebugParseResult debugParseStreamItems(ItemStream input, String junkRegex, ParseAction delegateAction)
	{
		return debugParseStreamItems( input.accessor(), junkRegex, delegateAction );
	}

	public DebugParseResult debugParseStreamItems(ItemStream input, PyObject delegateAction)
	{
		return debugParseStreamItems( input.accessor(), "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public DebugParseResult debugParseStreamItems(ItemStream input, String junkRegex, PyObject delegateAction)
	{
		return debugParseStreamItems( input.accessor(), junkRegex, new Action.PyAction( delegateAction ) );
	}

	private DebugParseResult debugParseStreamItems(ItemStreamAccessor input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableDebugging();
		DebugParseResult result = (DebugParseResult)evaluateStreamItems( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}

	
	
	
	public ParseResult parseListItems(List<Object> input)
	{
		return parseListItems( input, "[ ]*", (ParseAction)null );
	}

	public ParseResult parseListItems(List<Object> input, String junkRegex)
	{
		return parseListItems( input, junkRegex, (ParseAction)null );
	}

	public ParseResult parseListItems(List<Object> input, ParseAction delegateAction)
	{
		return parseListItems( input, "[ ]*", delegateAction );
	}

	public ParseResult parseListItems(List<Object> input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleListItems( state, input, 0 );
		
		return result;
	}

	public ParseResult parseListItems(List<Object> input, PyObject delegateAction)
	{
		return parseListItems( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseListItems(List<Object> input, String junkRegex, PyObject delegateAction)
	{
		return parseListItems( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	public DebugParseResult debugParseListItems(List<Object> input)
	{
		return debugParseListItems( input, "[ ]*", (ParseAction)null );
	}

	public DebugParseResult debugParseListItems(List<Object> input, String junkRegex)
	{
		return debugParseListItems( input, junkRegex, (ParseAction)null );
	}

	public DebugParseResult debugParseListItems(List<Object> input, ParseAction delegateAction)
	{
		return debugParseListItems( input, "[ ]*", delegateAction );
	}

	public DebugParseResult debugParseListItems(List<Object> input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableDebugging();
		DebugParseResult result = (DebugParseResult)handleListItems( state, input, 0 );
		
		return result;
	}

	public DebugParseResult debugParseListItems(List<Object> input, PyObject delegateAction)
	{
		return debugParseListItems( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public DebugParseResult debugParseListItems(List<Object> input, String junkRegex, PyObject delegateAction)
	{
		return debugParseListItems( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	
	
	private void debugBegin(ParserState state, Object input, int start)
	{
		// Get the current top of the debug stack (outer call)
		DebugNode prev = state.debugStack;
		// Create the debug info node
		DebugNode node = new DebugNode( prev, this, input, start );

		// Push @node onto the debug stack
		state.debugStack = node;
	}
	
	private ParseResult debugEnd(ParserState state, Object input, ParseResult result)
	{
		DebugNode node = state.debugStack;
		DebugNode prev = node.getPrev();
		
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
	
	
	
	protected ParseResult handleNode(ParserState state, Object input)
	{
		if ( state.bDebuggingEnabled )
		{
			debugBegin( state, input, 0 );
			// Get the parse result
			ParseResult result = evaluateNode( state, input );
			return debugEnd( state, input, result );
		}
		else
		{
			return evaluateNode( state, input );
		}
	}	
	
	protected ParseResult handleStringChars(ParserState state, String input, int start)
	{
		if ( state.bDebuggingEnabled )
		{
			debugBegin( state, input, start );
			// Get the parse result
			ParseResult result = evaluateStringChars( state, input, start );
			return debugEnd( state, input, result );
		}
		else
		{
			return evaluateStringChars( state, input, start );
		}
	}	
	
	protected ParseResult handleStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		if ( state.bDebuggingEnabled )
		{
			debugBegin( state, input, start );
			// Get the parse result
			ParseResult result = evaluateStreamItems( state, input, start );
			return debugEnd( state, input, result );
		}
		else
		{
			return evaluateStreamItems( state, input, start );
		}
	}	
	
	protected ParseResult handleListItems(ParserState state, List<Object> input, int start)
	{
		if ( state.bDebuggingEnabled )
		{
			debugBegin( state, input, start );
			// Get the parse result
			ParseResult result = evaluateListItems( state, input, start );
			return debugEnd( state, input, result );
		}
		else
		{
			return evaluateListItems( state, input, start );
		}
	}	
	
	
	protected abstract ParseResult evaluateNode(ParserState state, Object input);
	protected abstract ParseResult evaluateStringChars(ParserState state, String input, int start);
	protected abstract ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start);
	protected abstract ParseResult evaluateListItems(ParserState state, List<Object> input, int start);
	
	
	
	public ParserExpression debug(String debugName)
	{
		this.debugName = debugName;
		return this;
	}
	
	public String getExpressionName()
	{
		return debugName;
	}
	
	
	
	protected ParserExpression[] withSibling(ParserExpression sibling)
	{
		ParserExpression[] exprs = { this, sibling };
		return exprs;
	}


	
	

/*	public ParserExpression __add__(ParserExpression x)
	{
		return new Sequence( withSibling( x ) );
	}

	public ParserExpression __add__(Object x)
	{
		return new Sequence( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __or__(ParserExpression x)
	{
		return new Choice( withSibling( x ) );
	}

	public ParserExpression __or__(Object x)
	{
		return new Choice( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __xor__(ParserExpression x)
	{
		return new BestChoice( withSibling( x ) );
	}

	public ParserExpression __xor__(Object x)
	{
		return new BestChoice( withSibling( coerce( x ) ) );
	}

	
	public ParserExpression __and__(TreeParseCondition cond)
	{
		return condition( cond );
	}

	public ParserExpression __and__(PyObject cond)
	{
		return condition( cond );
	}*/




	public ParserExpression action(ParseAction a)
	{
		return new Action( this, a );
	}

	public ParserExpression action(PyObject a)
	{
		return new Action( this, a );
	}
	
	public ParserExpression mergeUpAction(ParseAction a)
	{
		return Action.mergeUpAction( this, a );
	}

	public ParserExpression mergeUpAction(PyObject a)
	{
		return Action.mergeUpAction( this, a );
	}
	
	/*public ParserExpression condition(TreeParseCondition cond)
	{
		return new Condition( this, cond );
	}
	
	public ParserExpression condition(PyObject cond)
	{
		return new Condition( this, cond );
	}
	
	public ParserExpression bindTo(String name)
	{
		return new Bind( name, this );
	}

	public ParserExpression clearBindings()
	{
		return new ClearBindings( this );
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
	}*/
	
	
	
	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = {};
		return Arrays.asList( children );
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

	public boolean compareTo(ParserExpression x)
	{
		return true;
	}
	
	
	public String toString()
	{
		return "ParserExpression()";
	}
	
	
	protected boolean isTerminal()
	{
		return false;
	}
	
	protected boolean isSequence()
	{
		return false;
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
}
