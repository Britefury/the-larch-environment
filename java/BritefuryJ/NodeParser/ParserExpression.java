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
	
	
	
	
	public ParseResult parseNode(Object input)
	{
		ParserState state = new ParserState();
		List<Object> inputInList = Arrays.asList( new Object[] { input } );
		return evaluateNode( state, inputInList, 0, 1 );
	}
	
	public DebugParseResult debugParseNode(Object input)
	{
		ParserState state = new ParserState();
		state.enableDebugging();
		List<Object> inputInList = Arrays.asList( new Object[] { input } );
		return (DebugParseResult)evaluateNode( state, inputInList, 0, 1 );
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
	
	
	protected abstract ParseResult parseNode(ParserState state, Object input, int start, int stop);
	
	
	
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
		return new Sequence( withSibling( toParserExpression( x ) ) );
	}

	
	public ParserExpression __or__(ParserExpression x)
	{
		return new Choice( withSibling( x ) );
	}

	public ParserExpression __or__(Object x)
	{
		return new Choice( withSibling( toParserExpression( x ) ) );
	}

	
	public ParserExpression __xor__(ParserExpression x)
	{
		return new BestChoice( withSibling( x ) );
	}

	public ParserExpression __xor__(Object x)
	{
		return new BestChoice( withSibling( toParserExpression( x ) ) );
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
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static ParserExpression toParserExpression(Object x)
	{
		if ( x instanceof ParserExpression )
		{
			return (ParserExpression)x;
		}
		else if ( x instanceof String )
		{
			return new Literal( (String)x );
		}
		else if ( x instanceof List )
		{
			return new ListNode( (List<Object>)x );
		}
		else if ( x.getClass().isArray() )
		{
			return new ListNode( Arrays.asList( (Object[])x ) );
		}
		else
		{
			return new Literal( (String)x );
		}
	}

	public static ParserExpression toParserExpression(String x)
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
