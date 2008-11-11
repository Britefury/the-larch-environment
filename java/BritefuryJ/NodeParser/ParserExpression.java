//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.NodeParser;

import java.util.Arrays;
import java.util.List;
import org.python.core.PyObject;

public abstract class ParserExpression
{
	protected String debugName = "";
	
	
	
	
	@SuppressWarnings("unchecked")
	public ParseResult parseNode(Object input)
	{
		int stop;
		if ( input instanceof String )
		{
			stop = ((String)input).length();
		}
		else if ( input instanceof List )
		{
			stop = ((List<Object>)input).size();
		}
		else
		{
			stop = 1;
		}
		ParserState state = new ParserState();
		return evaluateRootNode( state, input, 0, stop );
	}
	
	@SuppressWarnings("unchecked")
	public DebugParseResult debugParseNode(Object input)
	{
		int stop;
		if ( input instanceof String )
		{
			stop = ((String)input).length();
		}
		else if ( input instanceof List )
		{
			stop = ((List<Object>)input).size();
		}
		else
		{
			stop = 1;
		}
		ParserState state = new ParserState();
		state.enableDebugging();
		return (DebugParseResult)evaluateRootNode( state, input, 0, stop );
	}
	
	

	protected ParseResult evaluateNode(ParserState state, Object input, int start, int stop)
	{
		if ( state.bDebuggingEnabled )
		{
			// Get the current top of the debug stack (outer call)
			DebugParseResult.DebugNode prev = state.debugStack;
			// Create the debug info node
			DebugParseResult.DebugNode node = new DebugParseResult.DebugNode( prev, this );

			// Push @node onto the debug stack
			state.debugStack = node;
			
			// Get the parse result
			ParseResult result = parseNode( state, input, start, stop );
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
				
				DebugParseResult.DebugNode fromNode = node;
				DebugParseResult.DebugNode toNode = debugResult.debugNode;
				
				if ( !fromNode.getCallChildren().contains( toNode ) )
				{
					fromNode.addMemoChild( toNode );
				}
			}
			
			
			return result.debug( node );
		}
		else
		{
			return parseNode( state, input, start, stop );
		}
	}
	
	
	private ParseResult evaluateRootNode(ParserState state, Object input, int start, int stop)
	{
		if ( state.bDebuggingEnabled )
		{
			// Get the current top of the debug stack (outer call)
			DebugParseResult.DebugNode prev = state.debugStack;
			// Create the debug info node
			DebugParseResult.DebugNode node = new DebugParseResult.DebugNode( prev, this );

			// Push @node onto the debug stack
			state.debugStack = node;
			
			// Get the parse result
			ParseResult result = parseRootNode( state, input, start, stop );
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
				
				DebugParseResult.DebugNode fromNode = node;
				DebugParseResult.DebugNode toNode = debugResult.debugNode;
				
				if ( !fromNode.getCallChildren().contains( toNode ) )
				{
					fromNode.addMemoChild( toNode );
				}
			}
			
			
			return result.debug( node );
		}
		else
		{
			return parseRootNode( state, input, start, stop );
		}
	}
	
	
	
	protected abstract ParseResult parseNode(ParserState state, Object input, int start, int stop);
	
	protected ParseResult parseRootNode(ParserState state, Object input, int start, int stop)
	{
		return parseNode( state, input, start, stop );
	}
	
	
	
	public ParserExpression debug(String debugName)
	{
		this.debugName = debugName;
		return this;
	}
	
	public String getDebugName()
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
	
	public ParserExpression bindTo(String name)
	{
		return new Bind( name, this );
	}

	public ParserExpression suppress()
	{
		return new Suppress( this );
	}
	
	public ParserExpression optional()
	{
		return new Optional( this );
	}
	
	
	
	public static ParserExpression coerce(Object x)
	{
		if ( x instanceof ParserExpression )
		{
			return (ParserExpression)x;
		}
		else
		{
			return new Literal( (String)x );
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
