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
import org.python.core.PyString;

import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParserExpressionInterface;

public abstract class MatchExpression implements ParserExpressionInterface
{
	protected String debugName = "";
	
	
	
	
	public MatchResult parseNode(Object input)
	{
		return parseNode( input, null, (MatchAction)null );
	}
	
	public MatchResult parseNode(Object input, MatchAction delegateAction)
	{
		return parseNode( input, null, delegateAction );
	}
	
	public MatchResult parseNode(Object input, Object arg, MatchAction delegateAction)
	{
		MatchState state = new MatchState( arg, delegateAction );
		return processNode( state, input );
	}
	
	public MatchResult parseNode(Object input, PyObject delegateAction)
	{
		return parseNode( input, null, new Action.PyAction( delegateAction ) );
	}
	
	public MatchResult parseNode(Object input, Object arg, PyObject delegateAction)
	{
		return parseNode( input, arg, new Action.PyAction( delegateAction ) );
	}
	

	
	public DebugMatchResult debugParseNode(Object input)
	{
		return debugParseNode( input, null, (MatchAction)null );
	}
	
	public DebugMatchResult debugParseNode(Object input, MatchAction delegateAction)
	{
		return debugParseNode( input, null, delegateAction );
	}
	
	public DebugMatchResult debugParseNode(Object input, Object arg, MatchAction delegateAction)
	{
		MatchState state = new MatchState( arg, delegateAction );
		state.enableDebugging();
		return (DebugMatchResult)processNode( state, input );
	}
	
	public DebugMatchResult debugParseNode(Object input, PyObject delegateAction)
	{
		return debugParseNode( input, null, new Action.PyAction( delegateAction ) );
	}

	public DebugMatchResult debugParseNode(Object input, Object arg, PyObject delegateAction)
	{
		return debugParseNode( input, arg, new Action.PyAction( delegateAction ) );
	}
	
	

	protected MatchResult processNode(MatchState state, Object input)
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
			MatchResult result = evaluateNode( state, input );
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
			return evaluateNode( state, input );
		}
	}	
	
	protected MatchResult processList(MatchState state, List<Object> input, int start, int stop)
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
			MatchResult result = evaluateList( state, input, start, stop );
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
			return evaluateList( state, input, start, stop );
		}
	}	
	
	
	protected abstract MatchResult evaluateNode(MatchState state, Object input);
	protected abstract MatchResult evaluateList(MatchState state, List<Object> input, int start, int stop);
	
	
	
	public MatchExpression debug(String debugName)
	{
		this.debugName = debugName;
		return this;
	}
	
	public String getExpressionName()
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
		return new Sequence( withSibling( toMatchExpression( x ) ) );
	}

	
	public MatchExpression __or__(MatchExpression x)
	{
		return new Choice( withSibling( x ) );
	}

	public MatchExpression __or__(Object x)
	{
		return new Choice( withSibling( toMatchExpression( x ) ) );
	}

	
	public MatchExpression __xor__(MatchExpression x)
	{
		return new BestChoice( withSibling( x ) );
	}

	public MatchExpression __xor__(Object x)
	{
		return new BestChoice( withSibling( toMatchExpression( x ) ) );
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
	
	public MatchExpression mergeUpAction(MatchAction a)
	{
		return Action.mergeUpAction( this, a );
	}

	public MatchExpression mergeUpAction(PyObject a)
	{
		return Action.mergeUpAction( this, a );
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
	public static MatchExpression toMatchExpression(Object x)
	{
		if ( x instanceof MatchExpression )
		{
			return (MatchExpression)x;
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

	public static MatchExpression toMatchExpression(String x)
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
