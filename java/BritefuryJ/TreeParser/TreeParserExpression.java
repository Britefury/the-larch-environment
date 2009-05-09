//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.Arrays;
import java.util.List;
import org.python.core.PyObject;
import org.python.core.PyString;

import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParserExpressionInterface;

public abstract class TreeParserExpression implements ParserExpressionInterface
{
	protected String debugName = "";
	
	
	
	
	public TreeParseResult parseNode(Object input)
	{
		return parseNode( input, null, (TreeParseAction)null );
	}
	
	public TreeParseResult parseNode(Object input, TreeParseAction delegateAction)
	{
		return parseNode( input, null, delegateAction );
	}
	
	public TreeParseResult parseNode(Object input, Object arg, TreeParseAction delegateAction)
	{
		TreeParserState state = new TreeParserState( arg, delegateAction );
		return processNode( state, input );
	}
	
	public TreeParseResult parseNode(Object input, PyObject delegateAction)
	{
		return parseNode( input, null, new Action.PyAction( delegateAction ) );
	}
	
	public TreeParseResult parseNode(Object input, Object arg, PyObject delegateAction)
	{
		return parseNode( input, arg, new Action.PyAction( delegateAction ) );
	}
	

	
	public DebugMatchResult debugParseNode(Object input)
	{
		return debugParseNode( input, null, (TreeParseAction)null );
	}
	
	public DebugMatchResult debugParseNode(Object input, TreeParseAction delegateAction)
	{
		return debugParseNode( input, null, delegateAction );
	}
	
	public DebugMatchResult debugParseNode(Object input, Object arg, TreeParseAction delegateAction)
	{
		TreeParserState state = new TreeParserState( arg, delegateAction );
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
	
	

	protected TreeParseResult processNode(TreeParserState state, Object input)
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
			TreeParseResult result = evaluateNode( state, input );
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
	
	protected TreeParseResult processList(TreeParserState state, List<Object> input, int start, int stop)
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
			TreeParseResult result = evaluateList( state, input, start, stop );
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
	
	
	protected abstract TreeParseResult evaluateNode(TreeParserState state, Object input);
	protected abstract TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop);
	
	
	
	public TreeParserExpression debug(String debugName)
	{
		this.debugName = debugName;
		return this;
	}
	
	public String getExpressionName()
	{
		return debugName;
	}
	
	
	
	protected TreeParserExpression[] withSibling(TreeParserExpression sibling)
	{
		TreeParserExpression[] exprs = { this, sibling };
		return exprs;
	}


	
	

	public TreeParserExpression __add__(TreeParserExpression x)
	{
		return new Sequence( withSibling( x ) );
	}

	public TreeParserExpression __add__(Object x)
	{
		return new Sequence( withSibling( coerce( x ) ) );
	}

	
	public TreeParserExpression __or__(TreeParserExpression x)
	{
		return new Choice( withSibling( x ) );
	}

	public TreeParserExpression __or__(Object x)
	{
		return new Choice( withSibling( coerce( x ) ) );
	}

	
	public TreeParserExpression __xor__(TreeParserExpression x)
	{
		return new BestChoice( withSibling( x ) );
	}

	public TreeParserExpression __xor__(Object x)
	{
		return new BestChoice( withSibling( coerce( x ) ) );
	}

	
	public TreeParserExpression __and__(TreeParseCondition cond)
	{
		return condition( cond );
	}

	public TreeParserExpression __and__(PyObject cond)
	{
		return condition( cond );
	}




	public TreeParserExpression action(TreeParseAction a)
	{
		return new Action( this, a );
	}

	public TreeParserExpression action(PyObject a)
	{
		return new Action( this, a );
	}
	
	public TreeParserExpression mergeUpAction(TreeParseAction a)
	{
		return Action.mergeUpAction( this, a );
	}

	public TreeParserExpression mergeUpAction(PyObject a)
	{
		return Action.mergeUpAction( this, a );
	}
	
	public TreeParserExpression condition(TreeParseCondition cond)
	{
		return new Condition( this, cond );
	}
	
	public TreeParserExpression condition(PyObject cond)
	{
		return new Condition( this, cond );
	}
	
	public TreeParserExpression bindTo(String name)
	{
		return new Bind( name, this );
	}

	public TreeParserExpression clearBindings()
	{
		return new ClearBindings( this );
	}

	public TreeParserExpression suppress()
	{
		return new Suppress( this );
	}
	
	public TreeParserExpression optional()
	{
		return new Optional( this );
	}
	
	public TreeParserExpression zeroOrMore()
	{
		return new ZeroOrMore( this );
	}
	
	public TreeParserExpression oneOrMore()
	{
		return new OneOrMore( this );
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static TreeParserExpression coerce(Object x)
	{
		if ( x instanceof TreeParserExpression )
		{
			return (TreeParserExpression)x;
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

	public static TreeParserExpression coerce(String x)
	{
		return new Literal( x );
	}





	public List<TreeParserExpression> getChildren()
	{
		TreeParserExpression[] children = {};
		return Arrays.asList( children );
	}
	
	
	public boolean compareTo(TreeParserExpression x)
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
