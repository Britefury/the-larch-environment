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

import BritefuryJ.DocModel.DMObjectInterface;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParserExpressionInterface;

public abstract class ParserExpression implements ParserExpressionInterface
{
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

	
	
	
	

	
	public ParseResult parseString(String input)
	{
		return parseStream( new ItemStream( input ), (ParseAction)null );
	}

	public ParseResult parseString(String input, String junkRegex)
	{
		return parseStream( new ItemStream( input ), junkRegex, (ParseAction)null );
	}

	public ParseResult parseString(String input, ParseAction delegateAction)
	{
		return parseStream( new ItemStream( input ), delegateAction );
	}

	public ParseResult parseString(String input, String junkRegex, ParseAction delegateAction)
	{
		return parseStream( new ItemStream( input ), junkRegex, delegateAction );
	}

	public ParseResult parseString(String input, PyObject delegateAction)
	{
		return parseStream( new ItemStream( input ), delegateAction );
	}

	public ParseResult parseString(String input, String junkRegex, PyObject delegateAction)
	{
		return parseStream( new ItemStream( input ), junkRegex, delegateAction );
	}

	
	public DebugParseResult debugParseString(String input)
	{
		return debugParseStream( new ItemStream( input ), (ParseAction)null );
	}

	public DebugParseResult debugParseString(String input, String junkRegex)
	{
		return debugParseStream( new ItemStream( input ), junkRegex, (ParseAction)null );
	}

	public DebugParseResult debugParseString(String input, ParseAction delegateAction)
	{
		return debugParseStream( new ItemStream( input ), delegateAction );
	}

	public DebugParseResult debugParseString(String input, String junkRegex, ParseAction delegateAction)
	{
		return debugParseStream( new ItemStream( input ), junkRegex, delegateAction );
	}

	public DebugParseResult debugParseString(String input, PyObject delegateAction)
	{
		return debugParseStream( new ItemStream( input ), delegateAction );
	}

	public DebugParseResult debugParseString(String input, String junkRegex, PyObject delegateAction)
	{
		return debugParseStream( new ItemStream( input ), junkRegex, delegateAction );
	}

	

	
	public ParseResult parseStream(ItemStream input)
	{
		return parseStream( input.accessor(), "[ ]*", null );
	}

	public ParseResult parseStream(ItemStream input, String junkRegex)
	{
		return parseStream( input.accessor(), junkRegex, null );
	}

	public ParseResult parseStream(ItemStream input, ParseAction delegateAction)
	{
		return parseStream( input.accessor(), "[ ]*", delegateAction );
	}

	public ParseResult parseStream(ItemStream input, String junkRegex, ParseAction delegateAction)
	{
		return parseStream( input.accessor(), junkRegex, delegateAction );
	}

