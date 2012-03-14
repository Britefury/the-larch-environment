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

import BritefuryJ.ParserHelpers.TraceNode;
import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringAccessor;

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
		RICHSTRING,
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
		return handleNode( state, input );
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
		return (TracedParseResult)handleNode( state, input );
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

	

	
	public ParseResult parseRichStringItems(RichString input)
	{
		return parseRichStringItems( input.accessor(), "[ ]*", null );
	}

	public ParseResult parseRichStringItems(RichString input, String junkRegex)
	{
		return parseRichStringItems( input.accessor(), junkRegex, null );
	}

	public ParseResult parseRichStringItems(RichString input, ParseAction delegateAction)
	{
		return parseRichStringItems( input.accessor(), "[ ]*", delegateAction );
	}

	public ParseResult parseRichStringItems(RichString input, String junkRegex, ParseAction delegateAction)
	{
		return parseRichStringItems( input.accessor(), junkRegex, delegateAction );
	}

	public ParseResult parseRichStringItems(RichString input, PyObject delegateAction)
	{
		return parseRichStringItems( input.accessor(), "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseRichStringItems(RichString input, String junkRegex, PyObject delegateAction)
	{
		return parseRichStringItems( input.accessor(), junkRegex, new Action.PyAction( delegateAction ) );
	}

	private ParseResult parseRichStringItems(RichStringAccessor input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleRichStringItems( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}
	
	
	public TracedParseResult traceParseRichStringItems(RichString input)
	{
		return traceParseRichStringItems( input.accessor(), "[ ]*", null );
	}

	public TracedParseResult traceParseRichStringItems(RichString input, String junkRegex)
	{
		return traceParseRichStringItems( input.accessor(), junkRegex, null );
	}

	public TracedParseResult traceParseRichStringItems(RichString input, ParseAction delegateAction)
	{
		return traceParseRichStringItems( input.accessor(), "[ ]*", delegateAction );
	}

	public TracedParseResult traceParseRichStringItems(RichString input, String junkRegex, ParseAction delegateAction)
	{
		return traceParseRichStringItems( input.accessor(), junkRegex, delegateAction );
	}

	public TracedParseResult traceParseRichStringItems(RichString input, PyObject delegateAction)
	{
		return traceParseRichStringItems( input.accessor(), "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public TracedParseResult traceParseRichStringItems(RichString input, String junkRegex, PyObject delegateAction)
	{
		return traceParseRichStringItems( input.accessor(), junkRegex, new Action.PyAction( delegateAction ) );
	}

	private TracedParseResult traceParseRichStringItems(RichStringAccessor input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableTrace();
		TracedParseResult result = (TracedParseResult)handleRichStringItems( state, input, 0 );
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
		return handleListItems( state, input, 0 );
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
		return (TracedParseResult)handleListItems( state, input, 0 );
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
	
	protected ParseResult handleRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		if ( state.bTracingEnabled )
		{
			traceBegin( state, input, start );
			// Get the parse result
			ParseResult result = evaluateRichStringItems( state, input, start );
			return traceEnd( state, input, result );
		}
		else
		{
			return evaluateRichStringItems( state, input, start );
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
	protected abstract ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start);
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
		return new ParserExpression[] { this, sibling };
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
			return x.isEquivalentTo( y );
		}
		else
		{
			return false;
		}
	}

	public boolean isEquivalentTo(ParserExpression x)
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
