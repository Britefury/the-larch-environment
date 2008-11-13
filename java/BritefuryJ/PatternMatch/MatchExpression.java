//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.Arrays;
import java.util.List;
import org.python.core.PyObject;

import BritefuryJ.ParserSupport.DebugNode;
import BritefuryJ.ParserSupport.ParserExpressionInterface;

public abstract class MatchExpression implements ParserExpressionInterface
{
	protected String debugName = "";
	
	
	
	
	public MatchResult parseNode(Object input)
	{
		MatcherState state = new MatcherState();
		List<Object> inputInList = Arrays.asList( new Object[] { input } );
		return evaluateNode( state, inputInList, 0, 1 );
	}
	
	public DebugMatchResult debugParseNode(Object input)
	{
		MatcherState state = new MatcherState();
		state.enableDebugging();
		List<Object> inputInList = Arrays.asList( new Object[] { input } );
		return (DebugMatchResult)evaluateNode( state, inputInList, 0, 1 );
	}
	
	

	protected MatchResult evaluateNode(MatcherState state, Object input, int start, int stop)
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
			MatchResult result = parseNode( state, input, start, stop );
			node.setResult( result );
			
			// If @prev is valid, add @node as a call-child of @prev
			if ( prev != null )
			{
				prev.addCallChild( node );
			}
			
			// Pop @node off the debug stack
			state.debugStack = prev;
			
			
			if ( result instanceof DebugMatchResult )
			{
				DebugMatchResult debugResult = (DebugMatchResult)result;
				
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
			return parseNode( state, input, start, stop );
		}
	}	
	
	
	protected abstract MatchResult parseNode(MatcherState state, Object input, int start, int stop);
	
	
	
	public MatchExpression debug(String debugName)
	{
		this.debugName = debugName;
		return this;
	}
	
	public String getDebugName()
	{
		return debugName;
	}
	
	
	
	protected MatchExpression[] withSibling(MatchExpression sibling)
	{
		MatchExpression[] exprs = { this, sibling };
		return exprs;
	}


	
	

	public MatchExpression __add__(MatchExpression x)
	{
		return new Sequence( withSibling( x ) );
	}

	public MatchExpression __add__(Object x)
	{
		return new Sequence( withSibling( toParserExpression( x ) ) );
	}

	
	public MatchExpression __or__(MatchExpression x)
	{
		return new Choice( withSibling( x ) );
	}

	public MatchExpression __or__(Object x)
	{
		return new Choice( withSibling( toParserExpression( x ) ) );
	}

	
	public MatchExpression __xor__(MatchExpression x)
	{
		return new BestChoice( withSibling( x ) );
	}

	public MatchExpression __xor__(Object x)
	{
		return new BestChoice( withSibling( toParserExpression( x ) ) );
	}

	
	public MatchExpression __and__(MatchCondition cond)
	{
		return condition( cond );
	}

	public MatchExpression __and__(PyObject cond)
	{
		return condition( cond );
	}



	public MatchExpression action(MatchAction a)
	{
		return new Action( this, a );
	}

	public MatchExpression action(PyObject a)
	{
		return new Action( this, a );
	}
	
	public MatchExpression condition(MatchCondition cond)
	{
		return new Condition( this, cond );
	}
	
	public MatchExpression condition(PyObject cond)
	{
		return new Condition( this, cond );
	}
	
	public MatchExpression bindTo(String name)
	{
		return new Bind( name, this );
	}

	public MatchExpression clearBindings()
	{
		return new ClearBindings( this );
	}

	public MatchExpression suppress()
	{
		return new Suppress( this );
	}
	
	public MatchExpression optional()
	{
		return new Optional( this );
	}
	
	public MatchExpression zeroOrMore()
	{
		return new ZeroOrMore( this );
	}
	
	public MatchExpression oneOrMore()
	{
		return new OneOrMore( this );
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static MatchExpression toParserExpression(Object x)
	{
		if ( x instanceof MatchExpression )
		{
			return (MatchExpression)x;
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

	public static MatchExpression toParserExpression(String x)
	{
		return new Literal( x );
	}





	public List<MatchExpression> getChildren()
	{
		MatchExpression[] children = {};
		return Arrays.asList( children );
	}
	
	
	public boolean compareTo(MatchExpression x)
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
