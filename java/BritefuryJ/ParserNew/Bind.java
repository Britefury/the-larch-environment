//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class Bind extends UnaryBranchExpression
{
	//
	//
	//
	//
	// WARNING: UNRESOLVED PROBLEM:
	// Bindings can effect parse results; the state of the bindings is not considered by the memoisation system,
	// likely resulting in incorrect parse results.
	//
	//
	//
	//
	protected String name;
	
	
	public Bind(String name, ParserExpression subexp)
	{
		super( subexp );
		this.name = name;
	}
	
	
	
	public String getName()
	{
		return name;
	}
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			return res.bindValueTo( name );
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
			return res.bindValueTo( name );
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		ParseResult res = subexp.handleStreamItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res.bindValueTo( name );
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
			return res.bindValueTo( name );
		}
		else
		{
			return res;
		}
	}

	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Bind )
		{
			Bind bx = (Bind)x;
			return super.compareTo( x )  &&  name.equals( bx.name );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Bind( " + name + ": " + subexp.toString() + " )";
	}
}
