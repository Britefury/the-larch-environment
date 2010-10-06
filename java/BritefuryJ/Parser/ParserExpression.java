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

import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueAccessor;
import BritefuryJ.ParserHelpers.TraceNode;

public abstract class ParserExpression
{
	public static class ParserCoerceException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	

	
	protected enum Mode
	{
		NODE,
		STRING,
		STREAM,
		LIST
	}
	
	
	protected String debugName = "";
	
	
	
	public ParserExpression()
	{
	}
	
	

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

	
	public TracedParseResult traceParseNode(Object input)
	{
		return traceParseNode( input, "[ ]*", (ParseAction)null );
	}

	public TracedParseResult traceParseNode(Object input, String junkRegex)
	{
		return traceParseNode( input, junkRegex, (ParseAction)null );
	}

	public TracedParseResult traceParseNode(Object input, ParseAction delegateAction)
	{
		return traceParseNode( input, "[ ]*", delegateAction );
	}

	public TracedParseResult traceParseNode(Object input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableTrace();
		TracedParseResult result = (TracedParseResult)handleNode( state, input );
		
		return result;
	}

	public TracedParseResult traceParseNode(Object input, PyObject delegateAction)
	{
		return traceParseNode( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public TracedParseResult traceParseNode(Object input, String junkRegex, PyObject delegateAction)
	{
		return traceParseNode( input, junkRegex, new Action.PyAction( delegateAction ) );
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

	
	public TracedParseResult traceParseStringChars(String input)
	{
		return traceParseStringChars( input, "[ ]*", (ParseAction)null );
	}

	public TracedParseResult traceParseStringChars(String input, String junkRegex)
	{
		return traceParseStringChars( input, junkRegex, (ParseAction)null );
	}

	public TracedParseResult traceParseStringChars(String input, ParseAction delegateAction)
	{
		return traceParseStringChars( input, "[ ]*", delegateAction );
	}

	public TracedParseResult traceParseStringChars(String input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableTrace();
		TracedParseResult result = (TracedParseResult)handleStringChars( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}

	public TracedParseResult traceParseStringChars(String input, PyObject delegateAction)
	{
		return traceParseStringChars( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public TracedParseResult traceParseStringChars(String input, String junkRegex, PyObject delegateAction)
	{
		return traceParseStringChars( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	

	
	public ParseResult parseStreamItems(StreamValue input)
	{
		return parseStreamItems( input.accessor(), "[ ]*", null );
	}

	public ParseResult parseStreamItems(StreamValue input, String junkRegex)
	{
		return parseStreamItems( input.accessor(), junkRegex, null );
	}

	public ParseResult parseStreamItems(StreamValue input, ParseAction delegateAction)
	{
		return parseStreamItems( input.accessor(), "[ ]*", delegateAction );
	}

	public ParseResult parseStreamItems(StreamValue input, String junkRegex, ParseAction delegateAction)
	{
		return parseStreamItems( input.accessor(), junkRegex, delegateAction );
	}

	public ParseResult parseStreamItems(StreamValue input, PyObject delegateAction)
	{
		return parseStreamItems( input.accessor(), "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseStreamItems(StreamValue input, String junkRegex, PyObject delegateAction)
	{
		return parseStreamItems( input.accessor(), junkRegex, new Action.PyAction( delegateAction ) );
	}

	private ParseResult parseStreamItems(StreamValueAccessor input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleStreamItems( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}
	
	
	public TracedParseResult traceParseStreamItems(StreamValue input)
	{
		return traceParseStreamItems( input.accessor(), "[ ]*", null );
	}

	public TracedParseResult traceParseStreamItems(StreamValue input, String junkRegex)
	{
		return traceParseStreamItems( input.accessor(), junkRegex, null );
	}

	public TracedParseResult traceParseStreamItems(StreamValue input, ParseAction delegateAction)
	{
		return traceParseStreamItems( input.accessor(), "[ ]*", delegateAction );
	}

	public TracedParseResult traceParseStreamItems(StreamValue input, String junkRegex, ParseAction delegateAction)
	{
		return traceParseStreamItems( input.accessor(), junkRegex, delegateAction );
	}

	public TracedParseResult traceParseStreamItems(StreamValue input, PyObject delegateAction)
	{
		return traceParseStreamItems( input.accessor(), "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public TracedParseResult traceParseStreamItems(StreamValue input, String junkRegex, PyObject delegateAction)
	{
		return traceParseStreamItems( input.accessor(), junkRegex, new Action.PyAction( delegateAction ) );
	}

	private TracedParseResult traceParseStreamItems(StreamValueAccessor input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableTrace();
		TracedParseResult result = (TracedParseResult)handleStreamItems( state, input, 0 );
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

	
	public TracedParseResult traceParseListItems(List<Object> input)
	{
		return traceParseListItems( input, "[ ]*", (ParseAction)null );
	}

	public TracedParseResult traceParseListItems(List<Object> input, String junkRegex)
	{
		return traceParseListItems( input, junkRegex, (ParseAction)null );
	}

	public TracedParseResult traceParseListItems(List<Object> input, ParseAction delegateAction)
	{
		return traceParseListItems( input, "[ ]*", delegateAction );
	}

	public TracedParseResult traceParseListItems(List<Object> input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableTrace();
		TracedParseResult result = (TracedParseResult)handleListItems( state, input, 0 );
		
		return result;
	}

	public TracedParseResult traceParseListItems(List<Object> input, PyObject delegateAction)
	{
		return traceParseListItems( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public TracedParseResult traceParseListItems(List<Object> input, String junkRegex, PyObject delegateAction)
	{
		return traceParseListItems( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	
	
	private void traceBegin(ParserState state, Object input, int start)
	{
		// Get the current top of the debug stack (outer call)
		TraceNode prev = state.traceStack;
		// Create the debug info node
		TraceNode node = new TraceNode( prev, this, input, start );

		// Push @node onto the debug stack
		state.traceStack = node;
	}
	
	private ParseResult traceEnd(ParserState state, Object input, ParseResult result)
	{
		TraceNode node = state.traceStack;
		TraceNode prev = node.getPrev();
		
		node.setResult( result );
		
		// If @prev is valid, add @node as a call-child of @prev
		if ( prev != null )
		{
			prev.addCallChild( node );
		}
		
		// Pop @node off the debug stack
		state.traceStack = prev;
		
		
		if ( result instanceof TracedParseResult )
		{
			TracedParseResult debugResult = (TracedParseResult)result;
			
			TraceNode fromNode = node;
			TraceNode toNode = debugResult.traceNode;
			
			if ( !fromNode.getCallChildren().contains( toNode ) )
			{
				fromNode.addMemoChild( toNode );
			}
		}
		
		return result.debug( node );
	}
	
	
	
	protected ParseResult handleNode(ParserState state, Object input)
	{
		if ( state.bTracingEnabled )
		{
			traceBegin( state, input, 0 );
			// Get the parse result
			ParseResult result = evaluateNode( state, input );
			return traceEnd( state, input, result );
		}
		else
		{
			return evaluateNode( state, input );
		}
	}	
	
	protected ParseResult handleStringChars(ParserState state, String input, int start)
	{
		if ( state.bTracingEnabled )
		{
			traceBegin( state, input, start );
			// Get the parse result
			ParseResult result = evaluateStringChars( state, input, start );
			return traceEnd( state, input, result );
		}
		else
		{
			return evaluateStringChars( state, input, start );
		}
	}	
	
	protected ParseResult handleStreamItems(ParserState state, StreamValueAccessor input, int start)
	{
		if ( state.bTracingEnabled )
		{
			traceBegin( state, input, start );
			// Get the parse result
			ParseResult result = evaluateStreamItems( state, input, start );
			return traceEnd( state, input, result );
		}
		else
		{
			return evaluateStreamItems( state, input, start );
		}
	}	
	
	protected ParseResult handleListItems(ParserState state, List<Object> input, int start)
	{
		if ( state.bTracingEnabled )
		{
			traceBegin( state, input, start );
			// Get the parse result
			ParseResult result = evaluateListItems( state, input, start );
			return traceEnd( state, input, result );
		}
		else
		{
			return evaluateListItems( state, input, start );
		}
	}	
	
	
	protected abstract ParseResult evaluateNode(ParserState state, Object input);
	protected abstract ParseResult evaluateStringChars(ParserState state, String input, int start);
	protected abstract ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start);
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
	
	public ParserExpression mergeUpAction(ParseAction a)
	{
		return Action.mergeUpAction( this, a );
	}

	public ParserExpression mergeUpAction(PyObject a)
	{
		return Action.mergeUpAction( this, a );
	}
	
	public ParserExpression condition(ParseCondition cond)
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
	
	public ParserExpression peek()
	{
		return new Peek( this );
	}
	
	public ParserExpression peekNot()
	{
		return new PeekNot( this );
	}
	
	public ParserExpression repeat(int minRepetitions, int maxRepetitions)
	{
		return new Repetition( this, minRepetitions, maxRepetitions );
	}
	
	public ParserExpression repeat(int minRepetitions, int maxRepetitions, boolean bNullIfZero)
	{
		return new Repetition( this, minRepetitions, maxRepetitions, bNullIfZero );
	}
	
	public ParserExpression zeroOrMore()
	{
		return new ZeroOrMore( this );
	}
	
	public ParserExpression oneOrMore()
	{
		return new OneOrMore( this );
	}
	
	
	
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
		if ( x == null )
		{
			return new LiteralNode( null );
		}
		else if ( x instanceof ParserExpression )
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
