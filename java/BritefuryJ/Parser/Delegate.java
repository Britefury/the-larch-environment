//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.DocPresent.StreamValue.StreamValueAccessor;

/*
 * Delegate
 * 
 * Delegate:node( input )			->  state.delegateAction.action( Delegate.subexp:node( input ) )
 * Delegate:string( input, start )		->  state.delegateAction.action( Delegate.subexp:string( input, start ) )
 * Delegate:stream( input, start )	->  state.delegateAction.action( Delegate.subexp:stream( input, start ) )
 * Delegate:list( input, start )		->  state.delegateAction.action( Delegate.subexp:list( input, start ) )
 */
public class Delegate extends UnaryBranchExpression
{
	public Delegate()
	{
		this( new AnyNode() );
	}
	
	public Delegate(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public Delegate(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			if ( state.delegateAction != null )
			{
				return res.actionValue( state.delegateAction.invoke( input, 0, 1, res.value, res.bindings ), false );
			}
			else
			{
				throw new RuntimeException( "No delegate action" );
			}
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleStringChars( state, input, start );
		
		if ( res.isValid() )
		{
			if ( state.delegateAction != null )
			{
				return res.actionValue( state.delegateAction.invoke( input, start, res.end, res.value, res.bindings ), false );
			}
			else
			{
				throw new RuntimeException( "No delegate action" );
			}
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start)
	{
		ParseResult res = subexp.handleStreamItems( state, input, start );
		
		if ( res.isValid() )
		{
			if ( state.delegateAction != null )
			{
				return res.actionValue( state.delegateAction.invoke( input, start, res.end, res.value, res.bindings ), false );
			}
			else
			{
				throw new RuntimeException( "No delegate action" );
			}
		}
		else
		{
			return res;
		}
	}
	
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleListItems( state, input, start );
		
		if ( res.isValid() )
		{
			if ( state.delegateAction != null )
			{
				return res.actionValue( state.delegateAction.invoke( input, start, res.end, res.value, res.bindings ), false );
			}
			else
			{
				throw new RuntimeException( "No delegate action" );
			}
		}
		else
		{
			return res;
		}
	}


	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Delegate )
		{
			return super.compareTo( x );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Delegate( " + subexp.toString() + " )";
	}
}