	public ParseResult parseStream(ItemStream input, PyObject delegateAction)
	{
		return parseStream( input.accessor(), "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseStream(ItemStream input, String junkRegex, PyObject delegateAction)
	{
		return parseStream( input.accessor(), junkRegex, new Action.PyAction( delegateAction ) );
	}

	private ParseResult parseStream(ItemStreamAccessor input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleStream( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}
	
	
	public DebugParseResult debugParseStream(ItemStream input)
	{
		return debugParseStream( input.accessor(), "[ ]*", null );
	}

	public DebugParseResult debugParseStream(ItemStream input, String junkRegex)
	{
		return debugParseStream( input.accessor(), junkRegex, null );
	}

	public DebugParseResult debugParseStream(ItemStream input, ParseAction delegateAction)
	{
		return debugParseStream( input.accessor(), "[ ]*", delegateAction );
	}

	public DebugParseResult debugParseStream(ItemStream input, String junkRegex, ParseAction delegateAction)
	{
		return debugParseStream( input.accessor(), junkRegex, delegateAction );
	}

	public DebugParseResult debugParseStream(ItemStream input, PyObject delegateAction)
	{
		return debugParseStream( input.accessor(), "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public DebugParseResult debugParseStream(ItemStream input, String junkRegex, PyObject delegateAction)
	{
		return debugParseStream( input.accessor(), junkRegex, new Action.PyAction( delegateAction ) );
	}

	private DebugParseResult debugParseStream(ItemStreamAccessor input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableDebugging();
		DebugParseResult result = (DebugParseResult)evaluateStream( state, input, 0 );
		if ( result.isValid() )
		{
			result.end = state.skipJunkChars( input, result.end );
		}
		
		return result;
	}

	
	
	
	public ParseResult parseList(List<Object> input)
	{
		return parseList( input, "[ ]*", (ParseAction)null );
	}

	public ParseResult parseList(List<Object> input, String junkRegex)
	{
		return parseList( input, junkRegex, (ParseAction)null );
	}

	public ParseResult parseList(List<Object> input, ParseAction delegateAction)
	{
		return parseList( input, "[ ]*", delegateAction );
	}

	public ParseResult parseList(List<Object> input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleList( state, input, 0 );
		
		return result;
	}

	public ParseResult parseList(List<Object> input, PyObject delegateAction)
	{
		return parseList( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseList(List<Object> input, String junkRegex, PyObject delegateAction)
	{
		return parseList( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	public DebugParseResult debugParseList(List<Object> input)
	{
		return debugParseList( input, "[ ]*", (ParseAction)null );
	}

	public DebugParseResult debugParseList(List<Object> input, String junkRegex)
	{
		return debugParseList( input, junkRegex, (ParseAction)null );
	}

	public DebugParseResult debugParseList(List<Object> input, ParseAction delegateAction)
	{
		return debugParseList( input, "[ ]*", delegateAction );
	}

	public DebugParseResult debugParseList(List<Object> input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableDebugging();
		DebugParseResult result = (DebugParseResult)handleList( state, input, 0 );
		
		return result;
	}

	public DebugParseResult debugParseList(List<Object> input, PyObject delegateAction)
	{
		return debugParseList( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public DebugParseResult debugParseList(List<Object> input, String junkRegex, PyObject delegateAction)
	{
		return debugParseList( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	
	
	public ParseResult parseObject(DMObjectInterface input)
	{
		return parseObject( input, "[ ]*", (ParseAction)null );
	}

	public ParseResult parseObject(DMObjectInterface input, String junkRegex)
	{
		return parseObject( input, junkRegex, (ParseAction)null );
	}

	public ParseResult parseObject(DMObjectInterface input, ParseAction delegateAction)
	{
		return parseObject( input, "[ ]*", delegateAction );
	}

	public ParseResult parseObject(DMObjectInterface input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		ParseResult result = handleObject( state, input );
		
		return result;
	}

	public ParseResult parseObject(DMObjectInterface input, PyObject delegateAction)
	{
		return parseObject( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public ParseResult parseObject(DMObjectInterface input, String junkRegex, PyObject delegateAction)
	{
		return parseObject( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	public DebugParseResult debugParseObject(DMObjectInterface input)
	{
		return debugParseObject( input, "[ ]*", (ParseAction)null );
	}

	public DebugParseResult debugParseObject(DMObjectInterface input, String junkRegex)
	{
		return debugParseObject( input, junkRegex, (ParseAction)null );
	}

	public DebugParseResult debugParseObject(DMObjectInterface input, ParseAction delegateAction)
	{
		return debugParseObject( input, "[ ]*", delegateAction );
	}

	public DebugParseResult debugParseObject(DMObjectInterface input, String junkRegex, ParseAction delegateAction)
	{
		ParserState state = new ParserState( junkRegex, delegateAction );
		state.enableDebugging();
		DebugParseResult result = (DebugParseResult)handleObject( state, input );
		
		return result;
	}

	public DebugParseResult debugParseObject(DMObjectInterface input, PyObject delegateAction)
	{
		return debugParseObject( input, "[ ]*", new Action.PyAction( delegateAction ) );
	}

	public DebugParseResult debugParseObject(DMObjectInterface input, String junkRegex, PyObject delegateAction)
	{
		return debugParseObject( input, junkRegex, new Action.PyAction( delegateAction ) );
	}

	
	
	private void debugBegin(ParserState state, Object input)
	{
		// Get the current top of the debug stack (outer call)
		DebugNode prev = state.debugStack;
		// Create the debug info node
		DebugNode node = new DebugNode( prev, this, input );

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
			debugBegin( state, input );
			// Get the parse result
			ParseResult result = evaluateNode( state, input );
			return debugEnd( state, input, result );
		}
		else
		{
			return evaluateNode( state, input );
		}
	}	
	
	protected ParseResult handleStream(ParserState state, ItemStreamAccessor input, int start)
	{
		if ( state.bDebuggingEnabled )
		{
			debugBegin( state, input );
			// Get the parse result
			ParseResult result = evaluateStream( state, input, start );
			return debugEnd( state, input, result );
		}
		else
		{
			return evaluateStream( state, input, start );
		}
	}	
	
	protected ParseResult handleList(ParserState state, List<Object> input, int start)
	{
		if ( state.bDebuggingEnabled )
		{
			debugBegin( state, input );
			// Get the parse result
			ParseResult result = evaluateList( state, input, start );
			return debugEnd( state, input, result );
		}
		else
		{
			return evaluateList( state, input, start );
		}
	}	
	
	protected ParseResult handleObject(ParserState state, DMObjectInterface input)
	{
		if ( state.bDebuggingEnabled )
		{
			debugBegin( state, input );
			// Get the parse result
			ParseResult result = evaluateObject( state, input );
			return debugEnd( state, input, result );
		}
		else
		{
			return evaluateObject( state, input );
		}
	}	
	
	
	protected abstract ParseResult evaluateNode(ParserState state, Object input);
	protected abstract ParseResult evaluateStream(ParserState state, ItemStreamAccessor input, int start);
	protected abstract ParseResult evaluateList(ParserState state, List<Object> input, int start);
	protected abstract ParseResult evaluateObject(ParserState state, DMObjectInterface input);
	
	
	
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
	
	
	
/*	@SuppressWarnings("unchecked")
	public static ParserExpression coerce(Object x)
	{
		if ( x instanceof ParserExpression )
		{
			return (ParserExpression)x;
		}
		else if ( x instanceof String )
		{
			return new Literal( (String)x );
		}
		else if ( x instanceof PyString )
		{
			return new Literal( ((PyString)x).toString() );
		}
		else if ( x instanceof List )
		{
			return new ListMatch( (List<Object>)x );
		}
		else if ( x.getClass().isArray() )
		{
			return new ListMatch( Arrays.asList( (Object[])x ) );
		}
		else
		{
			return new Literal( x.toString() );
		}
	}

	public static ParserExpression coerce(String x)
	{
		return new Literal( x );
	}*/





	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = {};
		return Arrays.asList( children );
	}
	
	
	public boolean compareTo(ParserExpression x)
	{
		return false;
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
}
